package com.example.data.remote

import android.util.Log
import com.example.data.local.AccountEntity
import com.example.data.local.CompanyEntity
import com.example.data.local.DeviceSessionEntity
import com.example.data.local.ModerationReportEntity
import com.example.data.local.NotificationEntity
import com.example.data.local.PlatformActivityEntity
import com.example.data.local.ProjectApplicationEntity
import com.example.data.local.ProjectBriefEntity
import com.example.data.local.ProjectTaskEntity
import com.example.data.local.SkillBridgeDao
import com.example.data.local.WorkSubmissionEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.concurrent.ConcurrentLinkedQueue

enum class SyncState {
    LIVE_CLOUD,
    LOCAL_SYNCED,
    SYNCING,
    OFFLINE_QUEUED
}

/**
 * Central real-time synchronization engine bridging the shared Firestore database
 * and the local Room cache across Mobile and Desktop/Laptop experiences.
 *
 * Uses authenticated Firestore snapshot listeners when [FirebaseAuth.currentUser] is present
 * and buffers outbound writes seamlessly when offline or awaiting cloud authentication.
 */
class CloudCentralSyncEngine(
    private val dao: SkillBridgeDao,
    private val firestore: FirebaseFirestore?,
    private val auth: FirebaseAuth?,
    private val isOfflineFlow: StateFlow<Boolean>? = null
) {
    private val tag = "CloudCentralSync"
    private val engineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _syncState = MutableStateFlow(SyncState.LOCAL_SYNCED)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    private val _lastSyncedEpochMs = MutableStateFlow(System.currentTimeMillis())
    val lastSyncedEpochMs: StateFlow<Long> = _lastSyncedEpochMs.asStateFlow()

    private val _pendingWritesCount = MutableStateFlow(0)
    val pendingWritesCount: StateFlow<Int> = _pendingWritesCount.asStateFlow()

    private val pendingOps = ConcurrentLinkedQueue<Pair<String, Map<String, Any>>>()
    private val listeners = mutableListOf<ListenerRegistration>()
    private var activeAccountId: String = ""

    fun startListeners(accountId: String) {
        activeAccountId = accountId
        val offline = isOfflineFlow?.value == true
        if (offline) {
            _syncState.value = SyncState.OFFLINE_QUEUED
            return
        }
        val fbUser = auth?.currentUser
        val db = firestore
        if (fbUser == null || db == null) {
            _syncState.value = SyncState.LOCAL_SYNCED
            _lastSyncedEpochMs.value = System.currentTimeMillis()
            return
        }

        stopListeners()
        _syncState.value = SyncState.SYNCING

        // 0. Platform Accounts (keeps student & partner directories synchronized across devices)
        listeners += db.collection("platformAccounts").addSnapshotListener { snap, err ->
            if (err != null) {
                Log.w(tag, "platformAccounts listener error: ${err.message}")
                return@addSnapshotListener
            }
            val docs = snap?.documents.orEmpty()
            engineScope.launch {
                docs.forEach { doc ->
                    doc.toAccountEntity()?.let { incoming ->
                        safeRun {
                            val existing = dao.getAccountById(incoming.id)
                            val merged = if (existing != null && incoming.passwordHash.isBlank()) {
                                incoming.copy(
                                    passwordSalt = existing.passwordSalt,
                                    passwordHash = existing.passwordHash
                                )
                            } else {
                                incoming
                            }
                            dao.upsertAccount(merged)
                        }
                    }
                }
                markSynced()
            }
        }

        // 1. Companies
        listeners += db.collection("companies").addSnapshotListener { snap, err ->
            if (err != null) {
                Log.w(tag, "companies listener error: ${err.message}")
                return@addSnapshotListener
            }
            val docs = snap?.documents.orEmpty()
            engineScope.launch {
                docs.forEach { doc ->
                    doc.toCompanyEntity()?.let { safeRun { dao.upsertCompany(it) } }
                }
                markSynced()
            }
        }

        // 2. Project Briefs
        listeners += db.collection("projectBriefs").addSnapshotListener { snap, err ->
            if (err != null) {
                Log.w(tag, "projectBriefs listener error: ${err.message}")
                return@addSnapshotListener
            }
            val docs = snap?.documents.orEmpty()
            engineScope.launch {
                docs.forEach { doc ->
                    doc.toProjectBriefEntity()?.let { safeRun { dao.upsertBrief(it) } }
                }
                markSynced()
            }
        }

        // 3. Project Applications
        listeners += db.collection("projectApplications").addSnapshotListener { snap, err ->
            if (err != null) {
                Log.w(tag, "projectApplications listener error: ${err.message}")
                return@addSnapshotListener
            }
            val docs = snap?.documents.orEmpty()
            engineScope.launch {
                docs.forEach { doc ->
                    doc.toProjectApplicationEntity()?.let { safeRun { dao.upsertApplication(it) } }
                }
                markSynced()
            }
        }

        // 4. Project Tasks
        listeners += db.collection("projectTasks").addSnapshotListener { snap, err ->
            if (err != null) {
                Log.w(tag, "projectTasks listener error: ${err.message}")
                return@addSnapshotListener
            }
            val docs = snap?.documents.orEmpty()
            engineScope.launch {
                docs.forEach { doc ->
                    doc.toProjectTaskEntity()?.let { safeRun { dao.upsertTask(it) } }
                }
                markSynced()
            }
        }

        // 5. Work Submissions
        listeners += db.collection("workSubmissions").addSnapshotListener { snap, err ->
            if (err != null) {
                Log.w(tag, "workSubmissions listener error: ${err.message}")
                return@addSnapshotListener
            }
            val docs = snap?.documents.orEmpty()
            engineScope.launch {
                docs.forEach { doc ->
                    doc.toWorkSubmissionEntity()?.let { safeRun { dao.upsertSubmission(it) } }
                }
                markSynced()
            }
        }

        // 6. Platform Notifications
        listeners += db.collection("platformNotifications").addSnapshotListener { snap, err ->
            if (err != null) {
                Log.w(tag, "platformNotifications listener error: ${err.message}")
                return@addSnapshotListener
            }
            val docs = snap?.documents.orEmpty()
            engineScope.launch {
                docs.forEach { doc ->
                    doc.toNotificationEntity()?.let { safeRun { dao.upsertNotification(it) } }
                }
                markSynced()
            }
        }

        // 7. Moderation Reports
        listeners += db.collection("moderationReports").addSnapshotListener { snap, err ->
            if (err != null) {
                Log.w(tag, "moderationReports listener error: ${err.message}")
                return@addSnapshotListener
            }
            val docs = snap?.documents.orEmpty()
            engineScope.launch {
                docs.forEach { doc ->
                    doc.toModerationReportEntity()?.let { safeRun { dao.upsertReport(it) } }
                }
                markSynced()
            }
        }

        // 8. Active Account Cross-Device Sessions (Mobile & Desktop/Laptop)
        if (accountId.isNotBlank()) {
            listeners += db.collection("platformAccounts").document(accountId).collection("sessions")
                .addSnapshotListener { snap, err ->
                    if (err != null) {
                        Log.w(tag, "sessions listener error: ${err.message}")
                        return@addSnapshotListener
                    }
                    val docs = snap?.documents.orEmpty()
                    engineScope.launch {
                        docs.forEach { doc ->
                            doc.toDeviceSessionEntity()?.let { safeRun { dao.upsertDeviceSession(it) } }
                        }
                        markSynced()
                    }
                }
        }

        engineScope.launch { flushPendingQueue() }
    }

    fun stopListeners() {
        listeners.forEach { it.remove() }
        listeners.clear()
    }

    suspend fun triggerManualSync() {
        val offline = isOfflineFlow?.value == true
        if (offline) {
            _syncState.value = SyncState.OFFLINE_QUEUED
            return
        }
        val db = firestore
        val fbUser = auth?.currentUser
        if (db == null || fbUser == null) {
            _syncState.value = SyncState.LOCAL_SYNCED
            _lastSyncedEpochMs.value = System.currentTimeMillis()
            return
        }
        _syncState.value = SyncState.SYNCING
        flushPendingQueue()
        if (activeAccountId.isNotBlank()) {
            startListeners(activeAccountId)
        } else {
            markSynced()
        }
    }

    suspend fun pushAccount(account: AccountEntity) {
        val payload = mapOf(
            "id" to account.id,
            "loginId" to account.loginId,
            "displayName" to account.displayName,
            "email" to account.email,
            "role" to account.role,
            "companyId" to account.companyId,
            "isActive" to account.isActive,
            "careerLevel" to account.careerLevel,
            "domain" to account.domain,
            "country" to account.country,
            "points" to account.points,
            "streakDays" to account.streakDays,
            "verifiedCount" to account.verifiedCount,
            "skillsCsv" to account.skillsCsv,
            "targetSkillsCsv" to account.targetSkillsCsv,
            "goalsSummary" to account.goalsSummary,
            "weeklyHours" to account.weeklyHours,
            "profileCompletion" to account.profileCompletion,
            "createdAtEpochMs" to account.createdAtEpochMs
        )
        enqueueOrWrite("platformAccounts/${account.id}", payload)
    }

    suspend fun pushDeviceSession(session: DeviceSessionEntity) {
        val payload = mapOf(
            "id" to session.id,
            "accountId" to session.accountId,
            "deviceType" to session.deviceType,
            "deviceLabel" to session.deviceLabel,
            "windowWidthDp" to session.windowWidthDp,
            "lastActiveEpochMs" to session.lastActiveEpochMs,
            "revoked" to session.revoked
        )
        enqueueOrWrite("platformAccounts/${session.accountId}/sessions/${session.id}", payload)
    }

    suspend fun pushCompany(company: CompanyEntity) {
        val payload = mapOf(
            "id" to company.id,
            "ownerAccountId" to company.ownerAccountId,
            "name" to company.name,
            "city" to company.city,
            "domain" to company.domain,
            "verificationStatus" to company.verificationStatus,
            "verificationTier" to company.verificationTier,
            "verificationNotes" to company.verificationNotes,
            "rewardPolicy" to company.rewardPolicy,
            "ipPolicy" to company.ipPolicy,
            "verifiedAtEpochMs" to company.verifiedAtEpochMs,
            "createdAtEpochMs" to company.createdAtEpochMs
        )
        enqueueOrWrite("companies/${company.id}", payload)
    }

    suspend fun pushBrief(brief: ProjectBriefEntity) {
        val payload = mapOf(
            "id" to brief.id,
            "companyId" to brief.companyId,
            "companyName" to brief.companyName,
            "createdByAccountId" to brief.createdByAccountId,
            "assignedDirectorId" to brief.assignedDirectorId,
            "assignedDirectorName" to brief.assignedDirectorName,
            "title" to brief.title,
            "summary" to brief.summary,
            "domain" to brief.domain,
            "level" to brief.level,
            "skillsCsv" to brief.skillsCsv,
            "rubricCsv" to brief.rubricCsv,
            "deliverablesCsv" to brief.deliverablesCsv,
            "deadline" to brief.deadline,
            "pointsReward" to brief.pointsReward,
            "prizePool" to brief.prizePool,
            "approvalStatus" to brief.approvalStatus,
            "adminNotes" to brief.adminNotes,
            "isTeamAllowed" to brief.isTeamAllowed,
            "createdAtEpochMs" to brief.createdAtEpochMs,
            "updatedAtEpochMs" to brief.updatedAtEpochMs
        )
        enqueueOrWrite("projectBriefs/${brief.id}", payload)
    }

    suspend fun pushApplication(app: ProjectApplicationEntity) {
        val payload = mapOf(
            "id" to app.id,
            "briefId" to app.briefId,
            "studentAccountId" to app.studentAccountId,
            "studentName" to app.studentName,
            "studentLevel" to app.studentLevel,
            "studentSkillsCsv" to app.studentSkillsCsv,
            "coverNote" to app.coverNote,
            "roleRequested" to app.roleRequested,
            "status" to app.status,
            "decisionNote" to app.decisionNote,
            "appliedAtEpochMs" to app.appliedAtEpochMs,
            "updatedAtEpochMs" to app.updatedAtEpochMs
        )
        enqueueOrWrite("projectApplications/${app.id}", payload)
    }

    suspend fun pushTask(task: ProjectTaskEntity) {
        val payload = mapOf(
            "id" to task.id,
            "briefId" to task.briefId,
            "assignedStudentId" to task.assignedStudentId,
            "assignedStudentName" to task.assignedStudentName,
            "assignedDirectorId" to task.assignedDirectorId,
            "title" to task.title,
            "milestoneOrder" to task.milestoneOrder,
            "dueDateLabel" to task.dueDateLabel,
            "isOverdue" to task.isOverdue,
            "status" to task.status,
            "createdAtEpochMs" to task.createdAtEpochMs,
            "updatedAtEpochMs" to task.updatedAtEpochMs
        )
        enqueueOrWrite("projectTasks/${task.id}", payload)
    }

    suspend fun pushSubmission(sub: WorkSubmissionEntity) {
        val payload = mapOf(
            "id" to sub.id,
            "briefId" to sub.briefId,
            "briefTitle" to sub.briefTitle,
            "companyName" to sub.companyName,
            "studentAccountId" to sub.studentAccountId,
            "studentName" to sub.studentName,
            "directorAccountId" to sub.directorAccountId,
            "repoOrDemoUrl" to sub.repoOrDemoUrl,
            "understandingReport" to sub.understandingReport,
            "skillsAppliedCsv" to sub.skillsAppliedCsv,
            "status" to sub.status,
            "score" to sub.score,
            "pointsAwarded" to sub.pointsAwarded,
            "feedback" to sub.feedback,
            "rejectionReason" to sub.rejectionReason,
            "verificationCode" to sub.verificationCode,
            "isPublicOnPassport" to sub.isPublicOnPassport,
            "submittedAtEpochMs" to sub.submittedAtEpochMs,
            "evaluatedAtEpochMs" to sub.evaluatedAtEpochMs
        )
        enqueueOrWrite("workSubmissions/${sub.id}", payload)
    }

    suspend fun pushNotification(notification: NotificationEntity) {
        val payload = mapOf(
            "id" to notification.id,
            "recipientAccountId" to notification.recipientAccountId,
            "category" to notification.category,
            "titleEn" to notification.titleEn,
            "titleAr" to notification.titleAr,
            "bodyEn" to notification.bodyEn,
            "bodyAr" to notification.bodyAr,
            "isRead" to notification.isRead,
            "createdAtEpochMs" to notification.createdAtEpochMs
        )
        enqueueOrWrite("platformNotifications/${notification.id}", payload)
    }

    suspend fun pushActivity(activity: PlatformActivityEntity) {
        val payload = mapOf(
            "id" to activity.id,
            "actorAccountId" to activity.actorAccountId,
            "actorRole" to activity.actorRole,
            "actionType" to activity.actionType,
            "summary" to activity.summary,
            "targetId" to activity.targetId,
            "createdAtEpochMs" to activity.createdAtEpochMs
        )
        enqueueOrWrite("platformActivities/${activity.id}", payload)
    }

    suspend fun pushReport(report: ModerationReportEntity) {
        val payload = mapOf(
            "id" to report.id,
            "reporterAccountId" to report.reporterAccountId,
            "targetType" to report.targetType,
            "targetId" to report.targetId,
            "reason" to report.reason,
            "status" to report.status,
            "resolutionNote" to report.resolutionNote,
            "createdAtEpochMs" to report.createdAtEpochMs
        )
        enqueueOrWrite("moderationReports/${report.id}", payload)
    }

    private suspend fun enqueueOrWrite(docPath: String, payload: Map<String, Any>) {
        val offline = isOfflineFlow?.value == true
        val db = firestore
        val fbUser = auth?.currentUser
        if (offline || db == null || fbUser == null) {
            pendingOps.offer(docPath to payload)
            _pendingWritesCount.value = pendingOps.size
            _syncState.value = if (offline) SyncState.OFFLINE_QUEUED else SyncState.LOCAL_SYNCED
            _lastSyncedEpochMs.value = System.currentTimeMillis()
            return
        }
        try {
            _syncState.value = SyncState.SYNCING
            db.document(docPath).set(payload, SetOptions.merge()).await()
            markSynced()
        } catch (e: Exception) {
            Log.w(tag, "Cloud push deferred for $docPath: ${e.message}")
            pendingOps.offer(docPath to payload)
            _pendingWritesCount.value = pendingOps.size
            _syncState.value = SyncState.LOCAL_SYNCED
        }
    }

    private suspend fun flushPendingQueue() {
        val db = firestore ?: return
        if (auth?.currentUser == null || isOfflineFlow?.value == true) return
        while (pendingOps.isNotEmpty()) {
            val head = pendingOps.poll() ?: break
            try {
                db.document(head.first).set(head.second, SetOptions.merge()).await()
            } catch (e: Exception) {
                Log.w(tag, "Retry deferred for ${head.first}: ${e.message}")
                pendingOps.offer(head)
                break
            }
        }
        _pendingWritesCount.value = pendingOps.size
        markSynced()
    }

    private fun markSynced() {
        _lastSyncedEpochMs.value = System.currentTimeMillis()
        _syncState.value = when {
            isOfflineFlow?.value == true -> SyncState.OFFLINE_QUEUED
            auth?.currentUser != null && firestore != null -> SyncState.LIVE_CLOUD
            else -> SyncState.LOCAL_SYNCED
        }
    }

    private suspend fun safeRun(block: suspend () -> Unit) {
        try {
            block()
        } catch (e: Exception) {
            Log.w(tag, "Deferred Room merge: ${e.message}")
        }
    }

    private fun DocumentSnapshot.toAccountEntity(): AccountEntity? {
        val idVal = getString("id") ?: id
        val loginVal = getString("loginId") ?: return null
        return AccountEntity(
            id = idVal,
            loginId = loginVal,
            displayName = getString("displayName").orEmpty(),
            email = getString("email").orEmpty(),
            passwordSalt = "",
            passwordHash = "",
            role = getString("role") ?: "STUDENT",
            companyId = getString("companyId").orEmpty(),
            isActive = getBoolean("isActive") ?: true,
            careerLevel = getString("careerLevel") ?: "Junior",
            domain = getString("domain") ?: "Backend & Cloud",
            country = getString("country") ?: "Egypt",
            points = (getLong("points") ?: 0L).toInt(),
            streakDays = (getLong("streakDays") ?: 0L).toInt(),
            verifiedCount = (getLong("verifiedCount") ?: 0L).toInt(),
            skillsCsv = getString("skillsCsv").orEmpty(),
            targetSkillsCsv = getString("targetSkillsCsv").orEmpty(),
            goalsSummary = getString("goalsSummary").orEmpty(),
            weeklyHours = (getLong("weeklyHours") ?: 10L).toInt(),
            profileCompletion = (getLong("profileCompletion") ?: 50L).toInt(),
            createdAtEpochMs = getLong("createdAtEpochMs") ?: System.currentTimeMillis()
        )
    }

    private fun DocumentSnapshot.toCompanyEntity(): CompanyEntity? {
        val idVal = getString("id") ?: id
        val nameVal = getString("name") ?: return null
        return CompanyEntity(
            id = idVal,
            ownerAccountId = getString("ownerAccountId").orEmpty(),
            name = nameVal,
            city = getString("city").orEmpty(),
            domain = getString("domain").orEmpty(),
            verificationStatus = getString("verificationStatus") ?: "PENDING",
            verificationTier = getString("verificationTier") ?: "BASIC",
            verificationNotes = getString("verificationNotes").orEmpty(),
            rewardPolicy = getString("rewardPolicy").orEmpty(),
            ipPolicy = getString("ipPolicy").orEmpty(),
            verifiedAtEpochMs = getLong("verifiedAtEpochMs") ?: 0L,
            createdAtEpochMs = getLong("createdAtEpochMs") ?: System.currentTimeMillis()
        )
    }

    private fun DocumentSnapshot.toProjectBriefEntity(): ProjectBriefEntity? {
        val idVal = getString("id") ?: id
        val titleVal = getString("title") ?: return null
        val compId = getString("companyId") ?: return null
        return ProjectBriefEntity(
            id = idVal,
            companyId = compId,
            companyName = getString("companyName").orEmpty(),
            createdByAccountId = getString("createdByAccountId").orEmpty(),
            assignedDirectorId = getString("assignedDirectorId").orEmpty(),
            assignedDirectorName = getString("assignedDirectorName").orEmpty(),
            title = titleVal,
            summary = getString("summary").orEmpty(),
            domain = getString("domain").orEmpty(),
            level = getString("level") ?: "Mid-level",
            skillsCsv = getString("skillsCsv").orEmpty(),
            rubricCsv = getString("rubricCsv").orEmpty(),
            deliverablesCsv = getString("deliverablesCsv").orEmpty(),
            deadline = getString("deadline").orEmpty(),
            pointsReward = (getLong("pointsReward") ?: 200L).toInt(),
            prizePool = getString("prizePool").orEmpty(),
            approvalStatus = getString("approvalStatus") ?: "PENDING_APPROVAL",
            adminNotes = getString("adminNotes").orEmpty(),
            isTeamAllowed = getBoolean("isTeamAllowed") ?: true,
            createdAtEpochMs = getLong("createdAtEpochMs") ?: System.currentTimeMillis(),
            updatedAtEpochMs = getLong("updatedAtEpochMs") ?: System.currentTimeMillis()
        )
    }

    private fun DocumentSnapshot.toProjectApplicationEntity(): ProjectApplicationEntity? {
        val idVal = getString("id") ?: id
        val briefIdVal = getString("briefId") ?: return null
        val studentIdVal = getString("studentAccountId") ?: return null
        return ProjectApplicationEntity(
            id = idVal,
            briefId = briefIdVal,
            studentAccountId = studentIdVal,
            studentName = getString("studentName").orEmpty(),
            studentLevel = getString("studentLevel") ?: "Junior",
            studentSkillsCsv = getString("studentSkillsCsv").orEmpty(),
            coverNote = getString("coverNote").orEmpty(),
            roleRequested = getString("roleRequested").orEmpty(),
            status = getString("status") ?: "APPLIED",
            decisionNote = getString("decisionNote").orEmpty(),
            appliedAtEpochMs = getLong("appliedAtEpochMs") ?: System.currentTimeMillis(),
            updatedAtEpochMs = getLong("updatedAtEpochMs") ?: System.currentTimeMillis()
        )
    }

    private fun DocumentSnapshot.toProjectTaskEntity(): ProjectTaskEntity? {
        val idVal = getString("id") ?: id
        val briefIdVal = getString("briefId") ?: return null
        val titleVal = getString("title") ?: return null
        return ProjectTaskEntity(
            id = idVal,
            briefId = briefIdVal,
            assignedStudentId = getString("assignedStudentId").orEmpty(),
            assignedStudentName = getString("assignedStudentName").orEmpty(),
            assignedDirectorId = getString("assignedDirectorId").orEmpty(),
            title = titleVal,
            milestoneOrder = (getLong("milestoneOrder") ?: 1L).toInt(),
            dueDateLabel = getString("dueDateLabel").orEmpty(),
            isOverdue = getBoolean("isOverdue") ?: false,
            status = getString("status") ?: "TODO",
            createdAtEpochMs = getLong("createdAtEpochMs") ?: System.currentTimeMillis(),
            updatedAtEpochMs = getLong("updatedAtEpochMs") ?: System.currentTimeMillis()
        )
    }

    private fun DocumentSnapshot.toWorkSubmissionEntity(): WorkSubmissionEntity? {
        val idVal = getString("id") ?: id
        val briefIdVal = getString("briefId") ?: return null
        return WorkSubmissionEntity(
            id = idVal,
            briefId = briefIdVal,
            briefTitle = getString("briefTitle").orEmpty(),
            companyName = getString("companyName").orEmpty(),
            studentAccountId = getString("studentAccountId").orEmpty(),
            studentName = getString("studentName").orEmpty(),
            directorAccountId = getString("directorAccountId").orEmpty(),
            repoOrDemoUrl = getString("repoOrDemoUrl").orEmpty(),
            understandingReport = getString("understandingReport").orEmpty(),
            skillsAppliedCsv = getString("skillsAppliedCsv").orEmpty(),
            status = getString("status") ?: "PENDING_REVIEW",
            score = (getLong("score") ?: 0L).toInt(),
            pointsAwarded = (getLong("pointsAwarded") ?: 0L).toInt(),
            feedback = getString("feedback").orEmpty(),
            rejectionReason = getString("rejectionReason").orEmpty(),
            verificationCode = getString("verificationCode").orEmpty(),
            isPublicOnPassport = getBoolean("isPublicOnPassport") ?: true,
            submittedAtEpochMs = getLong("submittedAtEpochMs") ?: System.currentTimeMillis(),
            evaluatedAtEpochMs = getLong("evaluatedAtEpochMs") ?: 0L
        )
    }

    private fun DocumentSnapshot.toNotificationEntity(): NotificationEntity? {
        val idVal = getString("id") ?: id
        val recipient = getString("recipientAccountId") ?: return null
        return NotificationEntity(
            id = idVal,
            recipientAccountId = recipient,
            category = getString("category") ?: "SYSTEM",
            titleEn = getString("titleEn").orEmpty(),
            titleAr = getString("titleAr").orEmpty(),
            bodyEn = getString("bodyEn").orEmpty(),
            bodyAr = getString("bodyAr").orEmpty(),
            isRead = getBoolean("isRead") ?: false,
            createdAtEpochMs = getLong("createdAtEpochMs") ?: System.currentTimeMillis()
        )
    }

    private fun DocumentSnapshot.toModerationReportEntity(): ModerationReportEntity? {
        val idVal = getString("id") ?: id
        return ModerationReportEntity(
            id = idVal,
            reporterAccountId = getString("reporterAccountId").orEmpty(),
            targetType = getString("targetType").orEmpty(),
            targetId = getString("targetId").orEmpty(),
            reason = getString("reason").orEmpty(),
            status = getString("status") ?: "OPEN",
            resolutionNote = getString("resolutionNote").orEmpty(),
            createdAtEpochMs = getLong("createdAtEpochMs") ?: System.currentTimeMillis()
        )
    }

    private fun DocumentSnapshot.toDeviceSessionEntity(): DeviceSessionEntity? {
        val idVal = getString("id") ?: id
        val acct = getString("accountId") ?: return null
        return DeviceSessionEntity(
            id = idVal,
            accountId = acct,
            deviceType = getString("deviceType") ?: "MOBILE",
            deviceLabel = getString("deviceLabel").orEmpty(),
            windowWidthDp = (getLong("windowWidthDp") ?: 411L).toInt(),
            lastActiveEpochMs = getLong("lastActiveEpochMs") ?: System.currentTimeMillis(),
            revoked = getBoolean("revoked") ?: false
        )
    }
}
