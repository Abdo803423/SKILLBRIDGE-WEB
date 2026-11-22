package com.example.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.AppLanguage
import com.example.core.AppRoleMode
import com.example.core.ConnectivityObserver
import com.example.core.LocalizedText
import com.example.core.PersistedAppPreferences
import com.example.core.UserPreferencesStore
import com.example.data.model.AiAgentContextMode
import com.example.data.model.AiAgentMessage
import com.example.data.model.AiAgentPendingAction
import com.example.data.model.AuditLogEntry
import com.example.data.model.BadgeProgressItem
import com.example.data.model.CareerGoal
import com.example.data.model.ChallengeSubmission
import com.example.data.model.CoachMessage
import com.example.data.model.CommunityComment
import com.example.data.model.CommunityPost
import com.example.data.model.PointLedgerEntry
import com.example.data.model.RoadmapMilestone
import com.example.data.model.SkillGap
import com.example.data.model.SkillGapAnalysisResult
import com.example.data.model.Team
import com.example.data.model.TeamTask
import com.example.data.model.UserBadge
import com.example.data.model.UserProfile
import com.example.data.repository.AiCareerRepository
import com.example.data.repository.PointsAndBadgesEngine
import com.example.data.repository.SkillBridgeRepository
import com.example.data.seed.SkillBridgeEcosystemData
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

sealed interface AnalysisUiState {
    data object Idle : AnalysisUiState
    data object Loading : AnalysisUiState
    data class Success(val goalId: String, val targetRole: String) : AnalysisUiState
    data class Error(val message: String) : AnalysisUiState
}

sealed interface TutorUiState {
    data object Idle : TutorUiState
    data class Loading(val milestoneId: String) : TutorUiState
    data class Streaming(val milestoneId: String, val text: String) : TutorUiState
    data class Success(val milestoneId: String, val text: String) : TutorUiState
    data class Error(val milestoneId: String, val message: String) : TutorUiState
}

sealed interface CoachStreamState {
    data object Idle : CoachStreamState
    data class Streaming(val mode: String, val text: String) : CoachStreamState
    data class Error(val message: String) : CoachStreamState
}

sealed interface WorkSimState {
    data object Idle : WorkSimState
    data class Streaming(val challengeId: String, val text: String) : WorkSimState
    data class Success(val challengeId: String, val text: String) : WorkSimState
    data class Error(val message: String) : WorkSimState
}

data class AiAgentUiState(
    val isOpen: Boolean = false,
    val mode: AiAgentContextMode = AiAgentContextMode.SKILL_ADVISOR,
    val screenLabel: String = "Home",
    val contextSummary: String = "",
    val activeTeam: Team? = null,
    val messages: List<AiAgentMessage> = emptyList(),
    val streamingText: String = "",
    val isStreaming: Boolean = false,
    val errorMessage: String? = null,
    val lastPrompt: String = "",
    val pendingActionConfirmation: AiAgentPendingAction? = null
)

enum class PreviewStateMode {
    NORMAL, LOADING, EMPTY, ERROR, LOCKED
}

