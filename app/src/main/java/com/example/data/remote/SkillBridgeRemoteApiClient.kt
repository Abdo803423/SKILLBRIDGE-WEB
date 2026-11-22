package com.example.data.remote

import android.util.Log
import androidx.room.withTransaction
import com.example.core.UserPreferencesStore
import com.example.data.local.AccountEntity
import com.example.data.local.AccountRole
import com.example.data.local.CompanyEntity
import com.example.data.local.DeviceSessionEntity
import com.example.data.local.ModerationReportEntity
import com.example.data.local.NotificationEntity
import com.example.data.local.PlatformActivityEntity
import com.example.data.local.ProjectApplicationEntity
import com.example.data.local.ProjectBriefEntity
import com.example.data.local.ProjectTaskEntity
import com.example.data.local.SessionPrincipal
import com.example.data.local.SkillBridgeDatabase
import com.example.data.local.WorkSubmissionEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class BackendHttpApiException(
    val statusCode: Int,
    override val message: String
) : Exception(message)

data class RemoteAuthResult(
    val token: String,
    val principal: SessionPrincipal
)

data class RemoteSnapshotPayload(
    val revision: Long,
    val updatedAtEpochMs: Long,
    val accounts: List<AccountEntity>,
    val companies: List<CompanyEntity>,
    val briefs: List<ProjectBriefEntity>,
    val applications: List<ProjectApplicationEntity>,
    val tasks: List<ProjectTaskEntity>,
    val submissions: List<WorkSubmissionEntity>,
    val notifications: List<NotificationEntity>,
    val activities: List<PlatformActivityEntity>,
    val reports: List<ModerationReportEntity>,
    val deviceSessions: List<DeviceSessionEntity>
)

/**
 * Authoritative HTTP + Server-Sent Events (SSE) client connecting Android
 * to the exact same central Express backend and database shared with the
 * SkillBridge Web Application and Windows Desktop Application.
 */
