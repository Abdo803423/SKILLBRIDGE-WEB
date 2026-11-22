package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.core.AppLanguage
import com.example.core.AssetRegistry
import com.example.core.LocalAppLanguage
import com.example.core.LocalAppStrings
import com.example.core.localizedCareerLevel
import com.example.core.localizedDomain
import com.example.core.localizedLeaderboardScope
import com.example.core.localizedSubmissionStatus
import com.example.data.local.AccountEntity
import com.example.data.local.WorkSubmissionEntity
import com.example.data.model.AiAgentContextMode
import com.example.data.model.AuditLogEntry
import com.example.data.model.BadgeProgressItem
import com.example.data.model.ChallengeSubmission
import com.example.data.model.PointLedgerEntry
import com.example.data.model.UserProfile
import com.example.data.seed.SkillBridgeEcosystemData
import com.example.ui.components.ContextualAiAgentBanner
import com.example.ui.components.EmptyStateView
import com.example.ui.components.InfoNoteBanner
import com.example.ui.components.LevelAndPlanBadgeRow
import com.example.ui.components.SectionHeaderRow
import com.example.ui.components.StatusPill
import com.example.ui.components.StatusTone
import com.example.ui.theme.SkillBridgeColors
import com.example.ui.theme.SkillBridgeRadius
import com.example.ui.theme.SkillBridgeSpacing
import com.example.ui.viewmodel.UiState

/**
 * Screen 13: Multi-Dimensional Top-100 Leaderboards.
 */
@Composable
fun LeaderboardScreen(
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)
    val strings = LocalAppStrings.current
    val lang = LocalAppLanguage.current

    val scopes = listOf("Global", "MENA", "Egypt", "Saudi Arabia", "UAE", "Jordan", "Junior", "Mid-level", "Senior", "Teams", "Contests", "Community")
    var selectedScope by remember { mutableStateOf("MENA") }
    var selectedDomain by remember { mutableStateOf("All") }

    val filtered = remember(selectedScope, selectedDomain) {
        val base = SkillBridgeEcosystemData.leaderboardEntries.filter { entry ->
            val scopeOk = when (selectedScope) {
                "Global" -> true
                "MENA" -> entry.region == "MENA"
                "Egypt", "Saudi Arabia", "UAE", "Jordan" -> entry.country == selectedScope
                "Junior", "Mid-level", "Senior" -> entry.level == selectedScope
                "Teams" -> entry.isTeam
                else -> !entry.isTeam
            }
            val domainOk = selectedDomain == "All" || entry.domain == selectedDomain
            scopeOk && domainOk
        }
        val list = if (base.isEmpty()) SkillBridgeEcosystemData.leaderboardEntries.take(6) else base
        list.sortedByDescending { it.rating }.mapIndexed { idx, item -> item.copy(rank = idx + 1) }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(SkillBridgeSpacing.screenHorizontal),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(strings.leaderboardTitle, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text(strings.top100Title, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                StatusPill(strings.demoCatalogLabel, StatusTone.NEUTRAL)
            }
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                scopes.forEach { sc ->
                    FilterChip(
                        selected = selectedScope == sc,
                        onClick = { selectedScope = sc },
                        label = { Text(localizedLeaderboardScope(sc, lang)) }
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
            Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                (listOf("All") + SkillBridgeEcosystemData.domains).forEach { dom ->
                    FilterChip(
                        selected = selectedDomain == dom,
                        onClick = { selectedDomain = dom },
                        label = { Text(localizedDomain(dom, lang)) }
                    )
                }
            }
        }

        if (filtered.size >= 3) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SkillBridgeColors.DeepNavy)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        PodiumColumn(entry = filtered[1], medal = "#2", accent = SkillBridgeColors.SoftCyan, heightDp = 82, modifier = Modifier.weight(1f))
                        PodiumColumn(entry = filtered[0], medal = "#1", accent = SkillBridgeColors.LimeGreen, heightDp = 104, modifier = Modifier.weight(1.1f))
                        PodiumColumn(entry = filtered[2], medal = "#3", accent = SkillBridgeColors.BridgeBlue, heightDp = 70, modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SkillBridgeColors.SoftCyanTint.copy(alpha = 0.7f)),
                border = BorderStroke(1.dp, SkillBridgeColors.BridgeBlue.copy(alpha = 0.35f))
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(strings.ratingFormulaTitle, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = SkillBridgeColors.DeepNavy)
                    Text(
                        if (lang == AppLanguage.AR) {
                            "التقييم (Rating) = أداء المسابقات والتحديات الموثّقة (٦٠٪) + تقييم المرشدين (٢٥٪) + إجابات المجتمع المفيدة (١٥٪). النقاط (Points) تراكمية ولا تنخفض."
                        } else {
                            "Rating = Verified Challenges & Contests (60%) + Rubric Mentor Score (25%) + Community Accepted Answers (15%). Points accumulate cumulatively."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = SkillBridgeColors.DeepNavy
                    )
                }
            }
        }

        items(filtered) { entry ->
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
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
                        Surface(
                            shape = CircleShape,
                            color = if (entry.rank <= 3) SkillBridgeColors.DeepNavy else SkillBridgeColors.SurfaceSubtle,
                            modifier = Modifier.size(34.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    "${entry.rank}",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (entry.rank <= 3) SkillBridgeColors.LimeGreen else SkillBridgeColors.DeepNavy
                                )
                            }
                        }
                        Column {
                            Text(entry.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            val verifiedWord = if (lang == AppLanguage.AR) "موثّق" else "verified"
                            Text(
                                "${localizedLeaderboardScope(entry.country, lang)} • ${localizedCareerLevel(entry.level, lang)} • ${entry.verifiedCount} $verifiedWord",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("${entry.rating}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = SkillBridgeColors.BridgeBlueDark)
                        Text(
                            text = "${entry.points} ${strings.pointsLabel} (${if (entry.rankDelta >= 0) "+${entry.rankDelta}" else "${entry.rankDelta}"})",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (entry.rankDelta >= 0) SkillBridgeColors.VerifiedGreen else SkillBridgeColors.DisputedRed
                        )
                    }
                }
            }
        }
        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
