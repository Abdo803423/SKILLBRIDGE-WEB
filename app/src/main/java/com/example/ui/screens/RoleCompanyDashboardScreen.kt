package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
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
import com.example.data.local.CompanyEntity
import com.example.data.local.PlatformActivityEntity
import com.example.data.local.ProjectApplicationEntity
import com.example.data.local.ProjectBriefEntity
import com.example.data.seed.SkillBridgeEcosystemData
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
import com.example.ui.components.StatusPill
import com.example.ui.components.StatusTone
import com.example.ui.theme.SkillBridgeColors
import com.example.ui.theme.SkillBridgeRadius
import com.example.ui.theme.SkillBridgeSpacing

enum class CompanyNavTab { OVERVIEW, BRIEFS, APPLICANTS, TALENT }

@Composable
fun CompanyPortalContent(
    tab: CompanyNavTab,
    company: CompanyEntity?,
    directors: List<AccountEntity>,
    briefs: List<ProjectBriefEntity>,
    applications: List<ProjectApplicationEntity>,
    students: List<AccountEntity>,
    activities: List<PlatformActivityEntity>,
    isDesktop: Boolean = false,
    onNavigateTab: (CompanyNavTab) -> Unit,
    onCreateBrief: (
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
    ) -> Unit,
    onAssignDirector: (briefId: String, directorId: String) -> Unit,
    onDecideApplication: (applicationId: String, status: String, note: String) -> Unit
) {
    when (tab) {
        CompanyNavTab.OVERVIEW -> CompanyOverviewScreen(
            company = company,
            briefs = briefs,
            applications = applications,
            activities = activities,
            isDesktop = isDesktop,
            onNavigateTab = onNavigateTab
        )
        CompanyNavTab.BRIEFS -> CompanyBriefsScreen(
            directors = directors,
            briefs = briefs,
            applications = applications,
            isDesktop = isDesktop,
            onCreateBrief = onCreateBrief,
            onAssignDirector = onAssignDirector
        )
        CompanyNavTab.APPLICANTS -> CompanyApplicantsScreen(
            briefs = briefs,
            applications = applications,
            isDesktop = isDesktop,
            onDecideApplication = onDecideApplication
        )
        CompanyNavTab.TALENT -> CompanyTalentScreen(
            students = students,
            isDesktop = isDesktop
        )
    }
}