class SkillBridgeRemoteApiClient(
    private val prefsStore: UserPreferencesStore
) {
    private val tag = "SkillBridgeRemoteApi"
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    private val sseClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .build()

    private val sseScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var sseJob: Job? = null

    private val _remoteSyncState = MutableStateFlow<SyncState?>(null)
    val remoteSyncState: StateFlow<SyncState?> = _remoteSyncState.asStateFlow()

    private val _remoteLastSyncedEpochMs = MutableStateFlow(0L)
    val remoteLastSyncedEpochMs: StateFlow<Long> = _remoteLastSyncedEpochMs.asStateFlow()

    private suspend fun resolveBaseUrl(): String =
        prefsStore.getServerBaseUrl().trim().trimEnd('/')

    suspend fun login(loginId: String, password: String): RemoteAuthResult = withContext(Dispatchers.IO) {
        val body = JSONObject()
            .put("loginId", loginId.trim())
            .put("password", password)
        val json = executeJsonPost("/api/auth/login", body, token = null)
        parseAuthResult(json)
    }

    suspend fun registerStudent(
        loginId: String,
        displayName: String,
        email: String,
        password: String
    ): RemoteAuthResult = withContext(Dispatchers.IO) {
        val body = JSONObject()
            .put("loginId", loginId.trim())
            .put("displayName", displayName.trim())
            .put("email", email.trim())
            .put("password", password)
        val json = executeJsonPost("/api/auth/register", body, token = null)
        parseAuthResult(json)
    }

    suspend fun resolveGoogleSession(
        firebaseUid: String,
        displayName: String,
        email: String
    ): RemoteAuthResult = withContext(Dispatchers.IO) {
        val body = JSONObject()
            .put("firebaseUid", firebaseUid.trim())
            .put("displayName", displayName.trim())
            .put("email", email.trim())
        val json = executeJsonPost("/api/auth/google-session", body, token = null)
        parseAuthResult(json)
    }

    suspend fun fetchSnapshot(token: String): RemoteSnapshotPayload = withContext(Dispatchers.IO) {
        val baseUrl = resolveBaseUrl()
        val req = Request.Builder()
            .url("$baseUrl/api/ecosystem/snapshot")
            .header("Authorization", "Bearer $token")
            .get()
            .build()
        httpClient.newCall(req).execute().use { resp ->
            val raw = resp.body?.string().orEmpty()
            if (!resp.isSuccessful) {
                throw BackendHttpApiException(resp.code, extractErrorMessage(raw, resp.code))
            }
            val parsed = parseSnapshot(JSONObject(raw))
            _remoteSyncState.value = SyncState.LIVE_CLOUD
            _remoteLastSyncedEpochMs.value = System.currentTimeMillis()
            parsed
        }
    }

    suspend fun postMutationAndExtractSnapshot(
        path: String,
        body: JSONObject,
        token: String
    ): RemoteSnapshotPayload = withContext(Dispatchers.IO) {
        val json = executeJsonPost(path, body, token)
        val snapObj = json.optJSONObject("snapshot") ?: json
        val parsed = parseSnapshot(snapObj)
        _remoteSyncState.value = SyncState.LIVE_CLOUD
        _remoteLastSyncedEpochMs.value = System.currentTimeMillis()
        parsed
    }

    suspend fun postMutationReturningIdAndSnapshot(
        path: String,
        body: JSONObject,
        entityKey: String,
        token: String
    ): Pair<String, RemoteSnapshotPayload> = withContext(Dispatchers.IO) {
        val json = executeJsonPost(path, body, token)
        val createdId = json.optJSONObject(entityKey)?.optString("id").orEmpty()
        val snapObj = json.optJSONObject("snapshot") ?: json
        val parsed = parseSnapshot(snapObj)
        _remoteSyncState.value = SyncState.LIVE_CLOUD
        _remoteLastSyncedEpochMs.value = System.currentTimeMillis()
        createdId to parsed
    }

    fun startLiveSseSync(
        db: SkillBridgeDatabase,
        onSyncError: ((Throwable) -> Unit)? = null
    ) {
        stopLiveSseSync()
        sseJob = sseScope.launch {
            var backoffMs = 2_000L
            while (isActive) {
                val token = prefsStore.getBackendToken()
                if (token.isBlank()) {
                    delay(3_000L)
                    continue
                }
                val baseUrl = resolveBaseUrl()
                try {
                    _remoteSyncState.value = SyncState.SYNCING
                    val snapshot = fetchSnapshot(token)
                    syncSnapshotIntoDatabase(db, snapshot)
                    _remoteSyncState.value = SyncState.LIVE_CLOUD
                    _remoteLastSyncedEpochMs.value = System.currentTimeMillis()
                    backoffMs = 2_000L

                    val sseReq = Request.Builder()
                        .url("$baseUrl/api/events")
                        .header("Authorization", "Bearer $token")
                        .header("Accept", "text/event-stream")
                        .get()
                        .build()

                    sseClient.newCall(sseReq).execute().use { resp ->
                        if (!resp.isSuccessful) {
                            throw BackendHttpApiException(resp.code, "SSE connection rejected (${resp.code})")
                        }
                        val source = resp.body?.source() ?: throw IOException("Empty SSE stream")
                        while (isActive && !source.exhausted()) {
                            val line = source.readUtf8Line() ?: break
                            if (line.startsWith("data:")) {
                                val dataJson = line.removePrefix("data:").trim()
                                if (dataJson.isNotEmpty()) {
                                    val event = runCatching { JSONObject(dataJson) }.getOrNull()
                                    if (event?.optString("type") == "sync") {
                                        val latest = fetchSnapshot(token)
                                        syncSnapshotIntoDatabase(db, latest)
                                    }
                                }
                            }
                        }
                    }
                } catch (e: BackendHttpApiException) {
                    Log.w(tag, "Central SSE stopped (${e.statusCode}): ${e.message}")
                    onSyncError?.invoke(e)
                    if (e.statusCode == 401 || e.statusCode == 403) {
                        _remoteSyncState.value = SyncState.LOCAL_SYNCED
                        break
                    }
                } catch (e: Exception) {
                    Log.d(tag, "Central SSE reconnect scheduled: ${e.message}")
                    _remoteSyncState.value = SyncState.OFFLINE_QUEUED
                    onSyncError?.invoke(e)
                }
                delay(backoffMs)
                backoffMs = (backoffMs * 2).coerceAtMost(20_000L)
            }
        }
    }

    fun stopLiveSseSync() {
        sseJob?.cancel()
        sseJob = null
    }

    suspend fun syncSnapshotIntoDatabase(db: SkillBridgeDatabase, payload: RemoteSnapshotPayload) {
        val dao = db.skillBridgeDao()
        db.withTransaction {
            if (payload.accounts.isNotEmpty()) {
                val mergedAccounts = payload.accounts.map { remote ->
                    val existing = dao.getAccountById(remote.id)
                    if (existing != null && remote.passwordHash.isBlank()) {
                        remote.copy(
                            passwordSalt = existing.passwordSalt,
                            passwordHash = existing.passwordHash
                        )
                    } else {
                        remote
                    }
                }
                dao.insertAccounts(mergedAccounts)
            }
            if (payload.companies.isNotEmpty()) {
                dao.insertCompanies(payload.companies)
            }
            val knownCompanyIds = payload.companies.map { it.id }.toSet()
            val validBriefs = payload.briefs.filter { it.companyId in knownCompanyIds }
            if (validBriefs.isNotEmpty()) {
                dao.insertBriefs(validBriefs)
            }
            val knownBriefIds = validBriefs.map { it.id }.toSet()
            val validApps = payload.applications.filter { it.briefId in knownBriefIds }
            if (validApps.isNotEmpty()) {
                dao.insertApplications(validApps)
            }
            val validTasks = payload.tasks.filter { it.briefId in knownBriefIds }
            if (validTasks.isNotEmpty()) {
                dao.insertTasks(validTasks)
            }
            val validSubs = payload.submissions.filter { it.briefId in knownBriefIds }
            if (validSubs.isNotEmpty()) {
                dao.insertSubmissions(validSubs)
            }
            if (payload.notifications.isNotEmpty()) {
                dao.insertNotifications(payload.notifications)
            }
            if (payload.activities.isNotEmpty()) {
                dao.insertActivities(payload.activities)
            }
            if (payload.reports.isNotEmpty()) {
                dao.insertReports(payload.reports)
            }
            payload.deviceSessions.forEach { sess ->
                dao.upsertDeviceSession(sess)
            }
        }
    }

    private suspend fun executeJsonPost(
        path: String,
        body: JSONObject,
        token: String?
    ): JSONObject {
        val baseUrl = resolveBaseUrl()
        val reqBuilder = Request.Builder()
            .url("$baseUrl$path")
            .post(body.toString().toRequestBody(jsonMediaType))
            .header("Content-Type", "application/json")
        if (!token.isNullOrBlank()) {
            reqBuilder.header("Authorization", "Bearer $token")
        }
        httpClient.newCall(reqBuilder.build()).execute().use { resp ->
            val raw = resp.body?.string().orEmpty()
            if (!resp.isSuccessful) {
                throw BackendHttpApiException(resp.code, extractErrorMessage(raw, resp.code))
            }
            return if (raw.isBlank()) JSONObject() else JSONObject(raw)
        }
    }

    private fun extractErrorMessage(raw: String, code: Int): String {
        val parsed = runCatching { JSONObject(raw).optString("error") }.getOrNull()
        return if (!parsed.isNullOrBlank()) parsed else "Server returned HTTP $code"
    }

    private fun parseAuthResult(json: JSONObject): RemoteAuthResult {
        val token = json.getString("token")
        val p = json.getJSONObject("principal")
        val principal = SessionPrincipal(
            accountId = p.getString("accountId"),
            loginId = p.getString("loginId"),
            displayName = p.optString("displayName"),
            email = p.optString("email"),
            role = AccountRole.fromKey(p.optString("role", "STUDENT")),
            companyId = p.optString("companyId"),
            isGoogleLinked = p.optBoolean("isGoogleLinked", false)
        )
        return RemoteAuthResult(token = token, principal = principal)
    }

    private fun parseSnapshot(json: JSONObject): RemoteSnapshotPayload {
        val accountsArr = json.optJSONArray("accounts") ?: JSONArray()
        val accounts = buildList {
            for (i in 0 until accountsArr.length()) {
                val o = accountsArr.getJSONObject(i)
                add(
                    AccountEntity(
                        id = o.getString("id"),
                        loginId = o.getString("loginId"),
                        displayName = o.optString("displayName"),
                        email = o.optString("email"),
                        passwordSalt = o.optString("passwordSalt"),
                        passwordHash = o.optString("passwordHash"),
                        role = o.optString("role", AccountRole.STUDENT.key),
                        companyId = o.optString("companyId"),
                        isActive = o.optBoolean("isActive", true),
                        careerLevel = o.optString("careerLevel", "Junior"),
                        domain = o.optString("domain", "Backend & Cloud"),
                        country = o.optString("country", "Egypt"),
                        points = o.optInt("points", 0),
                        streakDays = o.optInt("streakDays", 0),
                        verifiedCount = o.optInt("verifiedCount", 0),
                        skillsCsv = o.optString("skillsCsv"),
                        targetSkillsCsv = o.optString("targetSkillsCsv"),
                        goalsSummary = o.optString("goalsSummary"),
                        weeklyHours = o.optInt("weeklyHours", 10),
                        profileCompletion = o.optInt("profileCompletion", 50),
                        createdAtEpochMs = o.optLong("createdAtEpochMs", System.currentTimeMillis())
                    )
                )
            }
        }

        val companiesArr = json.optJSONArray("companies") ?: JSONArray()
        val companies = buildList {
            for (i in 0 until companiesArr.length()) {
                val o = companiesArr.getJSONObject(i)
                add(
                    CompanyEntity(
                        id = o.getString("id"),
                        ownerAccountId = o.optString("ownerAccountId"),
                        name = o.optString("name"),
                        city = o.optString("city"),
                        domain = o.optString("domain"),
                        verificationStatus = o.optString("verificationStatus", "PENDING"),
                        verificationTier = o.optString("verificationTier", "BASIC"),
                        verificationNotes = o.optString("verificationNotes"),
                        rewardPolicy = o.optString("rewardPolicy"),
                        ipPolicy = o.optString("ipPolicy"),
                        verifiedAtEpochMs = o.optLong("verifiedAtEpochMs", 0L),
                        createdAtEpochMs = o.optLong("createdAtEpochMs", System.currentTimeMillis())
                    )
                )
            }
        }

        val briefsArr = json.optJSONArray("briefs") ?: JSONArray()
        val briefs = buildList {
            for (i in 0 until briefsArr.length()) {
                val o = briefsArr.getJSONObject(i)
                add(
                    ProjectBriefEntity(
                        id = o.getString("id"),
                        companyId = o.getString("companyId"),
                        companyName = o.optString("companyName"),
                        createdByAccountId = o.optString("createdByAccountId"),
                        assignedDirectorId = o.optString("assignedDirectorId"),
                        assignedDirectorName = o.optString("assignedDirectorName"),
                        title = o.optString("title"),
                        summary = o.optString("summary"),
                        domain = o.optString("domain"),
                        level = o.optString("level", "Mid-level"),
                        skillsCsv = o.optString("skillsCsv"),
                        rubricCsv = o.optString("rubricCsv"),
                        deliverablesCsv = o.optString("deliverablesCsv"),
                        deadline = o.optString("deadline"),
                        pointsReward = o.optInt("pointsReward", 200),
                        prizePool = o.optString("prizePool"),
                        approvalStatus = o.optString("approvalStatus", "PENDING_APPROVAL"),
                        adminNotes = o.optString("adminNotes"),
                        isTeamAllowed = o.optBoolean("isTeamAllowed", true),
                        createdAtEpochMs = o.optLong("createdAtEpochMs", System.currentTimeMillis()),
                        updatedAtEpochMs = o.optLong("updatedAtEpochMs", System.currentTimeMillis())
                    )
                )
            }
        }

        val appsArr = json.optJSONArray("applications") ?: JSONArray()
        val applications = buildList {
            for (i in 0 until appsArr.length()) {
                val o = appsArr.getJSONObject(i)
                add(
                    ProjectApplicationEntity(
                        id = o.getString("id"),
                        briefId = o.getString("briefId"),
                        studentAccountId = o.getString("studentAccountId"),
                        studentName = o.optString("studentName"),
                        studentLevel = o.optString("studentLevel", "Junior"),
                        studentSkillsCsv = o.optString("studentSkillsCsv"),
                        coverNote = o.optString("coverNote"),
                        roleRequested = o.optString("roleRequested"),
                        status = o.optString("status", "APPLIED"),
                        decisionNote = o.optString("decisionNote"),
                        appliedAtEpochMs = o.optLong("appliedAtEpochMs", System.currentTimeMillis()),
                        updatedAtEpochMs = o.optLong("updatedAtEpochMs", System.currentTimeMillis())
                    )
                )
            }
        }

        val tasksArr = json.optJSONArray("tasks") ?: JSONArray()
        val tasks = buildList {
            for (i in 0 until tasksArr.length()) {
                val o = tasksArr.getJSONObject(i)
                add(
                    ProjectTaskEntity(
                        id = o.getString("id"),
                        briefId = o.getString("briefId"),
                        assignedStudentId = o.optString("assignedStudentId"),
                        assignedStudentName = o.optString("assignedStudentName"),
                        assignedDirectorId = o.optString("assignedDirectorId"),
                        title = o.optString("title"),
                        milestoneOrder = o.optInt("milestoneOrder", 1),
                        dueDateLabel = o.optString("dueDateLabel"),
                        isOverdue = o.optBoolean("isOverdue", false),
                        status = o.optString("status", "TODO"),
                        createdAtEpochMs = o.optLong("createdAtEpochMs", System.currentTimeMillis()),
                        updatedAtEpochMs = o.optLong("updatedAtEpochMs", System.currentTimeMillis())
                    )
                )
            }
        }

        val subsArr = json.optJSONArray("submissions") ?: JSONArray()
        val submissions = buildList {
            for (i in 0 until subsArr.length()) {
                val o = subsArr.getJSONObject(i)
                add(
                    WorkSubmissionEntity(
                        id = o.getString("id"),
                        briefId = o.getString("briefId"),
                        briefTitle = o.optString("briefTitle"),
                        companyName = o.optString("companyName"),
                        studentAccountId = o.optString("studentAccountId"),
                        studentName = o.optString("studentName"),
                        directorAccountId = o.optString("directorAccountId"),
                        repoOrDemoUrl = o.optString("repoOrDemoUrl"),
                        understandingReport = o.optString("understandingReport"),
                        skillsAppliedCsv = o.optString("skillsAppliedCsv"),
                        status = o.optString("status", "PENDING_REVIEW"),
                        score = o.optInt("score", 0),
                        pointsAwarded = o.optInt("pointsAwarded", 0),
                        feedback = o.optString("feedback"),
                        rejectionReason = o.optString("rejectionReason"),
                        verificationCode = o.optString("verificationCode"),
                        isPublicOnPassport = o.optBoolean("isPublicOnPassport", true),
                        submittedAtEpochMs = o.optLong("submittedAtEpochMs", System.currentTimeMillis()),
                        evaluatedAtEpochMs = o.optLong("evaluatedAtEpochMs", 0L)
                    )
                )
            }
        }

        val notifArr = json.optJSONArray("notifications") ?: JSONArray()
        val notifications = buildList {
            for (i in 0 until notifArr.length()) {
                val o = notifArr.getJSONObject(i)
                add(
                    NotificationEntity(
                        id = o.getString("id"),
                        recipientAccountId = o.optString("recipientAccountId"),
                        category = o.optString("category", "SYSTEM"),
                        titleEn = o.optString("titleEn"),
                        titleAr = o.optString("titleAr"),
                        bodyEn = o.optString("bodyEn"),
                        bodyAr = o.optString("bodyAr"),
                        isRead = o.optBoolean("isRead", false),
                        createdAtEpochMs = o.optLong("createdAtEpochMs", System.currentTimeMillis())
                    )
                )
            }
        }

        val actArr = json.optJSONArray("activities") ?: JSONArray()
        val activities = buildList {
            for (i in 0 until actArr.length()) {
                val o = actArr.getJSONObject(i)
                add(
                    PlatformActivityEntity(
                        id = o.getString("id"),
                        actorAccountId = o.optString("actorAccountId"),
                        actorRole = o.optString("actorRole"),
                        actionType = o.optString("actionType"),
                        summary = o.optString("summary"),
                        targetId = o.optString("targetId"),
                        createdAtEpochMs = o.optLong("createdAtEpochMs", System.currentTimeMillis())
                    )
                )
            }
        }

        val repArr = json.optJSONArray("reports") ?: JSONArray()
        val reports = buildList {
            for (i in 0 until repArr.length()) {
                val o = repArr.getJSONObject(i)
                add(
                    ModerationReportEntity(
                        id = o.getString("id"),
                        reporterAccountId = o.optString("reporterAccountId"),
                        targetType = o.optString("targetType"),
                        targetId = o.optString("targetId"),
                        reason = o.optString("reason"),
                        status = o.optString("status", "OPEN"),
                        resolutionNote = o.optString("resolutionNote"),
                        createdAtEpochMs = o.optLong("createdAtEpochMs", System.currentTimeMillis())
                    )
                )
            }
        }

        val sessArr = json.optJSONArray("deviceSessions") ?: JSONArray()
        val deviceSessions = buildList {
            for (i in 0 until sessArr.length()) {
                val o = sessArr.getJSONObject(i)
                add(
                    DeviceSessionEntity(
                        id = o.getString("id"),
                        accountId = o.optString("accountId"),
                        deviceType = o.optString("deviceType", "MOBILE"),
                        deviceLabel = o.optString("deviceLabel"),
                        windowWidthDp = o.optInt("windowWidthDp", 411),
                        lastActiveEpochMs = o.optLong("lastActiveEpochMs", System.currentTimeMillis()),
                        revoked = o.optBoolean("revoked", false)
                    )
                )
            }
        }

        return RemoteSnapshotPayload(
            revision = json.optLong("revision", 0L),
            updatedAtEpochMs = json.optLong("updatedAtEpochMs", System.currentTimeMillis()),
            accounts = accounts,
            companies = companies,
            briefs = briefs,
            applications = applications,
            tasks = tasks,
            submissions = submissions,
            notifications = notifications,
            activities = activities,
            reports = reports,
            deviceSessions = deviceSessions
        )
    }
}
