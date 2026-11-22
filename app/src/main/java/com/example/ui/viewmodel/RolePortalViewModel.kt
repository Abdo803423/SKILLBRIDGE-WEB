package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import com.example.data.local.WorkSubmissionEntity
import com.example.data.remote.SyncState
import com.example.data.repository.SkillBridgeBackendRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Role-isolated ViewModel bound strictly to a single authenticated [SessionPrincipal].
 * Exposes only data authorized for [session.role]; no in-app role switching exists.
 */
class RolePortalViewModel(
    private val backend: SkillBridgeBackendRepository,
    val session: SessionPrincipal
) : ViewModel() {

    private val _actionMessage = MutableStateFlow<String?>(null)
    val actionMessage: StateFlow<String?> = _actionMessage.asStateFlow()

    private val _isBusy = MutableStateFlow(false)
    val isBusy: StateFlow<Boolean> = _isBusy.asStateFlow()

    val syncState: StateFlow<SyncState> = backend.syncState
    val lastSyncedEpochMs: StateFlow<Long> = backend.lastSyncedEpochMs
    val pendingWritesCount: StateFlow<Int> = backend.pendingWritesCount

    private val _currentDeviceSessionId = MutableStateFlow("")
    val currentDeviceSessionId: StateFlow<String> = _currentDeviceSessionId.asStateFlow()

    val deviceSessions: StateFlow<List<DeviceSessionEntity>> =
        backend.observeDeviceSessions(session.accountId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val isCurrentSessionRevoked: StateFlow<Boolean> = combine(
        deviceSessions,
        _currentDeviceSessionId
    ) { sessions, currentId ->
        currentId.isNotBlank() && sessions.any { it.id == currentId && it.revoked }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    init {
        backend.startCentralSync(session.accountId)
    }

    override fun onCleared() {
        super.onCleared()
        backend.stopCentralSync()
    }

    val currentAccount: StateFlow<AccountEntity?> = backend.observeAccount(session.accountId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val notifications: StateFlow<List<NotificationEntity>> = backend.observeNotifications(session.accountId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private fun <T> emptyListState(): StateFlow<List<T>> =
        MutableStateFlow<List<T>>(emptyList()).asStateFlow()

    // --- PLATFORM_ADMIN scoped streams ---
    val adminAccounts: StateFlow<List<AccountEntity>> =
        if (session.role == AccountRole.PLATFORM_ADMIN)
            backend.observeAllAccounts().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
        else emptyListState()

    val adminCompanies: StateFlow<List<CompanyEntity>> =
        if (session.role == AccountRole.PLATFORM_ADMIN)
            backend.observeAllCompanies().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
        else emptyListState()

    val adminBriefs: StateFlow<List<ProjectBriefEntity>> =
        if (session.role == AccountRole.PLATFORM_ADMIN)
            backend.observeAllBriefs().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
        else emptyListState()

    val adminApplications: StateFlow<List<ProjectApplicationEntity>> =
        if (session.role == AccountRole.PLATFORM_ADMIN)
            backend.observeAllApplications().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
        else emptyListState()

    val adminSubmissions: StateFlow<List<WorkSubmissionEntity>> =
        if (session.role == AccountRole.PLATFORM_ADMIN)
            backend.observeAllSubmissions().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
        else emptyListState()

    val adminActivities: StateFlow<List<PlatformActivityEntity>> =
        if (session.role == AccountRole.PLATFORM_ADMIN)
            backend.observeAllActivities().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
        else emptyListState()

    val adminReports: StateFlow<List<ModerationReportEntity>> =
        if (session.role == AccountRole.PLATFORM_ADMIN)
            backend.observeAllReports().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
        else emptyListState()

    // --- STUDENT scoped streams ---
    val publishedBriefs: StateFlow<List<ProjectBriefEntity>> =
        if (session.role == AccountRole.STUDENT)
            backend.observePublishedBriefs().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
        else emptyListState()

    val studentApplications: StateFlow<List<ProjectApplicationEntity>> =
        if (session.role == AccountRole.STUDENT)
            backend.observeStudentApplications(session.accountId).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
        else emptyListState()

    val studentTasks: StateFlow<List<ProjectTaskEntity>> =
        if (session.role == AccountRole.STUDENT)
            backend.observeStudentTasks(session.accountId).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
        else emptyListState()

    val studentSubmissions: StateFlow<List<WorkSubmissionEntity>> =
        if (session.role == AccountRole.STUDENT)
            backend.observeStudentSubmissions(session.accountId).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
        else emptyListState()

    // --- COMPANY scoped streams ---
    val companyProfile: StateFlow<CompanyEntity?> =
        if (session.role == AccountRole.COMPANY && session.companyId.isNotBlank())
            backend.observeCompany(session.companyId).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
        else MutableStateFlow<CompanyEntity?>(null).asStateFlow()

    val companyDirectors: StateFlow<List<AccountEntity>> =
        if (session.role == AccountRole.COMPANY && session.companyId.isNotBlank())
            backend.observeCompanyDirectors(session.companyId).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
        else emptyListState()

    val companyBriefs: StateFlow<List<ProjectBriefEntity>> =
        if (session.role == AccountRole.COMPANY && session.companyId.isNotBlank())
            backend.observeCompanyBriefs(session.companyId).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
        else emptyListState()

    val companyApplications: StateFlow<List<ProjectApplicationEntity>> =
        if (session.role == AccountRole.COMPANY && session.companyId.isNotBlank())
            backend.observeCompanyApplications(session.companyId).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
        else emptyListState()

    val studentTalentDirectory: StateFlow<List<AccountEntity>> =
        if (session.role == AccountRole.COMPANY || session.role == AccountRole.STUDENT)
            backend.observeStudentAccounts().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
        else emptyListState()

    val companyActivities: StateFlow<List<PlatformActivityEntity>> =
        if (session.role == AccountRole.COMPANY)
            backend.observeAllActivities().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
        else emptyListState()

    // --- COMPANY_DIRECTOR scoped streams (strictly isolated to assigned briefs/tasks/submissions) ---
    val directorBriefs: StateFlow<List<ProjectBriefEntity>> =
        if (session.role == AccountRole.COMPANY_DIRECTOR)
            backend.observeDirectorBriefs(session.accountId).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
        else emptyListState()

    val directorTasks: StateFlow<List<ProjectTaskEntity>> =
        if (session.role == AccountRole.COMPANY_DIRECTOR)
            backend.observeDirectorTasks(session.accountId).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
        else emptyListState()

    val directorSubmissions: StateFlow<List<WorkSubmissionEntity>> =
        if (session.role == AccountRole.COMPANY_DIRECTOR)
            backend.observeDirectorSubmissions(session.accountId).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
        else emptyListState()

    val directorApplications: StateFlow<List<ProjectApplicationEntity>> =
        if (session.role == AccountRole.COMPANY_DIRECTOR && session.companyId.isNotBlank())
            backend.observeCompanyApplications(session.companyId).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
        else emptyListState()

    fun clearMessage() {
        _actionMessage.value = null
    }

    fun showMessage(msg: String) {
        _actionMessage.value = msg
    }

    fun markAllNotificationsRead() {
        viewModelScope.launch {
            backend.markAllNotificationsRead(session.accountId)
        }
    }

    fun registerCurrentDeviceSession(
        deviceType: String,
        deviceLabel: String,
        windowWidthDp: Int
    ) {
        val safeType = if (deviceType in setOf("MOBILE", "DESKTOP_LAPTOP")) deviceType else "MOBILE"
        val sessionId = "sess_${session.accountId}_${safeType.lowercase()}"
        _currentDeviceSessionId.value = sessionId
        viewModelScope.launch {
            backend.registerDeviceSession(
                accountId = session.accountId,
                sessionId = sessionId,
                deviceType = safeType,
                deviceLabel = deviceLabel,
                windowWidthDp = windowWidthDp
            )
        }
    }

    fun triggerManualSync() {
        viewModelScope.launch {
            backend.triggerManualSync()
            _actionMessage.value = "Central database synchronized"
        }
    }

    fun revokeDeviceSession(targetSessionId: String) {
        runAction("Device session revoked") {
            backend.revokeDeviceSession(session.accountId, targetSessionId)
        }
    }

    // --- Admin Actions ---
    fun adminReviewCompany(companyId: String, approve: Boolean, tier: String, notes: String) {
        runAction(if (approve) "Company verified ($tier)" else "Company verification rejected") {
            backend.reviewCompanyVerification(session, companyId, approve, tier, notes)
        }
    }

    fun adminReviewBrief(briefId: String, approve: Boolean, notes: String) {
        runAction(if (approve) "Project brief approved" else "Project brief rejected") {
            backend.reviewProjectBrief(session, briefId, approve, notes)
        }
    }

    fun adminSetUserActive(accountId: String, active: Boolean) {
        runAction(if (active) "User activated" else "User suspended") {
            backend.setUserActiveStatus(session, accountId, active)
        }
    }

    fun adminResolveReport(reportId: String, status: String, note: String) {
        runAction("Report updated: $status") {
            backend.resolveModerationReport(session, reportId, status, note)
        }
    }

    // --- Company Actions ---
    fun companyCreateBrief(
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
    ) {
        runAction("Project brief created & sent for Admin approval") {
            backend.createProjectBrief(
                session = session,
                title = title,
                summary = summary,
                domain = domain,
                level = level,
                skills = skills,
                rubric = rubric,
                deliverables = deliverables,
                deadline = deadline,
                pointsReward = pointsReward,
                prizePool = prizePool,
                assignedDirectorId = assignedDirectorId,
                isTeamAllowed = isTeamAllowed
            )
        }
    }

    fun companyAssignDirector(briefId: String, directorId: String) {
        runAction("Director assigned to brief") {
            backend.assignDirectorToBrief(session, briefId, directorId)
        }
    }

    fun companyDecideApplication(applicationId: String, status: String, note: String) {
        runAction("Application marked $status") {
            backend.decideApplication(session, applicationId, status, note)
        }
    }

    // --- Student Actions ---
    fun studentApplyToBrief(briefId: String, coverNote: String, roleRequested: String) {
        runAction("Application submitted to company") {
            backend.applyToProjectBrief(session, briefId, coverNote, roleRequested)
        }
    }

    fun studentSubmitWork(briefId: String, repoUrl: String, report: String, skills: List<String>) {
        runAction("Deliverable submitted for Director evaluation") {
            backend.submitProjectWork(session, briefId, repoUrl, report, skills)
        }
    }

    fun studentUpdateTaskStatus(taskId: String, newStatus: String) {
        runAction("Task moved to $newStatus") {
            backend.updateStudentTaskStatus(session, taskId, newStatus)
        }
    }

    fun studentUpdateProfile(
        displayName: String,
        domain: String,
        country: String,
        skills: List<String>,
        targetSkills: List<String>,
        goalsSummary: String,
        weeklyHours: Int
    ) {
        runAction("Profile updated") {
            backend.updateStudentProfile(session, displayName, domain, country, skills, targetSkills, goalsSummary, weeklyHours)
        }
    }

    // --- Director Actions ---
    fun directorEvaluateSubmission(
        submissionId: String,
        score: Int,
        feedback: String,
        decision: String,
        rejectionReason: String
    ) {
        runAction("Evaluation saved ($decision • $score%)") {
            backend.evaluateSubmission(session, submissionId, score, feedback, decision, rejectionReason)
        }
    }

    fun directorCreateTask(briefId: String, studentId: String, title: String, dueDate: String) {
        runAction("Milestone task assigned") {
            backend.createProjectTask(session, briefId, studentId, title, dueDate)
        }
    }

    fun directorCompleteBrief(briefId: String) {
        runAction("Project marked completed") {
            backend.markBriefCompleted(session, briefId)
        }
    }

    fun submitReport(targetType: String, targetId: String, reason: String) {
        runAction("Report submitted to Platform Admin") {
            backend.submitModerationReport(session, targetType, targetId, reason)
        }
    }

    private fun <T> runAction(successMsg: String, block: suspend () -> Result<T>) {
        viewModelScope.launch {
            _isBusy.value = true
            val res = block()
            _isBusy.value = false
            res.fold(
                onSuccess = { _actionMessage.value = successMsg },
                onFailure = { _actionMessage.value = it.message ?: "Operation failed." }
            )
        }
    }
}