private fun PodiumColumn(
    entry: com.example.data.model.LeaderboardEntry,
    medal: String,
    accent: androidx.compose.ui.graphics.Color,
    heightDp: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(entry.name, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = SkillBridgeColors.PureWhite, maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center)
        Text("${entry.rating}", style = MaterialTheme.typography.labelMedium, color = accent, fontWeight = FontWeight.Bold)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(heightDp.dp)
                .background(accent.copy(alpha = 0.22f), RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(medal, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = accent)
        }
    }
}

/**
 * Screen 14: Experience Passport + Badges + Skill Evidence Graph + Real Audit Log.
 * Strictly separates real user achievements from optional Demo Reference examples.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExperiencePassportScreen(
    displayName: String,
    profile: UserProfile?,
    submissionsState: UiState<List<ChallengeSubmission>>,
    badgeProgressList: List<BadgeProgressItem>,
    auditLogsState: UiState<List<AuditLogEntry>>,
    onToggleSubmissionVisibility: (ChallengeSubmission) -> Unit,
    onOpenBadges: () -> Unit,
    onOpenWallet: () -> Unit,
    onOpenLeaderboards: () -> Unit,
    onOpenPrivacyStates: () -> Unit,
    onOpenChallenge: (String) -> Unit,
    onOpenAiAgent: (String) -> Unit,
    onShareFeedbackMessage: (String) -> Unit,
    dbAccount: AccountEntity? = null,
    dbSubmissions: List<WorkSubmissionEntity> = emptyList(),
    isDesktop: Boolean = false
) {
    val strings = LocalAppStrings.current
    val lang = LocalAppLanguage.current

    val realSubs = remember(submissionsState) {
        ((submissionsState as? UiState.Success)?.data.orEmpty()).filter { !it.isDemoSample && !it.id.startsWith("seed_") }
    }
    val auditLogs = (auditLogsState as? UiState.Success)?.data.orEmpty()
    val unlockedBadges = badgeProgressList.filter { it.isUnlocked }

    var selectedCertificate by remember { mutableStateOf<ChallengeSubmission?>(null) }
    var showDemoReference by remember { mutableStateOf(false) }
    val showRepos = profile?.showRepoLinks ?: true
    val careerLevel = dbAccount?.careerLevel?.ifBlank { null }
        ?: profile?.careerLevel?.ifBlank { "Junior" }
        ?: "Junior"
    val effectivePoints = maxOf(profile?.points ?: 0L, (dbAccount?.points ?: 0).toLong())

    val heroVerifiedCard: @Composable () -> Unit = {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(SkillBridgeRadius.lg),
            colors = CardDefaults.cardColors(containerColor = SkillBridgeColors.DeepNavy)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(SkillBridgeColors.DeepNavy, SkillBridgeColors.DeepNavyElevated)))
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(shape = CircleShape, color = SkillBridgeColors.BridgeBlue.copy(alpha = 0.25f), modifier = Modifier.size(56.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.Verified, contentDescription = null, tint = SkillBridgeColors.SoftCyan, modifier = Modifier.size(32.dp))
                    }
                }
                Text(strings.verifiedExperience, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = SkillBridgeColors.PureWhite)
                Text(
                    "$displayName • ${localizedCareerLevel(careerLevel, lang)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SkillBridgeColors.SoftCyan
                )
                Text(
                    if (lang == AppLanguage.AR) {
                        "النقاط الفعلية: $effectivePoints • الشارات المفتوحة: ${unlockedBadges.size}/${badgeProgressList.size}"
                    } else {
                        "Verified Points: $effectivePoints • Unlocked Badges: ${unlockedBadges.size}/${badgeProgressList.size}"
                    },
                    style = MaterialTheme.typography.labelLarge,
                    color = SkillBridgeColors.LimeGreen
                )
            }
        }
    }

    val quickActionsSection: @Composable () -> Unit = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = onOpenBadges, modifier = Modifier.weight(1f).height(48.dp).testTag("passport_open_badges_btn")) {
                    Icon(Icons.Filled.Verified, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("${strings.badgesTitle} (${unlockedBadges.size})", maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                OutlinedButton(onClick = onOpenWallet, modifier = Modifier.weight(1f).height(48.dp)) {
                    Icon(Icons.Filled.AccountBalanceWallet, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(strings.walletTitle, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = onOpenLeaderboards, modifier = Modifier.weight(1f).height(48.dp)) {
                    Icon(Icons.Filled.EmojiEvents, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(strings.leaderboardTitle, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                OutlinedButton(onClick = onOpenPrivacyStates, modifier = Modifier.weight(1f).height(48.dp)) {
                    Text(strings.privacySettingsTitle, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }

    val dbSubmissionCard: @Composable (WorkSubmissionEntity) -> Unit = { sub ->
        Card(
            modifier = Modifier.fillMaxWidth().testTag("passport_db_sub_${sub.id}"),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(sub.briefTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            "${sub.companyName} • ${sub.verificationCode}",
                            style = MaterialTheme.typography.labelMedium,
                            color = SkillBridgeColors.BridgeBlueDark
                        )
                    }
                    StatusPill(
                        text = if (sub.score > 0) "${sub.status} • ${sub.score}%" else sub.status,
                        tone = when (sub.status) {
                            "VERIFIED" -> StatusTone.VERIFIED
                            "REJECTED" -> StatusTone.DISPUTED
                            else -> StatusTone.PENDING
                        }
                    )
                }
                if (sub.feedback.isNotBlank()) {
                    Text(
                        text = sub.feedback,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (sub.pointsAwarded > 0) {
                    Text(
                        text = "+${sub.pointsAwarded} pts verified",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = SkillBridgeColors.VerifiedGreen
                    )
                }
            }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().testTag("experience_passport_list"),
        contentPadding = PaddingValues(if (isDesktop) 24.dp else SkillBridgeSpacing.screenHorizontal),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(strings.passportTitle, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                OutlinedButton(
                    onClick = {
                        onShareFeedbackMessage(
                            if (lang == AppLanguage.AR) "تم نسخ رابط التحقق: skillbridge.dev/passport/${profile?.userId?.take(8) ?: "sb"}"
                            else "Copied Verified Passport link: skillbridge.dev/passport/${profile?.userId?.take(8) ?: "sb"}"
                        )
                    },
                    modifier = Modifier.height(48.dp).testTag("share_passport_btn")
                ) {
                    Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(strings.sharePassport)
                }
            }
        }

        if (isDesktop) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(modifier = Modifier.weight(1.1f)) { heroVerifiedCard() }
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        LevelAndPlanBadgeRow(
                            careerLevel = careerLevel,
                            planTier = profile?.planTier?.ifBlank { strings.planStarterFree } ?: strings.planStarterFree
                        )
                        ContextualAiAgentBanner(
                            mode = AiAgentContextMode.PASSPORT_REVIEWER,
                            onOpenAgent = {
                                onOpenAiAgent(
                                    "Level=$careerLevel, RealSubmissions=${realSubs.size}, UnlockedBadges=${unlockedBadges.size}/${badgeProgressList.size}"
                                )
                            }
                        )
                        quickActionsSection()
                    }
                }
            }

            // Desktop 5-column Badges Grid Row
            item {
                SectionHeaderRow(
                    title = "${strings.badgesTitle} (${unlockedBadges.size}/${badgeProgressList.size})",
                    actionText = if (lang == AppLanguage.AR) "عرض الكل" else "View All",
                    onAction = onOpenBadges
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    badgeProgressList.take(5).forEach { item ->
                        val visual = AssetRegistry.badgeVisualFor(item.definition.id)
                        Card(
                            modifier = Modifier.weight(1f).clickable(onClick = onOpenBadges),
                            colors = CardDefaults.cardColors(
                                containerColor = if (item.isUnlocked) SkillBridgeColors.VerifiedBg.copy(alpha = 0.5f)
                                else MaterialTheme.colorScheme.surface
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(visual.icon, contentDescription = null, tint = if (item.isUnlocked) visual.accentLight else SkillBridgeColors.LockedSlate)
                                Text(item.definition.title.get(lang), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(
                                    text = if (item.isUnlocked) strings.unlockedBadgesLabel else "${item.progressPercent}% • ${strings.lockedBadgesLabel}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (item.isUnlocked) SkillBridgeColors.VerifiedGreen else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Desktop 2-column Deliverables + Evidence Graph / Audit Log
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Column(
                        modifier = Modifier.weight(1.15f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (dbSubmissions.isNotEmpty()) {
                            SectionHeaderRow(
                                title = if (lang == AppLanguage.AR) "أدلة مشاريع الشركات الموثّقة (${dbSubmissions.size})"
                                else "Company Brief Deliverables & Evidence (${dbSubmissions.size})"
                            )
                            dbSubmissions.forEach { sub -> dbSubmissionCard(sub) }
                        }

                        SectionHeaderRow(
                            title = if (lang == AppLanguage.AR) "سجل تسليماتك وأدلتك الفعلية" else "Your Verified & Active Submissions"
                        )
                        if (realSubs.isEmpty() && dbSubmissions.isEmpty()) {
                            EmptyStateView(
                                title = if (lang == AppLanguage.AR) "لا توجد تسليمات موثّقة بعد" else "0 Verified Submissions Yet",
                                subtitle = if (lang == AppLanguage.AR) {
                                    "أكمل تحديًا عمليًا وارفع تقرير الفهم لفتح شارة أول تحدٍ وتوثيق خبرتك في السجل."
                                } else {
                                    "Complete your first practical challenge and submit an Understanding Report to unlock the First Challenge badge and earn real points."
                                },
                                actionLabel = strings.emptyAction,
                                onAction = { onOpenChallenge("ch_smart_city") },
                                modifier = Modifier.testTag("passport_empty_submissions")
                            )
                        } else if (realSubs.isNotEmpty()) {
                            realSubs.forEach { sub ->
                                SubmissionPassportCard(
                                    sub = sub,
                                    showRepos = showRepos,
                                    onToggleVisibility = { onToggleSubmissionVisibility(sub) },
                                    onViewCertificate = { selectedCertificate = sub }
                                )
                            }
                        }
                    }

                    Column(
                        modifier = Modifier.weight(0.85f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (auditLogs.isNotEmpty()) {
                            SectionHeaderRow(title = strings.auditLogTitle)
                            auditLogs.take(5).forEach { log ->
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
                                            Text(log.summary, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                            Text("${log.actionType} • ${log.actorRole}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }
                            }
                        }

                        SectionHeaderRow(title = strings.skillEvidenceGraph)
                        SkillBridgeEcosystemData.evidenceNodes.forEach { node ->
                            val hasRealMatch = realSubs.any { sub ->
                                sub.status == "VERIFIED" && sub.skillsApplied.any { it.contains(node.skill, ignoreCase = true) }
                            }
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, if (hasRealMatch) SkillBridgeColors.VerifiedGreen.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        Icons.Filled.Hub,
                                        contentDescription = null,
                                        tint = if (hasRealMatch) SkillBridgeColors.VerifiedGreen else SkillBridgeColors.PendingAmber
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("${node.skill} (${localizedCareerLevel(node.level, lang)})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                        Text("→ ${node.proofTitle.get(lang)}", style = MaterialTheme.typography.bodyMedium, color = SkillBridgeColors.BridgeBlueDark)
                                        Text("${node.evaluator} • ${node.verificationId}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    StatusPill(
                                        if (hasRealMatch) strings.verifiedBadge else strings.lockedBadgesLabel,
                                        if (hasRealMatch) StatusTone.VERIFIED else StatusTone.LOCKED
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            item { heroVerifiedCard() }

            item {
                LevelAndPlanBadgeRow(
                    careerLevel = careerLevel,
                    planTier = profile?.planTier?.ifBlank { strings.planStarterFree } ?: strings.planStarterFree
                )
            }

            item {
                ContextualAiAgentBanner(
                    mode = AiAgentContextMode.PASSPORT_REVIEWER,
                    onOpenAgent = {
                        onOpenAiAgent(
                            "Level=$careerLevel, RealSubmissions=${realSubs.size}, UnlockedBadges=${unlockedBadges.size}/${badgeProgressList.size}"
                        )
                    }
                )
            }

            item { quickActionsSection() }

            item {
                SectionHeaderRow(
                    title = "${strings.badgesTitle} (${unlockedBadges.size}/${badgeProgressList.size})",
                    actionText = if (lang == AppLanguage.AR) "عرض الكل" else "View All",
                    onAction = onOpenBadges
                )
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    badgeProgressList.take(5).forEach { item ->
                        val visual = AssetRegistry.badgeVisualFor(item.definition.id)
                        Card(
                            modifier = Modifier.width(150.dp).clickable(onClick = onOpenBadges),
                            colors = CardDefaults.cardColors(
                                containerColor = if (item.isUnlocked) SkillBridgeColors.VerifiedBg.copy(alpha = 0.5f)
                                else MaterialTheme.colorScheme.surface
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(visual.icon, contentDescription = null, tint = if (item.isUnlocked) visual.accentLight else SkillBridgeColors.LockedSlate)
                                Text(item.definition.title.get(lang), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(
                                    text = if (item.isUnlocked) strings.unlockedBadgesLabel else "${item.progressPercent}% • ${strings.lockedBadgesLabel}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (item.isUnlocked) SkillBridgeColors.VerifiedGreen else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            if (dbSubmissions.isNotEmpty()) {
                item {
                    SectionHeaderRow(
                        title = if (lang == AppLanguage.AR) "أدلة مشاريع الشركات الموثّقة (${dbSubmissions.size})"
                        else "Company Brief Deliverables & Evidence (${dbSubmissions.size})"
                    )
                }
                items(dbSubmissions, key = { "db_${it.id}" }) { sub ->
                    dbSubmissionCard(sub)
                }
            }

            item {
                SectionHeaderRow(
                    title = if (lang == AppLanguage.AR) "سجل تسليماتك وأدلتك الفعلية" else "Your Verified & Active Submissions"
                )
            }

            if (realSubs.isEmpty() && dbSubmissions.isEmpty()) {
                item {
                    EmptyStateView(
                        title = if (lang == AppLanguage.AR) "لا توجد تسليمات موثّقة بعد" else "0 Verified Submissions Yet",
                        subtitle = if (lang == AppLanguage.AR) {
                            "أكمل تحديًا عمليًا وارفع تقرير الفهم لفتح شارة أول تحدٍ وتوثيق خبرتك في السجل."
                        } else {
                            "Complete your first practical challenge and submit an Understanding Report to unlock the First Challenge badge and earn real points."
                        },
                        actionLabel = strings.emptyAction,
                        onAction = { onOpenChallenge("ch_smart_city") },
                        modifier = Modifier.testTag("passport_empty_submissions")
                    )
                }
            } else if (realSubs.isNotEmpty()) {
                items(realSubs, key = { it.id }) { sub ->
                    SubmissionPassportCard(
                        sub = sub,
                        showRepos = showRepos,
                        onToggleVisibility = { onToggleSubmissionVisibility(sub) },
                        onViewCertificate = { selectedCertificate = sub }
                    )
                }
            }

            if (auditLogs.isNotEmpty()) {
                item {
                    SectionHeaderRow(title = strings.auditLogTitle)
                }
                items(auditLogs.take(5), key = { it.id }) { log ->
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
                                Text(log.summary, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                Text("${log.actionType} • ${log.actorRole}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { showDemoReference = !showDemoReference },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (lang == AppLanguage.AR) "معاينة أمثلة مرجعية للشهادات (غير محسوبة في نقاطك)"
                                else "Inspect Demo Reference Certificates (Not Counted)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            StatusPill(strings.demoCatalogLabel, StatusTone.NEUTRAL)
                        }
                        Icon(
                            imageVector = if (showDemoReference) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                            contentDescription = null
                        )
                    }
                }
            }

            if (showDemoReference) {
                items(SkillBridgeEcosystemData.seedSubmissions, key = { it.id }) { sub ->
                    SubmissionPassportCard(
                        sub = sub,
                        showRepos = showRepos,
                        onToggleVisibility = { onToggleSubmissionVisibility(sub) },
                        onViewCertificate = { selectedCertificate = sub }
                    )
                }
            }

            item {
                SectionHeaderRow(title = strings.skillEvidenceGraph)
            }
            items(SkillBridgeEcosystemData.evidenceNodes) { node ->
                val hasRealMatch = realSubs.any { sub ->
                    sub.status == "VERIFIED" && sub.skillsApplied.any { it.contains(node.skill, ignoreCase = true) }
                }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, if (hasRealMatch) SkillBridgeColors.VerifiedGreen.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            Icons.Filled.Hub,
                            contentDescription = null,
                            tint = if (hasRealMatch) SkillBridgeColors.VerifiedGreen else SkillBridgeColors.PendingAmber
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text("${node.skill} (${localizedCareerLevel(node.level, lang)})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("→ ${node.proofTitle.get(lang)}", style = MaterialTheme.typography.bodyMedium, color = SkillBridgeColors.BridgeBlueDark)
                            Text("${node.evaluator} • ${node.verificationId}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        StatusPill(
                            if (hasRealMatch) strings.verifiedBadge else strings.lockedBadgesLabel,
                            if (hasRealMatch) StatusTone.VERIFIED else StatusTone.LOCKED
                        )
                    }
                }
            }
        }
        item { Spacer(Modifier.height(20.dp)) }
    }

    selectedCertificate?.let { cert ->
        AlertDialog(
            onDismissRequest = { selectedCertificate = null },
            title = { Text(cert.challengeTitle) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (cert.isDemoSample || cert.id.startsWith("seed_")) {
                        StatusPill(strings.demoCatalogLabel, StatusTone.NEUTRAL)
                    }
                    Text(
                        "${if (lang == AppLanguage.AR) "رمز التحقق:" else "Verification Code:"} ${cert.verificationCode}",
                        fontWeight = FontWeight.Bold
                    )
                    Text("${if (lang == AppLanguage.AR) "الحالة:" else "Status:"} ${localizedSubmissionStatus(cert.status, lang)}")
                    if (cert.rejectionReason.isNotBlank()) {
                        Text("${if (lang == AppLanguage.AR) "سبب الرفض:" else "Reason:"} ${cert.rejectionReason}", color = SkillBridgeColors.DisputedRed)
                    }
                    Text("${if (lang == AppLanguage.AR) "الشريك:" else "Partner:"} ${cert.companyName}")
                    Text("${if (lang == AppLanguage.AR) "الدور:" else "Role:"} ${cert.roleInTeam}")
                    Text("${if (lang == AppLanguage.AR) "النتيجة:" else "Score:"} ${cert.score}% • +${cert.pointsAwarded} ${strings.pointsLabel}")
                    Text("${if (lang == AppLanguage.AR) "تقرير الفهم:" else "Understanding Report:"} ${cert.understandingReport}")
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedCertificate = null }) { Text(strings.closeLabel) }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SubmissionPassportCard(
    sub: ChallengeSubmission,
    showRepos: Boolean,
    onToggleVisibility: () -> Unit,
    onViewCertificate: () -> Unit
) {
    val strings = LocalAppStrings.current
    val lang = LocalAppLanguage.current
    val statusTone = when (sub.status) {
        "VERIFIED" -> StatusTone.VERIFIED
        "REJECTED", "DISPUTED" -> StatusTone.DISPUTED
        "PENDING" -> StatusTone.PENDING
        else -> StatusTone.REVIEW
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(sub.challengeTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("${sub.companyName} • ${sub.roleInTeam}", style = MaterialTheme.typography.bodyMedium, color = SkillBridgeColors.BridgeBlueDark)
                }
                IconButton(onClick = onToggleVisibility) {
                    Icon(
                        if (sub.isPublicOnPassport) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = "Toggle passport visibility"
                    )
                }
            }

            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                if (sub.isDemoSample || sub.id.startsWith("seed_")) {
                    StatusPill(strings.demoCatalogLabel, StatusTone.NEUTRAL)
                }
                sub.skillsApplied.forEach { sk ->
                    Surface(shape = RoundedCornerShape(SkillBridgeRadius.pill), color = SkillBridgeColors.SoftCyanTint) {
                        Text(sk, style = MaterialTheme.typography.labelSmall, color = SkillBridgeColors.DeepNavy, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                StatusPill(localizedSubmissionStatus(sub.status, lang), statusTone)
                StatusPill("ID: ${sub.verificationCode}", StatusTone.NEUTRAL)
            }

            if (sub.status == "REJECTED" && sub.rejectionReason.isNotBlank()) {
                Text(
                    text = "${if (lang == AppLanguage.AR) "سبب الرفض:" else "Rejection Reason:"} ${sub.rejectionReason}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SkillBridgeColors.DisputedRed
                )
            }

            if (sub.mentorFeedback.isNotBlank()) {
                Text(strings.mentorFeedback, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Text("\"${sub.mentorFeedback}\"", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (sub.status == "VERIFIED") {
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        repeat(5) {
                            Icon(Icons.Filled.Star, contentDescription = null, tint = SkillBridgeColors.PendingAmber, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            if (showRepos && sub.repoOrDemoUrl.isNotBlank()) {
                Text(sub.repoOrDemoUrl, style = MaterialTheme.typography.labelSmall, color = SkillBridgeColors.BridgeBlueDark)
            }

            OutlinedButton(
                onClick = onViewCertificate,
                shape = RoundedCornerShape(SkillBridgeRadius.pill),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text(if (lang == AppLanguage.AR) "عرض الشهادة وسجل التحقق" else "View Certificate & Proof")
            }
        }
    }
}

/**
 * Screen 15: Points & Rewards Transactions Ledger (real Firestore pointLedger + audit log).
 */
