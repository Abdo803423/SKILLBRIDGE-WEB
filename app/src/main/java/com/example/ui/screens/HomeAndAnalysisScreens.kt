package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Architecture
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.core.AppLanguage
import com.example.core.LocalAppLanguage
import com.example.core.LocalAppStrings
import com.example.core.localizedCareerLevel
import com.example.core.localizedDomain
import com.example.data.model.AiAgentContextMode
import com.example.data.model.BadgeProgressItem
import com.example.data.model.CareerGoal
import com.example.data.model.ChallengeSubmission
import com.example.data.model.RoadmapMilestone
import com.example.data.model.SkillCategoryScore
import com.example.data.model.SkillGap
import com.example.data.model.SkillGapAnalysisResult
import com.example.data.model.UserProfile
import com.example.data.seed.SkillBridgeEcosystemData
import com.example.ui.components.AiExplainabilityCard
import com.example.ui.components.CircularSkillMatchGauge
import com.example.ui.components.ContextualAiAgentBanner
import com.example.ui.components.EmptyStateView
import com.example.ui.components.ErrorStateView
import com.example.ui.components.EvidenceProgressStepper
import com.example.ui.components.LevelAndPlanBadgeRow
import com.example.ui.components.LoadingStateView
import com.example.ui.components.LockedStateView
import com.example.ui.components.SectionHeaderRow
import com.example.ui.components.SkillCategoryBar
import com.example.ui.components.StatusPill
import com.example.ui.components.StatusTone
import com.example.ui.theme.SkillBridgeColors
import com.example.ui.theme.SkillBridgeRadius
import com.example.ui.theme.SkillBridgeSpacing
import com.example.ui.viewmodel.AnalysisUiState
import com.example.ui.viewmodel.PreviewStateMode
import com.example.ui.viewmodel.UiState
import kotlin.math.roundToInt

/**
 * Screen 3: Personalized Career Home Dashboard.
 */
