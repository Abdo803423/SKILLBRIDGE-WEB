package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.core.AppLanguage
import com.example.core.LocalAppLanguage
import com.example.data.local.AccountEntity
import com.example.data.local.AccountRole
import com.example.data.local.CompanyEntity
import com.example.data.local.ModerationReportEntity
import com.example.data.local.PlatformActivityEntity
import com.example.data.local.ProjectApplicationEntity
import com.example.data.local.ProjectBriefEntity
import com.example.data.local.WorkSubmissionEntity
import com.example.ui.components.ChartDataPoint
import com.example.ui.components.DataTableColumn
import com.example.ui.components.DesktopDataTable
import com.example.ui.components.DesktopListDetailSplit
import com.example.ui.components.EmptyStateView
import com.example.ui.components.ResponsiveChartRow
import com.example.ui.components.ResponsiveKpiGrid
import com.example.ui.components.SaasKpiTile
import com.example.ui.components.SectionHeaderRow
import com.example.ui.components.SkillBridgeBarChart
import com.example.ui.components.SkillBridgeDoughnutChart
import com.example.ui.components.SkillBridgeLineChart
import com.example.ui.components.StatusPill
import com.example.ui.components.StatusTone
import com.example.ui.theme.SkillBridgeColors
import com.example.ui.theme.SkillBridgeRadius
import com.example.ui.theme.SkillBridgeSpacing
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class AdminNavTab { OVERVIEW, APPROVALS, USERS, MODERATION }

@Composable
fun PlatformAdminPortalContent(
    tab: AdminNavTab,
    currentAdminId: String,
    accounts: List<AccountEntity>,
    companies: List<CompanyEntity>,
    briefs: List<ProjectBriefEntity>,
    applications: List<ProjectApplicationEntity>,
    submissions: List<WorkSubmissionEntity>,
    activities: List<PlatformActivityEntity>,
    reports: List<ModerationReportEntity>,
    isDesktop: Boolean = false,
    onNavigateTab: (AdminNavTab) -> Unit,
    onReviewCompany: (String, Boolean, String, String) -> Unit,
    onReviewBrief: (String, Boolean, String) -> Unit,
    onSetUserActive: (String, Boolean) -> Unit,
    onResolveReport: (String, String, String) -> Unit
) {
    when (tab) {
        AdminNavTab.OVERVIEW -> AdminOverviewScreen(
            accounts = accounts,
            companies = companies,
            briefs = briefs,
            applications = applications,
            submissions = submissions,
            activities = activities,
            reports = reports,
            isDesktop = isDesktop,
            onOpenApprovals = { onNavigateTab(AdminNavTab.APPROVALS) },
            onOpenUsers = { onNavigateTab(AdminNavTab.USERS) },
            onOpenModeration = { onNavigateTab(AdminNavTab.MODERATION) }
        )
        AdminNavTab.APPROVALS -> AdminApprovalsScreen(
            companies = companies,
            briefs = briefs,
            isDesktop = isDesktop,
            onReviewCompany = onReviewCompany,
            onReviewBrief = onReviewBrief
        )
        AdminNavTab.USERS -> AdminUsersScreen(
            currentAdminId = currentAdminId,
            accounts = accounts,
            isDesktop = isDesktop,
            onSetUserActive = onSetUserActive
        )
        AdminNavTab.MODERATION -> AdminModerationScreen(
            reports = reports,
            activities = activities,
            isDesktop = isDesktop,
            onResolveReport = onResolveReport
        )
    }
}

