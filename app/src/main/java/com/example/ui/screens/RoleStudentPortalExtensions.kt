package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.WorkOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import com.example.core.localizedDomain
import com.example.data.local.AccountEntity
import com.example.data.local.NotificationEntity
import com.example.data.local.ProjectApplicationEntity
import com.example.data.local.ProjectBriefEntity
import com.example.data.local.ProjectTaskEntity
import com.example.data.local.WorkSubmissionEntity
import com.example.data.model.CareerGoal
import com.example.data.model.CoachMessage
import com.example.data.model.SkillGap
import com.example.ui.components.ChartDataPoint
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
import com.example.ui.viewmodel.CoachStreamState
import com.example.ui.viewmodel.UiState

/**
 * Real relational DB KPI tiles & analytics charts rendered inside the Student Dashboard.
 */
@Composable
fun StudentDbOverviewSection(
    account: AccountEntity?,
    publishedBriefs: List<ProjectBriefEntity>,
    applications: List<ProjectApplicationEntity>,
    tasks: List<ProjectTaskEntity>,
    submissions: List<WorkSubmissionEntity>,
    isDesktop: Boolean = false,
    onOpenBriefsWorkspace: () -> Unit,
    onOpenPassport: () -> Unit
) {
    val lang = LocalAppLanguage.current
    val activeAppsCount = applications.count { it.status in setOf("APPLIED", "SHORTLISTED") }
    val acceptedCount = applications.count { it.status == "ACCEPTED" }
    val pendingSubsCount = submissions.count { it.status == "PENDING_REVIEW" || it.status == "REVISION_REQUESTED" }
    val verifiedCount = submissions.count { it.status == "VERIFIED" }
    val tasksDoneCount = tasks.count { it.status == "DONE" }

    val skillCount = account?.skillsCsv?.split(",")?.count { it.isNotBlank() } ?: 0
    val targetSkillCount = account?.targetSkillsCsv?.split(",")?.count { it.isNotBlank() } ?: 0
    val completionPct = account?.profileCompletion ?: 0

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionHeaderRow(
            title = if (lang == AppLanguage.AR) "مؤشرات تقدم الطالب والمشاريع" else "Student Progress & Project KPIs",
            actionText = if (lang == AppLanguage.AR) "مشاريع الشركات (${publishedBriefs.size})" else "Briefs (${publishedBriefs.size})",
            onAction = onOpenBriefsWorkspace
        )

        ResponsiveKpiGrid(
            isDesktop = isDesktop,
            desktopColumns = 2,
            items = listOf(
                { mod ->
                    SaasKpiTile(
                        title = if (lang == AppLanguage.AR) "اكتمال الملف" else "Profile Complete",
                        value = "$completionPct%",
                        subtitle = if (lang == AppLanguage.AR) "$skillCount مهارات موثّقة" else "$skillCount skills logged",
                        icon = Icons.Filled.Badge,
                        accentColor = SkillBridgeColors.BridgeBlueDark,
                        modifier = mod,
                        onClick = onOpenBriefsWorkspace
                    )
                },
                { mod ->
                    SaasKpiTile(
                        title = if (lang == AppLanguage.AR) "طلبات نشطة" else "Active Apps",
                        value = "${activeAppsCount + acceptedCount}",
                        subtitle = if (lang == AppLanguage.AR) "$acceptedCount مقبول للتنفيذ" else "$acceptedCount accepted",
                        icon = Icons.Filled.WorkOutline,
                        accentColor = SkillBridgeColors.BridgeBlue,
                        modifier = mod,
                        onClick = onOpenBriefsWorkspace
                    )
                },
                { mod ->
                    SaasKpiTile(
                        title = if (lang == AppLanguage.AR) "المهام والتسليمات" else "Tasks & Reviews",
                        value = "$tasksDoneCount/${tasks.size.coerceAtLeast(0)}",
                        subtitle = if (lang == AppLanguage.AR) "$pendingSubsCount قيد التقييم" else "$pendingSubsCount pending review",
                        icon = Icons.Filled.PendingActions,
                        accentColor = SkillBridgeColors.PendingAmber,
                        modifier = mod,
                        onClick = onOpenBriefsWorkspace
                    )
                },
                { mod ->
                    SaasKpiTile(
                        title = if (lang == AppLanguage.AR) "أدلة الخبرة الموثّقة" else "Verified Evidence",
                        value = "$verifiedCount",
                        subtitle = account?.careerLevel ?: "Junior",
                        icon = Icons.Filled.Verified,
                        accentColor = SkillBridgeColors.VerifiedGreen,
                        modifier = mod,
                        onClick = onOpenPassport
                    )
                }
            )
        )

        ResponsiveChartRow(
            isDesktop = isDesktop,
            charts = listOf(
                { mod ->
                    SkillBridgeBarChart(
                        title = if (lang == AppLanguage.AR) "نظرة عامة على المهارات والتقدم" else "Skill & Evidence Overview",
                        subtitle = if (lang == AppLanguage.AR) "محتسب مباشرة من قاعدة البيانات" else "Computed live from student database records",
                        bars = listOf(
                            ChartDataPoint(if (lang == AppLanguage.AR) "مهارات" else "Skills", skillCount.toFloat(), SkillBridgeColors.BridgeBlue),
                            ChartDataPoint(if (lang == AppLanguage.AR) "مستهدفة" else "Targets", targetSkillCount.toFloat(), SkillBridgeColors.DeepNavy),
                            ChartDataPoint(if (lang == AppLanguage.AR) "مهام منجزة" else "Tasks Done", tasksDoneCount.toFloat(), SkillBridgeColors.SoftCyan),
                            ChartDataPoint(if (lang == AppLanguage.AR) "أدلة موثّقة" else "Verified", verifiedCount.toFloat(), SkillBridgeColors.VerifiedGreen)
                        ),
                        modifier = mod
                    )
                },
                { mod ->
                    SkillBridgeDoughnutChart(
                        title = if (lang == AppLanguage.AR) "حالة الطلبات والتسليمات" else "Application & Submission Pipeline",
                        subtitle = if (lang == AppLanguage.AR) "تتبع حقيقي من التقديم إلى التوثيق" else "Live lifecycle from application to director verification",
                        centerLabel = if (lang == AppLanguage.AR) "إجمالي" else "Total",
                        segments = listOf(
                            ChartDataPoint(if (lang == AppLanguage.AR) "تقديم" else "Applied", activeAppsCount.toFloat(), SkillBridgeColors.BridgeBlue),
                            ChartDataPoint(if (lang == AppLanguage.AR) "مقبول" else "Accepted", acceptedCount.toFloat(), SkillBridgeColors.DeepNavy),
                            ChartDataPoint(if (lang == AppLanguage.AR) "مراجعة" else "Pending", pendingSubsCount.toFloat(), SkillBridgeColors.PendingAmber),
                            ChartDataPoint(if (lang == AppLanguage.AR) "موثّق" else "Verified", verifiedCount.toFloat(), SkillBridgeColors.VerifiedGreen)
                        ),
                        modifier = mod
                    )
                }
            )
        )

        Button(
            onClick = onOpenBriefsWorkspace,
            colors = ButtonDefaults.buttonColors(containerColor = SkillBridgeColors.DeepNavy),
            shape = RoundedCornerShape(SkillBridgeRadius.md),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("student_open_briefs_btn")
        ) {
            Icon(Icons.Filled.BusinessCenter, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(
                text = if (lang == AppLanguage.AR) "تصفّح مشاريع الشركات ومهامي (${publishedBriefs.size})"
                else "Open Company Briefs & My Project Tasks (${publishedBriefs.size})",
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Full relational Company Briefs, Applications, Tasks & Work Deliverables Screen for Students.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StudentBriefsWorkspaceScreen(
    briefs: List<ProjectBriefEntity>,
    applications: List<ProjectApplicationEntity>,
    tasks: List<ProjectTaskEntity>,
    submissions: List<WorkSubmissionEntity>,
    isDesktop: Boolean = false,
    onApplyToBrief: (briefId: String, coverNote: String, roleRequested: String) -> Unit,
    onUpdateTaskStatus: (taskId: String, newStatus: String) -> Unit,
    onSubmitWork: (briefId: String, repoUrl: String, report: String, skills: List<String>) -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)
    val lang = LocalAppLanguage.current
    var selectedFilter by remember { mutableStateOf("ALL") }
    var applyTarget by remember { mutableStateOf<ProjectBriefEntity?>(null) }
    var submitTarget by remember { mutableStateOf<ProjectBriefEntity?>(null) }

    val appsByBrief = remember(applications) { applications.associateBy { it.briefId } }
    val filteredBriefs = remember(briefs, appsByBrief, selectedFilter) {
        when (selectedFilter) {
            "APPLIED" -> briefs.filter { appsByBrief.containsKey(it.id) }
            "ACCEPTED" -> briefs.filter { appsByBrief[it.id]?.status == "ACCEPTED" }
            else -> briefs
        }
    }
    var selectedBriefId by remember(filteredBriefs) { mutableStateOf(filteredBriefs.firstOrNull()?.id) }

    if (isDesktop) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .testTag("student_briefs_screen")
        ) {
            DesktopListDetailSplit(
                modifier = Modifier.testTag("desktop_briefs_split"),
                leftWeight = 0.44f,
                leftPane = {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Text(
                                text = if (lang == AppLanguage.AR) "مشاريع الشركات المعتمدة" else "Company Project Briefs",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (lang == AppLanguage.AR)
                                    "قدّم على المشاريع المعتمدة من الإدارة، أنجز المهام مع المشرف، وارفع تسليمك لتوثيق خبرتك."
                                else "Apply to Admin-approved company briefs, complete Director milestones, and submit work for Passport verification.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                FilterChip(
                                    selected = selectedFilter == "ALL",
                                    onClick = { selectedFilter = "ALL" },
                                    label = { Text(if (lang == AppLanguage.AR) "الكل (${briefs.size})" else "All (${briefs.size})") }
                                )
                                FilterChip(
                                    selected = selectedFilter == "APPLIED",
                                    onClick = { selectedFilter = "APPLIED" },
                                    label = { Text(if (lang == AppLanguage.AR) "طلباتي (${applications.size})" else "Applied (${applications.size})") }
                                )
                                FilterChip(
                                    selected = selectedFilter == "ACCEPTED",
                                    onClick = { selectedFilter = "ACCEPTED" },
                                    label = {
                                        val acc = applications.count { it.status == "ACCEPTED" }
                                        Text(if (lang == AppLanguage.AR) "نشطة ($acc)" else "Active ($acc)")
                                    }
                                )
                            }
                        }
                        if (filteredBriefs.isEmpty()) {
                            item {
                                EmptyStateView(
                                    subtitle = if (lang == AppLanguage.AR) "لا توجد مشاريع مطابقة لهذا الفلتر حاليًا." else "No company project briefs match this filter yet.",
                                    onAction = { selectedFilter = "ALL" }
                                )
                            }
                        } else {
                            items(filteredBriefs, key = { it.id }) { brief ->
                                val myApp = appsByBrief[brief.id]
                                val isSelected = selectedBriefId == brief.id
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedBriefId = brief.id }
                                        .testTag("student_brief_card_${brief.id}"),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) SkillBridgeColors.SoftCyanTint.copy(alpha = 0.45f)
                                        else MaterialTheme.colorScheme.surface
                                    ),
                                    border = BorderStroke(
                                        1.dp,
                                        if (isSelected) SkillBridgeColors.BridgeBlue else MaterialTheme.colorScheme.outlineVariant
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(brief.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                                Text(
                                                    text = "${brief.companyName} • ${localizedDomain(brief.domain, lang)} • ${brief.level}",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    color = SkillBridgeColors.BridgeBlueDark
                                                )
                                            }
                                            if (myApp != null) {
                                                StatusPill(
                                                    text = myApp.status,
                                                    tone = when (myApp.status) {
                                                        "ACCEPTED" -> StatusTone.VERIFIED
                                                        "REJECTED" -> StatusTone.DISPUTED
                                                        else -> StatusTone.PENDING
                                                    }
                                                )
                                            } else {
                                                StatusPill("+${brief.pointsReward} pts", StatusTone.PAID)
                                            }
                                        }
                                        if (myApp == null) {
                                            Button(
                                                onClick = { applyTarget = brief },
                                                colors = ButtonDefaults.buttonColors(containerColor = SkillBridgeColors.BridgeBlueDark),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(42.dp)
                                                    .testTag("apply_brief_${brief.id}")
                                            ) {
                                                Text(if (lang == AppLanguage.AR) "تقديم الآن" else "Apply Now", fontWeight = FontWeight.Bold)
                                            }
                                        } else if (myApp.status == "ACCEPTED") {
                                            Button(
                                                onClick = { submitTarget = brief },
                                                colors = ButtonDefaults.buttonColors(containerColor = SkillBridgeColors.DeepNavy),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(42.dp)
                                                    .testTag("submit_brief_${brief.id}")
                                            ) {
                                                Icon(Icons.Filled.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(Modifier.width(6.dp))
                                                Text(if (lang == AppLanguage.AR) "رفع تسليم العمل" else "Submit Deliverable")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                rightPane = {
                    val selected = filteredBriefs.firstOrNull { it.id == selectedBriefId } ?: filteredBriefs.firstOrNull()
                    if (selected == null) {
                        Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                            EmptyStateView(subtitle = "Select a project brief on the left to view milestones & deliverables.")
                        }
                    } else {
                        val myApp = appsByBrief[selected.id]
                        val briefTasks = tasks.filter { it.briefId == selected.id }
                        val briefSubs = submissions.filter { it.briefId == selected.id }
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(selected.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = "${selected.companyName} • ${localizedDomain(selected.domain, lang)} • ${selected.level}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = SkillBridgeColors.BridgeBlueDark
                                    )
                                }
                                StatusPill("+${selected.pointsReward} pts • ${selected.prizePool}", StatusTone.PAID)
                            }

                            Text(selected.summary, style = MaterialTheme.typography.bodyMedium)

                            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                selected.skillsCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() }.forEach { sk ->
                                    Surface(
                                        shape = RoundedCornerShape(SkillBridgeRadius.pill),
                                        color = SkillBridgeColors.SoftCyanTint
                                    ) {
                                        Text(
                                            text = sk,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = SkillBridgeColors.DeepNavy,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }

                            Text(
                                text = (if (lang == AppLanguage.AR) "المشرف: " else "Director: ") +
                                    selected.assignedDirectorName.ifBlank { "Company Director" } +
                                    " • " +
                                    (if (lang == AppLanguage.AR) "الموعد: " else "Deadline: ") +
                                    selected.deadline,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            if (selected.rubricCsv.isNotBlank()) {
                                InfoNoteBanner(text = "Rubric: ${selected.rubricCsv}")
                            }

                            if (myApp?.status == "ACCEPTED") {
                                HorizontalDivider()
                                Text(
                                    text = if (lang == AppLanguage.AR) "مهام المشروع (${briefTasks.size})" else "Project Tasks (${briefTasks.size})",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                if (briefTasks.isEmpty()) {
                                    Text(
                                        text = if (lang == AppLanguage.AR) "بانتظار مهام المشرف..." else "Awaiting Director milestones...",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                } else {
                                    briefTasks.forEach { t ->
                                        val nextStatus = when (t.status) {
                                            "TODO" -> "IN_PROGRESS"
                                            "IN_PROGRESS" -> "DONE"
                                            else -> "TODO"
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(SkillBridgeRadius.sm),
                                            color = MaterialTheme.colorScheme.surfaceContainerLow,
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable { onUpdateTaskStatus(t.id, nextStatus) }
                                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(t.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                                    Text(
                                                        text = "${t.dueDateLabel} • ${if (lang == AppLanguage.AR) "اضغط للتحديث" else "Click to advance"}",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                                StatusPill(
                                                    text = t.status,
                                                    tone = when (t.status) {
                                                        "DONE" -> StatusTone.VERIFIED
                                                        "IN_PROGRESS" -> StatusTone.PAID
                                                        else -> StatusTone.NEUTRAL
                                                    },
                                                    icon = if (t.status == "DONE") Icons.Filled.CheckCircle else Icons.Filled.PlayArrow
                                                )
                                            }
                                        }
                                    }
                                }
                            } else if (myApp != null && myApp.decisionNote.isNotBlank()) {
                                InfoNoteBanner(text = myApp.decisionNote)
                            }

                            if (briefSubs.isNotEmpty()) {
                                HorizontalDivider()
                                Text(
                                    text = if (lang == AppLanguage.AR) "التسليمات والتقييم" else "Submitted Deliverables & Evaluation",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                briefSubs.forEach { sub ->
                                    Surface(
                                        shape = RoundedCornerShape(SkillBridgeRadius.sm),
                                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(sub.verificationCode, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                                StatusPill(
                                                    text = if (sub.score > 0) "${sub.status} (${sub.score}%)" else sub.status,
                                                    tone = when (sub.status) {
                                                        "VERIFIED" -> StatusTone.VERIFIED
                                                        "REJECTED" -> StatusTone.DISPUTED
                                                        else -> StatusTone.PENDING
                                                    }
                                                )
                                            }
                                            if (sub.feedback.isNotBlank()) {
                                                Text(sub.feedback, style = MaterialTheme.typography.bodySmall)
                                            }
                                        }
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
            modifier = Modifier
                .fillMaxSize()
                .testTag("student_briefs_screen"),
            contentPadding = PaddingValues(SkillBridgeSpacing.screenHorizontal),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = if (lang == AppLanguage.AR) "مشاريع الشركات المعتمدة" else "Company Project Briefs",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (lang == AppLanguage.AR)
                        "قدّم على المشاريع المعتمدة من الإدارة، أنجز المهام مع المشرف، وارفع تسليمك لتوثيق خبرتك."
                    else "Apply to Admin-approved company briefs, complete Director milestones, and submit work for verified Passport evidence.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedFilter == "ALL",
                        onClick = { selectedFilter = "ALL" },
                        label = { Text(if (lang == AppLanguage.AR) "الكل (${briefs.size})" else "All Briefs (${briefs.size})") }
                    )
                    FilterChip(
                        selected = selectedFilter == "APPLIED",
                        onClick = { selectedFilter = "APPLIED" },
                        label = { Text(if (lang == AppLanguage.AR) "طلباتي (${applications.size})" else "My Applications (${applications.size})") }
                    )
                    FilterChip(
                        selected = selectedFilter == "ACCEPTED",
                        onClick = { selectedFilter = "ACCEPTED" },
                        label = {
                            val acc = applications.count { it.status == "ACCEPTED" }
                            Text(if (lang == AppLanguage.AR) "مشاريعي النشطة ($acc)" else "Active Projects ($acc)")
                        }
                    )
                }
            }

            if (filteredBriefs.isEmpty()) {
                item {
                    EmptyStateView(
                        subtitle = if (lang == AppLanguage.AR) "لا توجد مشاريع مطابقة لهذا الفلتر حاليًا." else "No company project briefs match this filter yet.",
                        onAction = { selectedFilter = "ALL" }
                    )
                }
            } else {
                items(filteredBriefs, key = { it.id }) { brief ->
                    val myApp = appsByBrief[brief.id]
                    val briefTasks = tasks.filter { it.briefId == brief.id }
                    val briefSubs = submissions.filter { it.briefId == brief.id }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("student_brief_card_${brief.id}"),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(brief.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = "${brief.companyName} • ${localizedDomain(brief.domain, lang)} • ${brief.level}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = SkillBridgeColors.BridgeBlueDark
                                    )
                                }
                                if (myApp != null) {
                                    StatusPill(
                                        text = myApp.status,
                                        tone = when (myApp.status) {
                                            "ACCEPTED" -> StatusTone.VERIFIED
                                            "REJECTED" -> StatusTone.DISPUTED
                                            else -> StatusTone.PENDING
                                        }
                                    )
                                } else {
                                    StatusPill("+${brief.pointsReward} pts", StatusTone.PAID)
                                }
                            }

                            Text(brief.summary, style = MaterialTheme.typography.bodyMedium)

                            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                brief.skillsCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() }.forEach { sk ->
                                    Surface(
                                        shape = RoundedCornerShape(SkillBridgeRadius.pill),
                                        color = SkillBridgeColors.SoftCyanTint
                                    ) {
                                        Text(
                                            text = sk,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = SkillBridgeColors.DeepNavy,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }

                            Text(
                                text = (if (lang == AppLanguage.AR) "المشرف: " else "Director: ") +
                                    brief.assignedDirectorName.ifBlank { "Company Director" } +
                                    " • " +
                                    (if (lang == AppLanguage.AR) "الموعد: " else "Deadline: ") +
                                    brief.deadline +
                                    " • " + brief.prizePool,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            if (myApp == null) {
                                Button(
                                    onClick = { applyTarget = brief },
                                    colors = ButtonDefaults.buttonColors(containerColor = SkillBridgeColors.BridgeBlueDark),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("apply_brief_${brief.id}")
                                ) {
                                    Text(if (lang == AppLanguage.AR) "تقديم الآن" else "Apply Now", fontWeight = FontWeight.Bold)
                                }
                            } else if (myApp.status == "ACCEPTED") {
                                HorizontalDivider()
                                Text(
                                    text = if (lang == AppLanguage.AR) "مهام المشروع (${briefTasks.size})" else "Project Tasks (${briefTasks.size})",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                if (briefTasks.isEmpty()) {
                                    Text(
                                        text = if (lang == AppLanguage.AR) "بانتظار مهام المشرف..." else "Awaiting Director milestones...",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                } else {
                                    briefTasks.forEach { t ->
                                        val nextStatus = when (t.status) {
                                            "TODO" -> "IN_PROGRESS"
                                            "IN_PROGRESS" -> "DONE"
                                            else -> "TODO"
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(SkillBridgeRadius.sm),
                                            color = MaterialTheme.colorScheme.surfaceContainerLow,
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable { onUpdateTaskStatus(t.id, nextStatus) }
                                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(t.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                                    Text(
                                                        text = "${t.dueDateLabel} • ${if (lang == AppLanguage.AR) "اضغط للتحديث" else "Tap to advance"}",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                                StatusPill(
                                                    text = t.status,
                                                    tone = when (t.status) {
                                                        "DONE" -> StatusTone.VERIFIED
                                                        "IN_PROGRESS" -> StatusTone.PAID
                                                        else -> StatusTone.NEUTRAL
                                                    },
                                                    icon = if (t.status == "DONE") Icons.Filled.CheckCircle else Icons.Filled.PlayArrow
                                                )
                                            }
                                        }
                                    }
                                }

                                Button(
                                    onClick = { submitTarget = brief },
                                    colors = ButtonDefaults.buttonColors(containerColor = SkillBridgeColors.DeepNavy),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("submit_brief_${brief.id}")
                                ) {
                                    Icon(Icons.Filled.UploadFile, contentDescription = null)
                                    Spacer(Modifier.width(8.dp))
                                    Text(if (lang == AppLanguage.AR) "رفع تسليم العمل للتقييم" else "Submit Deliverable for Director Review")
                                }
                            } else if (myApp.decisionNote.isNotBlank()) {
                                InfoNoteBanner(text = myApp.decisionNote)
                            }

                            if (briefSubs.isNotEmpty()) {
                                HorizontalDivider()
                                Text(
                                    text = if (lang == AppLanguage.AR) "التسليمات والتقييم" else "Submitted Deliverables & Evaluation",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                briefSubs.forEach { sub ->
                                    Surface(
                                        shape = RoundedCornerShape(SkillBridgeRadius.sm),
                                        color = MaterialTheme.colorScheme.surfaceContainerLow
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = sub.verificationCode,
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                StatusPill(
                                                    text = if (sub.score > 0) "${sub.status} (${sub.score}%)" else sub.status,
                                                    tone = when (sub.status) {
                                                        "VERIFIED" -> StatusTone.VERIFIED
                                                        "REJECTED" -> StatusTone.DISPUTED
                                                        else -> StatusTone.PENDING
                                                    }
                                                )
                                            }
                                            if (sub.feedback.isNotBlank()) {
                                                Text(sub.feedback, style = MaterialTheme.typography.bodySmall)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(20.dp)) }
        }
    }

    val targetApply = applyTarget
    if (targetApply != null) {
        var roleRequested by remember(targetApply.id) { mutableStateOf("Lead Implementation Engineer") }
        var coverNote by remember(targetApply.id) { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { applyTarget = null },
            title = { Text(if (lang == AppLanguage.AR) "التقديم على ${targetApply.title}" else "Apply: ${targetApply.title}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = roleRequested,
                        onValueChange = { roleRequested = it },
                        label = { Text(if (lang == AppLanguage.AR) "الدور المطلوب" else "Target Role") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = coverNote,
                        onValueChange = { coverNote = it },
                        label = { Text(if (lang == AppLanguage.AR) "نبذة عن جاهزيتك" else "Pitch / Readiness Note") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth().testTag("apply_cover_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onApplyToBrief(targetApply.id, coverNote, roleRequested)
                        applyTarget = null
                    },
                    modifier = Modifier.testTag("confirm_apply_btn")
                ) {
                    Text(if (lang == AppLanguage.AR) "إرسال الطلب" else "Submit Application")
                }
            },
            dismissButton = {
                TextButton(onClick = { applyTarget = null }) {
                    Text(if (lang == AppLanguage.AR) "إلغاء" else "Cancel")
                }
            }
        )
    }

    val targetSub = submitTarget
    if (targetSub != null) {
        var repoUrl by remember(targetSub.id) { mutableStateOf("https://github.com/skillbridge/solution") }
        var report by remember(targetSub.id) { mutableStateOf("") }
        var skillsCsv by remember(targetSub.id) { mutableStateOf(targetSub.skillsCsv) }
        AlertDialog(
            onDismissRequest = { submitTarget = null },
            title = { Text(if (lang == AppLanguage.AR) "رفع تسليم العمل" else "Submit Deliverable") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = repoUrl,
                        onValueChange = { repoUrl = it },
                        label = { Text(if (lang == AppLanguage.AR) "رابط المشروع / Git Repo" else "Repository / Demo URL") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = skillsCsv,
                        onValueChange = { skillsCsv = it },
                        label = { Text(if (lang == AppLanguage.AR) "المهارات المطبّقة" else "Applied Skills") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = report,
                        onValueChange = { report = it },
                        label = { Text(if (lang == AppLanguage.AR) "تقرير الفهم والتنفيذ (15+ حرف)" else "Understanding Report (15+ chars)") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth().testTag("submit_report_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val sk = skillsCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                        onSubmitWork(targetSub.id, repoUrl, report, sk)
                        submitTarget = null
                    },
                    enabled = report.trim().length >= 15,
                    modifier = Modifier.testTag("confirm_submit_work_btn")
                ) {
                    Text(if (lang == AppLanguage.AR) "إرسال للمشرف" else "Send to Director")
                }
            },
            dismissButton = {
                TextButton(onClick = { submitTarget = null }) {
                    Text(if (lang == AppLanguage.AR) "إلغاء" else "Cancel")
                }
            }
        )
    }
}

/**
 * Student-only AI Career Coach & Mock Interview screen (strictly no Mentor/Director review tabs).
 */
@Composable
fun StudentAiCoachOnlyScreen(
    goalsState: UiState<List<CareerGoal>>,
    gapsState: UiState<List<SkillGap>>,
    messagesState: UiState<List<CoachMessage>>,
    coachStreamState: CoachStreamState,
    onSendCoachPrompt: (String, String, CareerGoal?, List<SkillGap>) -> Unit,
    onClearCoachHistory: (String) -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)
    val lang = LocalAppLanguage.current
    val goals = (goalsState as? UiState.Success)?.data.orEmpty()
    val activeGoal = goals.firstOrNull { it.isActive } ?: goals.firstOrNull()
    val gaps = (gapsState as? UiState.Success)?.data.orEmpty()
    val messages = (messagesState as? UiState.Success)?.data.orEmpty()
    var mode by remember { mutableStateOf("COACH") }
    var prompt by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("student_ai_coach_screen"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = if (lang == AppLanguage.AR) "المدرب الذكي AI ومحاكاة المقابلات" else "AI Career Coach & Mock Interview",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            FilterChip(
                selected = mode == "COACH",
                onClick = { mode = "COACH" },
                label = { Text(if (lang == AppLanguage.AR) "مدرب المسار" else "Career Coach") }
            )
            FilterChip(
                selected = mode == "INTERVIEW",
                onClick = { mode = "INTERVIEW" },
                label = { Text(if (lang == AppLanguage.AR) "محاكاة المقابلة" else "Mock Interview") }
            )
            Spacer(Modifier.weight(1f))
            OutlinedButton(onClick = { onClearCoachHistory(mode) }) {
                Text(if (lang == AppLanguage.AR) "مسح" else "Clear")
            }
        }
        LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            val filtered = messages.filter { it.mode == mode }
            if (filtered.isEmpty()) {
                item {
                    InfoNoteBanner(
                        text = if (lang == AppLanguage.AR) "اسأل المدرب الذكي عن خطة المهارات أو اطلب سؤال مقابلة تقنية."
                        else "Ask SkillBridge AI Coach about your skill gaps or launch a live technical mock interview."
                    )
                }
            }
            items(filtered) { msg ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (msg.sender == "USER") SkillBridgeColors.DeepNavy else MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Text(
                        msg.content,
                        color = if (msg.sender == "USER") SkillBridgeColors.PureWhite else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            if (coachStreamState is CoachStreamState.Streaming && coachStreamState.mode == mode) {
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = SkillBridgeColors.SoftCyanTint)) {
                        Text(coachStreamState.text.ifBlank { "..." }, modifier = Modifier.padding(12.dp))
                    }
                }
            }
            if (coachStreamState is CoachStreamState.Error) {
                item {
                    Text(coachStreamState.message, color = SkillBridgeColors.DisputedRed, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = prompt,
                onValueChange = { prompt = it },
                placeholder = { Text(if (lang == AppLanguage.AR) "اكتب رسالتك للمدرب..." else "Ask AI Coach or answer interview prompt...") },
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = {
                    onSendCoachPrompt(mode, prompt, activeGoal, gaps)
                    prompt = ""
                },
                enabled = prompt.isNotBlank() && coachStreamState !is CoachStreamState.Streaming
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
            }
        }
    }
}

/**
 * Shared Notification Center Dialog for Admin, Company, Director & Student relational notifications.
 */
@Composable
fun RoleNotificationsDialog(
    notifications: List<NotificationEntity>,
    onMarkAllRead: () -> Unit,
    onDismiss: () -> Unit
) {
    val lang = LocalAppLanguage.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Filled.Notifications, contentDescription = null, tint = SkillBridgeColors.BridgeBlueDark)
                Text(if (lang == AppLanguage.AR) "الإشعارات" else "Notifications")
            }
        },
        text = {
            if (notifications.isEmpty()) {
                Text(
                    text = if (lang == AppLanguage.AR) "لا توجد إشعارات حتى الآن." else "No notifications yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().height(320.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(notifications, key = { it.id }) { item ->
                        Surface(
                            shape = RoundedCornerShape(SkillBridgeRadius.sm),
                            color = if (item.isRead) MaterialTheme.colorScheme.surfaceContainerLow else SkillBridgeColors.SoftCyanTint.copy(alpha = 0.55f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (lang == AppLanguage.AR) item.titleAr else item.titleEn,
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    StatusPill(
                                        text = item.category,
                                        tone = if (item.isRead) StatusTone.NEUTRAL else StatusTone.PAID
                                    )
                                }
                                Text(
                                    text = if (lang == AppLanguage.AR) item.bodyAr else item.bodyEn,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (notifications.any { !it.isRead }) {
                Button(onClick = onMarkAllRead) {
                    Icon(Icons.Filled.DoneAll, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(if (lang == AppLanguage.AR) "تحديد الكل كمقروء" else "Mark All Read")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (lang == AppLanguage.AR) "إغلاق" else "Close")
            }
        }
    )
}