@Composable
fun HomeDashboardScreen(
    displayName: String,
    profile: UserProfile?,
    goalsState: UiState<List<CareerGoal>>,
    submissions: List<ChallengeSubmission>,
    badgeProgressList: List<BadgeProgressItem>,
    previewStateMode: PreviewStateMode,
    isDesktop: Boolean = false,
    onRetry: () -> Unit,
    onOpenSkillAnalysis: () -> Unit,
    onOpenBadges: () -> Unit,
    onOpenChallenge: (String) -> Unit,
    onOpenContests: () -> Unit,
    onOpenReverseEng: () -> Unit,
    onOpenLeaderboards: () -> Unit,
    onOpenWallet: () -> Unit,
    onOpenCompanies: () -> Unit,
    onOpenCoach: () -> Unit,
    onOpenAiAgent: (AiAgentContextMode, String, String) -> Unit,
    onOpenOnboarding: () -> Unit,
    onOpenBlueprint: () -> Unit,
    onOpenPassport: () -> Unit,
    dbOverviewContent: @Composable () -> Unit = {}
) {
    val strings = LocalAppStrings.current
    val lang = LocalAppLanguage.current

    when (previewStateMode) {
        PreviewStateMode.LOADING -> {
            Box(Modifier.fillMaxSize().padding(16.dp)) { LoadingStateView() }
            return
        }
        PreviewStateMode.EMPTY -> {
            Box(Modifier.fillMaxSize().padding(16.dp)) {
                EmptyStateView(
                    subtitle = if (lang == AppLanguage.AR) "ابدأ أول تحدٍ لبناء سجل خبرتك الموثّق." else "Start your first practical challenge to populate verified evidence.",
                    onAction = onOpenSkillAnalysis
                )
            }
            return
        }
        PreviewStateMode.ERROR -> {
            Box(Modifier.fillMaxSize().padding(16.dp)) {
                ErrorStateView(
                    message = if (lang == AppLanguage.AR) "تعذّر مزامنة لوحة التقدم مؤقتًا." else "Could not sync career telemetry right now.",
                    onRetry = onRetry
                )
            }
            return
        }
        PreviewStateMode.LOCKED -> {
            Box(Modifier.fillMaxSize().padding(16.dp)) {
                LockedStateView(
                    reason = if (lang == AppLanguage.AR) "أكمل تحليل المهارات المبدئي لفتح المسار المتقدم." else "Complete Skill Gap baseline to unlock senior partner tracks.",
                    actionLabel = strings.skillAnalysisTitle,
                    onAction = onOpenSkillAnalysis
                )
            }
            return
        }
        PreviewStateMode.NORMAL -> Unit
    }

    val goals = (goalsState as? UiState.Success)?.data.orEmpty()
    val activeGoal = goals.firstOrNull { it.isActive } ?: goals.firstOrNull()
    val realSubs = submissions.filter { !it.isDemoSample }
    val verifiedCount = realSubs.count { it.status == "VERIFIED" }
    val unlockedBadgesCount = badgeProgressList.count { it.isUnlocked }
    val matchScore = activeGoal?.readinessScore?.toInt()
        ?: ((profile?.currentSkills?.size ?: 0) * 8 + verifiedCount * 15).coerceIn(0, 95)
    val careerLevel = profile?.careerLevel?.ifBlank { "Junior" } ?: "Junior"
    val planTier = profile?.planTier?.ifBlank { strings.planStarterFree } ?: strings.planStarterFree
    val points = profile?.points ?: 0L
    val streak = profile?.streakDays ?: 0L

    val shortcuts = listOf(
        Triple(strings.badgesTitle, Icons.Filled.Verified, onOpenBadges),
        Triple(strings.contestsTitle, Icons.Filled.Code, onOpenContests),
        Triple(strings.reverseEngTitle, Icons.Filled.Security, onOpenReverseEng),
        Triple(strings.leaderboardTitle, Icons.Filled.EmojiEvents, onOpenLeaderboards),
        Triple(strings.walletTitle, Icons.Filled.AccountBalanceWallet, onOpenWallet),
        Triple(if (lang == AppLanguage.AR) "مشاريع الشركات" else "Company Briefs", Icons.Filled.Business, onOpenCompanies),
        Triple(if (lang == AppLanguage.AR) "المدرب الذكي AI" else "AI Career Coach", Icons.Filled.Psychology, onOpenCoach),
        Triple(strings.blueprintTitle, Icons.Filled.Architecture, onOpenBlueprint)
    )

    val heroCard: @Composable () -> Unit = {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(SkillBridgeRadius.lg),
            colors = CardDefaults.cardColors(containerColor = SkillBridgeColors.DeepNavy)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            listOf(SkillBridgeColors.DeepNavy, ColorBlendHeader)
                        )
                    )
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (lang == AppLanguage.AR) "مرحبًا، $displayName" else "Welcome back, $displayName",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = SkillBridgeColors.PureWhite
                        )
                        Text(
                            text = localizedDomain(profile?.domain?.ifBlank { "Backend & Cloud" } ?: "Backend & Cloud", lang),
                            style = MaterialTheme.typography.bodyMedium,
                            color = SkillBridgeColors.SoftCyan
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(SkillBridgeRadius.pill),
                        color = SkillBridgeColors.PureWhite.copy(alpha = 0.12f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Filled.LocalFireDepartment,
                                contentDescription = null,
                                tint = SkillBridgeColors.LimeGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "$streak ${strings.streakLabel}",
                                style = MaterialTheme.typography.labelMedium,
                                color = SkillBridgeColors.PureWhite
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        modifier = Modifier.weight(1f).clickable(onClick = onOpenWallet),
                        shape = RoundedCornerShape(SkillBridgeRadius.md),
                        color = SkillBridgeColors.PureWhite.copy(alpha = 0.1f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(strings.pointsLabel, style = MaterialTheme.typography.labelSmall, color = SkillBridgeColors.SoftCyan)
                            Text(
                                "$points",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = SkillBridgeColors.PureWhite,
                                modifier = Modifier.testTag("home_points_value")
                            )
                        }
                    }
                    Surface(
                        modifier = Modifier.weight(1f).clickable(onClick = onOpenBadges),
                        shape = RoundedCornerShape(SkillBridgeRadius.md),
                        color = SkillBridgeColors.PureWhite.copy(alpha = 0.1f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(strings.badgesTitle, style = MaterialTheme.typography.labelSmall, color = SkillBridgeColors.LimeGreen)
                            Text(
                                "$unlockedBadgesCount / ${badgeProgressList.size}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = SkillBridgeColors.PureWhite,
                                modifier = Modifier.testTag("home_badges_value")
                            )
                        }
                    }
                }

                val levelIndex = SkillBridgeEcosystemData.careerLevels.indexOfFirst {
                    it.equals(careerLevel, ignoreCase = true)
                }.coerceAtLeast(0)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (lang == AppLanguage.AR) "المستوى الموثّق: ${localizedCareerLevel(careerLevel, lang)}"
                            else "Evidence Level: $careerLevel",
                            style = MaterialTheme.typography.labelMedium,
                            color = SkillBridgeColors.PureWhite
                        )
                        Text(
                            text = "${((levelIndex + 1) * 25)}%",
                            style = MaterialTheme.typography.labelMedium,
                            color = SkillBridgeColors.LimeGreen
                        )
                    }
                    LinearProgressIndicator(
                        progress = { (levelIndex + 1) / 4f },
                        modifier = Modifier.fillMaxWidth().height(7.dp).clip(RoundedCornerShape(999.dp)),
                        color = SkillBridgeColors.LimeGreen,
                        trackColor = SkillBridgeColors.PureWhite.copy(alpha = 0.2f)
                    )
                }
            }
        }
    }

    val skillMatchCard: @Composable () -> Unit = {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onOpenSkillAnalysis)
                .testTag("home_skill_match_card"),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                CircularSkillMatchGauge(
                    percent = matchScore,
                    label = strings.yourSkillMatch
                )
                EvidenceProgressStepper(activeStepIndex = 4)
                OutlinedButton(
                    onClick = onOpenSkillAnalysis,
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text(strings.skillAnalysisTitle)
                }
            }
        }
    }

    val aiRecommendationCard: @Composable () -> Unit = {
        AiExplainabilityCard(
            explanation = if (lang == AppLanguage.AR) {
                if (verifiedCount == 0) {
                    "نقطة البداية الموصى بها: ابدأ بتحدي لوحة المدينة الذكية لتسجيل أول تسليم عملي وفتح شارة أول تحدٍ (+150 نقطة)."
                } else {
                    "نرشح لك تحدي لوحة المدينة الذكية بناءً على مهاراتك الحالية وساعات الممارسة الأسبوعية (${profile?.weeklyHours ?: 8} س/أسبوع)."
                }
            } else {
                if (verifiedCount == 0) {
                    "Recommended starting milestone: Complete Smart City Analytics Dashboard to earn your first verified evidence & unlock the First Challenge badge."
                } else {
                    "Recommended next step: Smart City Analytics Dashboard aligns with your skills and closes your Time-Series Telemetry gap."
                }
            },
            inputsUsed = if (lang == AppLanguage.AR) {
                listOf("الأدلة الموثقة: $verifiedCount", "${profile?.weeklyHours ?: 8} س/أسبوع", localizedCareerLevel(careerLevel, lang))
            } else {
                listOf("Verified evidence: $verifiedCount", "${profile?.weeklyHours ?: 8}h/week", careerLevel)
            },
            confidencePercent = if (activeGoal != null) 91 else 78,
            onEditPreferences = onOpenOnboarding,
            onAppeal = { onOpenChallenge("ch_smart_city") }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(if (isDesktop) 24.dp else SkillBridgeSpacing.screenHorizontal),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (isDesktop) {
            // Desktop: 2-Column Top Workspace (Left: Hero + Skill Match + AI; Right: Live Central DB Telemetry)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        heroCard()
                        LevelAndPlanBadgeRow(careerLevel = careerLevel, planTier = planTier)
                        skillMatchCard()
                        ContextualAiAgentBanner(
                            mode = AiAgentContextMode.SKILL_ADVISOR,
                            onOpenAgent = {
                                onOpenAiAgent(
                                    AiAgentContextMode.SKILL_ADVISOR,
                                    "Home Dashboard",
                                    "User level=$careerLevel, points=$points, verified=$verifiedCount, activeGoal=${activeGoal?.targetRole ?: "None"}"
                                )
                            }
                        )
                    }
                    Column(
                        modifier = Modifier.weight(1.1f),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        dbOverviewContent()
                    }
                }
            }

            // Desktop: 4-Column Ecosystem Hubs Grid
            item {
                SectionHeaderRow(title = if (lang == AppLanguage.AR) "بوابات المنصة المتخصصة" else "Ecosystem Hubs")
                Spacer(Modifier.height(10.dp))
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    shortcuts.chunked(4).forEach { rowShortcuts ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            rowShortcuts.forEach { (label, icon, action) ->
                                Surface(
                                    shape = RoundedCornerShape(SkillBridgeRadius.md),
                                    color = MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable(onClick = action)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Icon(icon, contentDescription = label, tint = SkillBridgeColors.BridgeBlueDark)
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Desktop: 2-Column Bottom Row (AI Recommendation + Active Challenges)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        aiRecommendationCard()
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        SectionHeaderRow(
                            title = strings.activeChallenges,
                            actionText = strings.filterAll,
                            onAction = { onOpenChallenge("ch_smart_city") }
                        )
                        SkillBridgeEcosystemData.challenges.take(3).forEach { ch ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onOpenChallenge(ch.id) },
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = ch.title.get(lang),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.weight(1f)
                                        )
                                        StatusPill(text = "+${ch.pointsReward} ${strings.pointsLabel}", tone = StatusTone.PAID)
                                    }
                                    Text(
                                        text = "${ch.companyName} • ${ch.soloOrTeamLabel.get(lang)} • ${ch.deadline}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            item { heroCard() }
            item { LevelAndPlanBadgeRow(careerLevel = careerLevel, planTier = planTier) }
            item { skillMatchCard() }
            item {
                ContextualAiAgentBanner(
                    mode = AiAgentContextMode.SKILL_ADVISOR,
                    onOpenAgent = {
                        onOpenAiAgent(
                            AiAgentContextMode.SKILL_ADVISOR,
                            "Home Dashboard",
                            "User level=$careerLevel, points=$points, verified=$verifiedCount, activeGoal=${activeGoal?.targetRole ?: "None"}"
                        )
                    }
                )
            }
            item { dbOverviewContent() }
            item {
                SectionHeaderRow(title = if (lang == AppLanguage.AR) "بوابات المنصة المتخصصة" else "Ecosystem Hubs")
                Spacer(Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(shortcuts) { (label, icon, action) ->
                        Surface(
                            shape = RoundedCornerShape(SkillBridgeRadius.md),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier
                                .width(136.dp)
                                .clickable(onClick = action)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(icon, contentDescription = label, tint = SkillBridgeColors.BridgeBlueDark)
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
            item { aiRecommendationCard() }
            item {
                SectionHeaderRow(
                    title = strings.activeChallenges,
                    actionText = strings.filterAll,
                    onAction = { onOpenChallenge("ch_smart_city") }
                )
            }
            items(SkillBridgeEcosystemData.challenges.take(3)) { ch ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenChallenge(ch.id) },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = ch.title.get(lang),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            StatusPill(text = "+${ch.pointsReward} ${strings.pointsLabel}", tone = StatusTone.PAID)
                        }
                        Text(
                            text = "${ch.companyName} • ${ch.soloOrTeamLabel.get(lang)} • ${ch.deadline}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        item { Spacer(Modifier.height(16.dp)) }
    }
}

private val ColorBlendHeader = androidx.compose.ui.graphics.Color(0xFF0E3A5D)

/**
 * Screen 4: Skill Analysis & AI Skill Gap Analyzer matching Phone #2 in Poster.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SkillAnalysisScreen(
    goalsState: UiState<List<CareerGoal>>,
    gapsState: UiState<List<SkillGap>>,
    milestonesState: UiState<List<RoadmapMilestone>>,
    analysisState: AnalysisUiState,
    latestAnalysis: SkillGapAnalysisResult?,
    onRunAnalysis: (String, String, List<String>, String, Int) -> Unit,
    onToggleMilestone: (RoadmapMilestone) -> Unit,
    onProveSkill: (String) -> Unit,
    onOpenAiAgent: (String) -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)
    val strings = LocalAppStrings.current
    val lang = LocalAppLanguage.current

    val goals = (goalsState as? UiState.Success)?.data.orEmpty()
    val activeGoal = goals.firstOrNull { it.isActive } ?: goals.firstOrNull()
    val gaps = (gapsState as? UiState.Success)?.data.orEmpty()
        .filter { activeGoal == null || it.goalId == activeGoal.id }
    val milestones = (milestonesState as? UiState.Success)?.data.orEmpty()
        .filter { activeGoal == null || it.goalId == activeGoal.id }

    val score = latestAnalysis?.readinessScore ?: activeGoal?.readinessScore?.toInt() ?: 0
    val cats = latestAnalysis?.categoryScores ?: if (activeGoal != null) {
        SkillCategoryScore(score, (score - 8).coerceAtLeast(10), (score - 4).coerceAtLeast(10), score, (score - 6).coerceAtLeast(10))
    } else {
        SkillCategoryScore(0, 0, 0, 0, 0)
    }

    var showAnalyzerForm by remember { mutableStateOf(false) }
    var targetRole by remember { mutableStateOf(activeGoal?.targetRole?.ifBlank { "Senior Data & Backend Engineer" } ?: "Senior Data & Backend Engineer") }
    var transitionLevel by remember { mutableStateOf("Mid -> Senior") }
    var skillsText by remember { mutableStateOf("Kotlin, SQL, Jetpack Compose, REST APIs") }
    var notes by remember { mutableStateOf("") }
    var hours by remember { mutableFloatStateOf(10f) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(SkillBridgeSpacing.screenHorizontal),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(strings.skillAnalysisTitle, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                OutlinedButton(
                    onClick = { showAnalyzerForm = !showAnalyzerForm },
                    modifier = Modifier.height(48.dp).testTag("toggle_ai_analyzer_btn")
                ) {
                    Icon(Icons.Filled.Tune, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(strings.aiGapAnalyzer)
                }
            }
        }

        item {
            ContextualAiAgentBanner(
                mode = AiAgentContextMode.SKILL_ADVISOR,
                onOpenAgent = {
                    onOpenAiAgent("Target=${activeGoal?.targetRole ?: targetRole}, Score=$score%, OpenGaps=${gaps.size}")
                }
            )
        }

        if (showAnalyzerForm) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, SkillBridgeColors.BridgeBlue.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(strings.aiGapAnalyzer, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        OutlinedTextField(
                            value = targetRole,
                            onValueChange = { targetRole = it },
                            label = { Text(if (lang == AppLanguage.AR) "الدور المستهدف" else "Target Role") },
                            modifier = Modifier.fillMaxWidth().testTag("analyzer_target_role")
                        )
                        OutlinedTextField(
                            value = skillsText,
                            onValueChange = { skillsText = it },
                            label = { Text(if (lang == AppLanguage.AR) "مهاراتك الحالية" else "Current Skills") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text(if (lang == AppLanguage.AR) "ملاحظات أو اعتراض على التقييم" else "Background / Appeal Notes") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text("${if (lang == AppLanguage.AR) "ساعات الدراسة/أسبوع:" else "Weekly Hours:"} ${hours.roundToInt()}h")
                        Slider(value = hours, onValueChange = { hours = it }, valueRange = 2f..30f)

                        if (analysisState is AnalysisUiState.Error) {
                            Text(analysisState.message, color = SkillBridgeColors.DisputedRed, style = MaterialTheme.typography.bodyMedium)
                        }

                        Button(
                            onClick = {
                                onRunAnalysis(
                                    targetRole,
                                    transitionLevel,
                                    skillsText.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                                    notes,
                                    hours.roundToInt()
                                )
                            },
                            enabled = analysisState !is AnalysisUiState.Loading,
                            colors = ButtonDefaults.buttonColors(containerColor = SkillBridgeColors.DeepNavy),
                            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("run_ai_analysis_btn")
                        ) {
                            if (analysisState is AnalysisUiState.Loading) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = SkillBridgeColors.PureWhite, strokeWidth = 2.dp)
                                Spacer(Modifier.width(8.dp))
                            }
                            Text(if (lang == AppLanguage.AR) "تحليل الفجوات بالذكاء الاصطناعي" else "Generate Explainable Gap Analysis")
                        }
                    }
                }
            }
        }

        // Poster Phone #2 Card: Match Gauge + 5 Category Bars
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    CircularSkillMatchGauge(percent = score, label = strings.yourSkillMatch)
                    Text(strings.skillOverview, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    SkillCategoryBar(strings.techSkills, cats.technical, barColor = SkillBridgeColors.BridgeBlue)
                    SkillCategoryBar(strings.softSkills, cats.softSkills, barColor = SkillBridgeColors.SoftCyan)
                    SkillCategoryBar(strings.domainKnowledge, cats.domainKnowledge, barColor = SkillBridgeColors.BridgeBlueDark)
                    SkillCategoryBar(strings.problemSolving, cats.problemSolving, barColor = SkillBridgeColors.LimeGreenDark)
                    SkillCategoryBar(strings.commCollab, cats.communication, barColor = SkillBridgeColors.BridgeBlue)
                }
            }
        }

        item {
            AiExplainabilityCard(
                explanation = latestAnalysis?.aiExplanation
                    ?: activeGoal?.executiveSummary?.ifBlank { null }
                    ?: if (lang == AppLanguage.AR) "شغّل تحليل الفجوات الذكي لتوليد تقييم مخصص لمهاراتك وأهدافك دون أرقام افتراضية." else "Run AI Skill Gap Analyzer above to compute a personalized readiness score from your actual skills and goals.",
                inputsUsed = latestAnalysis?.inputsUsed ?: if (lang == AppLanguage.AR) {
                    listOf("تحليل المهارات", "ساعات الممارسة", "أدلة التحديات")
                } else {
                    listOf("Declared Skills", "Weekly Practice Hours", "Verified Submissions")
                },
                confidencePercent = latestAnalysis?.confidencePercent ?: if (activeGoal != null) 88 else 70,
                onEditPreferences = { showAnalyzerForm = true },
                onAppeal = { onProveSkill("ch_smart_city") }
            )
        }

        // Skill Gaps -> Direct Prove This Skill CTA
        item {
            SectionHeaderRow(title = if (lang == AppLanguage.AR) "فجوات المهارات والتطبيق العملي" else "Skill Gaps & Evidence Actions")
        }

        if (gaps.isEmpty()) {
            val defaultGaps = listOf(
                Triple("Time-Series SQL & Telemetry", "Developing -> Senior", "ch_smart_city"),
                Triple("Idempotency & Concurrency Locks", "Intermediate -> Expert", "ch_nilepay_guard"),
                Triple("Ethical Auth Protocol Auditing", "Beginner -> Proficient", "ch_re_jwt_lab")
            )
            items(defaultGaps) { (skill, levelDelta, challengeId) ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(skill, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(levelDelta, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Button(
                            onClick = { onProveSkill(challengeId) },
                            colors = ButtonDefaults.buttonColors(containerColor = SkillBridgeColors.BridgeBlueDark),
                            modifier = Modifier.height(48.dp)
                        ) {
                            Text(strings.proveThisSkill)
                        }
                    }
                }
            }
        } else {
            items(gaps) { gap ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(gap.skillName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            StatusPill(gap.priority, if (gap.priority == "HIGH") StatusTone.DISPUTED else StatusTone.PENDING)
                        }
                        Text("${gap.currentLevel} → ${gap.targetLevel}", style = MaterialTheme.typography.labelLarge, color = SkillBridgeColors.BridgeBlueDark)
                        Text(gap.whyItMatters, style = MaterialTheme.typography.bodyMedium)
                        Button(
                            onClick = { onProveSkill("ch_nilepay_guard") },
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Text(strings.proveThisSkill)
                        }
                    }
                }
            }
        }

        if (milestones.isNotEmpty()) {
            item {
                SectionHeaderRow(title = if (lang == AppLanguage.AR) "خطة الإغلاق العملي" else "Practice Roadmap Milestones")
            }
            items(milestones) { ms ->
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { onToggleMilestone(ms) },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatusPill(
                            text = if (ms.completed) (if (lang == AppLanguage.AR) "مكتمل" else "Done")
                            else (if (lang == AppLanguage.AR) "المرحلة ${ms.phaseOrder}" else "Phase ${ms.phaseOrder}"),
                            tone = if (ms.completed) StatusTone.VERIFIED else StatusTone.NEUTRAL
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(ms.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Text("${ms.estimatedHours}h • ${ms.practiceProject}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
        item { Spacer(Modifier.height(16.dp)) }
    }
}
