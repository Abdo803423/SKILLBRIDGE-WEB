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
import androidx.compose.material.icons.filled.AddTask
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.FolderShared
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.core.AppLanguage
import com.example.core.LocalAppLanguage
import com.example.data.local.ProjectApplicationEntity
import com.example.data.local.ProjectBriefEntity
import com.example.data.local.ProjectTaskEntity
import com.example.data.local.WorkSubmissionEntity
import com.example.ui.components.ChartDataPoint
import com.example.ui.components.DataTableColumn
import com.example.ui.components.DesktopDataTable
import com.example.ui.components.DesktopListDetailSplit
import com.example.ui.components.EmptyStateView
import com.example.ui.components.InfoNoteBanner
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
import kotlin.math.roundToInt

enum class DirectorNavTab { OVERVIEW, PROJECTS, REVIEWS, EVALUATIONS }

@Composable
fun CompanyDirectorPortalContent(
    tab: DirectorNavTab,
    directorName: String,
    assignedBriefs: List<ProjectBriefEntity>,
    assignedTasks: List<ProjectTaskEntity>,
    assignedSubmissions: List<WorkSubmissionEntity>,
    companyApplications: List<ProjectApplicationEntity>,
    isDesktop: Boolean = false,
    onNavigateTab: (DirectorNavTab) -> Unit,
    onEvaluateSubmission: (submissionId: String, score: Int, feedback: String, decision: String, rejectionReason: String) -> Unit,
    onCreateTask: (briefId: String, studentId: String, title: String, dueDate: String) -> Unit,
    onCompleteBrief: (briefId: String) -> Unit
) {
    val assignedBriefIds = remember(assignedBriefs) { assignedBriefs.map { it.id }.toSet() }
    val supervisedAcceptedApps = remember(companyApplications, assignedBriefIds) {
        companyApplications.filter { it.briefId in assignedBriefIds && it.status == "ACCEPTED" }
    }

    when (tab) {
        DirectorNavTab.OVERVIEW -> DirectorOverviewScreen(
            directorName = directorName,
            assignedBriefs = assignedBriefs,
            assignedTasks = assignedTasks,
            assignedSubmissions = assignedSubmissions,
            supervisedApps = supervisedAcceptedApps,
            isDesktop = isDesktop,
            onNavigateTab = onNavigateTab
        )
        DirectorNavTab.PROJECTS -> DirectorProjectsScreen(
            assignedBriefs = assignedBriefs,
            assignedTasks = assignedTasks,
            supervisedApps = supervisedAcceptedApps,
            isDesktop = isDesktop,
            onCreateTask = onCreateTask,
            onCompleteBrief = onCompleteBrief
        )
        DirectorNavTab.REVIEWS -> DirectorReviewsScreen(
            assignedBriefs = assignedBriefs,
            submissions = assignedSubmissions,
            isDesktop = isDesktop,
            onEvaluateSubmission = onEvaluateSubmission
        )
        DirectorNavTab.EVALUATIONS -> DirectorEvaluationsHistoryScreen(
            submissions = assignedSubmissions,
            isDesktop = isDesktop
        )
    }
}

