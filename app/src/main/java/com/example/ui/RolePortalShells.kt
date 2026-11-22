package com.example.ui

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Approval
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material.icons.filled.WorkOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isAltPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.AppLanguage
import com.example.core.LocalAppLanguage
import com.example.core.LocalAppStrings
import com.example.data.local.AccountRole
import com.example.data.local.SessionPrincipal
import com.example.ui.components.ActiveSessionsDialog
import com.example.ui.components.DesktopNavItem
import com.example.ui.components.DesktopPermanentSidebar
import com.example.ui.components.DesktopWorkspaceTopBar
import com.example.ui.components.DeviceFormFactor
import com.example.ui.components.OfflineStatusBanner
import com.example.ui.components.SkillBridgeLogo
import com.example.ui.components.StatusPill
import com.example.ui.components.StatusTone
import com.example.ui.components.resolveDeviceFormFactor
import com.example.ui.screens.AdminNavTab
import com.example.ui.screens.CompanyDirectorPortalContent
import com.example.ui.screens.CompanyNavTab
import com.example.ui.screens.CompanyPortalContent
import com.example.ui.screens.DirectorNavTab
import com.example.ui.screens.PlatformAdminPortalContent
import com.example.ui.screens.RoleNotificationsDialog
import com.example.ui.theme.SkillBridgeColors
import com.example.ui.viewmodel.RolePortalViewModel

