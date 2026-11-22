package com.example.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isAltPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.R
import com.example.core.AppLanguage
import com.example.core.AppRoleMode
import com.example.core.ArStrings
import com.example.core.EnStrings
import com.example.core.LocalAppLanguage
import com.example.core.LocalAppStrings
import com.example.core.NetworkMonitor
import com.example.core.PersistedAppPreferences
import com.example.core.UserPreferencesStore
import com.example.core.attemptAutoSignIn
import com.example.core.authStateFlow
import com.example.core.layoutDirection
import com.example.core.signOut
import com.example.data.local.AccountRole
import com.example.data.local.SessionPrincipal
import com.example.data.model.CommunityPost
import com.example.data.model.Team
import com.example.data.repository.AiCareerRepository
import com.example.data.repository.SkillBridgeBackendRepository
import com.example.data.repository.SkillBridgeRepository
import com.example.data.seed.SkillBridgeEcosystemData
import com.example.ui.components.ActiveSessionsDialog
import com.example.ui.components.DesktopNavItem
import com.example.ui.components.DesktopPermanentSidebar
import com.example.ui.components.DesktopWorkspaceTopBar
import com.example.ui.components.DeviceFormFactor
import com.example.ui.components.OfflineStatusBanner
import com.example.ui.components.SkillBridgeAiAgentSheet
import com.example.ui.components.SkillBridgeLogo
import com.example.ui.components.resolveDeviceFormFactor
import com.example.ui.screens.AccountSettingsScreen
import com.example.ui.screens.BadgesGalleryScreen
import com.example.ui.screens.ChallengeDetailScreen
import com.example.ui.screens.CommunityFeedScreen
import com.example.ui.screens.ContestsScreen
import com.example.ui.screens.DiscoverHubScreen
import com.example.ui.screens.ExperiencePassportScreen
import com.example.ui.screens.GlobalSearchScreen
import com.example.ui.screens.HomeDashboardScreen
import com.example.ui.screens.LeaderboardScreen
import com.example.ui.screens.NotificationsScreen
import com.example.ui.screens.OnboardingWizardScreen
import com.example.ui.screens.PointsWalletScreen
import com.example.ui.screens.PostDetailScreen
import com.example.ui.screens.PrivacyStatesAndBlueprintScreen
import com.example.ui.screens.ReverseEngineeringHubScreen
import com.example.ui.screens.RoleNotificationsDialog
import com.example.ui.screens.SkillAnalysisScreen
import com.example.ui.screens.StudentAiCoachOnlyScreen
import com.example.ui.screens.StudentBriefsWorkspaceScreen
import com.example.ui.screens.StudentDbOverviewSection
import com.example.ui.screens.TeamWorkspaceScreen
import com.example.ui.screens.TeamsHubScreen
import com.example.ui.screens.WelcomeScreen
import com.example.ui.theme.SkillBridgeColors
import com.example.ui.theme.SkillBridgeTheme
import com.example.ui.viewmodel.PreviewStateMode
import com.example.ui.viewmodel.RolePortalViewModel
import com.example.ui.viewmodel.SkillBridgeViewModel
import com.example.ui.viewmodel.UiState
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

const val ROUTE_HOME = "home"
const val ROUTE_DISCOVER = "discover"
const val ROUTE_COMMUNITY = "community"
const val ROUTE_TEAMS = "teams"
const val ROUTE_PASSPORT = "passport"

sealed interface SubRoute {
    data object None : SubRoute
    data object Onboarding : SubRoute
    data object SkillAnalysis : SubRoute
    data object Badges : SubRoute
    data class ChallengeDetail(val challengeId: String) : SubRoute
    data object Contests : SubRoute
    data object ReverseEng : SubRoute
    data class PostDetail(val post: CommunityPost) : SubRoute
    data class TeamWorkspace(val team: Team) : SubRoute
    data object Leaderboards : SubRoute
    data object Wallet : SubRoute
    data object Search : SubRoute
    data object Notifications : SubRoute
    data object StudentBriefs : SubRoute
    data object StudentAiCoach : SubRoute
    data object PrivacyStatesBlueprint : SubRoute
    data object AccountSettings : SubRoute
}