@Composable
fun PointsWalletScreen(
    pointsBalance: Long,
    pointLedgerState: UiState<List<PointLedgerEntry>>,
    auditLogsState: UiState<List<AuditLogEntry>>,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)
    val strings = LocalAppStrings.current
    val lang = LocalAppLanguage.current
    val entries = (pointLedgerState as? UiState.Success)?.data.orEmpty()
    val auditLogs = (auditLogsState as? UiState.Success)?.data.orEmpty()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(SkillBridgeSpacing.screenHorizontal),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(strings.walletTitle, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SkillBridgeColors.DeepNavy)
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(strings.pointsLabel, style = MaterialTheme.typography.labelLarge, color = SkillBridgeColors.SoftCyan)
                    Text("$pointsBalance ${strings.pointsLabel}", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = SkillBridgeColors.PureWhite)
                    Text(
                        if (lang == AppLanguage.AR) "رصيد النقاط الحقيقي المحسوب من السجل الموثّق (${entries.size} حركة)"
                        else "Verified ledger balance computed from ${entries.size} authenticated entries",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SkillBridgeColors.LimeGreen
                    )
                }
            }
        }
        item {
            InfoNoteBanner(text = strings.prizePoolDisclaimer)
        }

        if (entries.isEmpty()) {
            item {
                EmptyStateView(
                    title = if (lang == AppLanguage.AR) "سجل النقاط فارغ حاليًا" else "No Points Transactions Yet",
                    subtitle = if (lang == AppLanguage.AR) {
                        "ابدأ بتسليم تحدٍ أو فتح شارة ليتم تسجيل كل نقطة برقم عملية فريد غير قابل للتكرار."
                    } else {
                        "Submit and verify a challenge or unlock a badge to record idempotent transactions here."
                    }
                )
            }
        } else {
            items(entries, key = { it.txId.ifBlank { it.sourceId } }) { tx ->
                val tone = when (tx.status) {
                    "PAID" -> StatusTone.PAID
                    "APPROVED" -> StatusTone.VERIFIED
                    "UNDER_REVIEW" -> StatusTone.REVIEW
                    "DISPUTED", "REJECTED" -> StatusTone.DISPUTED
                    else -> StatusTone.PENDING
                }
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
                        Column(modifier = Modifier.weight(1f)) {
                            Text(tx.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Text(
                                "${tx.sourceType} • +${tx.points} ${strings.pointsLabel}${if (tx.cashAmount.isNotBlank()) " • ${tx.cashAmount}" else ""}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (tx.reason.isNotBlank()) {
                                Text(tx.reason, style = MaterialTheme.typography.labelSmall, color = SkillBridgeColors.BridgeBlueDark)
                            }
                        }
                        StatusPill(localizedSubmissionStatus(tx.status, lang), tone)
                    }
                }
            }
        }

        if (auditLogs.isNotEmpty()) {
            item { SectionHeaderRow(title = strings.auditLogTitle) }
            items(auditLogs.take(8), key = { it.id }) { log ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(log.summary, style = MaterialTheme.typography.bodyMedium)
                        Text("${log.actionType} • ${log.actorRole}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        item { Spacer(Modifier.height(16.dp)) }
    }
}