@Composable
private fun AdminOverviewScreen(
    accounts: List<AccountEntity>,
    companies: List<CompanyEntity>,
    briefs: List<ProjectBriefEntity>,
    applications: List<ProjectApplicationEntity>,
    submissions: List<WorkSubmissionEntity>,
    activities: List<PlatformActivityEntity>,
    reports: List<ModerationReportEntity>,
    isDesktop: Boolean,
    onOpenApprovals: () -> Unit,
    onOpenUsers: () -> Unit,
    onOpenModeration: () -> Unit
) {
    val lang = LocalAppLanguage.current
    val totalUsers = accounts.size
    val activeUsers = accounts.count { it.isActive }
    val verifiedCompanies = companies.count { it.verificationStatus == "VERIFIED" }
    val pendingCompanies = companies.count { it.verificationStatus == "PENDING" }
    val publishedBriefs = briefs.count { it.approvalStatus in setOf("APPROVED", "ACTIVE", "COMPLETED") }
    val pendingBriefs = briefs.count { it.approvalStatus == "PENDING_APPROVAL" }
    val activeProjects = briefs.count { it.approvalStatus == "ACTIVE" }
    val completedProjects = briefs.count { it.approvalStatus == "COMPLETED" }
    val completionDenom = (activeProjects + completedProjects).coerceAtLeast(1)
    val completionRate = (completedProjects * 100) / completionDenom
    val pendingReviews = submissions.count { it.status == "PENDING_REVIEW" }
    val openReports = reports.count { it.status == "OPEN" }

    val roleDistribution = listOf(
        ChartDataPoint("Students", accounts.count { it.role == AccountRole.STUDENT.key }.toFloat(), SkillBridgeColors.BridgeBlue),
        ChartDataPoint("Companies", accounts.count { it.role == AccountRole.COMPANY.key }.toFloat(), SkillBridgeColors.DeepNavy),
        ChartDataPoint("Directors", accounts.count { it.role == AccountRole.COMPANY_DIRECTOR.key }.toFloat(), SkillBridgeColors.LimeGreenDark),
        ChartDataPoint("Admins", accounts.count { it.role == AccountRole.PLATFORM_ADMIN.key }.toFloat(), SkillBridgeColors.EthicalPurple)
    )

    val briefStatusBars = listOf(
        ChartDataPoint("Pending", pendingBriefs.toFloat(), SkillBridgeColors.PendingAmber),
        ChartDataPoint("Approved", briefs.count { it.approvalStatus == "APPROVED" }.toFloat(), SkillBridgeColors.BridgeBlue),
        ChartDataPoint("Active", activeProjects.toFloat(), SkillBridgeColors.PaidTeal),
        ChartDataPoint("Done", completedProjects.toFloat(), SkillBridgeColors.VerifiedGreen),
        ChartDataPoint("Rejected", briefs.count { it.approvalStatus == "REJECTED" }.toFloat(), SkillBridgeColors.DisputedRed)
    )

    val activityTrend = remember(activities, applications) {
        val now = System.currentTimeMillis()
        val dayMs = 86_400_000L
        val fmt = SimpleDateFormat("MM/dd", Locale.US)
        (4 downTo 0).map { offset ->
            val start = now - (offset + 1) * 4 * dayMs
            val end = now - offset * 4 * dayMs
            val actCount = activities.count { it.createdAtEpochMs in start..end }
            val appCount = applications.count { it.appliedAtEpochMs in start..end }
            ChartDataPoint(
                label = fmt.format(Date(end)),
                value = (actCount + appCount).toFloat(),
                color = SkillBridgeColors.BridgeBlue
            )
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().testTag("admin_dashboard_root"),
        contentPadding = PaddingValues(if (isDesktop) 24.dp else SkillBridgeSpacing.screenHorizontal),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(SkillBridgeRadius.lg),
                colors = CardDefaults.cardColors(containerColor = SkillBridgeColors.DeepNavy)
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Filled.AdminPanelSettings, contentDescription = null, tint = SkillBridgeColors.SoftCyan)
                        Text(
                            text = if (lang == AppLanguage.AR) "مركز إدارة عمليات SkillBridge" else "Platform Admin Operations Center",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = SkillBridgeColors.PureWhite
                        )
                    }
                    Text(
                        text = if (lang == AppLanguage.AR)
                            "$pendingCompanies شركة و $pendingBriefs مشروع بانتظار المراجعة • $openReports بلاغ مفتوح"
                        else
                            "$pendingCompanies company verification(s) & $pendingBriefs brief(s) pending approval • $openReports open report(s)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SkillBridgeColors.SoftCyan
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = onOpenApprovals,
                            colors = ButtonDefaults.buttonColors(containerColor = SkillBridgeColors.BridgeBlue, contentColor = SkillBridgeColors.PureWhite),
                            modifier = Modifier.height(44.dp)
                        ) {
                            Text(if (lang == AppLanguage.AR) "مراجعة الموافقات" else "Review Queue (${pendingCompanies + pendingBriefs})")
                        }
                        OutlinedButton(
                            onClick = onOpenUsers,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = SkillBridgeColors.PureWhite),
                            border = BorderStroke(1.dp, SkillBridgeColors.SoftCyan.copy(alpha = 0.5f)),
                            modifier = Modifier.height(44.dp)
                        ) {
                            Text(if (lang == AppLanguage.AR) "المستخدمون" else "Users ($totalUsers)")
                        }
                    }
                }
            }
        }

        // Responsive KPI Grid (3x2 on Desktop, 2x3 on Mobile)
        item {
            ResponsiveKpiGrid(
                isDesktop = isDesktop,
                desktopColumns = 3,
                items = listOf(
                    { mod ->
                        SaasKpiTile(
                            title = if (lang == AppLanguage.AR) "إجمالي المستخدمين" else "Total / Active Users",
                            value = "$totalUsers / $activeUsers",
                            subtitle = "${accounts.count { it.role == AccountRole.STUDENT.key }} students",
                            icon = Icons.Filled.Groups,
                            accentColor = SkillBridgeColors.BridgeBlueDark,
                            modifier = mod,
                            onClick = onOpenUsers
                        )
                    },
                    { mod ->
                        SaasKpiTile(
                            title = if (lang == AppLanguage.AR) "الشركات الموثّقة" else "Verified Companies",
                            value = "$verifiedCompanies",
                            subtitle = "$pendingCompanies pending review",
                            icon = Icons.Filled.VerifiedUser,
                            accentColor = SkillBridgeColors.VerifiedGreen,
                            modifier = mod,
                            onClick = onOpenApprovals
                        )
                    },
                    { mod ->
                        SaasKpiTile(
                            title = if (lang == AppLanguage.AR) "المشاريع المنشورة" else "Published Briefs",
                            value = "$publishedBriefs",
                            subtitle = "$activeProjects active • $completedProjects done",
                            icon = Icons.Filled.Business,
                            accentColor = SkillBridgeColors.DeepNavy,
                            modifier = mod
                        )
                    },
                    { mod ->
                        SaasKpiTile(
                            title = if (lang == AppLanguage.AR) "إجمالي الطلبات" else "Total Applications",
                            value = "${applications.size}",
                            subtitle = "$completionRate% completion rate",
                            icon = Icons.Filled.TrendingUp,
                            accentColor = SkillBridgeColors.PaidTeal,
                            modifier = mod
                        )
                    },
                    { mod ->
                        SaasKpiTile(
                            title = if (lang == AppLanguage.AR) "مراجعات التسليم المعلقة" else "Pending Reviews",
                            value = "$pendingReviews",
                            subtitle = "${submissions.count { it.status == "VERIFIED" }} verified deliverables",
                            icon = Icons.Filled.RateReview,
                            accentColor = SkillBridgeColors.PendingAmber,
                            modifier = mod
                        )
                    },
                    { mod ->
                        SaasKpiTile(
                            title = if (lang == AppLanguage.AR) "مهام الإشراف" else "Moderation Queue",
                            value = "$openReports",
                            subtitle = "${pendingCompanies + pendingBriefs} approvals awaiting",
                            icon = Icons.Filled.Flag,
                            accentColor = SkillBridgeColors.DisputedRed,
                            modifier = mod,
                            onClick = onOpenModeration
                        )
                    }
                )
            )
        }

        // Responsive Multi-Column Analytics Charts
        item {
            ResponsiveChartRow(
                isDesktop = isDesktop,
                charts = listOf(
                    { mod ->
                        SkillBridgeLineChart(
                            title = if (lang == AppLanguage.AR) "اتجاه النشاط والطلبات" else "Activity & Applications Trend",
                            subtitle = if (lang == AppLanguage.AR) "مشتق من سجلات قاعدة البيانات الحية" else "Live database telemetry",
                            points = activityTrend,
                            modifier = mod
                        )
                    },
                    { mod ->
                        SkillBridgeDoughnutChart(
                            title = if (lang == AppLanguage.AR) "توزيع المستخدمين" else "User Distribution",
                            subtitle = if (lang == AppLanguage.AR) "الحسابات المسجلة فعليًا" else "Authenticated roles",
                            centerLabel = "Users",
                            segments = roleDistribution,
                            modifier = mod
                        )
                    },
                    { mod ->
                        SkillBridgeBarChart(
                            title = if (lang == AppLanguage.AR) "حالة مشاريع الشركات" else "Brief Pipeline",
                            subtitle = if (lang == AppLanguage.AR) "تحديث فوري عند الاعتماد" else "Approval & execution",
                            bars = briefStatusBars,
                            modifier = mod
                        )
                    }
                )
            )
        }

        item {
            SectionHeaderRow(
                title = if (lang == AppLanguage.AR) "آخر نشاطات المنصة" else "Recent Platform Activity Feed",
                actionText = if (lang == AppLanguage.AR) "الإشراف" else "Moderation",
                onAction = onOpenModeration
            )
        }

        if (isDesktop) {
            item {
                DesktopDataTable(
                    columns = listOf(
                        DataTableColumn(if (lang == AppLanguage.AR) "الملخص" else "Summary", weight = 2.4f),
                        DataTableColumn(if (lang == AppLanguage.AR) "الدور" else "Role", weight = 0.9f),
                        DataTableColumn(if (lang == AppLanguage.AR) "الإجراء" else "Action Type", weight = 1.1f)
                    ),
                    rows = activities.take(10),
                    rowKey = { it.id },
                    emptyMessage = "No platform activity recorded yet."
                ) { act, col ->
                    when (col) {
                        0 -> Text(act.summary, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        1 -> StatusPill(act.actorRole.take(9), StatusTone.NEUTRAL)
                        else -> Text(act.actionType, style = MaterialTheme.typography.labelMedium, color = SkillBridgeColors.BridgeBlueDark)
                    }
                }
            }
        } else {
            items(activities.take(8), key = { it.id }) { act ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(act.summary, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                            Text("${act.actorRole} • ${act.actionType}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        StatusPill(act.actorRole.take(7), StatusTone.NEUTRAL)
                    }
                }
            }
        }
        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
private fun AdminApprovalsScreen(
    companies: List<CompanyEntity>,
    briefs: List<ProjectBriefEntity>,
    isDesktop: Boolean,
    onReviewCompany: (String, Boolean, String, String) -> Unit,
    onReviewBrief: (String, Boolean, String) -> Unit
) {
    val lang = LocalAppLanguage.current
    var rejectBriefTarget by remember { mutableStateOf<ProjectBriefEntity?>(null) }
    var rejectCompanyTarget by remember { mutableStateOf<CompanyEntity?>(null) }

    val pendingCompanies = companies.filter { it.verificationStatus == "PENDING" }
    val otherCompanies = companies.filter { it.verificationStatus != "PENDING" }
    val pendingBriefs = briefs.filter { it.approvalStatus == "PENDING_APPROVAL" }
    val reviewedBriefs = briefs.filter { it.approvalStatus != "PENDING_APPROVAL" }

    var selectedBriefId by remember(briefs) { mutableStateOf(pendingBriefs.firstOrNull()?.id ?: briefs.firstOrNull()?.id) }
    var selectedCompanyId by remember(companies) { mutableStateOf(pendingCompanies.firstOrNull()?.id ?: companies.firstOrNull()?.id) }
    var inspectBriefMode by remember { mutableStateOf(true) }

    if (isDesktop) {
        Box(modifier = Modifier.fillMaxSize().testTag("admin_approvals_root")) {
            DesktopListDetailSplit(
                leftWeight = 0.48f,
                leftPane = {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            SectionHeaderRow(
                                title = if (lang == AppLanguage.AR) "توثيق الشركات (${pendingCompanies.size})" else "Pending Companies (${pendingCompanies.size})"
                            )
                        }
                        if (pendingCompanies.isEmpty()) {
                            item {
                                EmptyStateView(subtitle = if (lang == AppLanguage.AR) "لا توجد طلبات شركات معلقة." else "All company verification requests are processed.")
                            }
                        } else {
                            items(pendingCompanies, key = { it.id }) { comp ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedCompanyId = comp.id
                                            inspectBriefMode = false
                                        },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (!inspectBriefMode && selectedCompanyId == comp.id) SkillBridgeColors.SoftCyanTint.copy(alpha = 0.4f)
                                        else MaterialTheme.colorScheme.surface
                                    ),
                                    border = BorderStroke(1.dp, SkillBridgeColors.PendingAmber.copy(alpha = 0.55f))
                                ) {
                                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(comp.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                                Text("${comp.city} • ${comp.domain}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                            StatusPill("PENDING", StatusTone.PENDING, Icons.Filled.HourglassTop)
                                        }
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                            Button(
                                                onClick = { onReviewCompany(comp.id, true, "BUSINESS", "Verified Business Partner by Admin.") },
                                                colors = ButtonDefaults.buttonColors(containerColor = SkillBridgeColors.VerifiedGreen),
                                                modifier = Modifier.weight(1f).height(44.dp).testTag("approve_company_${comp.id}")
                                            ) { Text("Verify Company") }
                                            OutlinedButton(
                                                onClick = { rejectCompanyTarget = comp },
                                                modifier = Modifier.weight(1f).height(44.dp)
                                            ) { Text("Reject", color = SkillBridgeColors.DisputedRed) }
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            SectionHeaderRow(
                                title = if (lang == AppLanguage.AR) "اعتماد مشاريع الشركات (${pendingBriefs.size})" else "Brief Approval Queue (${pendingBriefs.size})"
                            )
                        }
                        if (pendingBriefs.isEmpty()) {
                            item {
                                EmptyStateView(subtitle = if (lang == AppLanguage.AR) "لا توجد مسودات مشاريع بانتظار الاعتماد." else "No project briefs awaiting approval.")
                            }
                        } else {
                            items(pendingBriefs, key = { it.id }) { brief ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedBriefId = brief.id
                                            inspectBriefMode = true
                                        },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (inspectBriefMode && selectedBriefId == brief.id) SkillBridgeColors.SoftCyanTint.copy(alpha = 0.4f)
                                        else MaterialTheme.colorScheme.surface
                                    ),
                                    border = BorderStroke(1.dp, SkillBridgeColors.BridgeBlue.copy(alpha = 0.55f))
                                ) {
                                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(brief.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                                Text("${brief.companyName} • ${brief.domain} • ${brief.level}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                            StatusPill("PENDING", StatusTone.PENDING, Icons.Filled.PendingActions)
                                        }
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                            Button(
                                                onClick = { onReviewBrief(brief.id, true, "Approved for public student discovery.") },
                                                colors = ButtonDefaults.buttonColors(containerColor = SkillBridgeColors.DeepNavy),
                                                modifier = Modifier.weight(1f).height(44.dp).testTag("approve_brief_${brief.id}")
                                            ) {
                                                Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(Modifier.width(6.dp))
                                                Text("Approve Brief")
                                            }
                                            OutlinedButton(
                                                onClick = { rejectBriefTarget = brief },
                                                modifier = Modifier.weight(1f).height(44.dp)
                                            ) { Text("Reject", color = SkillBridgeColors.DisputedRed) }
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                rightPane = {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        val inspectedBrief = briefs.firstOrNull { it.id == selectedBriefId }
                        val inspectedCompany = companies.firstOrNull { it.id == selectedCompanyId }
                        if (inspectBriefMode && inspectedBrief != null) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = BorderStroke(1.dp, SkillBridgeColors.BridgeBlue.copy(alpha = 0.4f))
                                ) {
                                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("Brief Compliance Inspector", style = MaterialTheme.typography.labelLarge, color = SkillBridgeColors.BridgeBlueDark, fontWeight = FontWeight.Bold)
                                            StatusPill(inspectedBrief.approvalStatus, if (inspectedBrief.approvalStatus == "PENDING_APPROVAL") StatusTone.PENDING else StatusTone.VERIFIED)
                                        }
                                        Text(inspectedBrief.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                        Text("${inspectedBrief.companyName} • ${inspectedBrief.domain} • ${inspectedBrief.level}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        HorizontalDivider()
                                        Text(inspectedBrief.summary, style = MaterialTheme.typography.bodyMedium)
                                        Text("Evaluation Rubric: ${inspectedBrief.rubricCsv}", style = MaterialTheme.typography.labelMedium, color = SkillBridgeColors.BridgeBlueDark)
                                        Text("Required Skills: ${inspectedBrief.skillsCsv}", style = MaterialTheme.typography.labelMedium)
                                        Text("Reward: +${inspectedBrief.pointsReward} pts • ${inspectedBrief.prizePool} • Deadline ${inspectedBrief.deadline}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        } else if (inspectedCompany != null) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = BorderStroke(1.dp, SkillBridgeColors.PendingAmber.copy(alpha = 0.5f))
                                ) {
                                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Text("Company Verification Inspector", style = MaterialTheme.typography.labelLarge, color = SkillBridgeColors.BridgeBlueDark, fontWeight = FontWeight.Bold)
                                        Text(inspectedCompany.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                        Text("${inspectedCompany.city} • ${inspectedCompany.domain} • ${inspectedCompany.verificationTier}", style = MaterialTheme.typography.bodySmall)
                                        HorizontalDivider()
                                        Text(inspectedCompany.verificationNotes, style = MaterialTheme.typography.bodyMedium)
                                    }
                                }
                            }
                        }

                        item {
                            SectionHeaderRow(title = if (lang == AppLanguage.AR) "الشركات المعتمدة" else "Company Verification Ledger")
                            Spacer(Modifier.height(6.dp))
                            DesktopDataTable(
                                columns = listOf(
                                    DataTableColumn("Company", 1.5f),
                                    DataTableColumn("City / Tier", 1.2f),
                                    DataTableColumn("Status", 0.9f, alignEnd = true)
                                ),
                                rows = otherCompanies,
                                rowKey = { it.id },
                                onRowClick = {
                                    selectedCompanyId = it.id
                                    inspectBriefMode = false
                                }
                            ) { comp, col ->
                                when (col) {
                                    0 -> Text(comp.name, fontWeight = FontWeight.SemiBold)
                                    1 -> Text("${comp.city} • ${comp.verificationTier}", style = MaterialTheme.typography.bodySmall)
                                    else -> StatusPill(comp.verificationStatus, if (comp.verificationStatus == "VERIFIED") StatusTone.VERIFIED else StatusTone.DISPUTED)
                                }
                            }
                        }

                        item {
                            SectionHeaderRow(title = if (lang == AppLanguage.AR) "سجل المشاريع المعتمدة" else "Reviewed Project Briefs")
                            Spacer(Modifier.height(6.dp))
                            DesktopDataTable(
                                columns = listOf(
                                    DataTableColumn("Brief Title", 1.6f),
                                    DataTableColumn("Company & Director", 1.4f),
                                    DataTableColumn("Status", 0.9f, alignEnd = true)
                                ),
                                rows = reviewedBriefs,
                                rowKey = { it.id },
                                onRowClick = {
                                    selectedBriefId = it.id
                                    inspectBriefMode = true
                                }
                            ) { brief, col ->
                                when (col) {
                                    0 -> Text(brief.title, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    1 -> Text("${brief.companyName} • ${brief.assignedDirectorName.ifBlank { "Unassigned" }}", style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    else -> StatusPill(
                                        brief.approvalStatus,
                                        when (brief.approvalStatus) {
                                            "APPROVED", "ACTIVE", "COMPLETED" -> StatusTone.VERIFIED
                                            "REJECTED" -> StatusTone.DISPUTED
                                            else -> StatusTone.PENDING
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize().testTag("admin_approvals_root"),
            contentPadding = PaddingValues(SkillBridgeSpacing.screenHorizontal),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                SectionHeaderRow(
                    title = if (lang == AppLanguage.AR) "طلبات توثيق الشركات (${pendingCompanies.size})" else "Pending Company Verifications (${pendingCompanies.size})"
                )
            }
            if (pendingCompanies.isEmpty()) {
                item {
                    EmptyStateView(subtitle = if (lang == AppLanguage.AR) "لا توجد طلبات شركات معلقة." else "All company verification requests are processed.")
                }
            } else {
                items(pendingCompanies, key = { it.id }) { comp ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, SkillBridgeColors.PendingAmber.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(comp.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text("${comp.city} • ${comp.domain}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                StatusPill("PENDING", StatusTone.PENDING, Icons.Filled.HourglassTop)
                            }
                            Text(comp.verificationNotes, style = MaterialTheme.typography.bodyMedium)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                Button(
                                    onClick = { onReviewCompany(comp.id, true, "BUSINESS", "Verified Business Partner by Admin.") },
                                    colors = ButtonDefaults.buttonColors(containerColor = SkillBridgeColors.VerifiedGreen),
                                    modifier = Modifier.weight(1f).height(46.dp).testTag("approve_company_${comp.id}")
                                ) { Text("Verify Company") }
                                OutlinedButton(
                                    onClick = { rejectCompanyTarget = comp },
                                    modifier = Modifier.weight(1f).height(46.dp)
                                ) { Text("Reject", color = SkillBridgeColors.DisputedRed) }
                            }
                        }
                    }
                }
            }

            item {
                SectionHeaderRow(
                    title = if (lang == AppLanguage.AR) "اعتماد مشاريع الشركات (${pendingBriefs.size})" else "Project Brief Approval Queue (${pendingBriefs.size})"
                )
            }
            if (pendingBriefs.isEmpty()) {
                item {
                    EmptyStateView(subtitle = if (lang == AppLanguage.AR) "لا توجد مسودات مشاريع بانتظار الاعتماد." else "No project briefs awaiting approval.")
                }
            } else {
                items(pendingBriefs, key = { it.id }) { brief ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, SkillBridgeColors.BridgeBlue.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(brief.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text("${brief.companyName} • ${brief.domain} • ${brief.level}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                StatusPill("PENDING", StatusTone.PENDING, Icons.Filled.PendingActions)
                            }
                            Text(brief.summary, style = MaterialTheme.typography.bodyMedium)
                            Text("Rubric: ${brief.rubricCsv}", style = MaterialTheme.typography.labelMedium, color = SkillBridgeColors.BridgeBlueDark)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                Button(
                                    onClick = { onReviewBrief(brief.id, true, "Approved for public student discovery.") },
                                    colors = ButtonDefaults.buttonColors(containerColor = SkillBridgeColors.DeepNavy),
                                    modifier = Modifier.weight(1f).height(46.dp).testTag("approve_brief_${brief.id}")
                                ) {
                                    Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Approve Brief")
                                }
                                OutlinedButton(
                                    onClick = { rejectBriefTarget = brief },
                                    modifier = Modifier.weight(1f).height(46.dp)
                                ) {
                                    Text("Reject", color = SkillBridgeColors.DisputedRed)
                                }
                            }
                        }
                    }
                }
            }

            if (otherCompanies.isNotEmpty()) {
                item { SectionHeaderRow(title = if (lang == AppLanguage.AR) "الشركات المعتمدة" else "Company Verification Ledger") }
                items(otherCompanies, key = { it.id }) { comp ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(comp.name, fontWeight = FontWeight.Bold)
                                Text("${comp.city} • ${comp.verificationTier}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            StatusPill(
                                comp.verificationStatus,
                                if (comp.verificationStatus == "VERIFIED") StatusTone.VERIFIED else StatusTone.DISPUTED
                            )
                        }
                    }
                }
            }

            if (reviewedBriefs.isNotEmpty()) {
                item { SectionHeaderRow(title = if (lang == AppLanguage.AR) "سجل المشاريع المعتمدة" else "Reviewed Project Briefs") }
                items(reviewedBriefs, key = { it.id }) { brief ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(brief.title, fontWeight = FontWeight.SemiBold)
                                Text("${brief.companyName} • Director: ${brief.assignedDirectorName.ifBlank { "Unassigned" }}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            StatusPill(
                                brief.approvalStatus,
                                when (brief.approvalStatus) {
                                    "APPROVED", "ACTIVE", "COMPLETED" -> StatusTone.VERIFIED
                                    "REJECTED" -> StatusTone.DISPUTED
                                    else -> StatusTone.PENDING
                                }
                            )
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(16.dp)) }
        }
    }

    rejectBriefTarget?.let { target ->
        var note by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { rejectBriefTarget = null },
            title = { Text("Reject Brief: ${target.title}") },
            text = {
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Reason / required improvements") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onReviewBrief(target.id, false, note.ifBlank { "Clarify rubric weights and deliverables." })
                        rejectBriefTarget = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SkillBridgeColors.DisputedRed)
                ) { Text("Reject Brief") }
            },
            dismissButton = { TextButton(onClick = { rejectBriefTarget = null }) { Text("Cancel") } }
        )
    }

    rejectCompanyTarget?.let { target ->
        var note by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { rejectCompanyTarget = null },
            title = { Text("Reject Verification: ${target.name}") },
            text = {
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Compliance feedback") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onReviewCompany(target.id, false, "UNVERIFIED", note.ifBlank { "Trade registration mismatch." })
                        rejectCompanyTarget = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SkillBridgeColors.DisputedRed)
                ) { Text("Reject") }
            },
            dismissButton = { TextButton(onClick = { rejectCompanyTarget = null }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun AdminUsersScreen(
    currentAdminId: String,
    accounts: List<AccountEntity>,
    isDesktop: Boolean,
    onSetUserActive: (String, Boolean) -> Unit
) {
    val lang = LocalAppLanguage.current
    var query by remember { mutableStateOf("") }
    var roleFilter by remember { mutableStateOf("ALL") }

    val filtered = accounts.filter { acc ->
        (roleFilter == "ALL" || acc.role == roleFilter) &&
            (query.isBlank() ||
                acc.displayName.contains(query, ignoreCase = true) ||
                acc.loginId.contains(query, ignoreCase = true) ||
                acc.email.contains(query, ignoreCase = true))
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().testTag("admin_users_root"),
        contentPadding = PaddingValues(if (isDesktop) 24.dp else SkillBridgeSpacing.screenHorizontal),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            if (isDesktop) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                        placeholder = { Text(if (lang == AppLanguage.AR) "ابحث بالاسم أو المعرّف..." else "Search users by name, loginId, or email") },
                        modifier = Modifier.weight(1f)
                    )
                    Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("ALL", "STUDENT", "COMPANY", "COMPANY_DIRECTOR", "PLATFORM_ADMIN").forEach { role ->
                            FilterChip(
                                selected = roleFilter == role,
                                onClick = { roleFilter = role },
                                label = { Text(role.replace("_", " ")) }
                            )
                        }
                    }
                }
            } else {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    placeholder = { Text(if (lang == AppLanguage.AR) "ابحث بالاسم أو المعرّف..." else "Search users by name, loginId, or email") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("ALL", "STUDENT", "COMPANY", "COMPANY_DIRECTOR", "PLATFORM_ADMIN").forEach { role ->
                        FilterChip(
                            selected = roleFilter == role,
                            onClick = { roleFilter = role },
                            label = { Text(role.replace("_", " ")) }
                        )
                    }
                }
            }
        }

        if (isDesktop) {
            item {
                DesktopDataTable(
                    columns = listOf(
                        DataTableColumn("User", 1.3f),
                        DataTableColumn("Login ID / Email", 1.6f),
                        DataTableColumn("Role", 1.0f),
                        DataTableColumn("Level / Points", 1.0f),
                        DataTableColumn("Active", 0.7f, alignEnd = true)
                    ),
                    rows = filtered,
                    rowKey = { it.id },
                    emptyMessage = "No user accounts match current filter."
                ) { acc, col ->
                    when (col) {
                        0 -> Text(acc.displayName, fontWeight = FontWeight.Bold)
                        1 -> Text("@${acc.loginId} • ${acc.email}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        2 -> StatusPill(acc.role.replace("_", " "), StatusTone.REVIEW)
                        3 -> if (acc.role == AccountRole.STUDENT.key) {
                            StatusPill("${acc.points} pts • ${acc.careerLevel}", StatusTone.PAID)
                        } else {
                            Text(acc.country, style = MaterialTheme.typography.bodySmall)
                        }
                        else -> if (acc.id != currentAdminId) {
                            Switch(checked = acc.isActive, onCheckedChange = { onSetUserActive(acc.id, it) })
                        } else {
                            StatusPill("YOU", StatusTone.VERIFIED)
                        }
                    }
                }
            }
        } else {
            items(filtered, key = { it.id }) { acc ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(acc.displayName, fontWeight = FontWeight.Bold)
                            Text("@${acc.loginId} • ${acc.email}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                StatusPill(acc.role.replace("_", " "), StatusTone.REVIEW)
                                if (acc.role == AccountRole.STUDENT.key) {
                                    StatusPill("${acc.points} pts • ${acc.careerLevel}", StatusTone.PAID)
                                }
                            }
                        }
                        if (acc.id != currentAdminId) {
                            Switch(
                                checked = acc.isActive,
                                onCheckedChange = { onSetUserActive(acc.id, it) }
                            )
                        } else {
                            StatusPill("YOU", StatusTone.VERIFIED)
                        }
                    }
                }
            }
        }
        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