private data class NavTab(
    val route: String,
    val labelEn: String,
    val labelAr: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

private val NAV_TABS = listOf(
    NavTab(ROUTE_HOME, "Home", "الرئيسية", Icons.Filled.Home, Icons.Outlined.Home, "nav_home"),
    NavTab(ROUTE_DISCOVER, "Discover", "استكشف", Icons.Filled.Explore, Icons.Outlined.Explore, "nav_discover"),
    NavTab(ROUTE_COMMUNITY, "Community", "المجتمع", Icons.Filled.Forum, Icons.Outlined.Forum, "nav_community"),
    NavTab(ROUTE_TEAMS, "Teams", "الفرق", Icons.Filled.Groups, Icons.Outlined.Groups, "nav_teams"),
    NavTab(ROUTE_PASSPORT, "Passport", "السجل", Icons.Filled.Badge, Icons.Outlined.Badge, "nav_passport")
)

@Composable
fun AppNavigation(
    auth: FirebaseAuth = Firebase.auth,
    backendRepoOverride: SkillBridgeBackendRepository? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }
    val prefsStore = remember(context) { UserPreferencesStore(context.applicationContext) }
    val networkMonitor = remember(context) { NetworkMonitor(context.applicationContext) }
    val backendRepo = remember(context, backendRepoOverride) {
        backendRepoOverride ?: SkillBridgeBackendRepository(context.applicationContext)
    }

    val persistedPrefs by prefsStore.preferencesFlow.collectAsStateWithLifecycle(initialValue = PersistedAppPreferences())
    val currentUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)
    val isOffline by networkMonitor.isOffline.collectAsStateWithLifecycle()

    var activeSession by remember { mutableStateOf<SessionPrincipal?>(null) }

    LaunchedEffect(Unit) {
        backendRepo.ensureSeeded()
        if (auth.currentUser == null && persistedPrefs.activeAccountId.isBlank()) {
            attemptAutoSignIn(
                context = context,
                credentialManager = credentialManager,
                onAuthSuccess = {},
                onUnauthenticated = {},
                scope = scope
            )
        }
    }

    // Restore persisted credential session or link Firebase Google user
    LaunchedEffect(persistedPrefs.activeAccountId, currentUser?.uid) {
        val fbUser = currentUser
        val savedId = persistedPrefs.activeAccountId
        activeSession = when {
            savedId.isNotBlank() -> backendRepo.getSessionByAccountId(savedId, isGoogleLinked = fbUser != null)
            fbUser != null -> backendRepo.resolveOrCreateGoogleStudentSession(
                firebaseUid = fbUser.uid,
                displayName = fbUser.displayName.orEmpty(),
                email = fbUser.email.orEmpty()
            ).getOrNull()
            else -> null
        }
    }

    val unauthLanguage = persistedPrefs.language
    val strings = if (unauthLanguage == AppLanguage.AR) ArStrings else EnStrings
    val session = activeSession

    val handleSignOut: () -> Unit = {
        scope.launch {
            activeSession = null
            prefsStore.clearSessionScopedStatePreserveLanguage()
            if (currentUser != null) {
                signOut(credentialManager = credentialManager, scope = scope)
            }
        }
    }

    if (session == null) {
        CompositionLocalProvider(
            LocalLayoutDirection provides unauthLanguage.layoutDirection(),
            LocalAppLanguage provides unauthLanguage,
            LocalAppStrings provides strings
        ) {
            SkillBridgeTheme(darkTheme = persistedPrefs.darkTheme, isArabic = unauthLanguage == AppLanguage.AR) {
                WelcomeScreen(
                    onToggleLanguage = {
                        val next = if (unauthLanguage == AppLanguage.AR) AppLanguage.EN else AppLanguage.AR
                        scope.launch { prefsStore.setLanguage(next) }
                    },
                    onLoginWithCredentials = { loginId, password ->
                        val res = backendRepo.authenticateWithCredentials(loginId, password)
                        res.onSuccess { principal ->
                            prefsStore.setActiveAccountId(principal.accountId)
                            activeSession = principal
                        }
                        res.map { }
                    },
                    onRegisterStudent = { loginId, displayName, email, password ->
                        val res = backendRepo.registerStudentAccount(loginId, displayName, email, password)
                        res.onSuccess { principal ->
                            prefsStore.setActiveAccountId(principal.accountId)
                            activeSession = principal
                        }
                        res.map { }
                    },
                    onGoogleAuthSuccess = {
                        scope.launch {
                            val fb = auth.currentUser
                            if (fb != null) {
                                backendRepo.resolveOrCreateGoogleStudentSession(
                                    firebaseUid = fb.uid,
                                    displayName = fb.displayName.orEmpty(),
                                    email = fb.email.orEmpty()
                                ).onSuccess { principal ->
                                    prefsStore.setActiveAccountId(principal.accountId)
                                    activeSession = principal
                                }
                            }
                        }
                    }
                )
            }
        }
    } else {
        key(session.accountId) {
            val roleVm: RolePortalViewModel = viewModel(
                key = "role_portal_${session.accountId}",
                factory = viewModelFactory {
                    initializer {
                        RolePortalViewModel(backend = backendRepo, session = session)
                    }
                }
            )
            val appLang = persistedPrefs.language
            val appStrings = if (appLang == AppLanguage.AR) ArStrings else EnStrings

            CompositionLocalProvider(
                LocalLayoutDirection provides appLang.layoutDirection(),
                LocalAppLanguage provides appLang,
                LocalAppStrings provides appStrings
            ) {
                SkillBridgeTheme(darkTheme = persistedPrefs.darkTheme, isArabic = appLang == AppLanguage.AR) {
                    when (session.role) {
                        AccountRole.PLATFORM_ADMIN,
                        AccountRole.COMPANY,
                        AccountRole.COMPANY_DIRECTOR -> {
                            IsolatedRolePortalScaffold(
                                session = session,
                                roleVm = roleVm,
                                darkTheme = persistedPrefs.darkTheme,
                                isOffline = isOffline,
                                onToggleLanguage = {
                                    val next = if (appLang == AppLanguage.AR) AppLanguage.EN else AppLanguage.AR
                                    scope.launch { prefsStore.setLanguage(next) }
                                },
                                onToggleDarkTheme = {
                                    scope.launch { prefsStore.setDarkTheme(!persistedPrefs.darkTheme) }
                                },
                                onSignOut = handleSignOut
                            )
                        }
                        AccountRole.STUDENT -> {
                            StudentRoleShell(
                                session = session,
                                roleVm = roleVm,
                                firebaseUser = currentUser,
                                prefsStore = prefsStore,
                                networkMonitor = networkMonitor,
                                onSignOut = handleSignOut
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StudentRoleShell(
    session: SessionPrincipal,
    roleVm: RolePortalViewModel,
    firebaseUser: FirebaseUser?,
    prefsStore: UserPreferencesStore,
    networkMonitor: NetworkMonitor,
    onSignOut: () -> Unit
) {
    val studentVm: SkillBridgeViewModel = viewModel(
        key = "student_shell_${session.accountId}",
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) { "APPLICATION_KEY missing" }
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                SkillBridgeViewModel(
                    repo = SkillBridgeRepository(db),
                    aiRepo = AiCareerRepository(),
                    prefsStore = prefsStore,
                    connectivityObserver = networkMonitor,
                    currentUserId = session.accountId,
                    userDisplayName = session.displayName,
                    isGuest = false
                )
            }
        }
    )

    val language by studentVm.language.collectAsStateWithLifecycle()
    val darkTheme by studentVm.darkTheme.collectAsStateWithLifecycle()
    val previewStateMode by studentVm.previewStateMode.collectAsStateWithLifecycle()
    val isOffline by studentVm.isOffline.collectAsStateWithLifecycle()
    val savedLastTab by studentVm.lastTab.collectAsStateWithLifecycle()

    val profileState by studentVm.profileState.collectAsStateWithLifecycle()
    val goalsState by studentVm.goalsState.collectAsStateWithLifecycle()
    val gapsState by studentVm.gapsState.collectAsStateWithLifecycle()
    val milestonesState by studentVm.milestonesState.collectAsStateWithLifecycle()
    val coachMessagesState by studentVm.coachMessagesState.collectAsStateWithLifecycle()
    val submissionsState by studentVm.submissionsState.collectAsStateWithLifecycle()
    val badgeProgressList by studentVm.badgeProgressList.collectAsStateWithLifecycle()
    val pointLedgerState by studentVm.pointLedgerState.collectAsStateWithLifecycle()
    val auditLogsState by studentVm.auditLogsState.collectAsStateWithLifecycle()
    val communityPostsState by studentVm.communityPostsState.collectAsStateWithLifecycle()
    val recruitingTeamsState by studentVm.recruitingTeamsState.collectAsStateWithLifecycle()
    val myTeamsState by studentVm.myTeamsState.collectAsStateWithLifecycle()
    val selectedCommentsState by studentVm.selectedPostCommentsState.collectAsStateWithLifecycle()
    val selectedTeamTasksState by studentVm.selectedTeamTasksState.collectAsStateWithLifecycle()

    val analysisState by studentVm.analysisState.collectAsStateWithLifecycle()
    val latestAnalysis by studentVm.latestAnalysis.collectAsStateWithLifecycle()
    val coachStreamState by studentVm.coachStreamState.collectAsStateWithLifecycle()
    val workSimState by studentVm.workSimState.collectAsStateWithLifecycle()
    val aiAgentState by studentVm.aiAgentState.collectAsStateWithLifecycle()

    val savedChallengeIds by studentVm.savedChallengeIds.collectAsStateWithLifecycle()
    val savedPostIds by studentVm.savedPostIds.collectAsStateWithLifecycle()
    val upvotedPostIds by studentVm.upvotedPostIds.collectAsStateWithLifecycle()
    val followedTags by studentVm.followedTags.collectAsStateWithLifecycle()
    val mutedAuthors by studentVm.mutedAuthors.collectAsStateWithLifecycle()
    val aiPersonalization by studentVm.aiPersonalizationEnabled.collectAsStateWithLifecycle()
    val aiContextSharing by studentVm.aiContextSharingEnabled.collectAsStateWithLifecycle()
    val notificationChannels by studentVm.notificationChannels.collectAsStateWithLifecycle()
    val unreadNotificationIds by studentVm.unreadNotificationIds.collectAsStateWithLifecycle()
    val studentActionMessage by studentVm.actionMessage.collectAsStateWithLifecycle()

    // Relational DB role-scoped student flows
    val dbAccount by roleVm.currentAccount.collectAsStateWithLifecycle()
    val dbBriefs by roleVm.publishedBriefs.collectAsStateWithLifecycle()
    val dbApps by roleVm.studentApplications.collectAsStateWithLifecycle()
    val dbTasks by roleVm.studentTasks.collectAsStateWithLifecycle()
    val dbSubmissions by roleVm.studentSubmissions.collectAsStateWithLifecycle()
    val dbNotifications by roleVm.notifications.collectAsStateWithLifecycle()
    val roleActionMessage by roleVm.actionMessage.collectAsStateWithLifecycle()
    val syncState by roleVm.syncState.collectAsStateWithLifecycle()
    val lastSyncedEpochMs by roleVm.lastSyncedEpochMs.collectAsStateWithLifecycle()
    val pendingWritesCount by roleVm.pendingWritesCount.collectAsStateWithLifecycle()
    val deviceSessions by roleVm.deviceSessions.collectAsStateWithLifecycle()
    val currentSessionId by roleVm.currentDeviceSessionId.collectAsStateWithLifecycle()
    val isCurrentSessionRevoked by roleVm.isCurrentSessionRevoked.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(studentActionMessage) {
        val msg = studentActionMessage
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            studentVm.clearActionMessage()
        }
    }
    LaunchedEffect(roleActionMessage) {
        val msg = roleActionMessage
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            roleVm.clearMessage()
        }
    }
    LaunchedEffect(isCurrentSessionRevoked) {
        if (isCurrentSessionRevoked) {
            onSignOut()
        }
    }

    var currentTab by rememberSaveable { mutableStateOf(ROUTE_HOME) }
    var initializedTab by remember { mutableStateOf(false) }
    LaunchedEffect(savedLastTab) {
        if (!initializedTab && savedLastTab in setOf(ROUTE_HOME, ROUTE_DISCOVER, ROUTE_COMMUNITY, ROUTE_TEAMS, ROUTE_PASSPORT)) {
            currentTab = savedLastTab
            initializedTab = true
        }
    }

    var subRoute by remember { mutableStateOf<SubRoute>(SubRoute.None) }
    var showProfileMenu by remember { mutableStateOf(false) }
    var showDbNotificationsDialog by remember { mutableStateOf(false) }
    var showSessionsDialog by remember { mutableStateOf(false) }
    var showSignOutConfirm by remember { mutableStateOf(false) }

    val profileData = (profileState as? UiState.Success)?.data
    val displayName = dbAccount?.displayName?.ifBlank { session.displayName } ?: session.displayName
    val submissionsList = (submissionsState as? UiState.Success)?.data.orEmpty()
    val strings = LocalAppStrings.current
    val dbUnreadCount = remember(dbNotifications) { dbNotifications.count { !it.isRead } }
    val totalBadgeUnread = unreadNotificationIds.size + dbUnreadCount
    val activeSessionCount = remember(deviceSessions) {
        deviceSessions.count { !it.revoked }.coerceAtLeast(1)
    }

    val navigateToTab: (String) -> Unit = { tabRoute ->
        currentTab = tabRoute
        subRoute = SubRoute.None
        studentVm.setLastTab(tabRoute)
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .testTag("student_dashboard_root")
    ) {
        val formFactor = resolveDeviceFormFactor(maxWidth)
        val isDesktop = formFactor == DeviceFormFactor.EXPANDED_DESKTOP
        val useNavigationRail = formFactor == DeviceFormFactor.MEDIUM_TABLET

        LaunchedEffect(formFactor, maxWidth) {
            val type = if (isDesktop) "DESKTOP_LAPTOP" else "MOBILE"
            val label = if (isDesktop) {
                "Desktop / Laptop (${maxWidth.value.toInt()}dp)"
            } else {
                "Mobile Handheld (${maxWidth.value.toInt()}dp)"
            }
            roleVm.registerCurrentDeviceSession(
                deviceType = type,
                deviceLabel = label,
                windowWidthDp = maxWidth.value.toInt()
            )
        }

        val studentMainContent: @Composable (Boolean) -> Unit = { desktopMode ->
            when (val sr = subRoute) {
                SubRoute.None -> {
                    when (currentTab) {
                        ROUTE_HOME -> HomeDashboardScreen(
                            displayName = displayName,
                            profile = profileData,
                            goalsState = goalsState,
                            submissions = submissionsList,
                            badgeProgressList = badgeProgressList,
                            previewStateMode = previewStateMode,
                            isDesktop = desktopMode,
                            onRetry = { studentVm.setPreviewStateMode(PreviewStateMode.NORMAL) },
                            onOpenSkillAnalysis = { subRoute = SubRoute.SkillAnalysis },
                            onOpenBadges = { subRoute = SubRoute.Badges },
                            onOpenChallenge = { chId -> subRoute = SubRoute.ChallengeDetail(chId) },
                            onOpenContests = { subRoute = SubRoute.Contests },
                            onOpenReverseEng = { subRoute = SubRoute.ReverseEng },
                            onOpenLeaderboards = { subRoute = SubRoute.Leaderboards },
                            onOpenWallet = { subRoute = SubRoute.Wallet },
                            onOpenCompanies = { subRoute = SubRoute.StudentBriefs },
                            onOpenCoach = { subRoute = SubRoute.StudentAiCoach },
                            onOpenAiAgent = { mode, screen, ctx ->
                                studentVm.openAiAgent(mode, screen, ctx)
                            },
                            onOpenOnboarding = { subRoute = SubRoute.Onboarding },
                            onOpenBlueprint = { subRoute = SubRoute.PrivacyStatesBlueprint },
                            onOpenPassport = { navigateToTab(ROUTE_PASSPORT) },
                            dbOverviewContent = {
                                StudentDbOverviewSection(
                                    account = dbAccount,
                                    publishedBriefs = dbBriefs,
                                    applications = dbApps,
                                    tasks = dbTasks,
                                    submissions = dbSubmissions,
                                    isDesktop = desktopMode,
                                    onOpenBriefsWorkspace = { subRoute = SubRoute.StudentBriefs },
                                    onOpenPassport = { navigateToTab(ROUTE_PASSPORT) }
                                )
                            }
                        )
                        ROUTE_DISCOVER -> DiscoverHubScreen(
                            savedChallengeIds = savedChallengeIds,
                            onToggleSave = studentVm::toggleSavedChallenge,
                            onOpenChallenge = { chId -> subRoute = SubRoute.ChallengeDetail(chId) },
                            onOpenContests = { subRoute = SubRoute.Contests },
                            onOpenReverseEng = { subRoute = SubRoute.ReverseEng },
                            onOpenTeams = { navigateToTab(ROUTE_TEAMS) }
                        )
                        ROUTE_COMMUNITY -> CommunityFeedScreen(
                            postsState = communityPostsState,
                            upvotedPostIds = upvotedPostIds,
                            savedPostIds = savedPostIds,
                            followedTags = followedTags,
                            mutedAuthors = mutedAuthors,
                            onToggleUpvote = studentVm::toggleUpvotePost,
                            onToggleSavePost = studentVm::toggleSavedPost,
                            onToggleFollowTag = studentVm::toggleFollowTag,
                            onMuteAuthor = studentVm::muteAuthor,
                            onCreatePost = studentVm::createCommunityPost,
                            onReportPost = { postId, reason ->
                                roleVm.submitReport("POST", postId, reason)
                                studentVm.submitReport("POST", postId, reason)
                            },
                            onOpenAiAgent = { mode, screen, ctx ->
                                studentVm.openAiAgent(mode, screen, ctx)
                            },
                            onOpenPost = { post ->
                                studentVm.selectPostForComments(post.id)
                                subRoute = SubRoute.PostDetail(post)
                            }
                        )
                        ROUTE_TEAMS -> TeamsHubScreen(
                            currentUserId = studentVm.currentUserId,
                            myTeamsState = myTeamsState,
                            recruitingTeamsState = recruitingTeamsState,
                            onCreateTeam = studentVm::createTeam,
                            onJoinTeam = studentVm::joinTeam,
                            onOpenTeamWorkspace = { team ->
                                studentVm.selectTeamForTasks(team.id)
                                subRoute = SubRoute.TeamWorkspace(team)
                            }
                        )
                        ROUTE_PASSPORT -> ExperiencePassportScreen(
                            displayName = displayName,
                            profile = profileData,
                            submissionsState = submissionsState,
                            badgeProgressList = badgeProgressList,
                            auditLogsState = auditLogsState,
                            onToggleSubmissionVisibility = studentVm::toggleSubmissionVisibility,
                            onOpenBadges = { subRoute = SubRoute.Badges },
                            onOpenWallet = { subRoute = SubRoute.Wallet },
                            onOpenLeaderboards = { subRoute = SubRoute.Leaderboards },
                            onOpenPrivacyStates = { subRoute = SubRoute.PrivacyStatesBlueprint },
                            onOpenChallenge = { chId -> subRoute = SubRoute.ChallengeDetail(chId) },
                            onOpenAiAgent = { ctx ->
                                studentVm.openAiAgent(
                                    com.example.data.model.AiAgentContextMode.PASSPORT_REVIEWER,
                                    strings.passportTitle,
                                    ctx
                                )
                            },
                            onShareFeedbackMessage = studentVm::showMessage,
                            dbAccount = dbAccount,
                            dbSubmissions = dbSubmissions,
                            isDesktop = desktopMode
                        )
                    }
                }
                SubRoute.StudentBriefs -> StudentBriefsWorkspaceScreen(
                    briefs = dbBriefs,
                    applications = dbApps,
                    tasks = dbTasks,
                    submissions = dbSubmissions,
                    isDesktop = desktopMode,
                    onApplyToBrief = roleVm::studentApplyToBrief,
                    onUpdateTaskStatus = roleVm::studentUpdateTaskStatus,
                    onSubmitWork = roleVm::studentSubmitWork,
                    onBack = { subRoute = SubRoute.None }
                )
                SubRoute.StudentAiCoach -> StudentAiCoachOnlyScreen(
                    goalsState = goalsState,
                    gapsState = gapsState,
                    messagesState = coachMessagesState,
                    coachStreamState = coachStreamState,
                    onSendCoachPrompt = studentVm::sendCoachMessage,
                    onClearCoachHistory = studentVm::clearCoachHistory,
                    onBack = { subRoute = SubRoute.None }
                )
                SubRoute.Onboarding -> OnboardingWizardScreen(
                    initialName = displayName,
                    onComplete = { name, country, tz, dom, lvl, cur, tgt, goal, hrs ->
                        studentVm.completeOnboarding(name, country, tz, dom, lvl, cur, tgt, goal, hrs)
                        roleVm.studentUpdateProfile(name, dom, country, cur, tgt, goal, hrs)
                        subRoute = SubRoute.None
                    },
                    onBack = { subRoute = SubRoute.None }
                )
                SubRoute.SkillAnalysis -> SkillAnalysisScreen(
                    goalsState = goalsState,
                    gapsState = gapsState,
                    milestonesState = milestonesState,
                    analysisState = analysisState,
                    latestAnalysis = latestAnalysis,
                    onRunAnalysis = studentVm::analyzeAndSaveCareerBridge,
                    onToggleMilestone = studentVm::toggleMilestone,
                    onProveSkill = { chId -> subRoute = SubRoute.ChallengeDetail(chId) },
                    onOpenAiAgent = { ctx ->
                        studentVm.openAiAgent(
                            com.example.data.model.AiAgentContextMode.SKILL_ADVISOR,
                            strings.skillAnalysisTitle,
                            ctx
                        )
                    },
                    onBack = { subRoute = SubRoute.None }
                )
                SubRoute.Badges -> BadgesGalleryScreen(
                    badgeProgressList = badgeProgressList,
                    isGuest = false,
                    onToggleShareable = studentVm::toggleBadgeShareable,
                    onProveNextStep = { chId -> subRoute = SubRoute.ChallengeDetail(chId) },
                    onBack = { subRoute = SubRoute.None }
                )
                is SubRoute.ChallengeDetail -> {
                    val ch = SkillBridgeEcosystemData.challenges.firstOrNull { it.id == sr.challengeId }
                        ?: SkillBridgeEcosystemData.challenges.first()
                    ChallengeDetailScreen(
                        challenge = ch,
                        isSaved = ch.id in savedChallengeIds,
                        workSimState = workSimState,
                        containsSecret = studentVm::containsLeakedSecret,
                        onToggleSave = { studentVm.toggleSavedChallenge(ch.id) },
                        onRunWorkSim = studentVm::runWorkSimulation,
                        onSubmitSolution = { role, url, report, skills ->
                            studentVm.submitChallengeSolution(
                                challengeId = ch.id,
                                challengeTitle = ch.title.en,
                                companyName = ch.companyName,
                                category = if (ch.category.name == "REVERSE_ENG") "REVERSE_ENG" else "CHALLENGE",
                                roleInTeam = role,
                                skillsApplied = skills,
                                repoOrDemoUrl = url,
                                understandingReport = report,
                                pointsReward = ch.pointsReward,
                                rewardAmount = ch.prizePool
                            )
                        },
                        onOpenAiAgent = { mode, screen, ctx ->
                            studentVm.openAiAgent(mode, screen, ctx)
                        },
                        onOpenTeams = { navigateToTab(ROUTE_TEAMS) },
                        onBack = { subRoute = SubRoute.None }
                    )
                }
                SubRoute.Contests -> ContestsScreen(
                    onJoinTeamForContest = { navigateToTab(ROUTE_TEAMS) },
                    onBack = { subRoute = SubRoute.None }
                )
                SubRoute.ReverseEng -> ReverseEngineeringHubScreen(
                    containsSecret = studentVm::containsLeakedSecret,
                    onSubmitLab = { lab, role, url, report ->
                        studentVm.submitChallengeSolution(
                            challengeId = lab.id,
                            challengeTitle = lab.title.en,
                            companyName = lab.companyName,
                            category = "REVERSE_ENG",
                            roleInTeam = role,
                            skillsApplied = lab.skills,
                            repoOrDemoUrl = url,
                            understandingReport = report,
                            pointsReward = lab.pointsReward,
                            rewardAmount = lab.prizePool
                        )
                    },
                    onOpenAiAgent = { mode, screen, ctx ->
                        studentVm.openAiAgent(mode, screen, ctx)
                    },
                    onBack = { subRoute = SubRoute.None }
                )
                is SubRoute.PostDetail -> PostDetailScreen(
                    post = sr.post,
                    currentUserId = studentVm.currentUserId,
                    commentsState = selectedCommentsState,
                    onAddComment = { parentId, body ->
                        studentVm.addComment(sr.post, parentId, body)
                    },
                    onAcceptBestAnswer = { commentId ->
                        studentVm.acceptBestAnswer(sr.post.id, commentId)
                    },
                    onBack = { subRoute = SubRoute.None }
                )
                is SubRoute.TeamWorkspace -> TeamWorkspaceScreen(
                    team = sr.team,
                    tasksState = selectedTeamTasksState,
                    onCreateTask = { title, assignee, role ->
                        studentVm.createTeamTask(sr.team, title, assignee, role)
                    },
                    onAdvanceTask = { task ->
                        studentVm.advanceTeamTaskStatus(sr.team.id, task)
                    },
                    onOpenAiAgent = { team, ctx ->
                        studentVm.openAiAgent(
                            com.example.data.model.AiAgentContextMode.TEAM_SPRINT_PLANNER,
                            team.name,
                            ctx,
                            activeTeam = team
                        )
                    },
                    onBack = { subRoute = SubRoute.None }
                )
                SubRoute.Leaderboards -> LeaderboardScreen(
                    onBack = { subRoute = SubRoute.None }
                )
                SubRoute.Wallet -> PointsWalletScreen(
                    pointsBalance = maxOf(profileData?.points ?: 0L, (dbAccount?.points ?: 0).toLong()),
                    pointLedgerState = pointLedgerState,
                    auditLogsState = auditLogsState,
                    onBack = { subRoute = SubRoute.None }
                )
                SubRoute.Search -> GlobalSearchScreen(
                    onOpenChallenge = { chId -> subRoute = SubRoute.ChallengeDetail(chId) },
                    onOpenContests = { subRoute = SubRoute.Contests },
                    onOpenCompanies = { subRoute = SubRoute.StudentBriefs },
                    onBack = { subRoute = SubRoute.None }
                )
                SubRoute.Notifications -> NotificationsScreen(
                    unreadIds = unreadNotificationIds,
                    channels = notificationChannels,
                    onMarkAllRead = {
                        studentVm.markNotificationsRead()
                        roleVm.markAllNotificationsRead()
                    },
                    onToggleChannel = studentVm::toggleNotificationChannel,
                    onBack = { subRoute = SubRoute.None }
                )
                SubRoute.PrivacyStatesBlueprint -> PrivacyStatesAndBlueprintScreen(
                    profile = profileData,
                    darkTheme = darkTheme,
                    aiPersonalizationEnabled = aiPersonalization,
                    aiContextSharingEnabled = aiContextSharing,
                    previewStateMode = previewStateMode,
                    onToggleDarkTheme = studentVm::setDarkTheme,
                    onToggleAiPrefs = studentVm::updateAiPreferences,
                    onUpdatePrivacy = studentVm::updatePrivacySettings,
                    onSelectPreviewMode = studentVm::setPreviewStateMode,
                    onOpenAccountSettings = { subRoute = SubRoute.AccountSettings },
                    onSignOut = onSignOut,
                    onBack = { subRoute = SubRoute.None }
                )
                SubRoute.AccountSettings -> AccountSettingsScreen(
                    displayName = displayName,
                    email = firebaseUser?.email ?: session.email,
                    isGuest = false,
                    roleMode = AppRoleMode.LEARNER,
                    isOffline = isOffline,
                    aiPersonalizationEnabled = aiPersonalization,
                    aiContextSharingEnabled = aiContextSharing,
                    onToggleAiPrefs = studentVm::updateAiPreferences,
                    onOpenPrivacyStates = { subRoute = SubRoute.PrivacyStatesBlueprint },
                    onSignInFromGuest = {},
                    onSignOut = onSignOut,
                    onDeleteAccount = {
                        studentVm.deleteAccountAndData(onDeleted = onSignOut)
                    },
                    onBack = { subRoute = SubRoute.None }
                )
            }
        }

        if (isDesktop) {
            val primaryItems = remember(language) {
                NAV_TABS.mapIndexed { idx, tab ->
                    DesktopNavItem(
                        key = tab.route,
                        label = if (language == AppLanguage.AR) tab.labelAr else tab.labelEn,
                        icon = tab.selectedIcon,
                        testTag = tab.testTag,
                        shortcutHint = "Alt+${idx + 1}"
                    )
                }
            }
            val secondaryItems = remember(language, dbBriefs.size) {
                listOf(
                    DesktopNavItem(
                        key = "BRIEFS",
                        label = if (language == AppLanguage.AR) "مشاريع الشركات" else "Company Briefs",
                        icon = Icons.Filled.BusinessCenter,
                        testTag = "desktop_nav_briefs",
                        badgeCount = dbBriefs.size
                    ),
                    DesktopNavItem(
                        key = "COACH",
                        label = if (language == AppLanguage.AR) "المدرب الذكي AI" else "AI Career Coach",
                        icon = Icons.Filled.Psychology,
                        testTag = "desktop_nav_coach"
                    ),
                    DesktopNavItem(
                        key = "ONBOARDING",
                        label = if (language == AppLanguage.AR) "إعداد الملف الشخصي" else "Profile Setup",
                        icon = Icons.Filled.Person,
                        testTag = "desktop_nav_profile"
                    ),
                    DesktopNavItem(
                        key = "SETTINGS",
                        label = strings.accountSettingsTitle,
                        icon = Icons.Filled.Settings,
                        testTag = "desktop_nav_settings"
                    )
                )
            }
            val selectedSecondaryKey = when (subRoute) {
                SubRoute.StudentBriefs -> "BRIEFS"
                SubRoute.StudentAiCoach -> "COACH"
                SubRoute.Onboarding -> "ONBOARDING"
                SubRoute.AccountSettings, SubRoute.PrivacyStatesBlueprint -> "SETTINGS"
                else -> null
            }
            val activeSectionTitle = when (val sr = subRoute) {
                SubRoute.None -> NAV_TABS.firstOrNull { it.route == currentTab }?.let {
                    if (language == AppLanguage.AR) it.labelAr else it.labelEn
                } ?: strings.appName
                SubRoute.StudentBriefs -> if (language == AppLanguage.AR) "مشاريع الشركات المعتمدة" else "Company Project Briefs"
                SubRoute.StudentAiCoach -> if (language == AppLanguage.AR) "المدرب الذكي AI" else "AI Career Coach"
                SubRoute.SkillAnalysis -> strings.skillAnalysisTitle
                SubRoute.Badges -> strings.badgesTitle
                SubRoute.Search -> strings.searchPlaceholder
                SubRoute.Notifications -> strings.notificationsTitle
                SubRoute.AccountSettings -> strings.accountSettingsTitle
                is SubRoute.ChallengeDetail -> "Challenge Workspace"
                is SubRoute.TeamWorkspace -> sr.team.name
                is SubRoute.PostDetail -> sr.post.title
                else -> strings.appName
            }

            Scaffold(
                contentWindowInsets = WindowInsets.safeDrawing,
                snackbarHost = { SnackbarHost(snackbarHostState) },
                modifier = Modifier.onPreviewKeyEvent { event ->
                    if (event.type == KeyEventType.KeyDown) {
                        when {
                            event.key == Key.Escape && subRoute !is SubRoute.None -> {
                                subRoute = SubRoute.None
                                true
                            }
                            event.isAltPressed -> {
                                val idx = when (event.key) {
                                    Key.One -> 0
                                    Key.Two -> 1
                                    Key.Three -> 2
                                    Key.Four -> 3
                                    Key.Five -> 4
                                    else -> -1
                                }
                                if (idx in NAV_TABS.indices) {
                                    navigateToTab(NAV_TABS[idx].route)
                                    true
                                } else false
                            }
                            else -> false
                        }
                    } else false
                }
            ) { innerPadding ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    DesktopPermanentSidebar(
                        displayName = displayName,
                        loginId = session.loginId,
                        roleLabel = if (language == AppLanguage.AR) "طالب" else "Student",
                        roleIcon = Icons.Filled.School,
                        primaryItems = primaryItems,
                        selectedKey = currentTab,
                        onSelectPrimary = navigateToTab,
                        secondaryItems = secondaryItems,
                        selectedSecondaryKey = selectedSecondaryKey,
                        onSelectSecondary = { key ->
                            subRoute = when (key) {
                                "BRIEFS" -> SubRoute.StudentBriefs
                                "COACH" -> SubRoute.StudentAiCoach
                                "ONBOARDING" -> SubRoute.Onboarding
                                "SETTINGS" -> SubRoute.AccountSettings
                                else -> SubRoute.None
                            }
                        },
                        activeSessionCount = activeSessionCount,
                        darkTheme = darkTheme,
                        onOpenActiveSessions = { showSessionsDialog = true },
                        onToggleDarkTheme = { studentVm.setDarkTheme(!darkTheme) },
                        onToggleLanguage = studentVm::toggleLanguage,
                        onSignOut = { showSignOutConfirm = true }
                    )

                    Column(modifier = Modifier.weight(1f).fillMaxSize()) {
                        DesktopWorkspaceTopBar(
                            sectionTitle = activeSectionTitle,
                            subtitle = "${dbAccount?.careerLevel ?: "Junior"} • $displayName",
                            syncState = syncState,
                            lastSyncedEpochMs = lastSyncedEpochMs,
                            pendingWritesCount = pendingWritesCount,
                            isOffline = isOffline,
                            activeSessionCount = activeSessionCount,
                            unreadNotifications = totalBadgeUnread,
                            notificationsTestTag = "top_notifications_btn",
                            onManualSync = roleVm::triggerManualSync,
                            onOpenActiveSessions = { showSessionsDialog = true },
                            onOpenNotifications = {
                                if (dbNotifications.isNotEmpty()) showDbNotificationsDialog = true
                                else subRoute = SubRoute.Notifications
                            },
                            onOpenSearch = { subRoute = SubRoute.Search },
                            canGoBack = subRoute !is SubRoute.None,
                            onGoBack = { subRoute = SubRoute.None }
                        )
                        if (isOffline) {
                            OfflineStatusBanner()
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            studentMainContent(true)
                        }
                    }
                }
            }
        } else {
            Scaffold(
                contentWindowInsets = WindowInsets.safeDrawing,
                snackbarHost = { SnackbarHost(snackbarHostState) },
                topBar = {
                    TopAppBar(
                        navigationIcon = {
                            if (subRoute !is SubRoute.None) {
                                IconButton(
                                    onClick = { subRoute = SubRoute.None },
                                    modifier = Modifier.minimumInteractiveComponentSize().testTag("top_back_btn")
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.backLabel)
                                }
                            }
                        },
                        title = {
                            SkillBridgeLogo(
                                darkBackground = darkTheme,
                                compactOnly = useNavigationRail || subRoute !is SubRoute.None
                            )
                        },
                        actions = {
                            IconButton(
                                onClick = { subRoute = SubRoute.Search },
                                modifier = Modifier.minimumInteractiveComponentSize().testTag("top_search_btn")
                            ) {
                                Icon(Icons.Filled.Search, contentDescription = strings.searchPlaceholder)
                            }
                            IconButton(
                                onClick = { showSessionsDialog = true },
                                modifier = Modifier.minimumInteractiveComponentSize().testTag("active_sessions_btn")
                            ) {
                                BadgedBox(
                                    badge = {
                                        if (activeSessionCount > 1) {
                                            Badge { Text("$activeSessionCount") }
                                        }
                                    }
                                ) {
                                    Icon(Icons.Filled.Devices, contentDescription = "Connected devices")
                                }
                            }
                            IconButton(
                                onClick = {
                                    if (dbNotifications.isNotEmpty()) showDbNotificationsDialog = true
                                    else subRoute = SubRoute.Notifications
                                },
                                modifier = Modifier.minimumInteractiveComponentSize().testTag("top_notifications_btn")
                            ) {
                                BadgedBox(
                                    badge = {
                                        if (totalBadgeUnread > 0) {
                                            Badge { Text("$totalBadgeUnread") }
                                        }
                                    }
                                ) {
                                    Icon(Icons.Filled.Notifications, contentDescription = strings.notificationsTitle)
                                }
                            }
                            TextButton(
                                onClick = studentVm::toggleLanguage,
                                modifier = Modifier.testTag("top_language_toggle")
                            ) {
                                Text(
                                    strings.languageToggleLabel,
                                    fontWeight = FontWeight.Bold,
                                    color = SkillBridgeColors.BridgeBlueDark
                                )
                            }
                            // Student-only Account Menu (ZERO role switcher!)
                            Box {
                                IconButton(
                                    onClick = { showProfileMenu = true },
                                    modifier = Modifier.minimumInteractiveComponentSize().testTag("student_account_menu_btn")
                                ) {
                                    Icon(
                                        Icons.Filled.AccountCircle,
                                        contentDescription = strings.accountSettingsTitle,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                                DropdownMenu(
                                    expanded = showProfileMenu,
                                    onDismissRequest = { showProfileMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text(if (language == AppLanguage.AR) "مشاريع الشركات" else "Company Briefs") },
                                        onClick = {
                                            subRoute = SubRoute.StudentBriefs
                                            showProfileMenu = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text(strings.badgesTitle) },
                                        onClick = {
                                            subRoute = SubRoute.Badges
                                            showProfileMenu = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text(if (language == AppLanguage.AR) "إعداد الملف الشخصي" else "Profile Setup") },
                                        onClick = {
                                            subRoute = SubRoute.Onboarding
                                            showProfileMenu = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text(strings.accountSettingsTitle) },
                                        onClick = {
                                            subRoute = SubRoute.AccountSettings
                                            showProfileMenu = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text(strings.privacySettingsTitle) },
                                        onClick = {
                                            subRoute = SubRoute.PrivacyStatesBlueprint
                                            showProfileMenu = false
                                        }
                                    )
                                    HorizontalDivider()
                                    DropdownMenuItem(
                                        text = { Text(strings.signOut, color = SkillBridgeColors.DisputedRed) },
                                        onClick = {
                                            showProfileMenu = false
                                            onSignOut()
                                        },
                                        modifier = Modifier.testTag("student_menu_sign_out")
                                    )
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.background
                        )
                    )
                },
                bottomBar = {
                    if (!useNavigationRail && subRoute is SubRoute.None) {
                        NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                            NAV_TABS.forEach { tab ->
                                val selected = currentTab == tab.route
                                val label = if (language == AppLanguage.AR) tab.labelAr else tab.labelEn
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { navigateToTab(tab.route) },
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                            contentDescription = label
                                        )
                                    },
                                    label = { Text(label) },
                                    modifier = Modifier.testTag(tab.testTag)
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    if (useNavigationRail) {
                        NavigationRail(containerColor = MaterialTheme.colorScheme.surface) {
                            Spacer(Modifier.size(8.dp))
                            NAV_TABS.forEach { tab ->
                                val selected = currentTab == tab.route && subRoute is SubRoute.None
                                val label = if (language == AppLanguage.AR) tab.labelAr else tab.labelEn
                                NavigationRailItem(
                                    selected = selected,
                                    onClick = { navigateToTab(tab.route) },
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                            contentDescription = label
                                        )
                                    },
                                    label = { Text(label) },
                                    modifier = Modifier.testTag(tab.testTag)
                                )
                            }
                        }
                    }

                    Column(modifier = Modifier.weight(1f).fillMaxSize()) {
                        if (isOffline) {
                            OfflineStatusBanner()
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            studentMainContent(false)
                        }
                    }
                }
            }
        }

        if (showDbNotificationsDialog) {
            RoleNotificationsDialog(
                notifications = dbNotifications,
                onMarkAllRead = roleVm::markAllNotificationsRead,
                onDismiss = { showDbNotificationsDialog = false }
            )
        }

        if (showSessionsDialog) {
            ActiveSessionsDialog(
                sessions = deviceSessions,
                currentSessionId = currentSessionId,
                syncState = syncState,
                onManualSync = roleVm::triggerManualSync,
                onRevokeSession = roleVm::revokeDeviceSession,
                onDismiss = { showSessionsDialog = false }
            )
        }

        if (showSignOutConfirm) {
            AlertDialog(
                onDismissRequest = { showSignOutConfirm = false },
                title = { Text(strings.signOut) },
                text = {
                    Text(
                        if (language == AppLanguage.AR) "هل تريد تسجيل الخروج من جلسة الطالب؟"
                        else "Sign out of your Student session?"
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showSignOutConfirm = false
                            onSignOut()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SkillBridgeColors.DisputedRed),
                        modifier = Modifier.testTag("confirm_role_sign_out_btn")
                    ) {
                        Text(strings.confirmLabel)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showSignOutConfirm = false }) {
                        Text(strings.cancelLabel)
                    }
                }
            )
        }

        SkillBridgeAiAgentSheet(
            state = aiAgentState,
            allowPersonalization = aiPersonalization,
            allowContextSharing = aiContextSharing,
            isOffline = isOffline,
            onSendPrompt = studentVm::sendAgentPrompt,
            onRetry = studentVm::retryLastAgentPrompt,
            onRateMessage = studentVm::rateAgentMessage,
            onReportMessage = studentVm::reportAiResponse,
            onRequestActionConfirm = studentVm::requestAgentActionConfirmation,
            onConfirmAction = studentVm::confirmPendingAgentAction,
            onDismissAction = studentVm::dismissPendingAgentAction,
            onDismiss = studentVm::closeAiAgent
        )
    }
}
