package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import com.example.core.AppLanguage
import com.example.core.AssetRegistry
import com.example.core.LocalAppLanguage
import com.example.core.LocalAppStrings
import com.example.core.localizedCareerLevel
import com.example.data.model.AiAgentContextMode
import com.example.data.model.ChallengeItem
import com.example.data.model.ContestStatus
import com.example.data.model.DiscoverCategory
import com.example.data.model.VerificationTier
import com.example.data.seed.SkillBridgeEcosystemData
import com.example.ui.components.ContextualAiAgentBanner
import com.example.ui.components.InfoNoteBanner
import com.example.ui.components.SectionHeaderRow
import com.example.ui.components.StatusPill
import com.example.ui.components.StatusTone
import com.example.ui.theme.SkillBridgeColors
import com.example.ui.theme.SkillBridgeRadius
import com.example.ui.theme.SkillBridgeSpacing
import com.example.ui.viewmodel.WorkSimState

/**
 * Screen 5: Unified Discover Hub.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DiscoverHubScreen(
    savedChallengeIds: Set<String>,
    onToggleSave: (String) -> Unit,
    onOpenChallenge: (String) -> Unit,
    onOpenContests: () -> Unit,
    onOpenReverseEng: () -> Unit,
    onOpenTeams: () -> Unit
) {
    val strings = LocalAppStrings.current
    val lang = LocalAppLanguage.current

    var selectedCategory by remember { mutableStateOf<DiscoverCategory?>(null) }
    var selectedLevel by remember { mutableStateOf<String?>(null) }
    var teamOnly by remember { mutableStateOf(false) }

    val filteredChallenges = remember(selectedCategory, selectedLevel, teamOnly) {
        SkillBridgeEcosystemData.challenges.filter { item ->
            val catMatch = selectedCategory == null || item.category == selectedCategory
            val lvlMatch = selectedLevel == null || item.level == selectedLevel
            val teamMatch = !teamOnly || item.isTeamAllowed
            catMatch && lvlMatch && teamMatch
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(SkillBridgeSpacing.screenHorizontal),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(strings.discoverTitle, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedCategory == null,
                    onClick = { selectedCategory = null },
                    label = { Text(strings.filterAll) }
                )
                FilterChip(
                    selected = selectedCategory == DiscoverCategory.CHALLENGE,
                    onClick = { selectedCategory = DiscoverCategory.CHALLENGE },
                    label = { Text(strings.filterChallenges) }
                )
                FilterChip(
                    selected = selectedCategory == DiscoverCategory.PROJECT,
                    onClick = { selectedCategory = DiscoverCategory.PROJECT },
                    label = { Text(strings.filterProjects) }
                )
                FilterChip(
                    selected = selectedCategory == DiscoverCategory.REVERSE_ENG,
                    onClick = { onOpenReverseEng() },
                    label = { Text(strings.filterReverseEng) }
                )
                FilterChip(
                    selected = false,
                    onClick = onOpenContests,
                    label = { Text(strings.filterContests) }
                )
                FilterChip(
                    selected = selectedCategory == DiscoverCategory.MENTOR,
                    onClick = { selectedCategory = DiscoverCategory.MENTOR },
                    label = { Text(strings.filterMentors) }
                )
                FilterChip(
                    selected = selectedCategory == DiscoverCategory.OPPORTUNITY,
                    onClick = { selectedCategory = DiscoverCategory.OPPORTUNITY },
                    label = { Text(strings.filterOpportunities) }
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SkillBridgeEcosystemData.careerLevels.forEach { lvl ->
                    FilterChip(
                        selected = selectedLevel == lvl,
                        onClick = { selectedLevel = if (selectedLevel == lvl) null else lvl },
                        label = { Text(localizedCareerLevel(lvl, lang)) }
                    )
                }
                FilterChip(
                    selected = teamOnly,
                    onClick = { teamOnly = !teamOnly },
                    label = { Text(if (lang == AppLanguage.AR) "عمل جماعي" else "Team Allowed") }
                )
            }
        }

        // Featured Specialized Hubs Banner
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                Card(
                    modifier = Modifier.weight(1f).clickable(onClick = onOpenContests),
                    colors = CardDefaults.cardColors(containerColor = SkillBridgeColors.DeepNavy)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Filled.Code, contentDescription = null, tint = SkillBridgeColors.LimeGreen)
                        Text(strings.contestsTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = SkillBridgeColors.PureWhite)
                        Text(if (lang == AppLanguage.AR) "مسابقات برمجية وتصنيف مباشر" else "Codeforces-style rounds & standings", style = MaterialTheme.typography.labelSmall, color = SkillBridgeColors.SoftCyan)
                    }
                }
                Card(
                    modifier = Modifier.weight(1f).clickable(onClick = onOpenReverseEng),
                    colors = CardDefaults.cardColors(containerColor = SkillBridgeColors.EthicalBg),
                    border = BorderStroke(1.dp, SkillBridgeColors.EthicalPurple.copy(alpha = 0.35f))
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Filled.Security, contentDescription = null, tint = SkillBridgeColors.EthicalPurple)
                        Text(strings.reverseEngTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = SkillBridgeColors.DeepNavy)
                        Text(strings.authorizedScopeBadge, style = MaterialTheme.typography.labelSmall, color = SkillBridgeColors.EthicalPurple)
                    }
                }
            }
        }

        items(filteredChallenges) { item ->
            val isSaved = item.id in savedChallengeIds
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenChallenge(item.id) }
                    .testTag("discover_card_${item.id}"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                StatusPill(
                                    text = if (lang == AppLanguage.AR) item.companyTier.badgeAr else item.companyTier.badgeEn,
                                    tone = if (item.companyTier == VerificationTier.TRUSTED) StatusTone.VERIFIED else StatusTone.REVIEW,
                                    icon = Icons.Filled.Verified
                                )
                                StatusPill(text = localizedCareerLevel(item.level, lang), tone = StatusTone.NEUTRAL)
                            }
                            Text(item.title.get(lang), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(item.companyName, style = MaterialTheme.typography.labelLarge, color = SkillBridgeColors.BridgeBlueDark)
                        }
                        IconButton(onClick = { onToggleSave(item.id) }) {
                            Icon(
                                imageVector = if (isSaved) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                                contentDescription = strings.saveForLater,
                                tint = SkillBridgeColors.BridgeBlueDark
                            )
                        }
                    }

                    Text(item.subtitle.get(lang), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        item.skills.forEach { sk ->
                            Surface(
                                shape = RoundedCornerShape(SkillBridgeRadius.pill),
                                color = SkillBridgeColors.SoftCyanTint
                            ) {
                                Text(sk, style = MaterialTheme.typography.labelSmall, color = SkillBridgeColors.DeepNavy, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "${item.soloOrTeamLabel.get(lang)} • ${item.deadline}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        StatusPill(
                            text = if (item.pointsReward > 0) "${item.prizePool} • +${item.pointsReward} ${strings.pointsLabel}" else item.prizePool,
                            tone = StatusTone.PAID
                        )
                    }
                }
            }
        }
        item { Spacer(Modifier.height(16.dp)) }
    }
}

/**
 * Screen 6: Project / Challenge Details matching Phone #3 in Poster + Work Simulation + Submission.
 */
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ChallengeDetailScreen(
    challenge: ChallengeItem,
    isSaved: Boolean,
    workSimState: WorkSimState,
    containsSecret: (String) -> Boolean,
    onToggleSave: () -> Unit,
    onRunWorkSim: (String, String, String) -> Unit,
    onSubmitSolution: (String, String, String, List<String>) -> Unit,
    onOpenAiAgent: (AiAgentContextMode, String, String) -> Unit,
    onOpenTeams: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)
    val strings = LocalAppStrings.current
    val lang = LocalAppLanguage.current

    var showSubmitSheet by remember { mutableStateOf(false) }
    var showWorkSimSheet by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(SkillBridgeSpacing.screenHorizontal),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
                    .clip(RoundedCornerShape(SkillBridgeRadius.lg))
            ) {
                Image(
                    painter = painterResource(id = AssetRegistry.HERO_BRIDGE_BANNER),
                    contentDescription = challenge.title.get(lang),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Surface(
                    modifier = Modifier.align(Alignment.TopStart).padding(12.dp),
                    shape = RoundedCornerShape(SkillBridgeRadius.pill),
                    color = SkillBridgeColors.DeepNavy.copy(alpha = 0.85f)
                ) {
                    Text(
                        text = if (lang == AppLanguage.AR) challenge.companyTier.badgeAr else challenge.companyTier.badgeEn,
                        style = MaterialTheme.typography.labelMedium,
                        color = SkillBridgeColors.LimeGreen,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        item {
            Text(challenge.title.get(lang), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                challenge.skills.forEach { sk ->
                    StatusPill(text = sk, tone = StatusTone.REVIEW)
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(challenge.subtitle.get(lang), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        // Poster Phone #3: Deadline + Mentor Row
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                        Icon(Icons.Filled.CalendarToday, contentDescription = null, tint = SkillBridgeColors.BridgeBlueDark, modifier = Modifier.size(18.dp))
                        Column {
                            Text(if (lang == AppLanguage.AR) "الموعد النهائي" else "Deadline", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(challenge.deadline, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                        Icon(Icons.Filled.Person, contentDescription = null, tint = SkillBridgeColors.BridgeBlueDark, modifier = Modifier.size(18.dp))
                        Column {
                            Text(if (lang == AppLanguage.AR) "المرشد" else "Mentor", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(challenge.mentorName, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Primary Actions matching Poster Phone #3 + AI Work Simulation
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = { showSubmitSheet = true },
                    colors = ButtonDefaults.buttonColors(containerColor = SkillBridgeColors.BridgeBlueDark),
                    shape = RoundedCornerShape(SkillBridgeRadius.pill),
                    modifier = Modifier.fillMaxWidth().height(50.dp).testTag("challenge_apply_submit_btn")
                ) {
                    Text("${strings.applyNow} / ${strings.submitSolution}", fontWeight = FontWeight.Bold)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = onToggleSave,
                        shape = RoundedCornerShape(SkillBridgeRadius.pill),
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Text(if (isSaved) strings.savedLabel else strings.saveForLater)
                    }
                    OutlinedButton(
                        onClick = { showWorkSimSheet = true },
                        shape = RoundedCornerShape(SkillBridgeRadius.pill),
                        modifier = Modifier.weight(1f).height(48.dp).testTag("launch_work_sim_btn")
                    ) {
                        Icon(Icons.Filled.Psychology, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(strings.launchWorkSim)
                    }
                }
                if (challenge.isTeamAllowed) {
                    OutlinedButton(
                        onClick = onOpenTeams,
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text(if (lang == AppLanguage.AR) "كوّن فريقًا أو انضم لفريق لهذا التحدي" else "Form or Join a Team for This Challenge")
                    }
                }
            }
        }

        // Disclosed Rubric Before Starting
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(strings.rubricBeforeStart, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    challenge.rubric.forEach { rule ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Top) {
                            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = SkillBridgeColors.VerifiedGreen, modifier = Modifier.size(16.dp))
                            Text(rule.get(lang), style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Text("${challenge.prizePool} • +${challenge.pointsReward} ${strings.pointsLabel}", style = MaterialTheme.typography.labelLarge, color = SkillBridgeColors.PaidTeal)
                    Text(strings.prizePoolDisclaimer, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        // Deliverables, IP & AI Policy
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(strings.ipRightsTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(challenge.ipPolicy.get(lang), style = MaterialTheme.typography.bodyMedium)
                    Text(strings.aiPolicyTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(challenge.aiPolicy.get(lang), style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        item {
            val agentMode = if (challenge.category == DiscoverCategory.REVERSE_ENG) {
                AiAgentContextMode.REVERSE_ENG_GUIDE
            } else {
                AiAgentContextMode.CHALLENGE_MENTOR
            }
            ContextualAiAgentBanner(
                mode = agentMode,
                onOpenAgent = {
                    onOpenAiAgent(
                        agentMode,
                        challenge.title.get(lang),
                        "Challenge=${challenge.title.en}, Skills=${challenge.skills.joinToString(", ")}, Rubric=${challenge.rubric.joinToString("; ") { it.en }}"
                    )
                }
            )
        }

        if (challenge.faq.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            if (lang == AppLanguage.AR) "الأسئلة الشائعة والتحديثات" else "Q&A / Changelog",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        challenge.faq.forEach { (q, a) ->
                            Text(
                                "${if (lang == AppLanguage.AR) "س:" else "Q:"} ${q.get(lang)}",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(a.get(lang), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
        item { Spacer(Modifier.height(20.dp)) }
    }

    if (showSubmitSheet) {
        SubmitSolutionSheet(
            challenge = challenge,
            containsSecret = containsSecret,
            onDismiss = { showSubmitSheet = false },
            onSubmit = { role, url, report ->
                onSubmitSolution(role, url, report, challenge.skills)
                showSubmitSheet = false
            }
        )
    }

    if (showWorkSimSheet) {
        WorkSimulationSheet(
            challenge = challenge,
            workSimState = workSimState,
            onRunSimulation = { decision ->
                onRunWorkSim(challenge.id, challenge.title.get(lang), decision)
            },
            onDismiss = { showWorkSimSheet = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubmitSolutionSheet(
    challenge: ChallengeItem,
    containsSecret: (String) -> Boolean,
    onDismiss: () -> Unit,
    onSubmit: (roleInTeam: String, repoUrl: String, understandingReport: String) -> Unit
) {
    val strings = LocalAppStrings.current
    val lang = LocalAppLanguage.current
    var roleInTeam by remember { mutableStateOf(if (challenge.isTeamAllowed) "Backend & Analytics Lead" else "Solo Contributor") }
    var repoUrl by remember { mutableStateOf("https://github.com/username/skillbridge-solution") }
    var report by remember { mutableStateOf("") }
    var showConfirmSubmit by remember { mutableStateOf(false) }
    val leaked = containsSecret(report) || containsSecret(repoUrl)

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(strings.submitSolution, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(challenge.title.get(lang), style = MaterialTheme.typography.bodyMedium, color = SkillBridgeColors.BridgeBlueDark)

            OutlinedTextField(
                value = roleInTeam,
                onValueChange = { roleInTeam = it },
                label = { Text(if (lang == AppLanguage.AR) "دورك في الحل / الفريق" else "Your Role") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = repoUrl,
                onValueChange = { repoUrl = it },
                label = { Text(if (lang == AppLanguage.AR) "رابط المستودع أو العرض التجريبي" else "Repository / Sandbox URL") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = report,
                onValueChange = { report = it },
                minLines = 3,
                label = { Text(strings.understandingReportLabel) },
                placeholder = { Text(if (lang == AppLanguage.AR) "اشرح قرارات التصميم والاختبارات..." else "Explain architecture choices, boundary tests & verification...") },
                modifier = Modifier.fillMaxWidth().testTag("submission_understanding_report")
            )
            if (leaked) {
                Card(colors = CardDefaults.cardColors(containerColor = SkillBridgeColors.DisputedBg)) {
                    Text(strings.secretLeakWarning, color = SkillBridgeColors.DisputedRed, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(10.dp))
                }
            }
            Button(
                onClick = { showConfirmSubmit = true },
                enabled = !leaked && report.trim().length >= 15,
                colors = ButtonDefaults.buttonColors(containerColor = SkillBridgeColors.DeepNavy),
                modifier = Modifier.fillMaxWidth().height(50.dp).testTag("confirm_submit_solution_btn")
            ) {
                Text(strings.submitSolution)
            }
            Spacer(Modifier.height(16.dp))
        }
    }

    if (showConfirmSubmit) {
        AlertDialog(
            onDismissRequest = { showConfirmSubmit = false },
            title = { Text(if (lang == AppLanguage.AR) "تأكيد إرسال الحل للتقييم" else "Confirm Solution Submission") },
            text = {
                Text(
                    if (lang == AppLanguage.AR) "سيتم إرسال الحل وتقرير الفهم إلى قائمة مراجعة المرشدين لتوثيقه في سجل الخبرة."
                    else "Your solution link and Understanding Report will be queued for Mentor & Rubric verification."
                )
            },
            confirmButton = {
                Button(onClick = {
                    showConfirmSubmit = false
                    onSubmit(roleInTeam, repoUrl, report)
                }) { Text(strings.confirmLabel) }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmSubmit = false }) { Text(strings.cancelLabel) }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkSimulationSheet(
    challenge: ChallengeItem,
    workSimState: WorkSimState,
    onRunSimulation: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val lang = LocalAppLanguage.current
    var decision by remember { mutableStateOf("") }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                if (lang == AppLanguage.AR) "محاكاة بيئة العمل الذكية (AI Work Simulation)" else "AI Work Simulation: Mid-Sprint Shift",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            InfoNoteBanner(
                text = if (lang == AppLanguage.AR) {
                    "مفاجأة العميل: ارتفع حجم الطلبات ٥× قبل التسليم بيومين مع شرط عدم فقدان أي معاملة. كيف ستعيد التخطيط وتبلغ الفريق؟"
                } else {
                    "Client Change Request: Traffic spiked 5x 48h before deadline with strict zero-data-loss SLA. How do you adapt architecture & scope?"
                }
            )
            OutlinedTextField(
                value = decision,
                onValueChange = { decision = it },
                minLines = 3,
                label = { Text(if (lang == AppLanguage.AR) "ردك وخطة التصرف الهندسية" else "Your Engineering & Scope Decision") },
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = { onRunSimulation(decision) },
                enabled = decision.isNotBlank() && workSimState !is WorkSimState.Streaming,
                colors = ButtonDefaults.buttonColors(containerColor = SkillBridgeColors.BridgeBlueDark),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text(if (lang == AppLanguage.AR) "تقييم القرار بالذكاء الاصطناعي" else "Evaluate Decision")
            }

            when (workSimState) {
                is WorkSimState.Streaming -> {
                    Card(colors = CardDefaults.cardColors(containerColor = SkillBridgeColors.SoftCyanTint)) {
                        Text(workSimState.text.ifBlank { "..." }, modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.bodyMedium)
                    }
                }
                is WorkSimState.Success -> {
                    Card(colors = CardDefaults.cardColors(containerColor = SkillBridgeColors.SoftCyanTint)) {
                        Text(workSimState.text, modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.bodyMedium)
                    }
                }
                is WorkSimState.Error -> {
                    Text(workSimState.message, color = SkillBridgeColors.DisputedRed)
                }
                WorkSimState.Idle -> Unit
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

/**
 * Screen 7: Codeforces-style Contests Screen.
 */
@Composable
fun ContestsScreen(
    onJoinTeamForContest: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)
    val strings = LocalAppStrings.current
    val lang = LocalAppLanguage.current

    var selectedStatus by remember { mutableStateOf(ContestStatus.LIVE) }
    var selectedInnerTab by remember { mutableIntStateOf(0) }

    val activeContests = SkillBridgeEcosystemData.contests.filter { it.status == selectedStatus }
    val focusedContest = activeContests.firstOrNull() ?: SkillBridgeEcosystemData.contests.first()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(SkillBridgeSpacing.screenHorizontal),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(strings.contestsTitle, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = selectedStatus == ContestStatus.LIVE, onClick = { selectedStatus = ContestStatus.LIVE }, label = { Text(strings.liveNow) })
                FilterChip(selected = selectedStatus == ContestStatus.UPCOMING, onClick = { selectedStatus = ContestStatus.UPCOMING }, label = { Text(strings.upcoming) })
                FilterChip(selected = selectedStatus == ContestStatus.ENDED, onClick = { selectedStatus = ContestStatus.ENDED }, label = { Text(strings.ended) })
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SkillBridgeColors.DeepNavy)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        StatusPill(focusedContest.countdownLabel.get(lang), if (focusedContest.status == ContestStatus.LIVE) StatusTone.PAID else StatusTone.PENDING)
                        StatusPill(focusedContest.prizePool, StatusTone.VERIFIED)
                    }
                    Text(focusedContest.title.get(lang), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = SkillBridgeColors.PureWhite)
                    Text(focusedContest.organizer, style = MaterialTheme.typography.labelLarge, color = SkillBridgeColors.SoftCyan)
                    Text(focusedContest.rulesSummary.get(lang), style = MaterialTheme.typography.bodyMedium, color = SkillBridgeColors.InkMutedOnDark)
                    if (focusedContest.isTeamContest) {
                        OutlinedButton(
                            onClick = onJoinTeamForContest,
                            border = BorderStroke(1.dp, SkillBridgeColors.SoftCyan),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Text(if (lang == AppLanguage.AR) "شارك كفريق أو ابحث عن زملاء" else "Compete as Team / Find Squad", color = SkillBridgeColors.SoftCyan)
                        }
                    }
                }
            }
        }

        item {
            TabRow(selectedTabIndex = selectedInnerTab) {
                Tab(selected = selectedInnerTab == 0, onClick = { selectedInnerTab = 0 }, text = { Text(strings.problemsTab) })
                Tab(selected = selectedInnerTab == 1, onClick = { selectedInnerTab = 1 }, text = { Text(strings.standingsTab) })
                Tab(selected = selectedInnerTab == 2, onClick = { selectedInnerTab = 2 }, text = { Text(strings.editorialTab) })
            }
        }

        when (selectedInnerTab) {
            0 -> {
                items(focusedContest.problems) { prob ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("${prob.code}. ${prob.title.get(lang)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text("${prob.difficulty} • +${prob.points} ${strings.pointsLabel} • ${prob.solvedByCount} AC", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            val tone = when (prob.status) {
                                "AC" -> StatusTone.VERIFIED
                                "PENDING" -> StatusTone.PENDING
                                "LOCKED" -> StatusTone.LOCKED
                                else -> StatusTone.NEUTRAL
                            }
                            val statusText = when {
                                lang == AppLanguage.AR && prob.status == "AC" -> "مقبول"
                                lang == AppLanguage.AR && prob.status == "PENDING" -> "قيد الانتظار"
                                lang == AppLanguage.AR && prob.status == "LOCKED" -> "مقفل"
                                else -> prob.status
                            }
                            StatusPill(statusText, tone)
                        }
                    }
                }
            }
            1 -> {
                items(SkillBridgeEcosystemData.leaderboardEntries.take(6)) { entry ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("#${entry.rank}  ${entry.name}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            StatusPill("${if (lang == AppLanguage.AR) "تقييم" else "Rating"} ${entry.rating} • ${entry.points} ${strings.pointsLabel}", StatusTone.REVIEW)
                        }
                    }
                }
            }
            else -> {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(strings.editorialTab, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(focusedContest.editorialSummary.get(lang), style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
        }
        item { Spacer(Modifier.height(16.dp)) }
    }
}

/**
 * Screen 8: Ethical Reverse Engineering Hub.
 */
@Composable
fun ReverseEngineeringHubScreen(
    containsSecret: (String) -> Boolean,
    onSubmitLab: (ChallengeItem, String, String, String) -> Unit,
    onOpenAiAgent: (AiAgentContextMode, String, String) -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)
    val strings = LocalAppStrings.current
    val lang = LocalAppLanguage.current
    val labs = SkillBridgeEcosystemData.challenges.filter { it.category == DiscoverCategory.REVERSE_ENG }
    var activeLabSheet by remember { mutableStateOf<ChallengeItem?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(SkillBridgeSpacing.screenHorizontal),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Filled.Shield, contentDescription = null, tint = SkillBridgeColors.EthicalPurple)
                Text(strings.reverseEngTitle, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(6.dp))
            StatusPill(strings.authorizedScopeBadge, StatusTone.ETHICAL, Icons.Filled.Security)
        }

        item {
            ContextualAiAgentBanner(
                mode = AiAgentContextMode.REVERSE_ENG_GUIDE,
                onOpenAgent = {
                    onOpenAiAgent(
                        AiAgentContextMode.REVERSE_ENG_GUIDE,
                        strings.reverseEngTitle,
                        "Authorized sandbox labs=${labs.joinToString(", ") { it.title.en }}"
                    )
                }
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SkillBridgeColors.EthicalBg.copy(alpha = 0.55f)),
                border = BorderStroke(1.dp, SkillBridgeColors.EthicalPurple.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(strings.allowedVsProhibited, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = SkillBridgeColors.DeepNavy)
                    Text(
                        if (lang == AppLanguage.AR) "مسموح: تحليل ملفات المختبر التجريبية المرفقة، إعادة بناء البروتوكول بأسلوب Clean-Room، وكتابة تقرير فهم."
                        else "ALLOWED: Inspect sandbox traces & mock specs, clean-room reconstruction, automated boundary testing, and educational Understanding Reports.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SkillBridgeColors.VerifiedGreen
                    )
                    Text(
                        if (lang == AppLanguage.AR) "ممنوع: استهداف خدمات حقيقية، فحص نطاقات خارجية، رفع مفاتيح API/بيانات سرية، أو نسخ حلول بلا فهم."
                        else "PROHIBITED: Targeting live services/external domains, uploading real API keys/PII, brute-force tooling, or submitting code without an Understanding Report.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SkillBridgeColors.DisputedRed
                    )
                }
            }
        }

        items(labs) { lab ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        StatusPill(strings.authorizedScopeBadge, StatusTone.ETHICAL)
                        StatusPill("+${lab.pointsReward} ${strings.pointsLabel} • ${lab.prizePool}", StatusTone.PAID)
                    }
                    Text(lab.title.get(lang), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(lab.scopeNote?.get(lang).orEmpty(), style = MaterialTheme.typography.labelMedium, color = SkillBridgeColors.EthicalPurple)

                    if (lab.sandboxSnippet.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(SkillBridgeRadius.sm),
                            color = SkillBridgeColors.DeepNavy
                        ) {
                            Text(
                                text = lab.sandboxSnippet,
                                style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                                color = SkillBridgeColors.SoftCyan,
                                modifier = Modifier.fillMaxWidth().padding(12.dp)
                            )
                        }
                    }

                    Text(if (lang == AppLanguage.AR) "حالات الاختبار المصرّحة:" else "Sandbox Verification Cases:", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    lab.testCases.forEach { tc ->
                        Text("• $tc", style = MaterialTheme.typography.bodyMedium)
                    }

                    Button(
                        onClick = { activeLabSheet = lab },
                        colors = ButtonDefaults.buttonColors(containerColor = SkillBridgeColors.DeepNavy),
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("reverse_eng_submit_${lab.id}")
                    ) {
                        Text(if (lang == AppLanguage.AR) "تسليم تقرير الفهم + الحل النظيف" else "Submit Clean-Room Fix & Understanding Report")
                    }
                }
            }
        }
        item { Spacer(Modifier.height(16.dp)) }
    }

    activeLabSheet?.let { lab ->
        SubmitSolutionSheet(
            challenge = lab,
            containsSecret = containsSecret,
            onDismiss = { activeLabSheet = null },
            onSubmit = { role, url, report ->
                onSubmitLab(lab, role, url, report)
                activeLabSheet = null
            }
        )
    }
}