private data class RoleShellTab(
    val key: String,
    val labelEn: String,
    val labelAr: String,
    val icon: ImageVector,
    val testTag: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IsolatedRolePortalScaffold(
    session: SessionPrincipal,
    roleVm: RolePortalViewModel,
    darkTheme: Boolean,
    isOffline: Boolean,
    onToggleLanguage: () -> Unit,
    onToggleDarkTheme: () -> Unit,
    onSignOut: () -> Unit
) {
    val lang = LocalAppLanguage.current
    val strings = LocalAppStrings.current

    val notifications by roleVm.notifications.collectAsStateWithLifecycle()
    val unreadCount = remember(notifications) { notifications.count { !it.isRead } }
    val actionMessage by roleVm.actionMessage.collectAsStateWithLifecycle()
    val syncState by roleVm.syncState.collectAsStateWithLifecycle()
    val lastSyncedEpochMs by roleVm.lastSyncedEpochMs.collectAsStateWithLifecycle()
    val pendingWritesCount by roleVm.pendingWritesCount.collectAsStateWithLifecycle()
    val deviceSessions by roleVm.deviceSessions.collectAsStateWithLifecycle()
    val currentSessionId by roleVm.currentDeviceSessionId.collectAsStateWithLifecycle()
    val isCurrentSessionRevoked by roleVm.isCurrentSessionRevoked.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(actionMessage) {
        val msg = actionMessage
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

    var showNotificationsDialog by remember { mutableStateOf(false) }
    var showSessionsDialog by remember { mutableStateOf(false) }
    var showSignOutConfirm by remember { mutableStateOf(false) }

    val tabs = remember(session.role) {
        when (session.role) {
            AccountRole.PLATFORM_ADMIN -> listOf(
                RoleShellTab("OVERVIEW", "Overview", "نظرة عامة", Icons.Filled.Analytics, "admin_nav_overview"),
                RoleShellTab("APPROVALS", "Approvals", "الاعتمادات", Icons.Filled.Approval, "admin_nav_approvals"),
                RoleShellTab("USERS", "Users", "المستخدمون", Icons.Filled.People, "admin_nav_users"),
                RoleShellTab("MODERATION", "Moderation", "المراقبة", Icons.Filled.Flag, "admin_nav_moderation")
            )
            AccountRole.COMPANY -> listOf(
                RoleShellTab("OVERVIEW", "Overview", "نظرة عامة", Icons.Filled.Analytics, "company_nav_overview"),
                RoleShellTab("BRIEFS", "Briefs", "المشاريع", Icons.Filled.WorkOutline, "company_nav_briefs"),
                RoleShellTab("APPLICANTS", "Applicants", "المتقدمون", Icons.Filled.Assignment, "company_nav_applicants"),
                RoleShellTab("TALENT", "Talent", "المواهب", Icons.Filled.Group, "company_nav_talent")
            )
            AccountRole.COMPANY_DIRECTOR -> listOf(
                RoleShellTab("OVERVIEW", "Overview", "نظرة عامة", Icons.Filled.Analytics, "director_nav_overview"),
                RoleShellTab("PROJECTS", "Projects", "المشاريع", Icons.Filled.FolderOpen, "director_nav_projects"),
                RoleShellTab("REVIEWS", "Reviews", "المراجعات", Icons.Filled.RateReview, "director_nav_reviews"),
                RoleShellTab("EVALUATIONS", "Evaluated", "التقييمات", Icons.Filled.FactCheck, "director_nav_evaluations")
            )
            AccountRole.STUDENT -> emptyList()
        }
    }

    var selectedTabKey by rememberSaveable(session.accountId) { mutableStateOf(tabs.firstOrNull()?.key ?: "OVERVIEW") }

    val rolePillText = when (session.role) {
        AccountRole.PLATFORM_ADMIN -> if (lang == AppLanguage.AR) "مدير المنصة" else "Platform Admin"
        AccountRole.COMPANY -> if (lang == AppLanguage.AR) "حساب شركة" else "Company"
        AccountRole.COMPANY_DIRECTOR -> if (lang == AppLanguage.AR) "مشرف شركة" else "Director"
        AccountRole.STUDENT -> if (lang == AppLanguage.AR) "طالب" else "Student"
    }
    val roleIcon = when (session.role) {
        AccountRole.PLATFORM_ADMIN -> Icons.Filled.AdminPanelSettings
        AccountRole.COMPANY -> Icons.Filled.Business
        AccountRole.COMPANY_DIRECTOR -> Icons.Filled.SupervisorAccount
        AccountRole.STUDENT -> Icons.Filled.People
    }

    val activeSessionCount = remember(deviceSessions) {
        deviceSessions.count { !it.revoked }.coerceAtLeast(1)
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
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

        val rolePortalBody: @Composable (Boolean) -> Unit = { desktopMode ->
            when (session.role) {
                AccountRole.PLATFORM_ADMIN -> {
                    val accounts by roleVm.adminAccounts.collectAsStateWithLifecycle()
                    val companies by roleVm.adminCompanies.collectAsStateWithLifecycle()
                    val briefs by roleVm.adminBriefs.collectAsStateWithLifecycle()
                    val applications by roleVm.adminApplications.collectAsStateWithLifecycle()
                    val submissions by roleVm.adminSubmissions.collectAsStateWithLifecycle()
                    val activities by roleVm.adminActivities.collectAsStateWithLifecycle()
                    val reports by roleVm.adminReports.collectAsStateWithLifecycle()

                    PlatformAdminPortalContent(
                        tab = AdminNavTab.entries.firstOrNull { it.name == selectedTabKey } ?: AdminNavTab.OVERVIEW,
                        currentAdminId = session.accountId,
                        accounts = accounts,
                        companies = companies,
                        briefs = briefs,
                        applications = applications,
                        submissions = submissions,
                        activities = activities,
                        reports = reports,
                        isDesktop = desktopMode,
                        onNavigateTab = { selectedTabKey = it.name },
                        onReviewCompany = roleVm::adminReviewCompany,
                        onReviewBrief = roleVm::adminReviewBrief,
                        onSetUserActive = roleVm::adminSetUserActive,
                        onResolveReport = roleVm::adminResolveReport
                    )
                }
                AccountRole.COMPANY -> {
                    val company by roleVm.companyProfile.collectAsStateWithLifecycle()
                    val directors by roleVm.companyDirectors.collectAsStateWithLifecycle()
                    val briefs by roleVm.companyBriefs.collectAsStateWithLifecycle()
                    val applications by roleVm.companyApplications.collectAsStateWithLifecycle()
                    val talent by roleVm.studentTalentDirectory.collectAsStateWithLifecycle()
                    val activities by roleVm.companyActivities.collectAsStateWithLifecycle()

                    CompanyPortalContent(
                        tab = CompanyNavTab.entries.firstOrNull { it.name == selectedTabKey } ?: CompanyNavTab.OVERVIEW,
                        company = company,
                        directors = directors,
                        briefs = briefs,
                        applications = applications,
                        students = talent,
                        activities = activities,
                        isDesktop = desktopMode,
                        onNavigateTab = { selectedTabKey = it.name },
                        onCreateBrief = roleVm::companyCreateBrief,
                        onAssignDirector = roleVm::companyAssignDirector,
                        onDecideApplication = roleVm::companyDecideApplication
                    )
                }
                AccountRole.COMPANY_DIRECTOR -> {
                    val briefs by roleVm.directorBriefs.collectAsStateWithLifecycle()
                    val tasks by roleVm.directorTasks.collectAsStateWithLifecycle()
                    val submissions by roleVm.directorSubmissions.collectAsStateWithLifecycle()
                    val applications by roleVm.directorApplications.collectAsStateWithLifecycle()

                    CompanyDirectorPortalContent(
                        tab = DirectorNavTab.entries.firstOrNull { it.name == selectedTabKey } ?: DirectorNavTab.OVERVIEW,
                        directorName = session.displayName,
                        assignedBriefs = briefs,
                        assignedTasks = tasks,
                        assignedSubmissions = submissions,
                        companyApplications = applications,
                        isDesktop = desktopMode,
                        onNavigateTab = { selectedTabKey = it.name },
                        onEvaluateSubmission = roleVm::directorEvaluateSubmission,
                        onCreateTask = roleVm::directorCreateTask,
                        onCompleteBrief = roleVm::directorCompleteBrief
                    )
                }
                AccountRole.STUDENT -> Unit
            }
        }

        if (isDesktop) {
            val activeTab = tabs.firstOrNull { it.key == selectedTabKey } ?: tabs.firstOrNull()
            val desktopNavItems = remember(tabs, lang) {
                tabs.mapIndexed { idx, t ->
                    DesktopNavItem(
                        key = t.key,
                        label = if (lang == AppLanguage.AR) t.labelAr else t.labelEn,
                        icon = t.icon,
                        testTag = t.testTag,
                        shortcutHint = "Alt+${idx + 1}"
                    )
                }
            }

            Scaffold(
                contentWindowInsets = WindowInsets.safeDrawing,
                snackbarHost = { SnackbarHost(snackbarHostState) },
                modifier = Modifier.onPreviewKeyEvent { event ->
                    if (event.type == KeyEventType.KeyDown && event.isAltPressed) {
                        val idx = when (event.key) {
                            Key.One -> 0
                            Key.Two -> 1
                            Key.Three -> 2
                            Key.Four -> 3
                            else -> -1
                        }
                        if (idx in tabs.indices) {
                            selectedTabKey = tabs[idx].key
                            true
                        } else false
                    } else false
                }
            ) { innerPadding ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    DesktopPermanentSidebar(
                        displayName = session.displayName,
                        loginId = session.loginId,
                        roleLabel = rolePillText,
                        roleIcon = roleIcon,
                        primaryItems = desktopNavItems,
                        selectedKey = selectedTabKey,
                        onSelectPrimary = { selectedTabKey = it },
                        activeSessionCount = activeSessionCount,
                        darkTheme = darkTheme,
                        onOpenActiveSessions = { showSessionsDialog = true },
                        onToggleDarkTheme = onToggleDarkTheme,
                        onToggleLanguage = onToggleLanguage,
                        onSignOut = { showSignOutConfirm = true }
                    )

                    Column(modifier = Modifier.weight(1f).fillMaxSize()) {
                        DesktopWorkspaceTopBar(
                            sectionTitle = activeTab?.let { if (lang == AppLanguage.AR) it.labelAr else it.labelEn } ?: rolePillText,
                            subtitle = "$rolePillText • ${session.displayName}",
                            syncState = syncState,
                            lastSyncedEpochMs = lastSyncedEpochMs,
                            pendingWritesCount = pendingWritesCount,
                            isOffline = isOffline,
                            activeSessionCount = activeSessionCount,
                            unreadNotifications = unreadCount,
                            notificationsTestTag = "role_notifications_btn",
                            onManualSync = roleVm::triggerManualSync,
                            onOpenActiveSessions = { showSessionsDialog = true },
                            onOpenNotifications = { showNotificationsDialog = true }
                        )
                        if (isOffline) {
                            OfflineStatusBanner()
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            rolePortalBody(true)
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
                        title = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                SkillBridgeLogo(
                                    darkBackground = darkTheme,
                                    compactOnly = true
                                )
                                Column {
                                    Text(
                                        text = session.displayName,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                    )
                                    StatusPill(
                                        text = rolePillText,
                                        tone = StatusTone.VERIFIED,
                                        icon = roleIcon
                                    )
                                }
                            }
                        },
                        actions = {
                            IconButton(
                                onClick = { showSessionsDialog = true },
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("active_sessions_btn")
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
                                onClick = { showNotificationsDialog = true },
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("role_notifications_btn")
                            ) {
                                BadgedBox(
                                    badge = {
                                        if (unreadCount > 0) {
                                            Badge { Text("$unreadCount") }
                                        }
                                    }
                                ) {
                                    Icon(Icons.Filled.Notifications, contentDescription = strings.notificationsTitle)
                                }
                            }
                            IconButton(
                                onClick = onToggleDarkTheme,
                                modifier = Modifier.minimumInteractiveComponentSize().testTag("role_theme_toggle")
                            ) {
                                Icon(
                                    imageVector = if (darkTheme) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                                    contentDescription = "Toggle theme"
                                )
                            }
                            TextButton(
                                onClick = onToggleLanguage,
                                modifier = Modifier.testTag("role_language_toggle")
                            ) {
                                Text(
                                    strings.languageToggleLabel,
                                    fontWeight = FontWeight.Bold,
                                    color = SkillBridgeColors.BridgeBlueDark
                                )
                            }
                            IconButton(
                                onClick = { showSignOutConfirm = true },
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("role_sign_out_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Logout,
                                    contentDescription = strings.signOut,
                                    tint = SkillBridgeColors.DisputedRed
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.background
                        )
                    )
                },
                bottomBar = {
                    if (!useNavigationRail && tabs.isNotEmpty()) {
                        NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                            tabs.forEach { tab ->
                                val selected = selectedTabKey == tab.key
                                val label = if (lang == AppLanguage.AR) tab.labelAr else tab.labelEn
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { selectedTabKey = tab.key },
                                    icon = { Icon(tab.icon, contentDescription = label) },
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
                    if (useNavigationRail && tabs.isNotEmpty()) {
                        NavigationRail(containerColor = MaterialTheme.colorScheme.surface) {
                            Spacer(Modifier.size(8.dp))
                            tabs.forEach { tab ->
                                val selected = selectedTabKey == tab.key
                                val label = if (lang == AppLanguage.AR) tab.labelAr else tab.labelEn
                                NavigationRailItem(
                                    selected = selected,
                                    onClick = { selectedTabKey = tab.key },
                                    icon = { Icon(tab.icon, contentDescription = label) },
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
                            rolePortalBody(false)
                        }
                    }
                }
            }
        }
    }

    if (showNotificationsDialog) {
        RoleNotificationsDialog(
            notifications = notifications,
            onMarkAllRead = roleVm::markAllNotificationsRead,
            onDismiss = { showNotificationsDialog = false }
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
                    if (lang == AppLanguage.AR) "هل تريد تسجيل الخروج من جلسة $rolePillText؟"
                    else "Sign out of your $rolePillText session?"
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
}