@Composable
private fun CompanyOverviewScreen(
    company: CompanyEntity?,
    briefs: List<ProjectBriefEntity>,
    applications: List<ProjectApplicationEntity>,
    activities: List<PlatformActivityEntity>,
    isDesktop: Boolean,
    onNavigateTab: (CompanyNavTab) -> Unit
) {
    val lang = LocalAppLanguage.current
    val publishedCount = briefs.count { it.approvalStatus in setOf("APPROVED", "ACTIVE", "COMPLETED") }
    val activeCount = briefs.count { it.approvalStatus == "ACTIVE" }
    val completedCount = briefs.count { it.approvalStatus == "COMPLETED" }
    val newApps = applications.count { it.status == "APPLIED" }
    val shortlisted = applications.count { it.status == "SHORTLISTED" }
    val accepted = applications.count { it.status == "ACCEPTED" }
    val rejected = applications.count { it.status == "REJECTED" }
    val pendingDecisions = newApps + shortlisted
    val completionRate = if ((activeCount + completedCount) == 0) 0 else (completedCount * 100) / (activeCount + completedCount)

    val funnelSegments = listOf(
        ChartDataPoint("Applied", newApps.toFloat(), SkillBridgeColors.BridgeBlue),
        ChartDataPoint("Shortlist", shortlisted.toFloat(), SkillBridgeColors.PendingAmber),
        ChartDataPoint("Accepted", accepted.toFloat(), SkillBridgeColors.VerifiedGreen),
        ChartDataPoint("Rejected", rejected.toFloat(), SkillBridgeColors.DisputedRed)
    )

    val appsPerBrief = briefs.take(5).map { b ->
        ChartDataPoint(
            label = b.title.split(" ").take(2).joinToString(" "),
            value = applications.count { it.briefId == b.id }.toFloat(),
            color = SkillBridgeColors.BridgeBlueDark
        )
    }

    val relevantActs = activities.filter { act ->
        act.actorAccountId == company?.ownerAccountId ||
            briefs.any { b -> act.summary.contains(b.title, ignoreCase = true) || act.targetId == b.id }
    }.take(8)

    LazyColumn(
        modifier = Modifier.fillMaxSize().testTag("company_dashboard_root"),
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = company?.name ?: "Company Partner",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = SkillBridgeColors.PureWhite
                            )
                            Text(
                                text = "${company?.city ?: "Cairo"} • ${company?.domain ?: "Technology"}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = SkillBridgeColors.SoftCyan
                            )
                        }
                        StatusPill(
                            text = "${company?.verificationTier ?: "VERIFIED"}",
                            tone = if (company?.verificationStatus == "VERIFIED") StatusTone.VERIFIED else StatusTone.PENDING,
                            icon = Icons.Filled.Verified
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { onNavigateTab(CompanyNavTab.BRIEFS) },
                            colors = ButtonDefaults.buttonColors(containerColor = SkillBridgeColors.BridgeBlue, contentColor = SkillBridgeColors.PureWhite),
                            modifier = Modifier.height(44.dp)
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(if (lang == AppLanguage.AR) "نشر مشروع" else "Create Brief")
                        }
                        OutlinedButton(
                            onClick = { onNavigateTab(CompanyNavTab.APPLICANTS) },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = SkillBridgeColors.PureWhite),
                            border = BorderStroke(1.dp, SkillBridgeColors.SoftCyan.copy(alpha = 0.5f)),
                            modifier = Modifier.height(44.dp)
                        ) {
                            Text(if (lang == AppLanguage.AR) "المتقدمون ($pendingDecisions)" else "Applicants ($pendingDecisions pending)")
                        }
                    }
                }
            }
        }

        item {
            ResponsiveKpiGrid(
                isDesktop = isDesktop,
                desktopColumns = 4,
                items = listOf(
                    { mod ->
                        SaasKpiTile(
                            title = if (lang == AppLanguage.AR) "المشاريع المنشورة" else "Published Briefs",
                            value = "$publishedCount / ${briefs.size}",
                            subtitle = "$activeCount active • $completedCount completed",
                            icon = Icons.Filled.Assignment,
                            accentColor = SkillBridgeColors.BridgeBlueDark,
                            modifier = mod,
                            onClick = { onNavigateTab(CompanyNavTab.BRIEFS) }
                        )
                    },
                    { mod ->
                        SaasKpiTile(
                            title = if (lang == AppLanguage.AR) "إجمالي المتقدمين" else "Total Applicants",
                            value = "${applications.size}",
                            subtitle = "$newApps new • $shortlisted shortlisted",
                            icon = Icons.Filled.GroupAdd,
                            accentColor = SkillBridgeColors.DeepNavy,
                            modifier = mod,
                            onClick = { onNavigateTab(CompanyNavTab.APPLICANTS) }
                        )
                    },
                    { mod ->
                        SaasKpiTile(
                            title = if (lang == AppLanguage.AR) "المقبولون" else "Accepted Candidates",
                            value = "$accepted",
                            subtitle = "$pendingDecisions decisions pending",
                            icon = Icons.Filled.CheckCircle,
                            accentColor = SkillBridgeColors.VerifiedGreen,
                            modifier = mod
                        )
                    },
                    { mod ->
                        SaasKpiTile(
                            title = if (lang == AppLanguage.AR) "نسبة الإتمام" else "Completion Rate",
                            value = "$completionRate%",
                            subtitle = "${briefs.count { it.approvalStatus == "PENDING_APPROVAL" }} awaiting Admin",
                            icon = Icons.Filled.HourglassTop,
                            accentColor = SkillBridgeColors.PaidTeal,
                            modifier = mod
                        )
                    }
                )
            )
        }

        item {
            ResponsiveChartRow(
                isDesktop = isDesktop,
                charts = listOf(
                    { mod ->
                        SkillBridgeDoughnutChart(
                            title = if (lang == AppLanguage.AR) "توزيع قرارات المتقدمين" else "Candidate Application Funnel",
                            subtitle = if (lang == AppLanguage.AR) "بيانات حية من طلبات التوظيف" else "Live counts from your project briefs",
                            centerLabel = "Total",
                            segments = funnelSegments,
                            modifier = mod
                        )
                    },
                    { mod ->
                        SkillBridgeBarChart(
                            title = if (lang == AppLanguage.AR) "أداء المشاريع (المتقدمون)" else "Brief Performance (Applicants)",
                            subtitle = if (lang == AppLanguage.AR) "تحدّث فورًا مع كل طلب جديد" else "Applicant volume per company brief",
                            bars = appsPerBrief,
                            modifier = mod
                        )
                    }
                )
            )
        }

        item {
            SectionHeaderRow(
                title = if (lang == AppLanguage.AR) "أحدث نشاطات الشركة" else "Recent Company Activity",
                actionText = if (lang == AppLanguage.AR) "المواهب" else "Discover Talent",
                onAction = { onNavigateTab(CompanyNavTab.TALENT) }
            )
        }

        if (relevantActs.isEmpty()) {
            item { EmptyStateView(subtitle = "Create a project brief or review applicants to record activity.") }
        } else if (isDesktop) {
            item {
                DesktopDataTable(
                    columns = listOf(
                        DataTableColumn("Summary", 2.4f),
                        DataTableColumn("Action Type", 1.1f, alignEnd = true)
                    ),
                    rows = relevantActs,
                    rowKey = { it.id }
                ) { act, col ->
                    when (col) {
                        0 -> Text(act.summary, style = MaterialTheme.typography.bodyMedium)
                        else -> Text(act.actionType, style = MaterialTheme.typography.labelMedium, color = SkillBridgeColors.BridgeBlueDark)
                    }
                }
            }
        } else {
            items(relevantActs, key = { it.id }) { act ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(act.summary, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                        Text(act.actionType, style = MaterialTheme.typography.labelSmall, color = SkillBridgeColors.BridgeBlueDark)
                    }
                }
            }
        }
        item { Spacer(Modifier.height(16.dp)) }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CompanyBriefsScreen(
    directors: List<AccountEntity>,
    briefs: List<ProjectBriefEntity>,
    applications: List<ProjectApplicationEntity>,
    isDesktop: Boolean,
    onCreateBrief: (String, String, String, String, List<String>, List<String>, List<String>, String, Int, String, String, Boolean) -> Unit,
    onAssignDirector: (String, String) -> Unit
) {
    val lang = LocalAppLanguage.current
    var showCreateDialog by remember { mutableStateOf(false) }
    var selectedBriefId by remember(briefs) { mutableStateOf(briefs.firstOrNull()?.id) }

    if (isDesktop) {
        Box(modifier = Modifier.fillMaxSize().testTag("company_briefs_root")) {
            DesktopListDetailSplit(
                leftWeight = 0.48f,
                leftPane = {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (lang == AppLanguage.AR) "مشاريع الشركة (${briefs.size})" else "Company Briefs (${briefs.size})",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Button(
                                    onClick = { showCreateDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = SkillBridgeColors.DeepNavy),
                                    modifier = Modifier.height(46.dp).testTag("company_new_brief_btn")
                                ) {
                                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text(if (lang == AppLanguage.AR) "مشروع جديد" else "New Brief")
                                }
                            }
                        }
                        if (briefs.isEmpty()) {
                            item { EmptyStateView(subtitle = "Publish your first project brief to receive student applications.") }
                        } else {
                            item {
                                DesktopDataTable(
                                    columns = listOf(
                                        DataTableColumn("Brief Title", 1.7f),
                                        DataTableColumn("Apps", 0.6f),
                                        DataTableColumn("Status", 0.9f, alignEnd = true)
                                    ),
                                    rows = briefs,
                                    rowKey = { it.id },
                                    selectedKey = selectedBriefId,
                                    onRowClick = { selectedBriefId = it.id }
                                ) { brief, col ->
                                    val appCount = applications.count { it.briefId == brief.id }
                                    val tone = when (brief.approvalStatus) {
                                        "APPROVED", "ACTIVE", "COMPLETED" -> StatusTone.VERIFIED
                                        "REJECTED" -> StatusTone.DISPUTED
                                        else -> StatusTone.PENDING
                                    }
                                    when (col) {
                                        0 -> Column {
                                            Text(brief.title, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            Text("${brief.domain} • ${brief.level}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        1 -> Text("$appCount", fontWeight = FontWeight.SemiBold)
                                        else -> StatusPill(brief.approvalStatus, tone)
                                    }
                                }
                            }
                        }
                    }
                },
                rightPane = {
                    val selected = briefs.firstOrNull { it.id == selectedBriefId } ?: briefs.firstOrNull()
                    if (selected == null) {
                        Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                            EmptyStateView(subtitle = "Select a project brief to inspect scope and assign a director.")
                        }
                    } else {
                        val appCount = applications.count { it.briefId == selected.id }
                        Column(
                            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(selected.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                                    Text("${selected.domain} • ${selected.level} • ${selected.pointsReward} pts • $appCount applicants", style = MaterialTheme.typography.bodySmall, color = SkillBridgeColors.BridgeBlueDark)
                                }
                                StatusPill(
                                    selected.approvalStatus,
                                    when (selected.approvalStatus) {
                                        "APPROVED", "ACTIVE", "COMPLETED" -> StatusTone.VERIFIED
                                        "REJECTED" -> StatusTone.DISPUTED
                                        else -> StatusTone.PENDING
                                    }
                                )
                            }
                            HorizontalDivider()
                            Text(selected.summary, style = MaterialTheme.typography.bodyMedium)
                            Text("Required Skills: ${selected.skillsCsv}", style = MaterialTheme.typography.labelMedium)
                            Text("Rubric: ${selected.rubricCsv}", style = MaterialTheme.typography.labelMedium, color = SkillBridgeColors.BridgeBlueDark)
                            Text("Deliverables: ${selected.deliverablesCsv} • Deadline: ${selected.deadline}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            if (selected.adminNotes.isNotBlank()) {
                                Text("Admin Note: ${selected.adminNotes}", style = MaterialTheme.typography.labelMedium, color = SkillBridgeColors.BridgeBlueDark)
                            }
                            if (directors.isNotEmpty()) {
                                HorizontalDivider()
                                Text(
                                    text = if (lang == AppLanguage.AR) "المشرف المعيّن:" else "Assigned Company Director:",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    directors.forEach { dir ->
                                        FilterChip(
                                            selected = selected.assignedDirectorId == dir.id,
                                            onClick = { onAssignDirector(selected.id, dir.id) },
                                            label = { Text(dir.displayName) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize().testTag("company_briefs_root"),
            contentPadding = PaddingValues(SkillBridgeSpacing.screenHorizontal),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (lang == AppLanguage.AR) "إدارة مشاريع الشركة (${briefs.size})" else "Company Project Briefs (${briefs.size})",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Button(
                        onClick = { showCreateDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = SkillBridgeColors.DeepNavy),
                        modifier = Modifier.height(46.dp).testTag("company_new_brief_btn")
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(if (lang == AppLanguage.AR) "مشروع جديد" else "New Brief")
                    }
                }
            }

            if (briefs.isEmpty()) {
                item { EmptyStateView(subtitle = "Publish your first project brief to receive student applications.") }
            } else {
                items(briefs, key = { it.id }) { brief ->
                    val appCount = applications.count { it.briefId == brief.id }
                    val tone = when (brief.approvalStatus) {
                        "APPROVED", "ACTIVE", "COMPLETED" -> StatusTone.VERIFIED
                        "REJECTED" -> StatusTone.DISPUTED
                        else -> StatusTone.PENDING
                    }
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(brief.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text("${brief.domain} • ${brief.level} • ${brief.pointsReward} pts • $appCount applicants", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                StatusPill(brief.approvalStatus, tone)
                            }
                            Text(brief.summary, style = MaterialTheme.typography.bodyMedium)
                            if (brief.adminNotes.isNotBlank()) {
                                Text("Admin Note: ${brief.adminNotes}", style = MaterialTheme.typography.labelMedium, color = SkillBridgeColors.BridgeBlueDark)
                            }
                            if (directors.isNotEmpty()) {
                                Text(
                                    text = if (lang == AppLanguage.AR) "المشرف المعيّن:" else "Assigned Director:",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    directors.forEach { dir ->
                                        FilterChip(
                                            selected = brief.assignedDirectorId == dir.id,
                                            onClick = { onAssignDirector(brief.id, dir.id) },
                                            label = { Text(dir.displayName) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(16.dp)) }
        }
    }

    if (showCreateDialog) {
        var title by remember { mutableStateOf("") }
        var summary by remember { mutableStateOf("") }
        var domain by remember { mutableStateOf(SkillBridgeEcosystemData.domains.first()) }
        var level by remember { mutableStateOf("Mid-level") }
        var skillsText by remember { mutableStateOf("Kotlin, SQL, REST APIs") }
        var deadline by remember { mutableStateOf("Mar 30, 2027") }
        var prizePool by remember { mutableStateOf("$500 Stipend Pool") }
        var selectedDirectorId by remember { mutableStateOf(directors.firstOrNull()?.id.orEmpty()) }
        var allowTeam by remember { mutableStateOf(true) }

        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Publish New Project Brief") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Project Title") },
                        modifier = Modifier.fillMaxWidth().testTag("brief_title_input")
                    )
                    OutlinedTextField(
                        value = summary,
                        onValueChange = { summary = it },
                        label = { Text("Scope & Technical Requirements") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth().testTag("brief_summary_input")
                    )
                    OutlinedTextField(
                        value = skillsText,
                        onValueChange = { skillsText = it },
                        label = { Text("Required Skills (comma-separated)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = deadline,
                            onValueChange = { deadline = it },
                            label = { Text("Deadline") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = prizePool,
                            onValueChange = { prizePool = it },
                            label = { Text("Reward / Stipend") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Allow Teams", style = MaterialTheme.typography.bodyMedium)
                        Switch(checked = allowTeam, onCheckedChange = { allowTeam = it })
                    }
                    if (directors.isNotEmpty()) {
                        Text("Assign Company Director:", style = MaterialTheme.typography.labelMedium)
                        Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            directors.forEach { dir ->
                                FilterChip(
                                    selected = selectedDirectorId == dir.id,
                                    onClick = { selectedDirectorId = dir.id },
                                    label = { Text(dir.displayName) }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onCreateBrief(
                            title,
                            summary,
                            domain,
                            level,
                            skillsText.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                            listOf("Architecture (40%)", "Tests & Reliability (35%)", "Documentation (25%)"),
                            listOf("Git Repository", "Understanding Summary", "Test Report"),
                            deadline,
                            240,
                            prizePool,
                            selectedDirectorId,
                            allowTeam
                        )
                        showCreateDialog = false
                    },
                    enabled = title.trim().length >= 4 && summary.trim().length >= 10,
                    colors = ButtonDefaults.buttonColors(containerColor = SkillBridgeColors.DeepNavy),
                    modifier = Modifier.testTag("brief_submit_btn")
                ) { Text("Create Brief") }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun CompanyApplicantsScreen(
    briefs: List<ProjectBriefEntity>,
    applications: List<ProjectApplicationEntity>,
    isDesktop: Boolean,
    onDecideApplication: (String, String, String) -> Unit
) {
    val lang = LocalAppLanguage.current
    var statusFilter by remember { mutableStateOf("ALL") }
    var search by remember { mutableStateOf("") }

    val briefMap = remember(briefs) { briefs.associateBy { it.id } }
    val filtered = applications.filter { app ->
        (statusFilter == "ALL" || app.status == statusFilter) &&
            (search.isBlank() ||
                app.studentName.contains(search, ignoreCase = true) ||
                app.roleRequested.contains(search, ignoreCase = true) ||
                (briefMap[app.briefId]?.title?.contains(search, ignoreCase = true) == true))
    }

    var selectedAppId by remember(filtered) { mutableStateOf(filtered.firstOrNull()?.id) }

    if (isDesktop) {
        Box(modifier = Modifier.fillMaxSize().testTag("company_applicants_root")) {
            DesktopListDetailSplit(
                leftWeight = 0.52f,
                leftPane = {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            OutlinedTextField(
                                value = search,
                                onValueChange = { search = it },
                                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                                placeholder = { Text(if (lang == AppLanguage.AR) "ابحث باسم الطالب أو المشروع..." else "Search applicants by name, role, or brief") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(8.dp))
                            Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf("ALL", "APPLIED", "SHORTLISTED", "ACCEPTED", "REJECTED").forEach { st ->
                                    FilterChip(selected = statusFilter == st, onClick = { statusFilter = st }, label = { Text(st) })
                                }
                            }
                        }

                        if (filtered.isEmpty()) {
                            item { EmptyStateView(subtitle = "No matching student applications.") }
                        } else {
                            items(filtered, key = { it.id }) { app ->
                                val briefTitle = briefMap[app.briefId]?.title ?: app.briefId
                                val tone = when (app.status) {
                                    "ACCEPTED" -> StatusTone.VERIFIED
                                    "SHORTLISTED" -> StatusTone.REVIEW
                                    "REJECTED" -> StatusTone.DISPUTED
                                    else -> StatusTone.PENDING
                                }
                                Card(
                                    modifier = Modifier.fillMaxWidth().clickable { selectedAppId = app.id },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (selectedAppId == app.id) SkillBridgeColors.SoftCyanTint.copy(alpha = 0.4f)
                                        else MaterialTheme.colorScheme.surface
                                    ),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(app.studentName, fontWeight = FontWeight.Bold)
                                            Text("$briefTitle • ${app.roleRequested}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        }
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            StatusPill(app.status, tone)
                                            if (app.status != "ACCEPTED" && app.status != "REJECTED") {
                                                Button(
                                                    onClick = { onDecideApplication(app.id, "ACCEPTED", "Welcome to the project cohort!") },
                                                    colors = ButtonDefaults.buttonColors(containerColor = SkillBridgeColors.VerifiedGreen),
                                                    modifier = Modifier.height(38.dp).testTag("accept_app_${app.id}")
                                                ) { Text("Accept") }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                rightPane = {
                    val selected = filtered.firstOrNull { it.id == selectedAppId } ?: filtered.firstOrNull()
                    if (selected == null) {
                        Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                            EmptyStateView(subtitle = "Select a candidate application to inspect skills & cover note.")
                        }
                    } else {
                        val briefTitle = briefMap[selected.briefId]?.title ?: selected.briefId
                        Column(
                            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(selected.studentName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                            Text("$briefTitle • ${selected.roleRequested} • ${selected.studentLevel}", style = MaterialTheme.typography.bodyMedium, color = SkillBridgeColors.BridgeBlueDark)
                            StatusPill(
                                selected.status,
                                when (selected.status) {
                                    "ACCEPTED" -> StatusTone.VERIFIED
                                    "SHORTLISTED" -> StatusTone.REVIEW
                                    "REJECTED" -> StatusTone.DISPUTED
                                    else -> StatusTone.PENDING
                                }
                            )
                            HorizontalDivider()
                            Text("Declared Skills", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                            Text(selected.studentSkillsCsv, style = MaterialTheme.typography.bodyMedium, color = SkillBridgeColors.BridgeBlueDark)
                            Text("Readiness Pitch", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                            Text(selected.coverNote, style = MaterialTheme.typography.bodyMedium)
                            if (selected.decisionNote.isNotBlank()) {
                                Text("Decision Note: ${selected.decisionNote}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            if (selected.status != "ACCEPTED" && selected.status != "REJECTED") {
                                HorizontalDivider()
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                    if (selected.status == "APPLIED") {
                                        OutlinedButton(
                                            onClick = { onDecideApplication(selected.id, "SHORTLISTED", "Shortlisted for technical evaluation.") },
                                            modifier = Modifier.weight(1f).height(46.dp)
                                        ) { Text("Shortlist") }
                                    }
                                    OutlinedButton(
                                        onClick = { onDecideApplication(selected.id, "REJECTED", "Not aligned with current sprint capacity.") },
                                        modifier = Modifier.weight(1f).height(46.dp)
                                    ) { Text("Reject", color = SkillBridgeColors.DisputedRed) }
                                }
                            }
                        }
                    }
                }
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize().testTag("company_applicants_root"),
            contentPadding = PaddingValues(SkillBridgeSpacing.screenHorizontal),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                OutlinedTextField(
                    value = search,
                    onValueChange = { search = it },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    placeholder = { Text(if (lang == AppLanguage.AR) "ابحث باسم الطالب أو المشروع..." else "Search applicants by name, role, or brief") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("ALL", "APPLIED", "SHORTLISTED", "ACCEPTED", "REJECTED").forEach { st ->
                        FilterChip(selected = statusFilter == st, onClick = { statusFilter = st }, label = { Text(st) })
                    }
                }
            }

            if (filtered.isEmpty()) {
                item { EmptyStateView(subtitle = "No matching student applications.") }
            } else {
                items(filtered, key = { it.id }) { app ->
                    val briefTitle = briefMap[app.briefId]?.title ?: app.briefId
                    val tone = when (app.status) {
                        "ACCEPTED" -> StatusTone.VERIFIED
                        "SHORTLISTED" -> StatusTone.REVIEW
                        "REJECTED" -> StatusTone.DISPUTED
                        else -> StatusTone.PENDING
                    }
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(app.studentName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text("$briefTitle • ${app.roleRequested} • ${app.studentLevel}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                StatusPill(app.status, tone)
                            }
                            Text("Skills: ${app.studentSkillsCsv}", style = MaterialTheme.typography.labelMedium, color = SkillBridgeColors.BridgeBlueDark)
                            Text(app.coverNote, style = MaterialTheme.typography.bodyMedium)
                            if (app.status != "ACCEPTED" && app.status != "REJECTED") {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                                    if (app.status == "APPLIED") {
                                        OutlinedButton(
                                            onClick = { onDecideApplication(app.id, "SHORTLISTED", "Shortlisted for technical evaluation.") },
                                            modifier = Modifier.weight(1f).height(44.dp)
                                        ) { Text("Shortlist") }
                                    }
                                    Button(
                                        onClick = { onDecideApplication(app.id, "ACCEPTED", "Welcome to the project cohort!") },
                                        colors = ButtonDefaults.buttonColors(containerColor = SkillBridgeColors.VerifiedGreen),
                                        modifier = Modifier.weight(1f).height(44.dp).testTag("accept_app_${app.id}")
                                    ) { Text("Accept") }
                                    OutlinedButton(
                                        onClick = { onDecideApplication(app.id, "REJECTED", "Not aligned with current sprint capacity.") },
                                        modifier = Modifier.weight(1f).height(44.dp)
                                    ) { Text("Reject", color = SkillBridgeColors.DisputedRed) }
                                }
                            }
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun CompanyTalentScreen(
    students: List<AccountEntity>,
    isDesktop: Boolean
) {
    val lang = LocalAppLanguage.current
    var query by remember { mutableStateOf("") }
    val filtered = students.filter {
        query.isBlank() ||
            it.displayName.contains(query, ignoreCase = true) ||
            it.skillsCsv.contains(query, ignoreCase = true) ||
            it.domain.contains(query, ignoreCase = true)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().testTag("company_talent_root"),
        contentPadding = PaddingValues(if (isDesktop) 24.dp else SkillBridgeSpacing.screenHorizontal),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionHeaderRow(title = if (lang == AppLanguage.AR) "استكشاف المواهب الموثّقة" else "Verified Student Talent Explorer")
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                leadingIcon = { Icon(Icons.Filled.PersonSearch, contentDescription = null) },
                placeholder = { Text("Search by skill, domain, or student name") },
                modifier = Modifier.fillMaxWidth()
            )
        }
        if (isDesktop) {
            item {
                DesktopDataTable(
                    columns = listOf(
                        DataTableColumn("Student", 1.3f),
                        DataTableColumn("Domain & Country", 1.3f),
                        DataTableColumn("Skills", 1.8f),
                        DataTableColumn("Evidence & Pts", 1.0f),
                        DataTableColumn("Level", 0.8f, alignEnd = true)
                    ),
                    rows = filtered,
                    rowKey = { it.id },
                    emptyMessage = "No matching student profiles."
                ) { st, col ->
                    when (col) {
                        0 -> Text(st.displayName, fontWeight = FontWeight.Bold)
                        1 -> Text("${st.domain} • ${st.country}", style = MaterialTheme.typography.bodySmall)
                        2 -> Text(st.skillsCsv.ifBlank { "In training" }, style = MaterialTheme.typography.bodySmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        3 -> Text("${st.verifiedCount} verified • ${st.points} pts", style = MaterialTheme.typography.labelMedium, color = SkillBridgeColors.BridgeBlueDark)
                        else -> StatusPill(st.careerLevel, StatusTone.VERIFIED, Icons.Filled.Star)
                    }
                }
            }
        } else {
            items(filtered, key = { it.id }) { st ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(st.displayName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text("${st.domain} • ${st.country}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            StatusPill(st.careerLevel, StatusTone.VERIFIED, Icons.Filled.Star)
                        }
                        Text("Verified Evidence: ${st.verifiedCount} • Points: ${st.points} • ${st.weeklyHours}h/wk", style = MaterialTheme.typography.labelMedium, color = SkillBridgeColors.BridgeBlueDark)
                        Text("Skills: ${st.skillsCsv.ifBlank { "In training" }}", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
        item { Spacer(Modifier.height(16.dp)) }
    }
}