private fun AdminModerationScreen(
    reports: List<ModerationReportEntity>,
    activities: List<PlatformActivityEntity>,
    isDesktop: Boolean,
    onResolveReport: (String, String, String) -> Unit
) {
    val lang = LocalAppLanguage.current
    if (isDesktop) {
        Box(modifier = Modifier.fillMaxSize().testTag("admin_moderation_root")) {
            DesktopListDetailSplit(
                leftWeight = 0.45f,
                leftPane = {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            SectionHeaderRow(title = if (lang == AppLanguage.AR) "بلاغات المحتوى والسلامة" else "Moderation & Compliance Reports")
                        }
                        if (reports.isEmpty()) {
                            item { EmptyStateView(subtitle = "No moderation reports logged.") }
                        } else {
                            items(reports, key = { it.id }) { rep ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("${rep.targetType} • ${rep.targetId}", fontWeight = FontWeight.Bold)
                                            StatusPill(rep.status, if (rep.status == "OPEN") StatusTone.DISPUTED else StatusTone.VERIFIED)
                                        }
                                        Text(rep.reason, style = MaterialTheme.typography.bodyMedium)
                                        if (rep.resolutionNote.isNotBlank()) {
                                            Text("Resolution: ${rep.resolutionNote}", style = MaterialTheme.typography.bodySmall, color = SkillBridgeColors.VerifiedGreen)
                                        }
                                        if (rep.status == "OPEN") {
                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Button(
                                                    onClick = { onResolveReport(rep.id, "RESOLVED", "Compliance note added by Admin.") },
                                                    colors = ButtonDefaults.buttonColors(containerColor = SkillBridgeColors.VerifiedGreen),
                                                    modifier = Modifier.weight(1f).height(44.dp)
                                                ) { Text("Resolve") }
                                                OutlinedButton(
                                                    onClick = { onResolveReport(rep.id, "DISMISSED", "No policy breach found.") },
                                                    modifier = Modifier.weight(1f).height(44.dp)
                                                ) { Text("Dismiss") }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                rightPane = {
                    Column(
                        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SectionHeaderRow(title = if (lang == AppLanguage.AR) "سجل تدقيق النظام" else "System Audit Trail")
                        DesktopDataTable(
                            columns = listOf(
                                DataTableColumn("Event Summary", 2.2f),
                                DataTableColumn("Role", 0.9f),
                                DataTableColumn("Action", 1.1f, alignEnd = true)
                            ),
                            rows = activities.take(20),
                            rowKey = { it.id },
                            emptyMessage = "No audit events recorded."
                        ) { act, col ->
                            when (col) {
                                0 -> Text(act.summary, style = MaterialTheme.typography.bodySmall)
                                1 -> StatusPill(act.actorRole.take(9), StatusTone.NEUTRAL)
                                else -> Text(act.actionType, style = MaterialTheme.typography.labelSmall, color = SkillBridgeColors.BridgeBlueDark)
                            }
                        }
                    }
                }
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize().testTag("admin_moderation_root"),
            contentPadding = PaddingValues(SkillBridgeSpacing.screenHorizontal),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                SectionHeaderRow(title = if (lang == AppLanguage.AR) "بلاغات المحتوى والسلامة" else "Moderation & Compliance Reports")
            }
            if (reports.isEmpty()) {
                item { EmptyStateView(subtitle = "No moderation reports logged.") }
            } else {
                items(reports, key = { it.id }) { rep ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("${rep.targetType} • ${rep.targetId}", fontWeight = FontWeight.Bold)
                                StatusPill(rep.status, if (rep.status == "OPEN") StatusTone.DISPUTED else StatusTone.VERIFIED)
                            }
                            Text(rep.reason, style = MaterialTheme.typography.bodyMedium)
                            if (rep.resolutionNote.isNotBlank()) {
                                Text("Resolution: ${rep.resolutionNote}", style = MaterialTheme.typography.bodySmall, color = SkillBridgeColors.VerifiedGreen)
                            }
                            if (rep.status == "OPEN") {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = { onResolveReport(rep.id, "RESOLVED", "Compliance note added by Admin.") },
                                        colors = ButtonDefaults.buttonColors(containerColor = SkillBridgeColors.VerifiedGreen),
                                        modifier = Modifier.weight(1f).height(44.dp)
                                    ) { Text("Resolve") }
                                    OutlinedButton(
                                        onClick = { onResolveReport(rep.id, "DISMISSED", "No policy breach found.") },
                                        modifier = Modifier.weight(1f).height(44.dp)
                                    ) { Text("Dismiss") }
                                }
                            }
                        }
                    }
                }
            }
            item { SectionHeaderRow(title = if (lang == AppLanguage.AR) "سجل تدقيق النظام" else "System Audit Trail") }
            items(activities.take(15), key = { it.id }) { act ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(act.summary, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                        Text(act.actionType, style = MaterialTheme.typography.labelSmall, color = SkillBridgeColors.BridgeBlueDark)
                    }
                }
            }
            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

