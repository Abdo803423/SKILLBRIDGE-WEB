package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.core.PasswordHasher
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
import com.example.data.local.SkillBridgeDao
import com.example.data.local.SkillBridgeDatabase
import com.example.data.local.WorkSubmissionEntity
import com.example.data.remote.BackendHttpApiException
import com.example.data.remote.CloudCentralSyncEngine
import com.example.data.remote.RemoteSnapshotPayload
import com.example.data.remote.SkillBridgeRemoteApiClient
import com.example.data.remote.SyncState
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class BackendAuthorizationException(message: String) : SecurityException(message)
class BackendValidationException(message: String) : IllegalArgumentException(message)

/**
 * Authoritative SkillBridge Backend Service bridging:
 * 1) Shared Central Express Server + Live SSE (unified Web / Windows Desktop / Android)
 * 2) Shared Firebase Firestore real-time mirror
 * 3) Local Room SQLite cache + Salted PBKDF2 offline fallback
 */
class SkillBridgeBackendRepository(
    private val db: SkillBridgeDatabase,
    private val dao: SkillBridgeDao = db.skillBridgeDao(),
    private val syncEngine: CloudCentralSyncEngine? = null,
    private val remoteApi: SkillBridgeRemoteApiClient? = null,
    private val prefsStore: UserPreferencesStore? = null
) {
    constructor(context: Context) : this(
        db = SkillBridgeDatabase.getInstance(context),
        dao = SkillBridgeDatabase.getInstance(context).skillBridgeDao(),
        syncEngine = createDefaultSyncEngine(context),
        remoteApi = SkillBridgeRemoteApiClient(UserPreferencesStore(context.applicationContext)),
        prefsStore = UserPreferencesStore(context.applicationContext)
    )

    private val repoScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val syncState: StateFlow<SyncState> = combine(
        remoteApi?.remoteSyncState ?: MutableStateFlow<SyncState?>(null),
        syncEngine?.syncState ?: MutableStateFlow(SyncState.LOCAL_SYNCED)
    ) { remoteState, firestoreState ->
        when {
            remoteState == SyncState.LIVE_CLOUD -> SyncState.LIVE_CLOUD
            remoteState == SyncState.SYNCING -> SyncState.SYNCING
            firestoreState == SyncState.LIVE_CLOUD -> SyncState.LIVE_CLOUD
            remoteState != null -> remoteState
            else -> firestoreState
        }
    }.stateIn(repoScope, SharingStarted.Eagerly, SyncState.LOCAL_SYNCED)

    val lastSyncedEpochMs: StateFlow<Long> = combine(
        remoteApi?.remoteLastSyncedEpochMs ?: MutableStateFlow(0L),
        syncEngine?.lastSyncedEpochMs ?: MutableStateFlow(System.currentTimeMillis())
    ) { remoteTs, fsTs ->
        maxOf(remoteTs, fsTs)
    }.stateIn(repoScope, SharingStarted.Eagerly, System.currentTimeMillis())

    val pendingWritesCount: StateFlow<Int> =
        syncEngine?.pendingWritesCount ?: MutableStateFlow(0)

    fun startCentralSync(accountId: String) {
        syncEngine?.startListeners(accountId)
        remoteApi?.startLiveSseSync(db)
    }

    fun stopCentralSync() {
        syncEngine?.stopListeners()
        remoteApi?.stopLiveSseSync()
    }

    suspend fun triggerManualSync() {
        val api = remoteApi
        val store = prefsStore
        if (api != null && store != null) {
            val token = store.getBackendToken()
            if (token.isNotBlank()) {
                runCatching {
                    val snapshot = api.fetchSnapshot(token)
                    api.syncSnapshotIntoDatabase(db, snapshot)
                }
            }
        }
        syncEngine?.triggerManualSync()
    }

    suspend fun ensureSeeded() {
        SkillBridgeDatabase.ensureSeeded(db)
    }

    companion object {
        private fun createDefaultSyncEngine(context: Context): CloudCentralSyncEngine? {
            return try {
                val app = if (FirebaseApp.getApps(context).isNotEmpty()) FirebaseApp.getInstance() else return null
                val dbId = context.getString(R.string.firestore_database_id)
                val firestore = FirebaseFirestore.getInstance(app, dbId)
                val auth = Firebase.auth
                CloudCentralSyncEngine(
                    dao = SkillBridgeDatabase.getInstance(context).skillBridgeDao(),
                    firestore = firestore,
                    auth = auth
                )
            } catch (_: Exception) {
                null
            }
        }
    }

    private fun mapHttpException(e: BackendHttpApiException): Throwable =
        when (e.statusCode) {
            401, 403 -> BackendAuthorizationException(e.message)
            else -> BackendValidationException(e.message)
        }

    // --- Authentication & Session Resolution ---

    suspend fun authenticateWithCredentials(loginId: String, password: String): Result<SessionPrincipal> {
        return try {
            ensureSeeded()
            val cleanLogin = loginId.trim()
            if (cleanLogin.isEmpty() || password.isEmpty()) {
                return Result.failure(BackendValidationException("Login ID and password are required."))
            }

            val api = remoteApi
            val store = prefsStore
            if (api != null && store != null) {
                try {
                    val authRes = api.login(cleanLogin, password)
                    store.setBackendToken(authRes.token)
                    runCatching {
                        val snapshot = api.fetchSnapshot(authRes.token)
                        api.syncSnapshotIntoDatabase(db, snapshot)
                    }
                    return Result.success(authRes.principal)
                } catch (e: BackendHttpApiException) {
                    return Result.failure(mapHttpException(e))
                } catch (_: Exception) {
                    // Fall through to local offline cache only if central server is unreachable
                }
            }

            val account = dao.getAccountByLoginId(cleanLogin)
                ?: return Result.failure(BackendAuthorizationException("Invalid Login ID or password."))
            if (!account.isActive) {
                return Result.failure(BackendAuthorizationException("This account has been suspended by Platform Admin."))
            }
            val valid = PasswordHasher.verifyPassword(password, account.passwordSalt, account.passwordHash)
            if (!valid) {
                return Result.failure(BackendAuthorizationException("Invalid Login ID or password."))
            }
            val principal = account.toPrincipal(isGoogleLinked = false)
            logActivity(
                actorAccountId = account.id,
                actorRole = principal.role.key,
                actionType = "AUTH_LOGIN",
                summary = "${account.displayName} (${principal.role.key}) signed in.",
                targetId = account.id
            )
            Result.success(principal)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun registerStudentAccount(
        loginId: String,
        displayName: String,
        email: String,
        password: String
    ): Result<SessionPrincipal> {
        return try {
            ensureSeeded()
            val cleanLogin = loginId.trim().lowercase().replace(Regex("[^a-z0-9_.-]"), "")
            val cleanName = displayName.trim()
            if (cleanLogin.length < 3) {
                return Result.failure(BackendValidationException("Login ID must be at least 3 characters."))
            }
            if (cleanName.length < 2) {
                return Result.failure(BackendValidationException("Display name is required."))
            }
            if (password.length < 6) {
                return Result.failure(BackendValidationException("Password must be at least 6 characters."))
            }

            val api = remoteApi
            val store = prefsStore
            if (api != null && store != null) {
                try {
                    val authRes = api.registerStudent(cleanLogin, cleanName, email.trim(), password)
                    store.setBackendToken(authRes.token)
                    runCatching {
                        val snapshot = api.fetchSnapshot(authRes.token)
                        api.syncSnapshotIntoDatabase(db, snapshot)
                    }
                    dao.getAccountById(authRes.principal.accountId)?.let { syncEngine?.pushAccount(it) }
                    return Result.success(authRes.principal)
                } catch (e: BackendHttpApiException) {
                    return Result.failure(mapHttpException(e))
                } catch (_: Exception) {
                    // Fall through to local offline registration if server is unreachable
                }
            }

            if (dao.getAccountByLoginId(cleanLogin) != null) {
                return Result.failure(BackendValidationException("Login ID '$cleanLogin' is already taken."))
            }
            val (salt, hash) = PasswordHasher.createSaltedHash(password)
            val now = System.currentTimeMillis()
            val account = AccountEntity(
                id = "acc_${UUID.randomUUID().toString().take(12)}",
                loginId = cleanLogin,
                displayName = cleanName,
                email = email.trim().ifBlank { "$cleanLogin@skillbridge.io" },
                passwordSalt = salt,
                passwordHash = hash,
                role = AccountRole.STUDENT.key,
                companyId = "",
                isActive = true,
                careerLevel = "Junior",
                domain = "Backend & Cloud",
                country = "Egypt",
                points = 0,
                streakDays = 0,
                verifiedCount = 0,
                skillsCsv = "",
                targetSkillsCsv = "",
                goalsSummary = "",
                weeklyHours = 8,
                profileCompletion = 35,
                createdAtEpochMs = now
            )
            dao.upsertAccount(account)
            syncEngine?.pushAccount(account)
            logActivity(account.id, AccountRole.STUDENT.key, "USER_REGISTERED", "New student account registered: $cleanName", account.id)
            Result.success(account.toPrincipal(isGoogleLinked = false))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun resolveOrCreateGoogleStudentSession(
        firebaseUid: String,
        displayName: String,
        email: String
    ): Result<SessionPrincipal> {
        return try {
            ensureSeeded()

            val api = remoteApi
            val store = prefsStore
            if (api != null && store != null) {
                try {
                    val authRes = api.resolveGoogleSession(firebaseUid, displayName, email)
                    store.setBackendToken(authRes.token)
                    runCatching {
                        val snapshot = api.fetchSnapshot(authRes.token)
                        api.syncSnapshotIntoDatabase(db, snapshot)
                    }
                    dao.getAccountById(authRes.principal.accountId)?.let { syncEngine?.pushAccount(it) }
                    return Result.success(authRes.principal)
                } catch (e: BackendHttpApiException) {
                    return Result.failure(mapHttpException(e))
                } catch (_: Exception) {
                    // Fall through to local offline cache when unreachable
                }
            }

            val existing = dao.getAccountById(firebaseUid)
            if (existing != null) {
                if (!existing.isActive) {
                    return Result.failure(BackendAuthorizationException("Account suspended by Platform Admin."))
                }
                return Result.success(existing.toPrincipal(isGoogleLinked = true))
            }
            val now = System.currentTimeMillis()
            val (salt, hash) = PasswordHasher.createSaltedHash(UUID.randomUUID().toString())
            val cleanLogin = "google_${firebaseUid.take(10)}"
            val account = AccountEntity(
                id = firebaseUid,
                loginId = cleanLogin,
                displayName = displayName.ifBlank { email.substringBefore("@").ifBlank { "Student" } },
                email = email,
                passwordSalt = salt,
                passwordHash = hash,
                role = AccountRole.STUDENT.key,
                companyId = "",
                isActive = true,
                careerLevel = "Junior",
                domain = "Backend & Cloud",
                country = "Egypt",
                points = 0,
                streakDays = 0,
                verifiedCount = 0,
                skillsCsv = "",
                targetSkillsCsv = "",
                goalsSummary = "",
                weeklyHours = 8,
                profileCompletion = 35,
                createdAtEpochMs = now
            )
            dao.upsertAccount(account)
            syncEngine?.pushAccount(account)
            Result.success(account.toPrincipal(isGoogleLinked = true))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getSessionByAccountId(accountId: String, isGoogleLinked: Boolean = false): SessionPrincipal? {
        ensureSeeded()
        val api = remoteApi
        val store = prefsStore
        if (api != null && store != null) {
            val token = store.getBackendToken()
            if (token.isNotBlank()) {
                runCatching {
                    val snapshot = api.fetchSnapshot(token)
                    api.syncSnapshotIntoDatabase(db, snapshot)
                }
            }
        }
        val account = dao.getAccountById(accountId) ?: return null
        if (!account.isActive) return null
        return account.toPrincipal(isGoogleLinked = isGoogleLinked)
    }

    // --- Reactive Observations ---

    fun observeAccount(accountId: String): Flow<AccountEntity?> = dao.observeAccount(accountId)
    fun observeAllAccounts(): Flow<List<AccountEntity>> = dao.observeAllAccounts()
    fun observeStudentAccounts(): Flow<List<AccountEntity>> = dao.observeStudentAccounts()
    fun observeCompanyDirectors(companyId: String): Flow<List<AccountEntity>> = dao.observeCompanyDirectors(companyId)

    fun observeAllCompanies(): Flow<List<CompanyEntity>> = dao.observeAllCompanies()
    fun observeCompany(companyId: String): Flow<CompanyEntity?> = dao.observeCompany(companyId)

    fun observeAllBriefs(): Flow<List<ProjectBriefEntity>> = dao.observeAllBriefs()
    fun observePublishedBriefs(): Flow<List<ProjectBriefEntity>> = dao.observePublishedBriefs()
    fun observeCompanyBriefs(companyId: String): Flow<List<ProjectBriefEntity>> = dao.observeCompanyBriefs(companyId)
    fun observeDirectorBriefs(directorId: String): Flow<List<ProjectBriefEntity>> = dao.observeDirectorBriefs(directorId)

    fun observeAllApplications(): Flow<List<ProjectApplicationEntity>> = dao.observeAllApplications()
    fun observeStudentApplications(studentId: String): Flow<List<ProjectApplicationEntity>> = dao.observeStudentApplications(studentId)
    fun observeCompanyApplications(companyId: String): Flow<List<ProjectApplicationEntity>> = dao.observeCompanyApplications(companyId)

    fun observeStudentTasks(studentId: String): Flow<List<ProjectTaskEntity>> = dao.observeStudentTasks(studentId)
    fun observeDirectorTasks(directorId: String): Flow<List<ProjectTaskEntity>> = dao.observeDirectorTasks(directorId)

    fun observeAllSubmissions(): Flow<List<WorkSubmissionEntity>> = dao.observeAllSubmissions()
    fun observeStudentSubmissions(studentId: String): Flow<List<WorkSubmissionEntity>> = dao.observeStudentSubmissions(studentId)
    fun observeDirectorSubmissions(directorId: String): Flow<List<WorkSubmissionEntity>> = dao.observeDirectorSubmissions(directorId)

    fun observeNotifications(accountId: String): Flow<List<NotificationEntity>> = dao.observeNotifications(accountId)
    fun observeAllActivities(): Flow<List<PlatformActivityEntity>> = dao.observeAllActivities()
    fun observeAllReports(): Flow<List<ModerationReportEntity>> = dao.observeAllReports()

    // --- Cross-Device Sessions (Mobile & Desktop/Laptop) ---
    fun observeDeviceSessions(accountId: String): Flow<List<DeviceSessionEntity>> =
        dao.observeDeviceSessions(accountId)

    fun observeDeviceSession(sessionId: String): Flow<DeviceSessionEntity?> =
        dao.observeDeviceSession(sessionId)

    private suspend fun tryRemotePostUnit(
        path: String,
        body: JSONObject
    ): Result<RemoteSnapshotPayload?> {
        val api = remoteApi ?: return Result.success(null)
        val token = prefsStore?.getBackendToken().orEmpty()
        if (token.isBlank()) return Result.success(null)
        return try {
            val snap = api.postMutationAndExtractSnapshot(path, body, token)
            api.syncSnapshotIntoDatabase(db, snap)
            Result.success(snap)
        } catch (e: BackendHttpApiException) {
            Result.failure(mapHttpException(e))
        } catch (_: Exception) {
            Result.success(null) // Offline -> caller performs local fallback
        }
    }

    private suspend fun tryRemotePostReturningId(
        path: String,
        body: JSONObject,
        entityKey: String
    ): Result<String?> {
        val api = remoteApi ?: return Result.success(null)
        val token = prefsStore?.getBackendToken().orEmpty()
        if (token.isBlank()) return Result.success(null)
        return try {
            val (createdId, snap) = api.postMutationReturningIdAndSnapshot(path, body, entityKey, token)
            api.syncSnapshotIntoDatabase(db, snap)
            Result.success(createdId)
        } catch (e: BackendHttpApiException) {
            Result.failure(mapHttpException(e))
        } catch (_: Exception) {
            Result.success(null) // Offline -> caller performs local fallback
        }
    }

    suspend fun registerDeviceSession(
        accountId: String,
        sessionId: String,
        deviceType: String,
        deviceLabel: String,
        windowWidthDp: Int
    ): Result<DeviceSessionEntity> {
        return try {
            val safeType = if (deviceType in setOf("MOBILE", "DESKTOP_LAPTOP")) deviceType else "MOBILE"
            val remoteBody = JSONObject()
                .put("sessionId", sessionId)
                .put("deviceType", safeType)
                .put("deviceLabel", deviceLabel.take(80))
                .put("windowWidthDp", windowWidthDp)
            val remoteAttempt = tryRemotePostUnit("/api/ecosystem/device-session", remoteBody)
            if (remoteAttempt.isFailure) return Result.failure(remoteAttempt.exceptionOrNull()!!)
            if (remoteAttempt.getOrNull() != null) {
                dao.getDeviceSessionById(sessionId)?.let { return Result.success(it) }
            }

            val existing = dao.getDeviceSessionById(sessionId)
            val entity = DeviceSessionEntity(
                id = sessionId,
                accountId = accountId,
                deviceType = safeType,
                deviceLabel = deviceLabel.take(80),
                windowWidthDp = windowWidthDp,
                lastActiveEpochMs = System.currentTimeMillis(),
                revoked = existing?.revoked ?: false
            )
            dao.upsertDeviceSession(entity)
            syncEngine?.pushDeviceSession(entity)
            Result.success(entity)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun revokeDeviceSession(actorAccountId: String, targetSessionId: String): Result<Unit> {
        return try {
            val remoteAttempt = tryRemotePostUnit(
                "/api/ecosystem/device-session/revoke",
                JSONObject().put("targetSessionId", targetSessionId)
            )
            if (remoteAttempt.isFailure) return Result.failure(remoteAttempt.exceptionOrNull()!!)
            if (remoteAttempt.getOrNull() != null) return Result.success(Unit)

            val target = dao.getDeviceSessionById(targetSessionId)
                ?: return Result.failure(BackendValidationException("Device session not found."))
            if (target.accountId != actorAccountId) {
                return Result.failure(BackendAuthorizationException("Cannot revoke a session belonging to another account."))
            }
            dao.revokeDeviceSession(targetSessionId)
            syncEngine?.pushDeviceSession(target.copy(revoked = true, lastActiveEpochMs = System.currentTimeMillis()))
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- Role-Enforced Mutations ---

    suspend fun createProjectBrief(
        session: SessionPrincipal,
        title: String,
        summary: String,
        domain: String,
        level: String,
        skills: List<String>,
        rubric: List<String>,
        deliverables: List<String>,
        deadline: String,
        pointsReward: Int,
        prizePool: String,
        assignedDirectorId: String,
        isTeamAllowed: Boolean
    ): Result<String> {
        return try {
            requireRole(session, AccountRole.COMPANY)
            val body = JSONObject()
                .put("title", title)
                .put("summary", summary)
                .put("domain", domain)
                .put("level", level)
                .put("skills", JSONArray(skills))
                .put("rubric", JSONArray(rubric))
                .put("deliverables", JSONArray(deliverables))
                .put("deadline", deadline)
                .put("pointsReward", pointsReward)
                .put("prizePool", prizePool)
                .put("assignedDirectorId", assignedDirectorId)
                .put("isTeamAllowed", isTeamAllowed)
            val remoteAttempt = tryRemotePostReturningId("/api/ecosystem/briefs", body, "brief")
            if (remoteAttempt.isFailure) return Result.failure(remoteAttempt.exceptionOrNull()!!)
            remoteAttempt.getOrNull()?.let { createdId ->
                dao.getBriefById(createdId)?.let { syncEngine?.pushBrief(it) }
                return Result.success(createdId)
            }

            val company = dao.getCompanyById(session.companyId)
                ?: return Result.failure(BackendAuthorizationException("No company entity linked to this account."))
            if (title.trim().length < 4) {
                return Result.failure(BackendValidationException("Project brief title must be at least 4 characters."))
            }
            if (summary.trim().length < 10) {
                return Result.failure(BackendValidationException("Please provide a clear project brief summary."))
            }
            var resolvedDirectorId = ""
            var resolvedDirectorName = ""
            if (assignedDirectorId.isNotBlank()) {
                val director = dao.getAccountById(assignedDirectorId)
                    ?: return Result.failure(BackendValidationException("Selected director does not exist."))
                if (director.role != AccountRole.COMPANY_DIRECTOR.key || director.companyId != company.id) {
                    return Result.failure(BackendAuthorizationException("Director must belong to ${company.name}."))
                }
                resolvedDirectorId = director.id
                resolvedDirectorName = director.displayName
            }

            val now = System.currentTimeMillis()
            val briefId = "brief_${UUID.randomUUID().toString().take(10)}"
            val brief = ProjectBriefEntity(
                id = briefId,
                companyId = company.id,
                companyName = company.name,
                createdByAccountId = session.accountId,
                assignedDirectorId = resolvedDirectorId,
                assignedDirectorName = resolvedDirectorName,
                title = title.trim().take(140),
                summary = summary.trim().take(1500),
                domain = domain.ifBlank { company.domain },
                level = if (level in setOf("Junior", "Mid-level", "Senior", "Director / Leadership")) level else "Mid-level",
                skillsCsv = skills.map { it.trim() }.filter { it.isNotEmpty() }.joinToString(","),
                rubricCsv = rubric.map { it.trim() }.filter { it.isNotEmpty() }.joinToString(",").ifBlank { "Architecture (40%),Tests (30%),Docs (30%)" },
                deliverablesCsv = deliverables.map { it.trim() }.filter { it.isNotEmpty() }.joinToString(",").ifBlank { "Git Repo,Understanding Summary" },
                deadline = deadline.trim().ifBlank { "Mar 15, 2027" },
                pointsReward = pointsReward.coerceIn(50, 2000),
                prizePool = prizePool.trim().ifBlank { "Points + Certificate" },
                approvalStatus = "PENDING_APPROVAL",
                adminNotes = "",
                isTeamAllowed = isTeamAllowed,
                createdAtEpochMs = now,
                updatedAtEpochMs = now
            )
            dao.upsertBrief(brief)
            syncEngine?.pushBrief(brief)
            logActivity(session.accountId, session.role.key, "BRIEF_CREATED", "${company.name} created brief '${brief.title}'", briefId)
            notifyAdmins(
                category = "APPROVAL",
                titleEn = "New Brief Pending Approval",
                titleAr = "مشروع جديد بانتظار الاعتماد",
                bodyEn = "${company.name} submitted '${brief.title}' for Admin review.",
                bodyAr = "قدّمت شركة ${company.name} مشروع '${brief.title}' لاعتماده."
            )
            Result.success(briefId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun assignDirectorToBrief(
        session: SessionPrincipal,
        briefId: String,
        directorAccountId: String
    ): Result<Unit> {
        return try {
            requireRole(session, AccountRole.COMPANY)
            val remoteAttempt = tryRemotePostUnit(
                "/api/ecosystem/briefs/$briefId/assign-director",
                JSONObject().put("directorAccountId", directorAccountId)
            )
            if (remoteAttempt.isFailure) return Result.failure(remoteAttempt.exceptionOrNull()!!)
            if (remoteAttempt.getOrNull() != null) {
                dao.getBriefById(briefId)?.let { syncEngine?.pushBrief(it) }
                return Result.success(Unit)
            }

            val brief = dao.getBriefById(briefId)
                ?: return Result.failure(BackendValidationException("Brief not found."))
            if (brief.companyId != session.companyId) {
                return Result.failure(BackendAuthorizationException("You can only manage briefs belonging to your company."))
            }
            val director = dao.getAccountById(directorAccountId)
                ?: return Result.failure(BackendValidationException("Director account not found."))
            if (director.role != AccountRole.COMPANY_DIRECTOR.key || director.companyId != session.companyId) {
                return Result.failure(BackendAuthorizationException("Director does not belong to your company."))
            }
            val now = System.currentTimeMillis()
            val updatedBrief = brief.copy(
                assignedDirectorId = director.id,
                assignedDirectorName = director.displayName,
                updatedAtEpochMs = now
            )
            dao.updateBrief(updatedBrief)
            syncEngine?.pushBrief(updatedBrief)
            sendNotification(
                recipientId = director.id,
                category = "TASK",
                titleEn = "Assigned to Project Brief",
                titleAr = "تم تعيينك مشرفًا على مشروع",
                bodyEn = "You are now supervising '${brief.title}' for ${brief.companyName}.",
                bodyAr = "تم تعيينك للإشراف على مشروع '${brief.title}'."
            )
            logActivity(session.accountId, session.role.key, "DIRECTOR_ASSIGNED", "Assigned ${director.displayName} to '${brief.title}'", briefId)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun reviewCompanyVerification(
        session: SessionPrincipal,
        companyId: String,
        approve: Boolean,
        tier: String,
        notes: String
    ): Result<Unit> {
        return try {
            requireRole(session, AccountRole.PLATFORM_ADMIN)
            val remoteAttempt = tryRemotePostUnit(
                "/api/ecosystem/companies/$companyId/verify",
                JSONObject().put("approve", approve).put("tier", tier).put("notes", notes)
            )
            if (remoteAttempt.isFailure) return Result.failure(remoteAttempt.exceptionOrNull()!!)
            if (remoteAttempt.getOrNull() != null) {
                dao.getCompanyById(companyId)?.let { syncEngine?.pushCompany(it) }
                return Result.success(Unit)
            }

            val company = dao.getCompanyById(companyId)
                ?: return Result.failure(BackendValidationException("Company not found."))
            val now = System.currentTimeMillis()
            val status = if (approve) "VERIFIED" else "REJECTED"
            val nextTier = if (approve) {
                if (tier in setOf("BASIC", "BUSINESS", "TRUSTED")) tier else "BUSINESS"
            } else {
                "UNVERIFIED"
            }
            val updatedCompany = company.copy(
                verificationStatus = status,
                verificationTier = nextTier,
                verificationNotes = notes.trim().ifBlank {
                    if (approve) "Verified by SkillBridge Platform Admin." else "Verification rejected; resubmit compliance docs."
                },
                verifiedAtEpochMs = if (approve) now else 0L
            )
            dao.updateCompany(updatedCompany)
            syncEngine?.pushCompany(updatedCompany)
            sendNotification(
                recipientId = company.ownerAccountId,
                category = "APPROVAL",
                titleEn = if (approve) "Company Verified ($nextTier)" else "Company Verification Rejected",
                titleAr = if (approve) "تم توثيق الشركة ($nextTier)" else "تم رفض طلب توثيق الشركة",
                bodyEn = notes.ifBlank { "Status updated to $status by Platform Admin." },
                bodyAr = notes.ifBlank { "تم تحديث حالة التوثيق إلى $status." }
            )
            logActivity(session.accountId, session.role.key, "COMPANY_$status", "Admin set ${company.name} verification to $status ($nextTier)", companyId)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun reviewProjectBrief(
        session: SessionPrincipal,
        briefId: String,
        approve: Boolean,
        adminNotes: String
    ): Result<Unit> {
        return try {
            requireRole(session, AccountRole.PLATFORM_ADMIN)
            val remoteAttempt = tryRemotePostUnit(
                "/api/ecosystem/briefs/$briefId/review",
                JSONObject().put("approve", approve).put("adminNotes", adminNotes)
            )
            if (remoteAttempt.isFailure) return Result.failure(remoteAttempt.exceptionOrNull()!!)
            if (remoteAttempt.getOrNull() != null) {
                dao.getBriefById(briefId)?.let { syncEngine?.pushBrief(it) }
                return Result.success(Unit)
            }

            val brief = dao.getBriefById(briefId)
                ?: return Result.failure(BackendValidationException("Project brief not found."))
            val nextStatus = if (approve) "APPROVED" else "REJECTED"
            val now = System.currentTimeMillis()
            val updatedBrief = brief.copy(
                approvalStatus = nextStatus,
                adminNotes = adminNotes.trim().ifBlank {
                    if (approve) "Approved by Platform Admin." else "Rejected: tighten rubric and deliverables."
                },
                updatedAtEpochMs = now
            )
            dao.updateBrief(updatedBrief)
            syncEngine?.pushBrief(updatedBrief)
            sendNotification(
                recipientId = brief.createdByAccountId,
                category = "APPROVAL",
                titleEn = if (approve) "Brief Approved & Published" else "Brief Needs Revision",
                titleAr = if (approve) "تم اعتماد ونشر المشروع" else "تم رفض مسودة المشروع",
                bodyEn = "'${brief.title}' is now $nextStatus.",
                bodyAr = "أصبحت حالة مشروع '${brief.title}' الآن $nextStatus."
            )
            if (approve && brief.assignedDirectorId.isNotBlank()) {
                sendNotification(
                    recipientId = brief.assignedDirectorId,
                    category = "APPROVAL",
                    titleEn = "Supervised Brief Approved",
                    titleAr = "تم اعتماد مشروع تحت إشرافك",
                    bodyEn = "'${brief.title}' is live for student applications.",
                    bodyAr = "مشروع '${brief.title}' متاح الآن لتقديم الطلاب."
                )
            }
            logActivity(session.accountId, session.role.key, "BRIEF_$nextStatus", "Admin marked '${brief.title}' as $nextStatus", briefId)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun setUserActiveStatus(
        session: SessionPrincipal,
        targetAccountId: String,
        active: Boolean
    ): Result<Unit> {
        return try {
            requireRole(session, AccountRole.PLATFORM_ADMIN)
            if (targetAccountId == session.accountId) {
                return Result.failure(BackendValidationException("Cannot suspend your own active admin session."))
            }
            val remoteAttempt = tryRemotePostUnit(
                "/api/ecosystem/users/$targetAccountId/active",
                JSONObject().put("active", active)
            )
            if (remoteAttempt.isFailure) return Result.failure(remoteAttempt.exceptionOrNull()!!)
            if (remoteAttempt.getOrNull() != null) {
                dao.getAccountById(targetAccountId)?.let { syncEngine?.pushAccount(it) }
                return Result.success(Unit)
            }

            val account = dao.getAccountById(targetAccountId)
                ?: return Result.failure(BackendValidationException("User not found."))
            val updatedAcc = account.copy(isActive = active)
            dao.updateAccount(updatedAcc)
            syncEngine?.pushAccount(updatedAcc)
            logActivity(
                session.accountId,
                session.role.key,
                if (active) "USER_ACTIVATED" else "USER_SUSPENDED",
                "Admin ${if (active) "activated" else "suspended"} ${account.loginId}",
                targetAccountId
            )
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun resolveModerationReport(
        session: SessionPrincipal,
        reportId: String,
        status: String,
        resolutionNote: String
    ): Result<Unit> {
        return try {
            requireRole(session, AccountRole.PLATFORM_ADMIN)
            val remoteAttempt = tryRemotePostUnit(
                "/api/ecosystem/reports/$reportId/resolve",
                JSONObject().put("status", status).put("resolutionNote", resolutionNote)
            )
            if (remoteAttempt.isFailure) return Result.failure(remoteAttempt.exceptionOrNull()!!)
            if (remoteAttempt.getOrNull() != null) return Result.success(Unit)

            val report = dao.getReportById(reportId)
                ?: return Result.failure(BackendValidationException("Report not found."))
            val safeStatus = if (status in setOf("RESOLVED", "DISMISSED")) status else "RESOLVED"
            val updatedReport = report.copy(
                status = safeStatus,
                resolutionNote = resolutionNote.trim().ifBlank { "Reviewed by Platform Admin." }
            )
            dao.upsertReport(updatedReport)
            syncEngine?.pushReport(updatedReport)
            logActivity(session.accountId, session.role.key, "REPORT_$safeStatus", "Moderation report $reportId marked $safeStatus", reportId)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun applyToProjectBrief(
        session: SessionPrincipal,
        briefId: String,
        coverNote: String,
        roleRequested: String
    ): Result<String> {
        return try {
            requireRole(session, AccountRole.STUDENT)
            val remoteAttempt = tryRemotePostReturningId(
                "/api/ecosystem/briefs/$briefId/apply",
                JSONObject().put("coverNote", coverNote).put("roleRequested", roleRequested),
                "application"
            )
            if (remoteAttempt.isFailure) return Result.failure(remoteAttempt.exceptionOrNull()!!)
            remoteAttempt.getOrNull()?.let { appId ->
                dao.getApplicationById(appId)?.let { syncEngine?.pushApplication(it) }
                return Result.success(appId)
            }

            val brief = dao.getBriefById(briefId)
                ?: return Result.failure(BackendValidationException("Project brief not found."))
            if (brief.approvalStatus !in setOf("APPROVED", "ACTIVE")) {
                return Result.failure(BackendAuthorizationException("Cannot apply to a brief that is not approved/active."))
            }
            val existing = dao.getApplicationForBriefAndStudent(briefId, session.accountId)
            if (existing != null) {
                return Result.failure(BackendValidationException("You already applied to '${brief.title}'."))
            }
            val student = dao.getAccountById(session.accountId)
                ?: return Result.failure(BackendValidationException("Student profile missing."))
            val now = System.currentTimeMillis()
            val appId = "app_${UUID.randomUUID().toString().take(10)}"
            val app = ProjectApplicationEntity(
                id = appId,
                briefId = briefId,
                studentAccountId = student.id,
                studentName = student.displayName,
                studentLevel = student.careerLevel,
                studentSkillsCsv = student.skillsCsv,
                coverNote = coverNote.trim().ifBlank { "Ready to execute deliverables and verify skills." }.take(600),
                roleRequested = roleRequested.trim().ifBlank { "Project Builder" }.take(80),
                status = "APPLIED",
                decisionNote = "",
                appliedAtEpochMs = now,
                updatedAtEpochMs = now
            )
            dao.upsertApplication(app)
            syncEngine?.pushApplication(app)
            sendNotification(
                recipientId = brief.createdByAccountId,
                category = "APPLICATION",
                titleEn = "New Application: ${student.displayName}",
                titleAr = "طلب التحاق جديد: ${student.displayName}",
                bodyEn = "Applied for '${brief.title}' (${app.roleRequested}).",
                bodyAr = "تقدّم إلى مشروع '${brief.title}' (${app.roleRequested})."
            )
            logActivity(session.accountId, session.role.key, "APPLIED", "${student.displayName} applied to '${brief.title}'", appId)
            Result.success(appId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun decideApplication(
        session: SessionPrincipal,
        applicationId: String,
        newStatus: String,
        decisionNote: String
    ): Result<Unit> {
        return try {
            requireRole(session, AccountRole.COMPANY)
            val remoteAttempt = tryRemotePostUnit(
                "/api/ecosystem/applications/$applicationId/decide",
                JSONObject().put("newStatus", newStatus).put("decisionNote", decisionNote)
            )
            if (remoteAttempt.isFailure) return Result.failure(remoteAttempt.exceptionOrNull()!!)
            if (remoteAttempt.getOrNull() != null) {
                dao.getApplicationById(applicationId)?.let { syncEngine?.pushApplication(it) }
                return Result.success(Unit)
            }

            if (newStatus !in setOf("SHORTLISTED", "ACCEPTED", "REJECTED")) {
                return Result.failure(BackendValidationException("Invalid application status: $newStatus"))
            }
            val application = dao.getApplicationById(applicationId)
                ?: return Result.failure(BackendValidationException("Application not found."))
            val brief = dao.getBriefById(application.briefId)
                ?: return Result.failure(BackendValidationException("Associated brief not found."))
            if (brief.companyId != session.companyId) {
                return Result.failure(BackendAuthorizationException("You cannot review applications for another company's brief."))
            }
            val now = System.currentTimeMillis()
            val updatedApp = application.copy(
                status = newStatus,
                decisionNote = decisionNote.trim(),
                updatedAtEpochMs = now
            )
            dao.upsertApplication(updatedApp)
            syncEngine?.pushApplication(updatedApp)
            if (newStatus == "ACCEPTED") {
                if (brief.approvalStatus == "APPROVED") {
                    val activeBrief = brief.copy(approvalStatus = "ACTIVE", updatedAtEpochMs = now)
                    dao.updateBrief(activeBrief)
                    syncEngine?.pushBrief(activeBrief)
                }
                val taskId = "task_${UUID.randomUUID().toString().take(10)}"
                val kickoffTask = ProjectTaskEntity(
                    id = taskId,
                    briefId = brief.id,
                    assignedStudentId = application.studentAccountId,
                    assignedStudentName = application.studentName,
                    assignedDirectorId = brief.assignedDirectorId,
                    title = "Kickoff milestone & architecture plan for ${brief.title}",
                    milestoneOrder = 1,
                    dueDateLabel = brief.deadline,
                    isOverdue = false,
                    status = "TODO",
                    createdAtEpochMs = now,
                    updatedAtEpochMs = now
                )
                dao.upsertTask(kickoffTask)
                syncEngine?.pushTask(kickoffTask)
                if (brief.assignedDirectorId.isNotBlank()) {
                    sendNotification(
                        recipientId = brief.assignedDirectorId,
                        category = "TASK",
                        titleEn = "Student Accepted to Supervised Brief",
                        titleAr = "تم قبول طالب في مشروع تحت إشرافك",
                        bodyEn = "${application.studentName} joined '${brief.title}'.",
                        bodyAr = "انضم ${application.studentName} إلى مشروع '${brief.title}'."
                    )
                }
            }
            sendNotification(
                recipientId = application.studentAccountId,
                category = "APPLICATION",
                titleEn = "Application $newStatus: ${brief.title}",
                titleAr = "تحديث طلبك في ${brief.title}: $newStatus",
                bodyEn = decisionNote.ifBlank { "Company updated your application status to $newStatus." },
                bodyAr = decisionNote.ifBlank { "قامت الشركة بتحديث حالة طلبك إلى $newStatus." }
            )
            logActivity(session.accountId, session.role.key, "APPLICATION_$newStatus", "${brief.companyName} marked ${application.studentName} as $newStatus for '${brief.title}'", applicationId)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun submitProjectWork(
        session: SessionPrincipal,
        briefId: String,
        repoOrDemoUrl: String,
        understandingReport: String,
        skillsApplied: List<String>
    ): Result<String> {
        return try {
            requireRole(session, AccountRole.STUDENT)
            val body = JSONObject()
                .put("briefId", briefId)
                .put("repoOrDemoUrl", repoOrDemoUrl)
                .put("understandingReport", understandingReport)
                .put("skillsApplied", JSONArray(skillsApplied))
            val remoteAttempt = tryRemotePostReturningId("/api/ecosystem/submissions", body, "submission")
            if (remoteAttempt.isFailure) return Result.failure(remoteAttempt.exceptionOrNull()!!)
            remoteAttempt.getOrNull()?.let { subId ->
                dao.getSubmissionById(subId)?.let { syncEngine?.pushSubmission(it) }
                return Result.success(subId)
            }

            val brief = dao.getBriefById(briefId)
                ?: return Result.failure(BackendValidationException("Project brief not found."))
            val app = dao.getApplicationForBriefAndStudent(briefId, session.accountId)
            if (app == null || app.status != "ACCEPTED") {
                return Result.failure(BackendAuthorizationException("You must be ACCEPTED into '${brief.title}' before submitting deliverables."))
            }
            if (understandingReport.trim().length < 15) {
                return Result.failure(BackendValidationException("Please include an Understanding Report (at least 15 characters)."))
            }
            val now = System.currentTimeMillis()
            val subId = "sub_${UUID.randomUUID().toString().take(10)}"
            val submission = WorkSubmissionEntity(
                id = subId,
                briefId = brief.id,
                briefTitle = brief.title,
                companyName = brief.companyName,
                studentAccountId = session.accountId,
                studentName = session.displayName,
                directorAccountId = brief.assignedDirectorId,
                repoOrDemoUrl = repoOrDemoUrl.trim().take(300),
                understandingReport = understandingReport.trim().take(2500),
                skillsAppliedCsv = skillsApplied.map { it.trim() }.filter { it.isNotEmpty() }.joinToString(",")
                    .ifBlank { brief.skillsCsv },
                status = "PENDING_REVIEW",
                score = 0,
                pointsAwarded = 0,
                feedback = "",
                rejectionReason = "",
                verificationCode = "SB-PEND-${subId.takeLast(4).uppercase()}",
                isPublicOnPassport = true,
                submittedAtEpochMs = now,
                evaluatedAtEpochMs = 0L
            )
            dao.upsertSubmission(submission)
            syncEngine?.pushSubmission(submission)
            if (brief.assignedDirectorId.isNotBlank()) {
                sendNotification(
                    recipientId = brief.assignedDirectorId,
                    category = "EVALUATION",
                    titleEn = "New Submission Ready for Review",
                    titleAr = "تسليم جديد بانتظار التقييم",
                    bodyEn = "${session.displayName} submitted work for '${brief.title}'.",
                    bodyAr = "رفع ${session.displayName} تسليمًا لمشروع '${brief.title}'."
                )
            }
            logActivity(session.accountId, session.role.key, "WORK_SUBMITTED", "${session.displayName} submitted deliverable for '${brief.title}'", subId)
            Result.success(subId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateStudentTaskStatus(
        session: SessionPrincipal,
        taskId: String,
        newStatus: String
    ): Result<Unit> {
        return try {
            requireRole(session, AccountRole.STUDENT)
            val remoteAttempt = tryRemotePostUnit(
                "/api/ecosystem/tasks/$taskId/status",
                JSONObject().put("newStatus", newStatus)
            )
            if (remoteAttempt.isFailure) return Result.failure(remoteAttempt.exceptionOrNull()!!)
            if (remoteAttempt.getOrNull() != null) {
                dao.getTaskById(taskId)?.let { syncEngine?.pushTask(it) }
                return Result.success(Unit)
            }

            if (newStatus !in setOf("TODO", "IN_PROGRESS", "DONE")) {
                return Result.failure(BackendValidationException("Invalid task status."))
            }
            val task = dao.getTaskById(taskId)
                ?: return Result.failure(BackendValidationException("Task not found."))
            if (task.assignedStudentId != session.accountId) {
                return Result.failure(BackendAuthorizationException("You can only update your own assigned project tasks."))
            }
            val updatedTask = task.copy(status = newStatus, updatedAtEpochMs = System.currentTimeMillis())
            dao.upsertTask(updatedTask)
            syncEngine?.pushTask(updatedTask)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createProjectTask(
        session: SessionPrincipal,
        briefId: String,
        studentAccountId: String,
        title: String,
        dueDateLabel: String
    ): Result<String> {
        return try {
            requireRole(session, AccountRole.COMPANY_DIRECTOR)
            val body = JSONObject()
                .put("briefId", briefId)
                .put("studentAccountId", studentAccountId)
                .put("title", title)
                .put("dueDateLabel", dueDateLabel)
            val remoteAttempt = tryRemotePostReturningId("/api/ecosystem/tasks", body, "task")
            if (remoteAttempt.isFailure) return Result.failure(remoteAttempt.exceptionOrNull()!!)
            remoteAttempt.getOrNull()?.let { taskId ->
                dao.getTaskById(taskId)?.let { syncEngine?.pushTask(it) }
                return Result.success(taskId)
            }

            val brief = dao.getBriefById(briefId)
                ?: return Result.failure(BackendValidationException("Project brief not found."))
            if (brief.assignedDirectorId != session.accountId) {
                return Result.failure(BackendAuthorizationException("Access denied: you are not the assigned Director for '${brief.title}'."))
            }
            val student = dao.getAccountById(studentAccountId)
                ?: return Result.failure(BackendValidationException("Student not found."))
            val app = dao.getApplicationForBriefAndStudent(briefId, studentAccountId)
            if (app?.status != "ACCEPTED") {
                return Result.failure(BackendValidationException("Student must be accepted on this brief before assigning tasks."))
            }
            if (title.trim().length < 3) {
                return Result.failure(BackendValidationException("Task title is required."))
            }
            val now = System.currentTimeMillis()
            val taskId = "task_${UUID.randomUUID().toString().take(10)}"
            val task = ProjectTaskEntity(
                id = taskId,
                briefId = briefId,
                assignedStudentId = student.id,
                assignedStudentName = student.displayName,
                assignedDirectorId = session.accountId,
                title = title.trim().take(160),
                milestoneOrder = 2,
                dueDateLabel = dueDateLabel.trim().ifBlank { brief.deadline },
                isOverdue = false,
                status = "TODO",
                createdAtEpochMs = now,
                updatedAtEpochMs = now
            )
            dao.upsertTask(task)
            syncEngine?.pushTask(task)
            sendNotification(
                recipientId = student.id,
                category = "TASK",
                titleEn = "New Milestone Task Assigned",
                titleAr = "تم إسناد مهمة جديدة لك",
                bodyEn = "${session.displayName} added '${title.trim()}' in '${brief.title}'.",
                bodyAr = "أضاف ${session.displayName} مهمة '${title.trim()}' في '${brief.title}'."
            )
            Result.success(taskId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun evaluateSubmission(
        session: SessionPrincipal,
        submissionId: String,
        score: Int,
        feedback: String,
        decision: String,
        rejectionReason: String
    ): Result<Unit> {
        return try {
            requireRole(session, AccountRole.COMPANY_DIRECTOR)
            val body = JSONObject()
                .put("score", score)
                .put("feedback", feedback)
                .put("decision", decision)
                .put("rejectionReason", rejectionReason)
            val remoteAttempt = tryRemotePostUnit("/api/ecosystem/submissions/$submissionId/evaluate", body)
            if (remoteAttempt.isFailure) return Result.failure(remoteAttempt.exceptionOrNull()!!)
            if (remoteAttempt.getOrNull() != null) {
                dao.getSubmissionById(submissionId)?.let { syncEngine?.pushSubmission(it) }
                return Result.success(Unit)
            }

            if (decision !in setOf("VERIFIED", "REVISION_REQUESTED", "REJECTED")) {
                return Result.failure(BackendValidationException("Invalid evaluation decision."))
            }
            val sub = dao.getSubmissionById(submissionId)
                ?: return Result.failure(BackendValidationException("Submission not found."))
            val brief = dao.getBriefById(sub.briefId)
                ?: return Result.failure(BackendValidationException("Project brief not found."))
            if (brief.assignedDirectorId != session.accountId) {
                return Result.failure(BackendAuthorizationException("Access denied: you are not assigned to supervise '${brief.title}'."))
            }
            val safeScore = score.coerceIn(0, 100)
            val now = System.currentTimeMillis()
            val previouslyVerified = sub.status == "VERIFIED"
            val awardedPoints = if (decision == "VERIFIED") {
                if (previouslyVerified) sub.pointsAwarded else brief.pointsReward
            } else {
                0
            }
            val verificationCode = if (decision == "VERIFIED") {
                "SB-VER-${submissionId.takeLast(4).uppercase()}-${safeScore}"
            } else {
                sub.verificationCode
            }

            val evaluatedSub = sub.copy(
                status = decision,
                score = safeScore,
                pointsAwarded = awardedPoints,
                feedback = feedback.trim().ifBlank {
                    if (decision == "VERIFIED") "Verified implementation meeting rubric standards." else "Please review director comments."
                },
                rejectionReason = if (decision == "VERIFIED") "" else rejectionReason.trim().ifBlank { "Needs rubric refinement." },
                verificationCode = verificationCode,
                evaluatedAtEpochMs = now
            )
            dao.upsertSubmission(evaluatedSub)
            syncEngine?.pushSubmission(evaluatedSub)

            if (decision == "VERIFIED" && !previouslyVerified) {
                val student = dao.getAccountById(sub.studentAccountId)
                if (student != null) {
                    val newPoints = student.points + awardedPoints
                    val newVerifiedCount = student.verifiedCount + 1
                    val newLevel = PointsAndBadgesEngine.computeLevelFromEvidence(
                        totalPoints = newPoints.toLong(),
                        verifiedSubmissionsCount = newVerifiedCount
                    )
                    val mergedSkills = (student.skillsCsv.split(",") + sub.skillsAppliedCsv.split(","))
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }
                        .distinct()
                        .joinToString(",")
                    val updatedStudent = student.copy(
                        points = newPoints,
                        verifiedCount = newVerifiedCount,
                        streakDays = student.streakDays + 1,
                        careerLevel = newLevel,
                        skillsCsv = mergedSkills
                    )
                    dao.updateAccount(updatedStudent)
                    syncEngine?.pushAccount(updatedStudent)
                }
            }

            sendNotification(
                recipientId = sub.studentAccountId,
                category = "EVALUATION",
                titleEn = if (decision == "VERIFIED") "Passport Evidence Verified ($safeScore%)" else "Submission $decision ($safeScore%)",
                titleAr = if (decision == "VERIFIED") "تم توثيق دليلك العملي ($safeScore%)" else "تحديث مراجعة التسليم: $decision",
                bodyEn = "${session.displayName} evaluated '${sub.briefTitle}': ${feedback.ifBlank { decision }}",
                bodyAr = "قيّم ${session.displayName} مشروع '${sub.briefTitle}': ${feedback.ifBlank { decision }}"
            )
            sendNotification(
                recipientId = brief.createdByAccountId,
                category = "EVALUATION",
                titleEn = "Director Evaluated ${sub.studentName}",
                titleAr = "قيّم المشرف تسليم ${sub.studentName}",
                bodyEn = "${sub.briefTitle}: $decision ($safeScore%).",
                bodyAr = "${sub.briefTitle}: $decision ($safeScore%)."
            )
            logActivity(session.accountId, session.role.key, "SUBMISSION_$decision", "Director scored ${sub.studentName}'s '${sub.briefTitle}' at $safeScore% ($decision)", submissionId)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun markBriefCompleted(
        session: SessionPrincipal,
        briefId: String
    ): Result<Unit> {
        return try {
            requireRole(session, AccountRole.COMPANY_DIRECTOR)
            val remoteAttempt = tryRemotePostUnit("/api/ecosystem/briefs/$briefId/complete", JSONObject())
            if (remoteAttempt.isFailure) return Result.failure(remoteAttempt.exceptionOrNull()!!)
            if (remoteAttempt.getOrNull() != null) {
                dao.getBriefById(briefId)?.let { syncEngine?.pushBrief(it) }
                return Result.success(Unit)
            }

            val brief = dao.getBriefById(briefId)
                ?: return Result.failure(BackendValidationException("Brief not found."))
            if (brief.assignedDirectorId != session.accountId) {
                return Result.failure(BackendAuthorizationException("Access denied: you are not assigned to this project."))
            }
            val completedBrief = brief.copy(approvalStatus = "COMPLETED", updatedAtEpochMs = System.currentTimeMillis())
            dao.updateBrief(completedBrief)
            syncEngine?.pushBrief(completedBrief)
            sendNotification(
                recipientId = brief.createdByAccountId,
                category = "SYSTEM",
                titleEn = "Project Completed: ${brief.title}",
                titleAr = "اكتمل المشروع: ${brief.title}",
                bodyEn = "Director ${session.displayName} marked '${brief.title}' completed.",
                bodyAr = "أنهى المشرف ${session.displayName} مشروع '${brief.title}'."
            )
            logActivity(session.accountId, session.role.key, "BRIEF_COMPLETED", "Director completed '${brief.title}'", briefId)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateStudentProfile(
        session: SessionPrincipal,
        displayName: String,
        domain: String,
        country: String,
        skills: List<String>,
        targetSkills: List<String>,
        goalsSummary: String,
        weeklyHours: Int
    ): Result<Unit> {
        return try {
            requireRole(session, AccountRole.STUDENT)
            val body = JSONObject()
                .put("displayName", displayName)
                .put("domain", domain)
                .put("country", country)
                .put("skills", JSONArray(skills))
                .put("targetSkills", JSONArray(targetSkills))
                .put("goalsSummary", goalsSummary)
                .put("weeklyHours", weeklyHours)
            val remoteAttempt = tryRemotePostUnit("/api/ecosystem/profile", body)
            if (remoteAttempt.isFailure) return Result.failure(remoteAttempt.exceptionOrNull()!!)
            if (remoteAttempt.getOrNull() != null) {
                dao.getAccountById(session.accountId)?.let { syncEngine?.pushAccount(it) }
                return Result.success(Unit)
            }

            val account = dao.getAccountById(session.accountId)
                ?: return Result.failure(BackendValidationException("Account not found."))
            val cleanName = displayName.trim().ifBlank { account.displayName }
            val completion = listOf(
                cleanName.isNotBlank(),
                domain.isNotBlank(),
                country.isNotBlank(),
                skills.isNotEmpty(),
                targetSkills.isNotEmpty(),
                goalsSummary.isNotBlank()
            ).count { it } * 100 / 6
            val updatedAccount = account.copy(
                displayName = cleanName,
                domain = domain.ifBlank { account.domain },
                country = country.ifBlank { account.country },
                skillsCsv = skills.map { it.trim() }.filter { it.isNotEmpty() }.joinToString(","),
                targetSkillsCsv = targetSkills.map { it.trim() }.filter { it.isNotEmpty() }.joinToString(","),
                goalsSummary = goalsSummary.trim(),
                weeklyHours = weeklyHours.coerceIn(1, 60),
                profileCompletion = completion.coerceIn(30, 100)
            )
            dao.updateAccount(updatedAccount)
            syncEngine?.pushAccount(updatedAccount)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun submitModerationReport(
        session: SessionPrincipal,
        targetType: String,
        targetId: String,
        reason: String
    ): Result<Unit> {
        return try {
            val remoteAttempt = tryRemotePostUnit(
                "/api/ecosystem/reports",
                JSONObject().put("targetType", targetType).put("targetId", targetId).put("reason", reason)
            )
            if (remoteAttempt.isFailure) return Result.failure(remoteAttempt.exceptionOrNull()!!)
            if (remoteAttempt.getOrNull() != null) return Result.success(Unit)

            if (reason.trim().length < 3) {
                return Result.failure(BackendValidationException("Please provide a clear report reason."))
            }
            val reportId = "rep_${UUID.randomUUID().toString().take(10)}"
            val report = ModerationReportEntity(
                id = reportId,
                reporterAccountId = session.accountId,
                targetType = targetType.trim().ifBlank { "BRIEF" },
                targetId = targetId.trim(),
                reason = reason.trim().take(600),
                status = "OPEN",
                resolutionNote = "",
                createdAtEpochMs = System.currentTimeMillis()
            )
            dao.upsertReport(report)
            syncEngine?.pushReport(report)
            notifyAdmins(
                category = "SYSTEM",
                titleEn = "New Moderation Report",
                titleAr = "بلاغ إشراف جديد",
                bodyEn = "${session.displayName} flagged $targetType ($targetId).",
                bodyAr = "أبلغ ${session.displayName} عن $targetType ($targetId)."
            )
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun markAllNotificationsRead(accountId: String) {
        tryRemotePostUnit("/api/ecosystem/notifications/read-all", JSONObject())
        dao.markAllNotificationsRead(accountId)
    }

    suspend fun markNotificationRead(accountId: String, notificationId: String) {
        tryRemotePostUnit("/api/ecosystem/notifications/$notificationId/read", JSONObject())
        dao.markNotificationRead(notificationId, accountId)
    }

    // --- Helpers ---

    private fun requireRole(session: SessionPrincipal, expected: AccountRole) {
        if (session.role != expected) {
            throw BackendAuthorizationException("Forbidden: requires ${expected.key} role (current: ${session.role.key}).")
        }
    }

    private suspend fun sendNotification(
        recipientId: String,
        category: String,
        titleEn: String,
        titleAr: String,
        bodyEn: String,
        bodyAr: String
    ) {
        if (recipientId.isBlank()) return
        val notification = NotificationEntity(
            id = "notif_${UUID.randomUUID().toString().take(10)}",
            recipientAccountId = recipientId,
            category = category,
            titleEn = titleEn,
            titleAr = titleAr,
            bodyEn = bodyEn,
            bodyAr = bodyAr,
            isRead = false,
            createdAtEpochMs = System.currentTimeMillis()
        )
        dao.insertNotification(notification)
        syncEngine?.pushNotification(notification)
    }

    private suspend fun notifyAdmins(
        category: String,
        titleEn: String,
        titleAr: String,
        bodyEn: String,
        bodyAr: String
    ) {
        dao.getAdminAccounts().forEach { admin ->
            sendNotification(admin.id, category, titleEn, titleAr, bodyEn, bodyAr)
        }
    }

    private suspend fun logActivity(
        actorAccountId: String,
        actorRole: String,
        actionType: String,
        summary: String,
        targetId: String
    ) {
        val activity = PlatformActivityEntity(
            id = "act_${UUID.randomUUID().toString().take(10)}",
            actorAccountId = actorAccountId,
            actorRole = actorRole,
            actionType = actionType,
            summary = summary.take(280),
            targetId = targetId,
            createdAtEpochMs = System.currentTimeMillis()
        )
        dao.insertActivity(activity)
        syncEngine?.pushActivity(activity)
    }

    private fun AccountEntity.toPrincipal(isGoogleLinked: Boolean): SessionPrincipal = SessionPrincipal(
        accountId = id,
        loginId = loginId,
        displayName = displayName,
        email = email,
        role = AccountRole.fromKey(role),
        companyId = companyId,
        isGoogleLinked = isGoogleLinked
    )
}