@OptIn(ExperimentalCoroutinesApi::class)
class SkillBridgeViewModel(
    private val repo: SkillBridgeRepository,
    private val aiRepo: AiCareerRepository,
    private val prefsStore: UserPreferencesStore,
    private val connectivityObserver: ConnectivityObserver,
    val currentUserId: String,
    private val userDisplayName: String,
    val isGuest: Boolean = false
) : ViewModel() {

    val isOffline: StateFlow<Boolean> = connectivityObserver.isOffline

    val profileState: StateFlow<UiState<UserProfile?>> = if (isGuest) {
        flowOf(
            UiState.Success(
                UserProfile(
                    userId = "guest",
                    displayName = "Guest Preview",
                    careerLevel = "Junior",
                    points = 0L,
                    streakDays = 0L
                )
            )
        )
    } else {
        repo.observeProfile(currentUserId)
            .map<UserProfile?, UiState<UserProfile?>> { UiState.Success(it) }
            .catch { error -> emit(UiState.Error(unwrapFirestoreError(error, "profile"))) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), UiState.Loading)

    val goalsState: StateFlow<UiState<List<CareerGoal>>> = if (isGuest) {
        flowOf(UiState.Success(emptyList()))
    } else {
        repo.observeGoals(currentUserId)
            .map<List<CareerGoal>, UiState<List<CareerGoal>>> { UiState.Success(it) }
            .catch { error -> emit(UiState.Error(unwrapFirestoreError(error, "career goals"))) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), UiState.Loading)

    val gapsState: StateFlow<UiState<List<SkillGap>>> = if (isGuest) {
        flowOf(UiState.Success(emptyList()))
    } else {
        repo.observeGaps(currentUserId)
            .map<List<SkillGap>, UiState<List<SkillGap>>> { UiState.Success(it) }
            .catch { error -> emit(UiState.Error(unwrapFirestoreError(error, "skill gaps"))) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), UiState.Loading)

    val milestonesState: StateFlow<UiState<List<RoadmapMilestone>>> = if (isGuest) {
        flowOf(UiState.Success(emptyList()))
    } else {
        repo.observeMilestones(currentUserId)
            .map<List<RoadmapMilestone>, UiState<List<RoadmapMilestone>>> { UiState.Success(it) }
            .catch { error -> emit(UiState.Error(unwrapFirestoreError(error, "roadmap milestones"))) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), UiState.Loading)

    val coachMessagesState: StateFlow<UiState<List<CoachMessage>>> = if (isGuest) {
        flowOf(UiState.Success(emptyList()))
    } else {
        repo.observeCoachMessages(currentUserId)
            .map<List<CoachMessage>, UiState<List<CoachMessage>>> { UiState.Success(it) }
            .catch { error -> emit(UiState.Error(unwrapFirestoreError(error, "coach history"))) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), UiState.Loading)

    val submissionsState: StateFlow<UiState<List<ChallengeSubmission>>> = if (isGuest) {
        flowOf(UiState.Success(emptyList()))
    } else {
        repo.observeSubmissions(currentUserId)
            .map<List<ChallengeSubmission>, UiState<List<ChallengeSubmission>>> { UiState.Success(it) }
            .catch { error -> emit(UiState.Error(unwrapFirestoreError(error, "submissions"))) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), UiState.Loading)

    val badgesState: StateFlow<UiState<List<UserBadge>>> = if (isGuest) {
        flowOf(UiState.Success(emptyList()))
    } else {
        repo.observeBadges(currentUserId)
            .map<List<UserBadge>, UiState<List<UserBadge>>> { UiState.Success(it) }
            .catch { error -> emit(UiState.Error(unwrapFirestoreError(error, "badges"))) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), UiState.Loading)

    val pointLedgerState: StateFlow<UiState<List<PointLedgerEntry>>> = if (isGuest) {
        flowOf(UiState.Success(emptyList()))
    } else {
        repo.observePointLedger(currentUserId)
            .map<List<PointLedgerEntry>, UiState<List<PointLedgerEntry>>> { UiState.Success(it) }
            .catch { error -> emit(UiState.Error(unwrapFirestoreError(error, "points ledger"))) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), UiState.Loading)

    val auditLogsState: StateFlow<UiState<List<AuditLogEntry>>> = if (isGuest) {
        flowOf(UiState.Success(emptyList()))
    } else {
        repo.observeAuditLogs(currentUserId)
            .map<List<AuditLogEntry>, UiState<List<AuditLogEntry>>> { UiState.Success(it) }
            .catch { error -> emit(UiState.Error(unwrapFirestoreError(error, "audit log"))) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), UiState.Loading)

    val communityPostsState: StateFlow<UiState<List<CommunityPost>>> = if (isGuest) {
        flowOf(UiState.Success(SkillBridgeEcosystemData.seedCommunityPosts))
    } else {
        repo.observeCommunityPosts()
            .map<List<CommunityPost>, UiState<List<CommunityPost>>> { UiState.Success(it) }
            .catch { error -> emit(UiState.Error(unwrapFirestoreError(error, "community posts"))) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), UiState.Loading)

    val recruitingTeamsState: StateFlow<UiState<List<Team>>> = if (isGuest) {
        flowOf(UiState.Success(SkillBridgeEcosystemData.seedTeams))
    } else {
        repo.observeRecruitingTeams()
            .map<List<Team>, UiState<List<Team>>> { UiState.Success(it) }
            .catch { error -> emit(UiState.Error(unwrapFirestoreError(error, "recruiting teams"))) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), UiState.Loading)

    val myTeamsState: StateFlow<UiState<List<Team>>> = if (isGuest) {
        flowOf(UiState.Success(emptyList()))
    } else {
        repo.observeMyTeams(currentUserId)
            .map<List<Team>, UiState<List<Team>>> { UiState.Success(it) }
            .catch { error -> emit(UiState.Error(unwrapFirestoreError(error, "my teams"))) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), UiState.Loading)

    private val _selectedPostId = MutableStateFlow<String?>(null)
    val selectedPostCommentsState: StateFlow<UiState<List<CommunityComment>>> = _selectedPostId
        .flatMapLatest { postId ->
            if (postId.isNullOrBlank() || postId.startsWith("seed_") || isGuest) {
                flowOf(UiState.Success(SkillBridgeEcosystemData.seedComments[postId].orEmpty()))
            } else {
                repo.observePostComments(postId)
                    .map<List<CommunityComment>, UiState<List<CommunityComment>>> { UiState.Success(it) }
                    .catch { error -> emit(UiState.Error(unwrapFirestoreError(error, "comments"))) }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), UiState.Success(emptyList()))

    private val _selectedTeamId = MutableStateFlow<String?>(null)
    val selectedTeamTasksState: StateFlow<UiState<List<TeamTask>>> = _selectedTeamId
        .flatMapLatest { teamId ->
            if (teamId.isNullOrBlank() || teamId.startsWith("seed_") || isGuest) {
                flowOf(UiState.Success(SkillBridgeEcosystemData.seedTeamTasks[teamId].orEmpty()))
            } else {
                repo.observeTeamTasks(teamId, currentUserId)
                    .map<List<TeamTask>, UiState<List<TeamTask>>> { UiState.Success(it) }
                    .catch { error -> emit(UiState.Error(unwrapFirestoreError(error, "team tasks"))) }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), UiState.Success(emptyList()))

    private val _language = MutableStateFlow(AppLanguage.EN)
    val language: StateFlow<AppLanguage> = _language.asStateFlow()

    private val _darkTheme = MutableStateFlow(false)
    val darkTheme: StateFlow<Boolean> = _darkTheme.asStateFlow()

    private val _roleMode = MutableStateFlow(AppRoleMode.LEARNER)
    val roleMode: StateFlow<AppRoleMode> = _roleMode.asStateFlow()

    private val _previewStateMode = MutableStateFlow(PreviewStateMode.NORMAL)
    val previewStateMode: StateFlow<PreviewStateMode> = _previewStateMode.asStateFlow()

    private val _onboardingCompleted = MutableStateFlow(false)
    val onboardingCompleted: StateFlow<Boolean> = _onboardingCompleted.asStateFlow()

    private val _lastTab = MutableStateFlow("home")
    val lastTab: StateFlow<String> = _lastTab.asStateFlow()

    private val _analysisState = MutableStateFlow<AnalysisUiState>(AnalysisUiState.Idle)
    val analysisState: StateFlow<AnalysisUiState> = _analysisState.asStateFlow()

    private val _latestAnalysis = MutableStateFlow<SkillGapAnalysisResult?>(null)
    val latestAnalysis: StateFlow<SkillGapAnalysisResult?> = _latestAnalysis.asStateFlow()

    private val _tutorState = MutableStateFlow<TutorUiState>(TutorUiState.Idle)
    val tutorState: StateFlow<TutorUiState> = _tutorState.asStateFlow()

    private val _coachStreamState = MutableStateFlow<CoachStreamState>(CoachStreamState.Idle)
    val coachStreamState: StateFlow<CoachStreamState> = _coachStreamState.asStateFlow()

    private val _workSimState = MutableStateFlow<WorkSimState>(WorkSimState.Idle)
    val workSimState: StateFlow<WorkSimState> = _workSimState.asStateFlow()

    private val _aiAgentState = MutableStateFlow(AiAgentUiState())
    val aiAgentState: StateFlow<AiAgentUiState> = _aiAgentState.asStateFlow()

    private val _savedChallengeIds = MutableStateFlow<Set<String>>(emptySet())
    val savedChallengeIds: StateFlow<Set<String>> = _savedChallengeIds.asStateFlow()

    private val _savedPostIds = MutableStateFlow<Set<String>>(emptySet())
    val savedPostIds: StateFlow<Set<String>> = _savedPostIds.asStateFlow()

    private val _upvotedPostIds = MutableStateFlow<Set<String>>(emptySet())
    val upvotedPostIds: StateFlow<Set<String>> = _upvotedPostIds.asStateFlow()

    private val _followedTags = MutableStateFlow<Set<String>>(emptySet())
    val followedTags: StateFlow<Set<String>> = _followedTags.asStateFlow()

    private val _mutedAuthors = MutableStateFlow<Set<String>>(emptySet())
    val mutedAuthors: StateFlow<Set<String>> = _mutedAuthors.asStateFlow()

    private val _aiPersonalizationEnabled = MutableStateFlow(true)
    val aiPersonalizationEnabled: StateFlow<Boolean> = _aiPersonalizationEnabled.asStateFlow()

    private val _aiContextSharingEnabled = MutableStateFlow(true)
    val aiContextSharingEnabled: StateFlow<Boolean> = _aiContextSharingEnabled.asStateFlow()

    private val _notificationChannels = MutableStateFlow(
        mapOf("In-App" to true, "Push" to true, "Email Digest" to false, "Team Mentions" to true)
    )
    val notificationChannels: StateFlow<Map<String, Boolean>> = _notificationChannels.asStateFlow()

    private val _unreadNotificationIds = MutableStateFlow(setOf("n1", "n2"))
    val unreadNotificationIds: StateFlow<Set<String>> = _unreadNotificationIds.asStateFlow()

    private val _actionMessage = MutableStateFlow<String?>(null)
    val actionMessage: StateFlow<String?> = _actionMessage.asStateFlow()

    private val _mentorEvaluationsInSession = MutableStateFlow(0)
    private var hasHydratedFromProfile = false
    private var userToggledLanguage = false
    private var userToggledTheme = false
    private val inFlightBadgeIds = mutableSetOf<String>()

    val badgeProgressList: StateFlow<List<BadgeProgressItem>> = combine(
        profileState,
        submissionsState,
        badgesState,
        myTeamsState,
        combine(selectedTeamTasksState, milestonesState, communityPostsState, _mentorEvaluationsInSession) { tasksUi, msUi, postsUi, mentorCount ->
            QuadState(
                tasks = (tasksUi as? UiState.Success)?.data.orEmpty(),
                milestones = (msUi as? UiState.Success)?.data.orEmpty(),
                posts = (postsUi as? UiState.Success)?.data.orEmpty(),
                mentorCount = mentorCount
            )
        }
    ) { profUi, subsUi, badgesUi, teamsUi, extra ->
        val profile = (profUi as? UiState.Success)?.data
        val subs = (subsUi as? UiState.Success)?.data.orEmpty()
        val badges = (badgesUi as? UiState.Success)?.data.orEmpty()
        val teams = (teamsUi as? UiState.Success)?.data.orEmpty()
        PointsAndBadgesEngine.evaluateBadgesProgress(
            currentUserId = currentUserId,
            profile = profile,
            submissions = subs,
            earnedBadges = badges,
            teams = teams,
            teamTasks = extra.tasks,
            milestones = extra.milestones,
            communityPosts = extra.posts,
            mentorReviewCount = extra.mentorCount
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        PointsAndBadgesEngine.evaluateBadgesProgress(
            currentUserId = currentUserId,
            profile = null,
            submissions = emptyList(),
            earnedBadges = emptyList(),
            teams = emptyList(),
            teamTasks = emptyList(),
            milestones = emptyList(),
            communityPosts = emptyList()
        )
    )

    private data class QuadState(
        val tasks: List<TeamTask>,
        val milestones: List<RoadmapMilestone>,
        val posts: List<CommunityPost>,
        val mentorCount: Int
    )

    init {
        viewModelScope.launch {
            val localPrefs = prefsStore.preferencesFlow.first()
            if (!userToggledLanguage) _language.value = localPrefs.language
            if (!userToggledTheme) _darkTheme.value = localPrefs.darkTheme
            _lastTab.value = localPrefs.lastTab
            _aiPersonalizationEnabled.value = localPrefs.aiPersonalizationEnabled
            _aiContextSharingEnabled.value = localPrefs.aiContextSharingEnabled
            if (isGuest) {
                _savedChallengeIds.value = localPrefs.guestSavedChallengeIds
            } else {
                // Ensure profile exists with 0 fake achievements & user's local language/theme preserved
                val existing = repo.observeProfile(currentUserId).first()
                if (existing == null) {
                    repo.upsertProfile(
                        userId = currentUserId,
                        displayName = userDisplayName.ifBlank { "SkillBridge Learner" },
                        currentRole = "Builder",
                        experienceLevel = "Junior",
                        preferredLanguage = _language.value.name,
                        darkTheme = _darkTheme.value
                    )
                }
            }
        }

        if (!isGuest) {
            // Hydrate user preferences from Firestore without overwriting user's local language/theme if already chosen
            viewModelScope.launch {
                profileState.collect { st ->
                    val prof = (st as? UiState.Success)?.data ?: return@collect
                    if (!hasHydratedFromProfile) {
                        hasHydratedFromProfile = true
                        _onboardingCompleted.value = prof.onboardingCompleted
                        _savedChallengeIds.value = prof.savedChallengeIds.toSet()
                        _savedPostIds.value = prof.savedPostIds.toSet()
                        _followedTags.value = prof.followedTags.toSet()
                        _mutedAuthors.value = prof.mutedAuthors.toSet()
                        _aiPersonalizationEnabled.value = prof.aiPersonalizationEnabled
                        _aiContextSharingEnabled.value = prof.aiContextSharingEnabled
                        _notificationChannels.value = mapOf(
                            "In-App" to true,
                            "Push" to prof.pushNotifications,
                            "Email Digest" to prof.emailNotifications,
                            "Team Mentions" to prof.teamMentionsNotifications
                        )
                        AppRoleMode.entries.firstOrNull { it.id == prof.roleMode }?.let {
                            _roleMode.value = it
                        }
                        if (prof.lastRoute.isNotBlank()) {
                            _lastTab.value = prof.lastRoute
                        }
                        // Sync local language/theme to profile if profile still had default
                        val localPrefs = prefsStore.preferencesFlow.first()
                        if (localPrefs.language.name != prof.preferredLanguage || localPrefs.darkTheme != prof.darkTheme) {
                            repo.updateUserPreferences(
                                currentUserId,
                                mapOf(
                                    "preferredLanguage" to localPrefs.language.name,
                                    "darkTheme" to localPrefs.darkTheme
                                )
                            )
                        }
                    }
                }
            }

            // Idempotently award newly unlocked badges when evidence changes
            viewModelScope.launch {
                combine(badgeProgressList, badgesState) { progress, badgesUi ->
                    if (badgesUi is UiState.Success) {
                        PointsAndBadgesEngine.findNewlyEligibleBadges(progress, badgesUi.data)
                    } else {
                        emptyList()
                    }
                }.collect { newlyEligible ->
                    newlyEligible.forEach { badgeDef ->
                        if (inFlightBadgeIds.add(badgeDef.id)) {
                            val res = repo.awardBadgeIfAbsent(currentUserId, badgeDef)
                            if (res.getOrNull() == true) {
                                val title = badgeDef.title.get(_language.value)
                                _actionMessage.value = if (_language.value == AppLanguage.AR) {
                                    "تم فتح شارة جديدة: $title (+${badgeDef.pointsAwarded} نقطة)"
                                } else {
                                    "Badge unlocked: $title (+${badgeDef.pointsAwarded} pts)!"
                                }
                            }
                            inFlightBadgeIds.remove(badgeDef.id)
                        }
                    }
                }
            }
        }
    }

    fun toggleLanguage() {
        userToggledLanguage = true
        val next = if (_language.value == AppLanguage.EN) AppLanguage.AR else AppLanguage.EN
        _language.value = next
        viewModelScope.launch {
            prefsStore.setLanguage(next)
            if (!isGuest) {
                val res = repo.updateUserPreferences(currentUserId, mapOf("preferredLanguage" to next.name))
                if (res.isFailure) {
                    _actionMessage.value = localizedSaveError(res.exceptionOrNull())
                }
            }
        }
    }

    fun setDarkTheme(enabled: Boolean) {
        userToggledTheme = true
        _darkTheme.value = enabled
        viewModelScope.launch {
            prefsStore.setDarkTheme(enabled)
            if (!isGuest) {
                val res = repo.updateUserPreferences(currentUserId, mapOf("darkTheme" to enabled))
                if (res.isFailure) {
                    _actionMessage.value = localizedSaveError(res.exceptionOrNull())
                }
            }
        }
    }

    fun setLastTab(route: String) {
        _lastTab.value = route
        viewModelScope.launch {
            prefsStore.setLastTab(route)
            if (!isGuest) {
                repo.updateUserPreferences(currentUserId, mapOf("lastRoute" to route.take(64)))
            }
        }
    }

    fun setRoleMode(mode: AppRoleMode) {
        if (isGuest && mode != AppRoleMode.LEARNER) {
            _actionMessage.value = guestRequiredMessage()
            return
        }
        _roleMode.value = mode
        if (isGuest) return
        viewModelScope.launch {
            val res = repo.updateUserPreferences(currentUserId, mapOf("roleMode" to mode.id))
            if (res.isFailure) {
                _actionMessage.value = localizedSaveError(res.exceptionOrNull())
            }
        }
    }

    fun setPreviewStateMode(mode: PreviewStateMode) {
        _previewStateMode.value = mode
    }

    fun selectPostForComments(postId: String?) {
        _selectedPostId.value = postId
    }

    fun selectTeamForTasks(teamId: String?) {
        _selectedTeamId.value = teamId
    }

    fun markNotificationsRead() {
        _unreadNotificationIds.value = emptySet()
    }

    fun toggleNotificationChannel(channel: String) {
        val current = _notificationChannels.value.toMutableMap()
        val newValue = !(current[channel] ?: false)
        current[channel] = newValue
        _notificationChannels.value = current
        if (isGuest) return
        val fieldName = when (channel) {
            "Push" -> "pushNotifications"
            "Email Digest" -> "emailNotifications"
            "Team Mentions" -> "teamMentionsNotifications"
            else -> null
        } ?: return
        viewModelScope.launch {
            val res = repo.updateUserPreferences(currentUserId, mapOf(fieldName to newValue))
            if (res.isFailure) {
                _actionMessage.value = localizedSaveError(res.exceptionOrNull())
            }
        }
    }

    fun updateAiPreferences(personalization: Boolean, contextSharing: Boolean) {
        _aiPersonalizationEnabled.value = personalization
        _aiContextSharingEnabled.value = contextSharing
        viewModelScope.launch {
            prefsStore.setAiPreferences(personalization, contextSharing)
            if (!isGuest) {
                val res = repo.updateUserPreferences(
                    currentUserId,
                    mapOf(
                        "aiPersonalizationEnabled" to personalization,
                        "aiContextSharingEnabled" to contextSharing
                    )
                )
                if (res.isFailure) {
                    _actionMessage.value = localizedSaveError(res.exceptionOrNull())
                }
            }
        }
    }

    fun clearActionMessage() {
        _actionMessage.value = null
    }

    fun showMessage(msg: String) {
        _actionMessage.value = msg
    }

    fun completeOnboarding(
        displayName: String,
        country: String,
        timezone: String,
        domain: String,
        careerLevel: String,
        currentSkills: List<String>,
        targetSkills: List<String>,
        goalsSummary: String,
        weeklyHours: Int
    ) {
        if (isGuest) {
            viewModelScope.launch {
                prefsStore.setGuestDomain(domain)
                _onboardingCompleted.value = true
                _actionMessage.value = guestRequiredMessage()
            }
            return
        }
        viewModelScope.launch {
            val existing = (profileState.value as? UiState.Success)?.data
            val res = repo.upsertProfile(
                userId = currentUserId,
                displayName = displayName,
                currentRole = domain,
                experienceLevel = existing?.careerLevel ?: "Junior",
                country = country,
                timezone = timezone,
                domain = domain,
                careerLevel = existing?.careerLevel?.ifBlank { "Junior" } ?: "Junior",
                planTier = existing?.planTier ?: "Starter — Free",
                roleMode = _roleMode.value.id,
                weeklyHours = weeklyHours,
                currentSkills = currentSkills,
                targetSkills = targetSkills,
                goalsSummary = goalsSummary,
                passportPublic = existing?.passportPublic ?: true,
                showRepoLinks = existing?.showRepoLinks ?: true,
                preferredLanguage = _language.value.name,
                darkTheme = _darkTheme.value,
                onboardingCompleted = true
            )
            if (res.isFailure) {
                _actionMessage.value = localizedSaveError(res.exceptionOrNull())
            } else {
                _onboardingCompleted.value = true
                _actionMessage.value = if (_language.value == AppLanguage.AR) "تم حفظ إعداد ملفك في السحابة" else "Onboarding profile saved!"
            }
        }
    }

    fun updatePrivacySettings(passportPublic: Boolean, showRepoLinks: Boolean) {
        if (isGuest) {
            _actionMessage.value = guestRequiredMessage()
            return
        }
        viewModelScope.launch {
            val res = repo.updateUserPreferences(
                currentUserId,
                mapOf(
                    "passportPublic" to passportPublic,
                    "showRepoLinks" to showRepoLinks
                )
            )
            _actionMessage.value = if (res.isSuccess) {
                if (_language.value == AppLanguage.AR) "تم تحديث إعدادات الخصوصية" else "Privacy preferences saved"
            } else {
                localizedSaveError(res.exceptionOrNull())
            }
        }
    }

    fun toggleBadgeShareable(badgeId: String, currentShareable: Boolean) {
        if (isGuest) {
            _actionMessage.value = guestRequiredMessage()
            return
        }
        viewModelScope.launch {
            val res = repo.toggleBadgeShareable(currentUserId, badgeId, !currentShareable)
            if (res.isFailure) {
                _actionMessage.value = localizedSaveError(res.exceptionOrNull())
            }
        }
    }

    fun resetAnalysisState() {
        _analysisState.value = AnalysisUiState.Idle
    }

    fun analyzeAndSaveCareerBridge(
        targetRole: String,
        experienceLevel: String,
        currentSkills: List<String>,
        backgroundNotes: String,
        weeklyHours: Int
    ) {
        if (isGuest) {
            _analysisState.value = AnalysisUiState.Error(guestRequiredMessage())
            return
        }
        if (targetRole.isBlank()) {
            _analysisState.value = AnalysisUiState.Error(
                if (_language.value == AppLanguage.AR) "أدخل الدور الوظيفي المستهدف للتحليل." else "Please enter a target role to analyze."
            )
            return
        }
        if (isOffline.value) {
            _analysisState.value = AnalysisUiState.Error(
                if (_language.value == AppLanguage.AR) "التحليل الذكي يتطلب اتصالًا بالإنترنت." else "AI Gap Analysis requires an active internet connection."
            )
            return
        }
        viewModelScope.launch {
            _analysisState.value = AnalysisUiState.Loading
            try {
                val analysis = aiRepo.analyzeSkillGap(
                    targetRole = targetRole.trim(),
                    experienceLevel = experienceLevel,
                    currentSkills = currentSkills,
                    backgroundNotes = backgroundNotes,
                    weeklyHours = weeklyHours,
                    isArabic = _language.value == AppLanguage.AR
                )
                _latestAnalysis.value = analysis
                val existingIds = (goalsState.value as? UiState.Success)?.data?.map { it.id }.orEmpty()
                val saveResult = repo.saveAnalysisPlan(
                    userId = currentUserId,
                    targetRole = targetRole.trim(),
                    experienceLevel = experienceLevel,
                    weeklyHours = weeklyHours,
                    currentSkills = currentSkills,
                    analysis = analysis,
                    existingGoalIds = existingIds
                )
                if (saveResult.isSuccess) {
                    _analysisState.value = AnalysisUiState.Success(
                        goalId = saveResult.getOrThrow(),
                        targetRole = targetRole.trim()
                    )
                } else {
                    _analysisState.value = AnalysisUiState.Error(
                        saveResult.exceptionOrNull()?.localizedMessage ?: "Failed to save roadmap."
                    )
                }
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Skill gap analysis timed out", e)
                _analysisState.value = AnalysisUiState.Error(
                    if (_language.value == AppLanguage.AR) "انتهت مهلة الذكاء الاصطناعي. أعد المحاولة." else "AI analysis timed out. Please retry."
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Skill gap analysis failed", e)
                _analysisState.value = AnalysisUiState.Error(e.localizedMessage ?: "Skill gap analysis failed.")
            }
        }
    }

    fun toggleSavedChallenge(challengeId: String) {
        val current = _savedChallengeIds.value.toMutableSet()
        if (!current.add(challengeId)) current.remove(challengeId)
        _savedChallengeIds.value = current
        viewModelScope.launch {
            if (isGuest) {
                prefsStore.setGuestSavedChallenges(current)
            } else {
                val res = repo.updateUserPreferences(currentUserId, mapOf("savedChallengeIds" to current.toList()))
                if (res.isFailure) _actionMessage.value = localizedSaveError(res.exceptionOrNull())
            }
        }
    }

    fun toggleSavedPost(postId: String) {
        if (isGuest) {
            _actionMessage.value = guestRequiredMessage()
            return
        }
        val current = _savedPostIds.value.toMutableSet()
        if (!current.add(postId)) current.remove(postId)
        _savedPostIds.value = current
        viewModelScope.launch {
            val res = repo.updateUserPreferences(currentUserId, mapOf("savedPostIds" to current.toList()))
            if (res.isFailure) _actionMessage.value = localizedSaveError(res.exceptionOrNull())
        }
    }

    fun toggleFollowTag(tag: String) {
        val current = _followedTags.value.toMutableSet()
        if (!current.add(tag)) current.remove(tag)
        _followedTags.value = current
        if (isGuest) return
        viewModelScope.launch {
            val res = repo.updateUserPreferences(currentUserId, mapOf("followedTags" to current.toList()))
            if (res.isFailure) _actionMessage.value = localizedSaveError(res.exceptionOrNull())
        }
    }

    fun muteAuthor(authorName: String) {
        val updated = _mutedAuthors.value + authorName
        _mutedAuthors.value = updated
        if (!isGuest) {
            viewModelScope.launch {
                repo.updateUserPreferences(currentUserId, mapOf("mutedAuthors" to updated.toList()))
            }
        }
        _actionMessage.value = if (_language.value == AppLanguage.AR) "تم كتم $authorName" else "Muted $authorName"
    }

    fun containsLeakedSecret(input: String): Boolean {
        val patterns = listOf("AIza", "sk-", "Bearer ", "-----BEGIN", "ghp_", "AKIA")
        return patterns.any { input.contains(it, ignoreCase = false) }
    }

    fun submitChallengeSolution(
        challengeId: String,
        challengeTitle: String,
        companyName: String,
        category: String,
        roleInTeam: String,
        skillsApplied: List<String>,
        repoOrDemoUrl: String,
        understandingReport: String,
        pointsReward: Int,
        rewardAmount: String
    ) {
        if (isGuest) {
            _actionMessage.value = guestRequiredMessage()
            return
        }
        if (containsLeakedSecret(understandingReport) || containsLeakedSecret(repoOrDemoUrl)) {
            _actionMessage.value = if (_language.value == AppLanguage.AR) {
                "تم حظر الإرسال: تم رصد مفتاح سري أو Token. احذفه أولًا."
            } else {
                "Submission blocked: secret/API key detected. Remove credentials first."
            }
            return
        }
        if (understandingReport.trim().length < 15) {
            _actionMessage.value = if (_language.value == AppLanguage.AR) {
                "يرجى كتابة تقرير فهم واضح قبل التسليم."
            } else {
                "Please write a clear Understanding Report before submitting."
            }
            return
        }
        viewModelScope.launch {
            val res = repo.submitChallengeSolution(
                userId = currentUserId,
                challengeId = challengeId,
                challengeTitle = challengeTitle,
                companyName = companyName,
                category = category,
                roleInTeam = roleInTeam,
                skillsApplied = skillsApplied,
                repoOrDemoUrl = repoOrDemoUrl,
                understandingReport = understandingReport,
                pointsReward = pointsReward,
                rewardAmount = rewardAmount
            )
            _actionMessage.value = if (res.isSuccess) {
                if (_language.value == AppLanguage.AR) "تم رفع التسليم للمراجعة الموثّقة" else "Submitted for rubric & mentor verification!"
            } else {
                localizedSaveError(res.exceptionOrNull())
            }
        }
    }

    fun toggleSubmissionVisibility(submission: ChallengeSubmission) {
        if (isGuest || submission.isDemoSample || submission.id.startsWith("seed_")) {
            _actionMessage.value = if (_language.value == AppLanguage.AR) {
                "هذه بطاقة عرض نموذجية — ارفع حلًا حقيقيًا للتحكم بخصوصيته"
            } else {
                "Demo catalog preview — submit a real solution to control its visibility"
            }
            return
        }
        viewModelScope.launch {
            val res = repo.updateSubmissionPassportVisibility(currentUserId, submission.id, !submission.isPublicOnPassport)
            if (res.isFailure) _actionMessage.value = localizedSaveError(res.exceptionOrNull())
        }
    }

    fun evaluateSubmission(
        submission: ChallengeSubmission,
        score: Int,
        feedback: String,
        approve: Boolean,
        rejectionReason: String = ""
    ) {
        if (isGuest || submission.isDemoSample || submission.id.startsWith("seed_")) {
            _actionMessage.value = if (_language.value == AppLanguage.AR) {
                "لا يمكن تعديل عناصر الكتالوج النموذجي — قيّم تسليمات المستخدم الفعلية"
            } else {
                "Demo reference items are read-only; evaluate real user submissions"
            }
            return
        }
        viewModelScope.launch {
            val res = repo.awardSubmissionEvaluationAndSync(
                userId = submission.userId.ifBlank { currentUserId },
                submission = submission,
                approve = approve,
                score = score,
                feedback = feedback,
                rejectionReason = rejectionReason,
                actorRole = _roleMode.value.id
            )
            if (res.isSuccess) {
                _mentorEvaluationsInSession.value += 1
                _actionMessage.value = if (_language.value == AppLanguage.AR) {
                    if (approve) "تم توثيق التسليم وإضافة النقاط للسجل" else "تم حفظ قرار المراجعة مع السبب"
                } else {
                    if (approve) "Submission verified & points ledger updated!" else "Evaluation & reason saved"
                }
            } else {
                _actionMessage.value = localizedSaveError(res.exceptionOrNull())
            }
        }
    }

    fun createCommunityPost(postType: String, title: String, body: String, tags: List<String>) {
        if (isGuest) {
            _actionMessage.value = guestRequiredMessage()
            return
        }
        if (title.trim().length < 3 || body.trim().length < 3) return
        if (containsLeakedSecret(title) || containsLeakedSecret(body)) {
            _actionMessage.value = if (_language.value == AppLanguage.AR) {
                "تم منع النشر لاحتوائه على مفتاح سري أو Token."
            } else {
                "Blocked: remove leaked API key or token before posting."
            }
            return
        }
        val profile = (profileState.value as? UiState.Success)?.data
        val authorName = profile?.displayName?.ifBlank { userDisplayName } ?: userDisplayName
        val authorLevel = profile?.careerLevel?.ifBlank { "Junior" } ?: "Junior"
        val hasVerified = ((submissionsState.value as? UiState.Success)?.data.orEmpty().any { it.status == "VERIFIED" && !it.isDemoSample })
        viewModelScope.launch {
            val res = repo.createCommunityPost(
                authorId = currentUserId,
                authorName = authorName,
                authorLevel = authorLevel,
                authorVerified = hasVerified,
                postType = postType,
                title = title,
                body = body,
                tags = tags
            )
            _actionMessage.value = if (res.isSuccess) {
                if (_language.value == AppLanguage.AR) "تم نشر مشاركتك في المجتمع" else "Post published to Community"
            } else {
                localizedSaveError(res.exceptionOrNull())
            }
        }
    }

    fun toggleUpvotePost(post: CommunityPost) {
        if (isGuest) {
            _actionMessage.value = guestRequiredMessage()
            return
        }
        val current = _upvotedPostIds.value.toMutableSet()
        val adding = current.add(post.id)
        if (!adding) current.remove(post.id)
        _upvotedPostIds.value = current
        if (!post.id.startsWith("seed_")) {
            viewModelScope.launch {
                val delta = if (adding) 1L else -1L
                repo.updatePostHelpfulCount(post.id, post.helpfulCount + delta)
            }
        }
    }

    fun addComment(post: CommunityPost, parentCommentId: String, body: String) {
        if (isGuest) {
            _actionMessage.value = guestRequiredMessage()
            return
        }
        if (body.isBlank()) return
        if (post.id.startsWith("seed_")) {
            _actionMessage.value = if (_language.value == AppLanguage.AR) "هذه مناقشة نموذجية — أنشئ منشورًا حيًا للتعليق الفعلي" else "Demo post preview — publish a live post to store comments"
            return
        }
        val profile = (profileState.value as? UiState.Success)?.data
        val authorName = profile?.displayName?.ifBlank { userDisplayName } ?: userDisplayName
        val authorLevel = profile?.careerLevel?.ifBlank { "Junior" } ?: "Junior"
        val hasVerified = ((submissionsState.value as? UiState.Success)?.data.orEmpty().any { it.status == "VERIFIED" && !it.isDemoSample })
        viewModelScope.launch {
            val res = repo.addComment(
                postId = post.id,
                parentCommentId = parentCommentId,
                authorId = currentUserId,
                authorName = authorName,
                authorLevel = authorLevel,
                authorVerified = hasVerified,
                body = body,
                newCommentCount = post.commentCount + 1L
            )
            if (res.isFailure) {
                _actionMessage.value = localizedSaveError(res.exceptionOrNull())
            }
        }
    }

    fun acceptBestAnswer(postId: String, commentId: String) {
        if (isGuest || postId.startsWith("seed_")) {
            _actionMessage.value = guestRequiredMessage()
            return
        }
        viewModelScope.launch {
            val res = repo.acceptBestAnswer(postId, commentId)
            _actionMessage.value = if (res.isSuccess) {
                if (_language.value == AppLanguage.AR) "تم تعيين أفضل إجابة" else "Marked as Best Answer"
            } else {
                if (_language.value == AppLanguage.AR) "صاحب السؤال فقط يمكنه تعيين أفضل إجابة" else "Only the post author can mark Best Answer"
            }
        }
    }

    fun createTeam(
        name: String,
        description: String,
        domain: String,
        level: String,
        maxMembers: Int,
        requiredSkills: List<String>,
        rolesNeeded: List<String>,
        challengeTitle: String
    ) {
        if (isGuest) {
            _actionMessage.value = guestRequiredMessage()
            return
        }
        if (name.trim().length < 2 || description.trim().length < 2) return
        val captainName = (profileState.value as? UiState.Success)?.data?.displayName?.ifBlank { userDisplayName } ?: userDisplayName
        viewModelScope.launch {
            val res = repo.createTeam(
                captainId = currentUserId,
                captainName = captainName,
                name = name,
                description = description,
                domain = domain,
                level = level,
                maxMembers = maxMembers,
                requiredSkills = requiredSkills,
                rolesNeeded = rolesNeeded,
                challengeTitle = challengeTitle
            )
            _actionMessage.value = if (res.isSuccess) {
                if (_language.value == AppLanguage.AR) "تم إنشاء الفريق ونشر إعلان الأعضاء" else "Team created & recruitment ad published"
            } else {
                localizedSaveError(res.exceptionOrNull())
            }
        }
    }

    fun joinTeam(team: Team) {
        if (isGuest) {
            _actionMessage.value = guestRequiredMessage()
            return
        }
        if (team.id.startsWith("seed_")) {
            _actionMessage.value = if (_language.value == AppLanguage.AR) "هذا فريق مرجعي نموذجي — أنشئ فريقًا حقيقيًا أو انضم لفريق نشط" else "Demo reference team — create or join a live Firestore team"
            return
        }
        val memberName = (profileState.value as? UiState.Success)?.data?.displayName?.ifBlank { userDisplayName } ?: userDisplayName
        viewModelScope.launch {
            val res = repo.joinTeam(team, currentUserId, memberName)
            _actionMessage.value = if (res.isSuccess) {
                if (_language.value == AppLanguage.AR) "تم الانضمام إلى ${team.name}" else "Joined ${team.name}!"
            } else {
                localizedSaveError(res.exceptionOrNull())
            }
        }
    }

    fun createTeamTask(team: Team, title: String, assigneeName: String, role: String) {
        if (isGuest || team.id.startsWith("seed_")) {
            _actionMessage.value = guestRequiredMessage()
            return
        }
        if (title.isBlank()) return
        viewModelScope.launch {
            val res = repo.createTeamTask(team, title, assigneeName, role)
            if (res.isFailure) _actionMessage.value = localizedSaveError(res.exceptionOrNull())
        }
    }

    fun advanceTeamTaskStatus(teamId: String, task: TeamTask) {
        if (isGuest || teamId.startsWith("seed_")) {
            _actionMessage.value = guestRequiredMessage()
            return
        }
        val next = when (task.status) {
            "TODO" -> "IN_PROGRESS"
            "IN_PROGRESS" -> "DONE"
            else -> "TODO"
        }
        viewModelScope.launch {
            val res = repo.updateTeamTaskStatus(teamId, task.id, next)
            if (res.isFailure) _actionMessage.value = localizedSaveError(res.exceptionOrNull())
        }
    }

    fun submitReport(targetType: String, targetId: String, reason: String) {
        if (isGuest) {
            _actionMessage.value = guestRequiredMessage()
            return
        }
        if (reason.trim().length < 3) return
        viewModelScope.launch {
            val res = repo.submitReport(currentUserId, targetType, targetId, reason)
            _actionMessage.value = if (res.isSuccess) {
                if (_language.value == AppLanguage.AR) "تم استلام البلاغ لمراجعة المشرفين" else "Report submitted to moderation queue"
            } else {
                localizedSaveError(res.exceptionOrNull())
            }
        }
    }

    // --- Contextual AI Agent Lifecycle ---

    fun openAiAgent(
        mode: AiAgentContextMode,
        screenLabel: String,
        contextSummary: String,
        activeTeam: Team? = null
    ) {
        val current = _aiAgentState.value
        val shouldResetHistory = current.mode != mode || current.screenLabel != screenLabel
        _aiAgentState.value = current.copy(
            isOpen = true,
            mode = mode,
            screenLabel = screenLabel,
            contextSummary = contextSummary,
            activeTeam = activeTeam,
            errorMessage = null,
            messages = if (shouldResetHistory) emptyList() else current.messages
        )
    }

    fun closeAiAgent() {
        _aiAgentState.value = _aiAgentState.value.copy(isOpen = false, pendingActionConfirmation = null)
    }

    fun sendAgentPrompt(prompt: String) {
        val trimmed = prompt.trim()
        if (trimmed.isEmpty()) return
        val state = _aiAgentState.value
        val isArabic = _language.value == AppLanguage.AR

        if (isOffline.value) {
            _aiAgentState.value = state.copy(
                lastPrompt = trimmed,
                errorMessage = if (isArabic) "لا يوجد اتصال بالإنترنت. تحقق من الشبكة ثم اضغط إعادة المحاولة." else "Offline: AI Agent requires internet access. Check connection and tap Retry."
            )
            return
        }

        val allowPersonalization = _aiPersonalizationEnabled.value
        val allowContextSharing = _aiContextSharingEnabled.value
        val dataUsedLabel = if (allowPersonalization && allowContextSharing) {
            if (isArabic) "بيانات حسابك وسياق ${state.screenLabel}" else "Your profile + ${state.screenLabel} context"
        } else {
            if (isArabic) "معرفة عامة فقط (مشاركة السياق متوقفة)" else "General guidelines only (context sharing off)"
        }

        val userMsg = AiAgentMessage(
            id = "u_${UUID.randomUUID().toString().take(8)}",
            sender = "USER",
            content = trimmed
        )
        val updatedMessages = state.messages + userMsg
        _aiAgentState.value = state.copy(
            messages = updatedMessages,
            isStreaming = true,
            streamingText = "",
            errorMessage = null,
            lastPrompt = trimmed
        )

        viewModelScope.launch {
            var latest = ""
            try {
                val groundedSummary = buildGroundedAgentContext(state, allowPersonalization, allowContextSharing)
                aiRepo.streamContextualAgent(
                    mode = state.mode,
                    isArabic = isArabic,
                    contextSummary = groundedSummary,
                    allowPersonalization = allowPersonalization,
                    allowContextSharing = allowContextSharing,
                    history = updatedMessages,
                    userPrompt = trimmed
                ).collect { partial ->
                    latest = partial
                    _aiAgentState.value = _aiAgentState.value.copy(streamingText = partial)
                }

                val proposedAction = detectOptionalConfirmableAction(state, trimmed, isArabic)
                val aiMsg = AiAgentMessage(
                    id = "ai_${UUID.randomUUID().toString().take(8)}",
                    sender = "AI",
                    content = latest,
                    dataUsedLabel = dataUsedLabel,
                    boundariesLabel = state.mode.boundaries.get(_language.value),
                    pendingAction = proposedAction
                )
                _aiAgentState.value = _aiAgentState.value.copy(
                    messages = _aiAgentState.value.messages + aiMsg,
                    isStreaming = false,
                    streamingText = ""
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _aiAgentState.value = _aiAgentState.value.copy(
                    isStreaming = false,
                    streamingText = "",
                    errorMessage = e.localizedMessage ?: if (isArabic) "تعذّر الوصول لخدمة الذكاء الاصطناعي." else "AI Agent could not respond right now."
                )
            }
        }
    }

    fun retryLastAgentPrompt() {
        val last = _aiAgentState.value.lastPrompt
        if (last.isNotBlank()) {
            sendAgentPrompt(last)
        }
    }

    fun rateAgentMessage(messageId: String, positive: Boolean) {
        val updated = _aiAgentState.value.messages.map { msg ->
            if (msg.id == messageId) msg.copy(feedbackPositive = positive) else msg
        }
        _aiAgentState.value = _aiAgentState.value.copy(messages = updated)
        _actionMessage.value = if (_language.value == AppLanguage.AR) "شكرًا لتقييمك دقة المساعد الذكي" else "Feedback recorded"
    }

    fun reportAiResponse(messageId: String, reason: String) {
        val updated = _aiAgentState.value.messages.map { msg ->
            if (msg.id == messageId) msg.copy(isReported = true) else msg
        }
        _aiAgentState.value = _aiAgentState.value.copy(messages = updated)
        if (isGuest) {
            _actionMessage.value = guestRequiredMessage()
            return
        }
        submitReport("AI_RESPONSE", messageId, reason.ifBlank { "Inaccurate or off-boundary AI response" })
    }

    fun requestAgentActionConfirmation(action: AiAgentPendingAction) {
        _aiAgentState.value = _aiAgentState.value.copy(pendingActionConfirmation = action)
    }

    fun dismissPendingAgentAction() {
        _aiAgentState.value = _aiAgentState.value.copy(pendingActionConfirmation = null)
    }

    fun confirmPendingAgentAction() {
        val action = _aiAgentState.value.pendingActionConfirmation ?: return
        _aiAgentState.value = _aiAgentState.value.copy(pendingActionConfirmation = null)
        if (isGuest) {
            _actionMessage.value = guestRequiredMessage()
            return
        }
        when (action) {
            is AiAgentPendingAction.AddSprintTasks -> {
                if (action.team.id.startsWith("seed_")) {
                    _actionMessage.value = if (_language.value == AppLanguage.AR) "أنشئ فريقًا حقيقيًا لإضافة المهام في Firestore" else "Create a live team to persist sprint tasks"
                    return
                }
                val assignee = (profileState.value as? UiState.Success)?.data?.displayName?.ifBlank { userDisplayName } ?: userDisplayName
                viewModelScope.launch {
                    var okCount = 0
                    action.taskTitles.forEach { title ->
                        val res = repo.createTeamTask(action.team, title, assignee, action.role)
                        if (res.isSuccess) okCount++
                    }
                    if (okCount > 0) {
                        repo.appendAuditLog(currentUserId, "AI_ACTION_CONFIRMED", "Confirmed $okCount AI sprint tasks for ${action.team.name}", _roleMode.value.id)
                    }
                    _actionMessage.value = if (_language.value == AppLanguage.AR) "تمت إضافة $okCount مهام للفريق" else "Added $okCount sprint tasks"
                }
            }
            is AiAgentPendingAction.UpdateTargetSkills -> {
                val existing = (profileState.value as? UiState.Success)?.data ?: return
                val merged = (existing.targetSkills + action.skillsToAdd).distinct().take(30)
                viewModelScope.launch {
                    val res = repo.updateUserPreferences(currentUserId, mapOf("targetSkills" to merged))
                    _actionMessage.value = if (res.isSuccess) {
                        if (_language.value == AppLanguage.AR) "تم تحديث مهاراتك المستهدفة" else "Target skills updated"
                    } else localizedSaveError(res.exceptionOrNull())
                }
            }
            is AiAgentPendingAction.DraftCommunityQuestion -> {
                createCommunityPost("Technical Question", action.title, action.body, action.tags)
            }
        }
    }

    private fun buildGroundedAgentContext(
        state: AiAgentUiState,
        allowPersonalization: Boolean,
        allowContextSharing: Boolean
    ): String {
        if (!allowPersonalization || !allowContextSharing) return ""
        val prof = (profileState.value as? UiState.Success)?.data
        val realSubs = (submissionsState.value as? UiState.Success)?.data.orEmpty().filter { !it.isDemoSample }
        val unlockedCount = badgeProgressList.value.count { it.isUnlocked }
        return buildString {
            appendLine("Screen Context: ${state.screenLabel}")
            appendLine(state.contextSummary)
            if (prof != null) {
                appendLine("User Level: ${prof.careerLevel}, Points: ${prof.points}, Domain: ${prof.domain}")
                if (prof.currentSkills.isNotEmpty()) appendLine("User Skills: ${prof.currentSkills.joinToString(", ")}")
            }
            appendLine("Verified Real Submissions: ${realSubs.count { it.status == "VERIFIED" }}, Unlocked Badges: $unlockedCount/9")
        }
    }

    private fun detectOptionalConfirmableAction(
        state: AiAgentUiState,
        prompt: String,
        isArabic: Boolean
    ): AiAgentPendingAction? {
        return when (state.mode) {
            AiAgentContextMode.TEAM_SPRINT_PLANNER -> {
                val team = state.activeTeam ?: return null
                if (team.id.startsWith("seed_")) return null
                val tasks = if (isArabic) {
                    listOf("توثيق عقد الـ API والمدخلات", "كتابة اختبارات الحدود والأخطاء", "مراجعة معايير التحقق قبل التسليم")
                } else {
                    listOf("Define API & schema contract", "Add boundary & failure unit tests", "Run rubric readiness checklist")
                }
                AiAgentPendingAction.AddSprintTasks(
                    team = team,
                    taskTitles = tasks,
                    role = "Contributor",
                    description = LocalizedText(
                        "Add 3 sprint tasks to ${team.name}?",
                        "هل تريد إضافة ٣ مهام مقترحة إلى لوحة فريق ${team.name}؟"
                    )
                )
            }
            AiAgentContextMode.SKILL_ADVISOR -> {
                if (prompt.contains("skill", ignoreCase = true) || prompt.contains("مهار", ignoreCase = true)) {
                    AiAgentPendingAction.UpdateTargetSkills(
                        skillsToAdd = listOf("Automated Testing", "System Observability"),
                        description = LocalizedText(
                            "Add Automated Testing & System Observability to your target skills?",
                            "هل تريد إضافة الاختبارات الآلية ومراقبة الأنظمة إلى مهاراتك المستهدفة؟"
                        )
                    )
                } else null
            }
            else -> null
        }
    }

    fun deleteAccountAndData(onDeleted: () -> Unit) {
        if (isGuest) {
            viewModelScope.launch {
                prefsStore.clearSessionScopedStatePreserveLanguage()
                onDeleted()
            }
            return
        }
        viewModelScope.launch {
            val res = repo.deleteUserAccountData(currentUserId)
            if (res.isSuccess) {
                prefsStore.clearSessionScopedStatePreserveLanguage()
                onDeleted()
            } else {
                _actionMessage.value = localizedSaveError(res.exceptionOrNull())
            }
        }
    }

    fun runWorkSimulation(challengeId: String, challengeTitle: String, userDecision: String) {
        if (userDecision.isBlank()) return
        if (isOffline.value) {
            _workSimState.value = WorkSimState.Error(
                if (_language.value == AppLanguage.AR) "المحاكاة تتطلب اتصالًا بالإنترنت." else "Work simulation requires an active connection."
            )
            return
        }
        viewModelScope.launch {
            _workSimState.value = WorkSimState.Streaming(challengeId, "")
            var latest = ""
            try {
                aiRepo.streamWorkSimulationReply(
                    challengeTitle = challengeTitle,
                    userDecision = userDecision,
                    isArabic = _language.value == AppLanguage.AR
                ).collect { partial ->
                    latest = partial
                    _workSimState.value = WorkSimState.Streaming(challengeId, partial)
                }
                _workSimState.value = WorkSimState.Success(challengeId, latest)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _workSimState.value = WorkSimState.Error(e.localizedMessage ?: "Work simulation failed.")
            }
        }
    }

    fun selectActiveGoal(goalId: String) {
        if (isGuest) return
        val allIds = (goalsState.value as? UiState.Success)?.data?.map { it.id }.orEmpty()
        if (allIds.isEmpty()) return
        viewModelScope.launch {
            repo.setActiveGoal(currentUserId, goalId, allIds)
        }
    }

    fun toggleMilestone(milestone: RoadmapMilestone) {
        if (isGuest) {
            _actionMessage.value = guestRequiredMessage()
            return
        }
        val newCompleted = !milestone.completed
        val allMilestones = (milestonesState.value as? UiState.Success)?.data.orEmpty()
            .filter { it.goalId == milestone.goalId }
        val activeGoal = (goalsState.value as? UiState.Success)?.data?.firstOrNull { it.id == milestone.goalId }
        val updatedScore = if (activeGoal != null && allMilestones.isNotEmpty()) {
            val completedCount = allMilestones.count { if (it.id == milestone.id) newCompleted else it.completed }
            val progressBoost = ((completedCount.toFloat() / allMilestones.size) * 24f).toInt()
            val baseScore = activeGoal.readinessScore.toInt().coerceIn(20, 95)
            val delta = if (newCompleted) (100 - baseScore).coerceAtMost(maxOf(4, progressBoost / allMilestones.size.coerceAtLeast(1))) else -4
            (baseScore + delta).coerceIn(15, 100)
        } else null

        viewModelScope.launch {
            val res = repo.toggleMilestoneCompleted(currentUserId, milestone, newCompleted, updatedScore)
            if (res.isFailure) _actionMessage.value = localizedSaveError(res.exceptionOrNull())
        }
    }

    fun requestMilestoneTutor(targetRole: String, milestone: RoadmapMilestone) {
        if (isOffline.value) {
            _tutorState.value = TutorUiState.Error(milestone.id, "Offline: connect to internet and retry.")
            return
        }
        viewModelScope.launch {
            _tutorState.value = TutorUiState.Loading(milestone.id)
            var latest = ""
            try {
                aiRepo.streamMilestoneTutor(targetRole, milestone, isArabic = _language.value == AppLanguage.AR).collect { partial ->
                    latest = partial
                    _tutorState.value = TutorUiState.Streaming(milestone.id, partial)
                }
                _tutorState.value = TutorUiState.Success(milestone.id, latest)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _tutorState.value = TutorUiState.Error(milestone.id, e.localizedMessage ?: "Tutor failed")
            }
        }
    }

    fun dismissTutor() {
        _tutorState.value = TutorUiState.Idle
    }

    fun sendCoachMessage(
        mode: String,
        prompt: String,
        activeGoal: CareerGoal?,
        topGaps: List<SkillGap>
    ) {
        if (isGuest) {
            _coachStreamState.value = CoachStreamState.Error(guestRequiredMessage())
            return
        }
        val trimmed = prompt.trim()
        if (trimmed.isEmpty()) return
        if (isOffline.value) {
            _coachStreamState.value = CoachStreamState.Error(
                if (_language.value == AppLanguage.AR) "المدرب الذكي يتطلب اتصالًا بالإنترنت." else "Offline: connect to internet and retry."
            )
            return
        }

        val goalId = activeGoal?.id?.ifBlank { "general" } ?: "general"
        val history = (coachMessagesState.value as? UiState.Success)?.data.orEmpty().filter { it.mode == mode }

        viewModelScope.launch {
            val saveUser = repo.saveCoachMessage(currentUserId, goalId, mode, "USER", trimmed)
            if (saveUser.isFailure) {
                _coachStreamState.value = CoachStreamState.Error(saveUser.exceptionOrNull()?.localizedMessage ?: "Failed to send")
                return@launch
            }
            _coachStreamState.value = CoachStreamState.Streaming(mode, "")
            var latestReply = ""
            try {
                aiRepo.streamCoachResponse(
                    mode = mode,
                    targetRole = activeGoal?.targetRole.orEmpty(),
                    matchedSkills = activeGoal?.matchedSkills.orEmpty(),
                    topGaps = topGaps.map { "${it.skillName} (${it.priority})" },
                    history = history,
                    userPrompt = trimmed
                ).collect { partial ->
                    latestReply = partial
                    _coachStreamState.value = CoachStreamState.Streaming(mode, partial)
                }
                if (latestReply.isNotBlank()) {
                    repo.saveCoachMessage(currentUserId, goalId, mode, "AI", latestReply)
                }
                _coachStreamState.value = CoachStreamState.Idle
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _coachStreamState.value = CoachStreamState.Error(e.localizedMessage ?: "Coach failed to respond.")
            }
        }
    }

    fun clearCoachHistory(mode: String) {
        if (isGuest) return
        val ids = (coachMessagesState.value as? UiState.Success)?.data.orEmpty().filter { it.mode == mode }.map { it.id }
        viewModelScope.launch { repo.clearCoachMessages(currentUserId, ids) }
    }

    private fun guestRequiredMessage(): String = if (_language.value == AppLanguage.AR) {
        "هذا الإجراء يتطلب تسجيل الدخول بحساب Google لحفظ بياناتك."
    } else {
        "Sign in with Google to save progress, submit work, or join teams."
    }

    private fun localizedSaveError(err: Throwable?): String {
        val base = err?.localizedMessage
        return if (_language.value == AppLanguage.AR) {
            "تعذّر الحفظ في السحابة${if (!base.isNullOrBlank()) ": $base" else ". تحقق من الاتصال."}"
        } else {
            "Cloud save failed${if (!base.isNullOrBlank()) ": $base" else ". Check connection."}"
        }
    }

    private suspend fun unwrapFirestoreError(error: Throwable, label: String): String {
        val firestoreError = generateSequence(error) { it.cause }.filterIsInstance<FirebaseFirestoreException>().firstOrNull()
        if (firestoreError == null && error is CancellationException) throw error
        val root = firestoreError ?: error
        Log.w(TAG, "Error observing $label", root)
        return root.message ?: "Failed to load $label"
    }

    private companion object {
        const val TAG = "SkillBridgeVM"
        const val STOP_TIMEOUT_MILLIS = 5000L
    }
}