@Composable
private fun DirectorOverviewScreen(
    directorName: String,
    assignedBriefs: List<ProjectBriefEntity>,
    assignedTasks: List<ProjectTaskEntity>,
    assignedSubmissions: List<WorkSubmissionEntity>,
    supervisedApps: List<ProjectApplicationEntity>,
    isDesktop: Boolean,
    onNavigateTab: (DirectorNavTab) -> Unit
) {
    val lang = LocalAppLanguage.current
    val pendingSubs = assignedSubmissions.count { it.status == "PENDING_REVIEW" }
    val verifiedSubs = assignedSubmissions.count { it.status == "VERIFIED" }
    val revisionSubs = assignedSubmissions.count { it.status == "REVISION_REQUESTED" }
    val rejectedSubs = assignedSubmissions.count { it.status == "REJECTED" }
    val supervisedStudentsCount = supervisedApps.map { it.studentAccountId }.distinct().size
    val openTasks = assignedTasks.count { it.status != "DONE" }
    val overdueTasks = assignedTasks.count { it.isOverdue && it.status != "DONE" }

    val evaluated = assignedSubmissions.filter { it.evaluatedAtEpochMs > 0L && it.submittedAtEpochMs > 0L }
    val avgTurnaroundHours = if (evaluated.isEmpty()) 0 else {
        (evaluated.sumOf { (it.evaluatedAtEpochMs - it.submittedAtEpochMs).coerceAtLeast(3_600_000L) } / evaluated.size / 3_600_000L).toInt()
    }

    val reviewBreakdown = listOf(
        ChartDataPoint("Pending", pendingSubs.toFloat(), SkillBridgeColors.PendingAmber),
        ChartDataPoint("Verified", verifiedSubs.toFloat(), SkillBridgeColors.VerifiedGreen),
        ChartDataPoint("Revision", revisionSubs.toFloat(), SkillBridgeColors.ReviewBlue),
        ChartDataPoint("Rejected", rejectedSubs.toFloat(), SkillBridgeColors.DisputedRed)
    )

    val projectProgressBars = assignedBriefs.take(5).map { b ->
        val bTasks = assignedTasks.filter { it.briefId == b.id }
        val pct = if (b.approvalStatus == "COMPLETED") 100f
        else if (bTasks.isEmpty()) 25f
        else (bTasks.count { it.status == "DONE" } * 100f) / bTasks.size
        ChartDataPoint(
            label = b.title.split(" ").take(2).joinToString(" "),
            value = pct,
            color = SkillBridgeColors.BridgeBlue
        )
    }

    val recentEvals = assignedSubmissions.filter { it.status != "PENDING_REVIEW" }.take(6)

    LazyColumn(
        modifier = Modifier.fillMaxSize().testTag("director_dashboard_root"),
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
                        Icon(Icons.Filled.SupervisorAccount, contentDescription = null, tint = SkillBridgeColors.SoftCyan)
                        Text(
                            text = directorName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = SkillBridgeColors.PureWhite
                        )
                    }
                    Text(
                        text = if (lang == AppLanguage.AR)
                            "مشرف مشاريع الشركة • ترى فقط المشاريع والتسليمات المسندة إليك (${assignedBriefs.size})"
                        else
                            "Company Director Supervision • Scoped strictly to your ${assignedBriefs.size} assigned project(s)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SkillBridgeColors.SoftCyan
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { onNavigateTab(DirectorNavTab.REVIEWS) },
                            colors = ButtonDefaults.buttonColors(containerColor = SkillBridgeColors.BridgeBlue, contentColor = SkillBridgeColors.PureWhite),
                            modifier = Modifier.height(44.dp)
                        ) {
                            Text("Pending Reviews ($pendingSubs)")
                        }
                        OutlinedButton(
                            onClick = { onNavigateTab(DirectorNavTab.PROJECTS) },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = SkillBridgeColors.PureWhite),
                            border = BorderStroke(1.dp, SkillBridgeColors.SoftCyan.copy(alpha = 0.5f)),
                            modifier = Modifier.height(44.dp)
                        ) {
                            Text("Assigned Projects")
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
                            title = if (lang == AppLanguage.AR) "المشاريع المسندة" else "Assigned Projects",
                            value = "${assignedBriefs.size}",
                            subtitle = "$supervisedStudentsCount supervised student(s)",
                            icon = Icons.Filled.FolderShared,
                            accentColor = SkillBridgeColors.DeepNavy,
                            modifier = mod,
                            onClick = { onNavigateTab(DirectorNavTab.PROJECTS) }
                        )
                    },
                    { mod ->
                        SaasKpiTile(
                            title = if (lang == AppLanguage.AR) "مراجعات بانتظار التقييم" else "Awaiting Evaluation",
                            value = "$pendingSubs",
                            subtitle = "$verifiedSubs verified total",
                            icon = Icons.Filled.PendingActions,
                            accentColor = SkillBridgeColors.PendingAmber,
                            modifier = mod,
                            onClick = { onNavigateTab(DirectorNavTab.REVIEWS) }
                        )
                    },
                    { mod ->
                        SaasKpiTile(
                            title = if (lang == AppLanguage.AR) "المهام المفتوحة" else "Milestones & Overdue",
                            value = "$openTasks",
                            subtitle = "$overdueTasks overdue • ${assignedTasks.count { it.status == "DONE" }} done",
                            icon = Icons.Filled.Schedule,
                            accentColor = SkillBridgeColors.BridgeBlueDark,
                            modifier = mod
                        )
                    },
                    { mod ->
                        SaasKpiTile(
                            title = if (lang == AppLanguage.AR) "سرعة التقييم" else "Review Turnaround",
                            value = if (avgTurnaroundHours > 0) "${avgTurnaroundHours}h" else "Live",
                            subtitle = "${evaluated.size} evaluated deliverables",
                            icon = Icons.Filled.FactCheck,
                            accentColor = SkillBridgeColors.VerifiedGreen,
                            modifier = mod,
                            onClick = { onNavigateTab(DirectorNavTab.EVALUATIONS) }
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
                            title = if (lang == AppLanguage.AR) "حالة تقييم التسليمات" else "Supervised Submission Breakdown",
                            subtitle = "Scoped strictly to your assigned company projects",
                            centerLabel = "Total",
                            segments = reviewBreakdown,
                            modifier = mod
                        )
                    },
                    { mod ->
                        SkillBridgeBarChart(
                            title = if (lang == AppLanguage.AR) "تقدم إنجاز المشاريع المسندة (%)" else "Assigned Project Progress (%)",
                            subtitle = "Computed from completed milestone tasks",
                            bars = projectProgressBars,
                            modifier = mod
                        )
                    }
                )
            )
        }

        item {
            SectionHeaderRow(
                title = if (lang == AppLanguage.AR) "أحدث التقييمات" else "Recent Evaluations",
                actionText = "View All",
                onAction = { onNavigateTab(DirectorNavTab.EVALUATIONS) }
            )
        }

        if (recentEvals.isEmpty()) {
            item { EmptyStateView(subtitle = "No completed evaluations yet.") }
        } else if (isDesktop) {
            item {
                DesktopDataTable(
                    columns = listOf(
                        DataTableColumn("Student & Project", 1.5f),
                        DataTableColumn("Director Feedback", 2.0f),
                        DataTableColumn("Score", 0.7f, alignEnd = true)
                    ),
                    rows = recentEvals,
                    rowKey = { it.id }
                ) { sub, col ->
                    when (col) {
                        0 -> Text("${sub.studentName} • ${sub.briefTitle}", fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        1 -> Text(sub.feedback, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        else -> StatusPill("${sub.score}%", if (sub.status == "VERIFIED") StatusTone.VERIFIED else StatusTone.DISPUTED)
                    }
                }
            }
        } else {
            items(recentEvals, key = { it.id }) { sub ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("${sub.studentName} • ${sub.briefTitle}", fontWeight = FontWeight.Bold)
                            Text(sub.feedback, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        StatusPill("${sub.score}%", if (sub.status == "VERIFIED") StatusTone.VERIFIED else StatusTone.DISPUTED)
                    }
                }
            }
        }
        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
private fun DirectorProjectsScreen(
    assignedBriefs: List<ProjectBriefEntity>,
    assignedTasks: List<ProjectTaskEntity>,
    supervisedApps: List<ProjectApplicationEntity>,
    isDesktop: Boolean,
    onCreateTask: (briefId: String, studentId: String, title: String, dueDate: String) -> Unit,
    onCompleteBrief: (briefId: String) -> Unit
) {
    var taskTargetBrief by remember { mutableStateOf<ProjectBriefEntity?>(null) }
    var selectedBriefId by remember(assignedBriefs) { mutableStateOf(assignedBriefs.firstOrNull()?.id) }

    if (isDesktop) {
        Box(modifier = Modifier.fillMaxSize().testTag("director_projects_root")) {
            DesktopListDetailSplit(
                leftWeight = 0.45f,
                leftPane = {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            InfoNoteBanner(
                                text = "Role isolation active: you only see briefs assigned to you by your Company."
                            )
                        }
                        if (assignedBriefs.isEmpty()) {
                            item { EmptyStateView(subtitle = "No project briefs have been assigned to you yet.") }
                        } else {
                            items(assignedBriefs, key = { it.id }) { brief ->
                                val briefTasks = assignedTasks.filter { it.briefId == brief.id }
                                val acceptedStudents = supervisedApps.filter { it.briefId == brief.id }
                                val progress = if (brief.approvalStatus == "COMPLETED") 1f
                                else if (briefTasks.isEmpty()) 0.1f
                                else briefTasks.count { it.status == "DONE" }.toFloat() / briefTasks.size.toFloat()

                                Card(
                                    modifier = Modifier.fillMaxWidth().clickable { selectedBriefId = brief.id },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (selectedBriefId == brief.id) SkillBridgeColors.SoftCyanTint.copy(alpha = 0.4f)
                                        else MaterialTheme.colorScheme.surface
                                    ),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(brief.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                                Text("${brief.companyName} • ${acceptedStudents.size} student(s) • Due ${brief.deadline}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                            StatusPill(
                                                brief.approvalStatus,
                                                if (brief.approvalStatus == "COMPLETED" || brief.approvalStatus == "ACTIVE") StatusTone.VERIFIED else StatusTone.REVIEW
                                            )
                                        }
                                        LinearProgressIndicator(
                                            progress = { progress },
                                            modifier = Modifier.fillMaxWidth().height(7.dp).clip(RoundedCornerShape(999.dp)),
                                            color = SkillBridgeColors.BridgeBlue
                                        )
                                        if (brief.approvalStatus != "COMPLETED") {
                                            Button(
                                                onClick = { onCompleteBrief(brief.id) },
                                                colors = ButtonDefaults.buttonColors(containerColor = SkillBridgeColors.DeepNavy),
                                                modifier = Modifier.fillMaxWidth().height(42.dp).testTag("complete_brief_${brief.id}")
                                            ) {
                                                Icon(Icons.Filled.AssignmentTurnedIn, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(Modifier.width(4.dp))
                                                Text("Complete Project")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                rightPane = {
                    val selected = assignedBriefs.firstOrNull { it.id == selectedBriefId } ?: assignedBriefs.firstOrNull()
                    if (selected == null) {
                        Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                            EmptyStateView(subtitle = "Select an assigned brief to manage milestones & cohorts.")
                        }
                    } else {
                        val briefTasks = assignedTasks.filter { it.briefId == selected.id }
                        val acceptedStudents = supervisedApps.filter { it.briefId == selected.id }
                        Column(
                            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(selected.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                                    Text("${selected.companyName} • ${selected.domain} • ${selected.level}", style = MaterialTheme.typography.bodySmall, color = SkillBridgeColors.BridgeBlueDark)
                                }
                                if (selected.approvalStatus != "COMPLETED" && acceptedStudents.isNotEmpty()) {
                                    OutlinedButton(onClick = { taskTargetBrief = selected }, modifier = Modifier.height(44.dp)) {
                                        Icon(Icons.Filled.AddTask, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Assign Task")
                                    }
                                }
                            }
                            HorizontalDivider()
                            Text(selected.summary, style = MaterialTheme.typography.bodyMedium)
                            Text("Rubric: ${selected.rubricCsv}", style = MaterialTheme.typography.labelMedium, color = SkillBridgeColors.BridgeBlueDark)
                            HorizontalDivider()
                            SectionHeaderRow(title = "Milestone Tasks (${briefTasks.size})")
                            DesktopDataTable(
                                columns = listOf(
                                    DataTableColumn("Task Title", 1.8f),
                                    DataTableColumn("Student", 1.1f),
                                    DataTableColumn("Due", 0.7f),
                                    DataTableColumn("Status", 0.9f, alignEnd = true)
                                ),
                                rows = briefTasks,
                                rowKey = { it.id },
                                emptyMessage = "No milestone tasks assigned yet."
                            ) { t, col ->
                                when (col) {
                                    0 -> Text(t.title, fontWeight = FontWeight.SemiBold)
                                    1 -> Text(t.assignedStudentName, style = MaterialTheme.typography.bodySmall)
                                    2 -> Text(t.dueDateLabel, style = MaterialTheme.typography.bodySmall)
                                    else -> StatusPill(t.status, if (t.status == "DONE") StatusTone.VERIFIED else StatusTone.REVIEW)
                                }
                            }
                        }
                    }
                }
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize().testTag("director_projects_root"),
            contentPadding = PaddingValues(SkillBridgeSpacing.screenHorizontal),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                InfoNoteBanner(
                    text = "Role isolation active: you only see briefs assigned to you by your Company."
                )
            }
            if (assignedBriefs.isEmpty()) {
                item { EmptyStateView(subtitle = "No project briefs have been assigned to you yet.") }
            } else {
                items(assignedBriefs, key = { it.id }) { brief ->
                    val briefTasks = assignedTasks.filter { it.briefId == brief.id }
                    val acceptedStudents = supervisedApps.filter { it.briefId == brief.id }
                    val progress = if (brief.approvalStatus == "COMPLETED") 1f
                    else if (briefTasks.isEmpty()) 0.1f
                    else briefTasks.count { it.status == "DONE" }.toFloat() / briefTasks.size.toFloat()

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(brief.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text("${brief.companyName} • ${acceptedStudents.size} student(s) • Due ${brief.deadline}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                StatusPill(
                                    brief.approvalStatus,
                                    if (brief.approvalStatus == "COMPLETED" || brief.approvalStatus == "ACTIVE") StatusTone.VERIFIED else StatusTone.REVIEW
                                )
                            }
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier.fillMaxWidth().height(7.dp).clip(RoundedCornerShape(999.dp)),
                                color = SkillBridgeColors.BridgeBlue
                            )
                            if (briefTasks.isNotEmpty()) {
                                briefTasks.forEach { t ->
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("• ${t.title} (${t.assignedStudentName})", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                                        Text(t.status, style = MaterialTheme.typography.labelSmall, color = SkillBridgeColors.BridgeBlueDark)
                                    }
                                }
                            }
                            if (brief.approvalStatus != "COMPLETED") {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                    if (acceptedStudents.isNotEmpty()) {
                                        OutlinedButton(
                                            onClick = { taskTargetBrief = brief },
                                            modifier = Modifier.weight(1f).height(44.dp)
                                        ) {
                                            Icon(Icons.Filled.AddTask, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text("Assign Task")
                                        }
                                    }
                                    Button(
                                        onClick = { onCompleteBrief(brief.id) },
                                        colors = ButtonDefaults.buttonColors(containerColor = SkillBridgeColors.DeepNavy),
                                        modifier = Modifier.weight(1f).height(44.dp).testTag("complete_brief_${brief.id}")
                                    ) {
                                        Icon(Icons.Filled.AssignmentTurnedIn, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Complete Project")
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

    taskTargetBrief?.let { brief ->
        val students = supervisedApps.filter { it.briefId == brief.id }
        var selectedStudentId by remember { mutableStateOf(students.firstOrNull()?.studentAccountId.orEmpty()) }
        var taskTitle by remember { mutableStateOf("") }
        var dueDate by remember { mutableStateOf("Dec 20") }

        AlertDialog(
            onDismissRequest = { taskTargetBrief = null },
            title = { Text("Assign Milestone Task") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        students.forEach { st ->
                            FilterChip(
                                selected = selectedStudentId == st.studentAccountId,
                                onClick = { selectedStudentId = st.studentAccountId },
                                label = { Text(st.studentName) }
                            )
                        }
                    }
                    OutlinedTextField(
                        value = taskTitle,
                        onValueChange = { taskTitle = it },
                        label = { Text("Milestone Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = dueDate,
                        onValueChange = { dueDate = it },
                        label = { Text("Due Date") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onCreateTask(brief.id, selectedStudentId, taskTitle, dueDate)
                        taskTargetBrief = null
                    },
                    enabled = selectedStudentId.isNotBlank() && taskTitle.trim().length >= 3,
                    colors = ButtonDefaults.buttonColors(containerColor = SkillBridgeColors.DeepNavy)
                ) { Text("Assign") }
            },
            dismissButton = { TextButton(onClick = { taskTargetBrief = null }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun DirectorReviewsScreen(
    assignedBriefs: List<ProjectBriefEntity>,
    submissions: List<WorkSubmissionEntity>,
    isDesktop: Boolean,
    onEvaluateSubmission: (String, Int, String, String, String) -> Unit
) {
    val briefMap = remember(assignedBriefs) { assignedBriefs.associateBy { it.id } }
    var rejectTarget by remember { mutableStateOf<Triple<WorkSubmissionEntity, Int, String>?>(null) }

    val pending = submissions.filter { it.status == "PENDING_REVIEW" || it.status == "REVISION_REQUESTED" }
    var selectedSubId by remember(pending) { mutableStateOf(pending.firstOrNull()?.id) }

    if (isDesktop) {
        Box(modifier = Modifier.fillMaxSize().testTag("director_reviews_root")) {
            DesktopListDetailSplit(
                leftWeight = 0.42f,
                leftPane = {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(20.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item { SectionHeaderRow(title = "Pending Submissions Queue (${pending.size})") }
                        if (pending.isEmpty()) {
                            item { EmptyStateView(subtitle = "All supervised project submissions have been evaluated.") }
                        } else {
                            items(pending, key = { it.id }) { sub ->
                                Surface(
                                    onClick = { selectedSubId = sub.id },
                                    shape = RoundedCornerShape(SkillBridgeRadius.md),
                                    color = if (selectedSubId == sub.id) SkillBridgeColors.SoftCyanTint.copy(alpha = 0.45f)
                                    else MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(
                                        1.dp,
                                        if (selectedSubId == sub.id) SkillBridgeColors.BridgeBlue else MaterialTheme.colorScheme.outlineVariant
                                    )
                                ) {
                                    Column(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text(sub.briefTitle, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            StatusPill(sub.status, StatusTone.REVIEW, Icons.Filled.RateReview)
                                        }
                                        Text("${sub.studentName} • ${sub.companyName}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                },
                rightPane = {
                    val selected = pending.firstOrNull { it.id == selectedSubId } ?: pending.firstOrNull()
                    if (selected == null) {
                        Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                            EmptyStateView(subtitle = "No pending deliverables awaiting rubric evaluation.")
                        }
                    } else {
                        val rubric = briefMap[selected.briefId]?.rubricCsv.orEmpty()
                        var score by remember(selected.id) { mutableFloatStateOf(92f) }
                        var feedback by remember(selected.id) {
                            mutableStateOf("Strong implementation and clear understanding report matching rubric criteria.")
                        }
                        Column(
                            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(selected.briefTitle, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                                    Text("Student: ${selected.studentName} • ${selected.companyName} • ${selected.verificationCode}", style = MaterialTheme.typography.bodySmall, color = SkillBridgeColors.BridgeBlueDark)
                                }
                                StatusPill(selected.status, StatusTone.REVIEW, Icons.Filled.RateReview)
                            }
                            HorizontalDivider()
                            if (selected.repoOrDemoUrl.isNotBlank()) {
                                Text("Artifact URL: ${selected.repoOrDemoUrl}", style = MaterialTheme.typography.labelLarge, color = SkillBridgeColors.BridgeBlueDark)
                            }
                            Text("Understanding Report", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                            Surface(
                                shape = RoundedCornerShape(SkillBridgeRadius.sm),
                                color = MaterialTheme.colorScheme.surfaceContainerLow,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Text(selected.understandingReport, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(14.dp))
                            }
                            if (rubric.isNotBlank()) {
                                Text("Evaluation Rubric: $rubric", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            HorizontalDivider()
                            Text("Evaluation Score: ${score.roundToInt()}%", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Slider(value = score, onValueChange = { score = it }, valueRange = 40f..100f)
                            OutlinedTextField(
                                value = feedback,
                                onValueChange = { feedback = it },
                                label = { Text("Director Evaluation Feedback") },
                                minLines = 2,
                                modifier = Modifier.fillMaxWidth().testTag("director_feedback_${selected.id}")
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                                Button(
                                    onClick = {
                                        onEvaluateSubmission(selected.id, score.roundToInt(), feedback, "VERIFIED", "")
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = SkillBridgeColors.VerifiedGreen),
                                    modifier = Modifier.weight(1f).height(48.dp).testTag("verify_submission_${selected.id}")
                                ) {
                                    Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Verify Deliverable")
                                }
                                OutlinedButton(
                                    onClick = { rejectTarget = Triple(selected, score.roundToInt(), feedback) },
                                    modifier = Modifier.weight(1f).height(48.dp)
                                ) { Text("Revise / Reject") }
                            }
                        }
                    }
                }
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize().testTag("director_reviews_root"),
            contentPadding = PaddingValues(SkillBridgeSpacing.screenHorizontal),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                SectionHeaderRow(title = "Pending Submissions Queue (${pending.size})")
            }
            if (pending.isEmpty()) {
                item { EmptyStateView(subtitle = "All supervised project submissions have been evaluated.") }
            } else {
                items(pending, key = { it.id }) { sub ->
                    val rubric = briefMap[sub.briefId]?.rubricCsv.orEmpty()
                    var score by remember(sub.id) { mutableFloatStateOf(92f) }
                    var feedback by remember(sub.id) {
                        mutableStateOf("Strong implementation and clear understanding report matching rubric criteria.")
                    }
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, SkillBridgeColors.BridgeBlue.copy(alpha = 0.45f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(sub.briefTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text("Student: ${sub.studentName} • ${sub.companyName}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                StatusPill(sub.status, StatusTone.REVIEW, Icons.Filled.RateReview)
                            }
                            if (sub.repoOrDemoUrl.isNotBlank()) {
                                Text("Artifact URL: ${sub.repoOrDemoUrl}", style = MaterialTheme.typography.labelMedium, color = SkillBridgeColors.BridgeBlueDark)
                            }
                            Text("Understanding Report: ${sub.understandingReport}", style = MaterialTheme.typography.bodyMedium)
                            if (rubric.isNotBlank()) {
                                Text("Rubric: $rubric", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text("Evaluation Score: ${score.roundToInt()}%", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                            Slider(value = score, onValueChange = { score = it }, valueRange = 40f..100f)
                            OutlinedTextField(
                                value = feedback,
                                onValueChange = { feedback = it },
                                label = { Text("Director Evaluation Feedback") },
                                modifier = Modifier.fillMaxWidth().testTag("director_feedback_${sub.id}")
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                Button(
                                    onClick = {
                                        onEvaluateSubmission(sub.id, score.roundToInt(), feedback, "VERIFIED", "")
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = SkillBridgeColors.VerifiedGreen),
                                    modifier = Modifier.weight(1f).height(46.dp).testTag("verify_submission_${sub.id}")
                                ) {
                                    Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Verify")
                                }
                                OutlinedButton(
                                    onClick = { rejectTarget = Triple(sub, score.roundToInt(), feedback) },
                                    modifier = Modifier.weight(1f).height(46.dp)
                                ) { Text("Revise / Reject") }
                            }
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(16.dp)) }
        }
    }

    rejectTarget?.let { (sub, sc, fb) ->
        var reason by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { rejectTarget = null },
            title = { Text("Request Revision / Reject") },
            text = {
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Required improvement reason") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onEvaluateSubmission(sub.id, sc.coerceAtMost(70), fb, "REVISION_REQUESTED", reason.ifBlank { "Add unit test assertions for edge cases." })
                        rejectTarget = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SkillBridgeColors.PendingAmber)
                ) { Text("Request Revision") }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        onEvaluateSubmission(sub.id, sc.coerceAtMost(50), fb, "REJECTED", reason.ifBlank { "Submission does not satisfy rubric criteria." })
                        rejectTarget = null
                    }
                ) { Text("Reject", color = SkillBridgeColors.DisputedRed) }
            }
        )
    }
}

@Composable
private fun DirectorEvaluationsHistoryScreen(
    submissions: List<WorkSubmissionEntity>,
    isDesktop: Boolean
) {
    val evaluated = submissions.filter { it.status != "PENDING_REVIEW" }
    LazyColumn(
        modifier = Modifier.fillMaxSize().testTag("director_evals_root"),
        contentPadding = PaddingValues(if (isDesktop) 24.dp else SkillBridgeSpacing.screenHorizontal),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { SectionHeaderRow(title = "Completed Evaluations (${evaluated.size})") }
        if (evaluated.isEmpty()) {
            item { EmptyStateView(subtitle = "No evaluated deliverables yet.") }
        } else if (isDesktop) {
            item {
                DesktopDataTable(
                    columns = listOf(
                        DataTableColumn("Student & Brief", 1.5f),
                        DataTableColumn("Verification ID", 1.0f),
                        DataTableColumn("Feedback", 1.8f),
                        DataTableColumn("Points", 0.7f),
                        DataTableColumn("Decision", 1.0f, alignEnd = true)
                    ),
                    rows = evaluated,
                    rowKey = { it.id }
                ) { sub, col ->
                    when (col) {
                        0 -> Text("${sub.studentName} • ${sub.briefTitle}", fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        1 -> Text(sub.verificationCode, style = MaterialTheme.typography.labelMedium, color = SkillBridgeColors.BridgeBlueDark)
                        2 -> Text(sub.feedback, style = MaterialTheme.typography.bodySmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        3 -> Text("+${sub.pointsAwarded} pts", fontWeight = FontWeight.SemiBold)
                        else -> StatusPill("${sub.status} (${sub.score}%)", if (sub.status == "VERIFIED") StatusTone.VERIFIED else StatusTone.DISPUTED)
                    }
                }
            }
        } else {
            items(evaluated, key = { it.id }) { sub ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("${sub.studentName} • ${sub.briefTitle}", fontWeight = FontWeight.Bold)
                                Text("Verification ID: ${sub.verificationCode} • +${sub.pointsAwarded} pts", style = MaterialTheme.typography.labelSmall, color = SkillBridgeColors.BridgeBlueDark)
                            }
                            StatusPill("${sub.status} (${sub.score}%)", if (sub.status == "VERIFIED") StatusTone.VERIFIED else StatusTone.DISPUTED)
                        }
                        Text(sub.feedback, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
        item { Spacer(Modifier.height(16.dp)) }
    }
}

