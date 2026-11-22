package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.core.AppLanguage
import com.example.core.LocalAppLanguage
import com.example.core.LocalAppStrings
import com.example.core.localizedCareerLevel
import com.example.core.localizedChannelName
import com.example.core.localizedDomain
import com.example.core.localizedNotificationCategory
import com.example.core.localizedSubmissionStatus
import com.example.data.model.CareerGoal
import com.example.data.model.ChallengeSubmission
import com.example.data.model.CoachMessage
import com.example.data.model.SkillGap
import com.example.data.model.UserProfile
import com.example.data.model.VerificationTier
import com.example.data.seed.SkillBridgeEcosystemData
import com.example.ui.components.EmptyStateView
import com.example.ui.components.ErrorStateView
import com.example.ui.components.InfoNoteBanner
import com.example.ui.components.LoadingStateView
import com.example.ui.components.LockedStateView
import com.example.ui.components.SectionHeaderRow
import com.example.ui.components.StatusPill
import com.example.ui.components.StatusTone
import com.example.ui.theme.SkillBridgeColors
import com.example.ui.theme.SkillBridgeSpacing
import com.example.ui.viewmodel.CoachStreamState
import com.example.ui.viewmodel.PreviewStateMode
import com.example.ui.viewmodel.UiState

/**
 * Screen 16: Global Search across Challenges, Contests, Teams, Companies, People, and Community.
 */
@Composable
fun GlobalSearchScreen(
    onOpenChallenge: (String) -> Unit,
    onOpenContests: () -> Unit,
    onOpenCompanies: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)
    val strings = LocalAppStrings.current
    val lang = LocalAppLanguage.current
    var query by remember { mutableStateOf("") }

    val matchingChallenges = SkillBridgeEcosystemData.challenges.filter {
        query.isBlank() ||
            it.title.get(lang).contains(query, ignoreCase = true) ||
            it.companyName.contains(query, ignoreCase = true) ||
            it.skills.any { s -> s.contains(query, ignoreCase = true) }
    }
    val matchingPeople = SkillBridgeEcosystemData.leaderboardEntries.filter {
        query.isBlank() || it.name.contains(query, ignoreCase = true) || it.domain.contains(query, ignoreCase = true)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(SkillBridgeSpacing.screenHorizontal),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                placeholder = { Text(strings.searchPlaceholder) },
                modifier = Modifier.fillMaxWidth().testTag("global_search_input")
            )
        }

        item { SectionHeaderRow(title = strings.filterChallenges) }
        items(matchingChallenges) { ch ->
            Card(
                modifier = Modifier.fillMaxWidth().clickable { onOpenChallenge(ch.id) },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(ch.title.get(lang), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("${ch.companyName} • ${localizedDomain(ch.domain, lang)}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        item { SectionHeaderRow(title = strings.top100Title) }
        items(matchingPeople.take(5)) { p ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("${p.name} (${localizedCareerLevel(p.level, lang)})", fontWeight = FontWeight.SemiBold)
                    Text(
                        "${p.rating} ${if (lang == AppLanguage.AR) "تقييم" else "Rating"}",
                        color = SkillBridgeColors.BridgeBlueDark
                    )
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = onOpenContests, modifier = Modifier.weight(1f).height(48.dp)) { Text(strings.contestsTitle) }
                OutlinedButton(onClick = onOpenCompanies, modifier = Modifier.weight(1f).height(48.dp)) { Text(strings.companyDashTitle) }
            }
        }
        item { Spacer(Modifier.height(16.dp)) }
    }
}

/**
 * Screen 17: Notifications + Channel Preferences.
 */
@Composable
fun NotificationsScreen(
    unreadIds: Set<String>,
    channels: Map<String, Boolean>,
    onMarkAllRead: () -> Unit,
    onToggleChannel: (String) -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)
    val strings = LocalAppStrings.current
    val lang = LocalAppLanguage.current
    var selectedCat by remember { mutableStateOf("All") }

    val categories = listOf("All", "Evaluation", "Contests", "Teams", "Rewards")
    val list = SkillBridgeEcosystemData.notifications.filter { selectedCat == "All" || it.category == selectedCat }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(SkillBridgeSpacing.screenHorizontal),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(strings.notificationsTitle, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                OutlinedButton(onClick = onMarkAllRead, modifier = Modifier.height(48.dp)) {
                    Text(if (lang == AppLanguage.AR) "تحديد كمقروء" else "Mark Read")
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                categories.forEach { c ->
                    FilterChip(
                        selected = selectedCat == c,
                        onClick = { selectedCat = c },
                        label = { Text(localizedNotificationCategory(c, lang)) }
                    )
                }
            }
        }

        items(list) { notif ->
            val isUnread = notif.id in unreadIds
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isUnread) SkillBridgeColors.SoftCyanTint.copy(alpha = 0.45f)
                    else MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        StatusPill(localizedNotificationCategory(notif.category, lang), if (isUnread) StatusTone.REVIEW else StatusTone.NEUTRAL)
                        Text(notif.timeAgo, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(notif.title.get(lang), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(notif.body.get(lang), style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(if (lang == AppLanguage.AR) "تفضيلات قنوات التنبيه" else "Notification Channels", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    channels.forEach { (channel, enabled) ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(localizedChannelName(channel, lang), style = MaterialTheme.typography.bodyMedium)
                            Switch(checked = enabled, onCheckedChange = { onToggleChannel(channel) })
                        }
                    }
                }
            }
        }
        item { Spacer(Modifier.height(16.dp)) }
    }
}

/**
 * Screen 18: Company Partner Directory, Tiered Verification, Transparency & Challenge Publishing.
 */
@Composable
fun CompanyDashboardScreen(
    onReportCompany: (String, String) -> Unit,
    onShowToast: (String) -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)
    val strings = LocalAppStrings.current
    val lang = LocalAppLanguage.current
    var newChallengeTitle by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(SkillBridgeSpacing.screenHorizontal),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(strings.companyDashTitle, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            InfoNoteBanner(
                text = if (lang == AppLanguage.AR) {
                    "تخضع الشركات لمستويات توثيق متدرجة (غير موثقة / أساسية / تجارية / شريك موثوق) مع عرض سبب التوثيق ونطاقه وتاريخه."
                } else {
                    "Tiered Company Verification ensures transparent trust: Basic Verified companies may run unpaid practice labs; Business Verified & Trusted Partners run escrowed paid challenges."
                }
            )
        }

        items(SkillBridgeEcosystemData.companies) { comp ->
            val tone = when (comp.tier) {
                VerificationTier.TRUSTED -> StatusTone.VERIFIED
                VerificationTier.BUSINESS -> StatusTone.REVIEW
                VerificationTier.BASIC -> StatusTone.PENDING
                VerificationTier.UNVERIFIED -> StatusTone.DISPUTED
            }
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(comp.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("${comp.city} • ${localizedDomain(comp.domain, lang)}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        StatusPill(if (lang == AppLanguage.AR) comp.tier.badgeAr else comp.tier.badgeEn, tone, Icons.Filled.Verified)
                    }
                    Text(comp.verificationReason.get(lang), style = MaterialTheme.typography.bodyMedium)
                    Text(
                        if (lang == AppLanguage.AR) {
                            "التوثيق: ${comp.verifiedDate} • ${comp.publishedChallenges} تحديات • ${comp.completionRate}% إتمام • ★ ${comp.avgMentorRating}"
                        } else {
                            "Verified: ${comp.verifiedDate} • ${comp.publishedChallenges} Challenges • ${comp.completionRate}% Completion • ★ ${comp.avgMentorRating}"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = SkillBridgeColors.BridgeBlueDark
                    )
                    Text("${if (lang == AppLanguage.AR) "المكافآت:" else "Rewards:"} ${comp.rewardPolicy.get(lang)}", style = MaterialTheme.typography.bodyMedium)
                    Text("${if (lang == AppLanguage.AR) "الملكية الفكرية:" else "IP:"} ${comp.ipPolicy.get(lang)}", style = MaterialTheme.typography.bodyMedium)
                    OutlinedButton(
                        onClick = { onReportCompany(comp.id, "Verification audit check requested") },
                        modifier = Modifier.height(48.dp)
                    ) {
                        Icon(Icons.Filled.Flag, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(strings.reportContent)
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, SkillBridgeColors.BridgeBlue.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(if (lang == AppLanguage.AR) "نشر تحدٍ عملي جديد (بوابة الشركات)" else "Publish Company Challenge Draft", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = newChallengeTitle,
                        onValueChange = { newChallengeTitle = it },
                        label = { Text(if (lang == AppLanguage.AR) "عنوان التحدي ومعايير القبول" else "Challenge Title & Rubric Summary") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = {
                            onShowToast(
                                if (lang == AppLanguage.AR) "تم إرسال مسودة التحدي لمراجعة التوثيق ومعايير التقييم"
                                else "Challenge draft queued for Company Verification & Rubric audit"
                            )
                            newChallengeTitle = ""
                        },
                        enabled = newChallengeTitle.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = SkillBridgeColors.DeepNavy),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text(if (lang == AppLanguage.AR) "إرسال للمراجعة والنشر" else "Submit for Escrow & Rubric Review")
                    }
                }
            }
        }
        item { Spacer(Modifier.height(16.dp)) }
    }
}

/**
 * Screen 19 & 20: AI Coach + Mock Interview + Mentor Review Studio (with REJECTED + Reason) + Director/Moderator Queue.
 */
@Composable
fun MentorCoachAndAdminScreen(
    initialTab: Int,
    goalsState: UiState<List<CareerGoal>>,
    gapsState: UiState<List<SkillGap>>,
    messagesState: UiState<List<CoachMessage>>,
    coachStreamState: CoachStreamState,
    submissionsState: UiState<List<ChallengeSubmission>>,
    onSendCoachPrompt: (String, String, CareerGoal?, List<SkillGap>) -> Unit,
    onClearCoachHistory: (String) -> Unit,
    onEvaluateSubmission: (ChallengeSubmission, Int, String, Boolean, String) -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)
    val strings = LocalAppStrings.current
    val lang = LocalAppLanguage.current
    var selectedTab by remember { mutableIntStateOf(initialTab) }
    var rejectTarget by remember { mutableStateOf<Pair<ChallengeSubmission, String>?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text(if (lang == AppLanguage.AR) "المدرب الذكي AI" else "AI Coach") }
            )
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text(strings.roleMentor) })
            Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text(strings.roleDirector) })
        }

        when (selectedTab) {
            0 -> {
                val goals = (goalsState as? UiState.Success)?.data.orEmpty()
                val activeGoal = goals.firstOrNull { it.isActive } ?: goals.firstOrNull()
                val gaps = (gapsState as? UiState.Success)?.data.orEmpty()
                val messages = (messagesState as? UiState.Success)?.data.orEmpty()
                var mode by remember { mutableStateOf("COACH") }
                var prompt by remember { mutableStateOf("") }

                Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
                                    text = if (lang == AppLanguage.AR) "اسأل المدرب الذكي عن خطة المهارات أو اطلب سؤال مقابلة تقنية." else "Ask SkillBridge AI Coach about your skill gaps or launch a live technical mock interview."
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
            1 -> {
                val realSubs = ((submissionsState as? UiState.Success)?.data.orEmpty()).filter { !it.isDemoSample && !it.id.startsWith("seed_") }
                val listToReview = if (realSubs.isNotEmpty()) realSubs else SkillBridgeEcosystemData.seedSubmissions
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    item {
                        Text(strings.mentorDashTitle, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        if (realSubs.isEmpty()) {
                            InfoNoteBanner(
                                text = if (lang == AppLanguage.AR) {
                                    "لا توجد تسليمات مستخدمين للمراجعة بعد. تظهر أدناه نماذج مرجعية للقراءة فقط."
                                } else {
                                    "No live user submissions pending review yet. Showing read-only Demo Reference cards below."
                                }
                            )
                        }
                    }
                    items(listToReview, key = { it.id }) { sub ->
                        var feedback by remember(sub.id) {
                            mutableStateOf(
                                sub.mentorFeedback.ifBlank {
                                    if (lang == AppLanguage.AR) "تنفيذ واضح واختبارات تحقق متكاملة."
                                    else "Clean implementation and solid test verification."
                                }
                            )
                        }
                        val statusTone = when (sub.status) {
                            "VERIFIED" -> StatusTone.VERIFIED
                            "REJECTED", "DISPUTED" -> StatusTone.DISPUTED
                            else -> StatusTone.REVIEW
                        }
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text(sub.challengeTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                    StatusPill(localizedSubmissionStatus(sub.status, lang), statusTone)
                                }
                                if (sub.isDemoSample || sub.id.startsWith("seed_")) {
                                    StatusPill(strings.demoCatalogLabel, StatusTone.NEUTRAL)
                                }
                                Text(
                                    "${if (lang == AppLanguage.AR) "تقرير الفهم:" else "Understanding Report:"} ${sub.understandingReport}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                if (sub.rejectionReason.isNotBlank()) {
                                    Text(
                                        "${if (lang == AppLanguage.AR) "سبب الرفض:" else "Rejection Reason:"} ${sub.rejectionReason}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = SkillBridgeColors.DisputedRed
                                    )
                                }
                                OutlinedTextField(
                                    value = feedback,
                                    onValueChange = { feedback = it },
                                    label = { Text(strings.mentorFeedback) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                    Button(
                                        onClick = { onEvaluateSubmission(sub, 92, feedback, true, "") },
                                        colors = ButtonDefaults.buttonColors(containerColor = SkillBridgeColors.VerifiedGreen),
                                        modifier = Modifier.weight(1f).height(48.dp)
                                    ) {
                                        Text(if (lang == AppLanguage.AR) "توثيق (92%)" else "Verify (92%)")
                                    }
                                    OutlinedButton(
                                        onClick = { rejectTarget = sub to feedback },
                                        modifier = Modifier.weight(1f).height(48.dp)
                                    ) {
                                        Text(if (lang == AppLanguage.AR) "رفض / تعديل" else "Reject / Revise")
                                    }
                                }
                            }
                        }
                    }
                }
            }
            else -> {
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    item {
                        Text(strings.directorAdminTitle, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        InfoNoteBanner(
                            text = if (lang == AppLanguage.AR) "لوحة القادة والمشرفين: مراجعة البلاغات، اعتماد جوائز المسابقات، ومراقبة التزام الهندسة العكسية." else "Director & Moderator Console: audit reports, release contest prize escrows, and verify Reverse Engineering compliance."
                        )
                    }
                    item {
                        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                StatusPill(if (lang == AppLanguage.AR) "طابور الإشراف • نشط" else "MODERATION QUEUE • ACTIVE", StatusTone.PENDING)
                                Text(
                                    if (lang == AppLanguage.AR) "حاجز فحص الأسرار والتسريبات مفعّل" else "Secret Scanner Guardrail Active",
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    if (lang == AppLanguage.AR) {
                                        "جميع تسليمات الهندسة العكسية والمسابقات وردود المساعد الذكي تخضع لفحص التسريبات ومراجعة المرشدين."
                                    } else {
                                        "All Reverse Engineering & Contest submissions and flagged AI responses undergo automated credential detection + mentor understanding review."
                                    },
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    rejectTarget?.let { (sub, feedback) ->
        var reason by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { rejectTarget = null },
            title = { Text(if (lang == AppLanguage.AR) "سبب الرفض أو طلب التعديل" else "Rejection / Revision Reason") },
            text = {
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text(if (lang == AppLanguage.AR) "اكتب السبب بوضوح" else "Explicit rejection reason") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onEvaluateSubmission(sub, 45, feedback, false, reason.ifBlank { "Needs stronger verification tests" })
                        rejectTarget = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SkillBridgeColors.DisputedRed)
                ) { Text(strings.confirmLabel) }
            },
            dismissButton = {
                TextButton(onClick = { rejectTarget = null }) { Text(strings.cancelLabel) }
            }
        )
    }
}

/**
 * Screen 21 & 22: Privacy Controls + AI Preferences + Interactive UI State Previewer + Product Blueprint.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PrivacyStatesAndBlueprintScreen(
    profile: UserProfile?,
    darkTheme: Boolean,
    aiPersonalizationEnabled: Boolean,
    aiContextSharingEnabled: Boolean,
    previewStateMode: PreviewStateMode,
    onToggleDarkTheme: (Boolean) -> Unit,
    onToggleAiPrefs: (Boolean, Boolean) -> Unit,
    onUpdatePrivacy: (Boolean, Boolean) -> Unit,
    onSelectPreviewMode: (PreviewStateMode) -> Unit,
    onOpenAccountSettings: () -> Unit,
    onSignOut: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)
    val strings = LocalAppStrings.current
    val lang = LocalAppLanguage.current

    var passportPublic by remember(profile?.passportPublic) { mutableStateOf(profile?.passportPublic ?: true) }
    var showRepoLinks by remember(profile?.showRepoLinks) { mutableStateOf(profile?.showRepoLinks ?: true) }
    var showSignOutConfirm by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(SkillBridgeSpacing.screenHorizontal),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(strings.privacySettingsTitle, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        }

        // Granular Privacy & AI Controls
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(if (lang == AppLanguage.AR) "السجل المهني متاح بالمشاركة" else "Public Experience Passport Link", fontWeight = FontWeight.SemiBold)
                            Text(if (lang == AppLanguage.AR) "إظهار الأدلة الموثقة عند مشاركة الرابط" else "Allow companies to verify code IDs", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(checked = passportPublic, onCheckedChange = {
                            passportPublic = it
                            onUpdatePrivacy(it, showRepoLinks)
                        })
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(if (lang == AppLanguage.AR) "إظهار روابط المستودعات (Repo)" else "Show Git Repository Links", fontWeight = FontWeight.SemiBold)
                            Text(if (lang == AppLanguage.AR) "يمكنك إخفاؤها للسرية" else "Hide code links while keeping score & mentor feedback visible", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(checked = showRepoLinks, onCheckedChange = {
                            showRepoLinks = it
                            onUpdatePrivacy(passportPublic, it)
                        })
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(strings.aiAgentPersonalizationToggle, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                        Switch(checked = aiPersonalizationEnabled, onCheckedChange = { onToggleAiPrefs(it, aiContextSharingEnabled) })
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(strings.aiAgentContextToggle, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                        Switch(checked = aiContextSharingEnabled, onCheckedChange = { onToggleAiPrefs(aiPersonalizationEnabled, it) })
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(if (lang == AppLanguage.AR) "الوضع الداكن (Deep Navy)" else "Deep Navy Dark Theme", fontWeight = FontWeight.SemiBold)
                        Switch(checked = darkTheme, onCheckedChange = onToggleDarkTheme)
                    }
                    OutlinedButton(
                        onClick = onOpenAccountSettings,
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Icon(Icons.Filled.ManageAccounts, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(strings.accountSettingsTitle)
                    }
                    OutlinedButton(
                        onClick = { showSignOutConfirm = true },
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("sign_out_btn")
                    ) {
                        Text(strings.signOut, color = SkillBridgeColors.DisputedRed)
                    }
                }
            }
        }

        // Interactive UI State Previewer
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(strings.statePreviewTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        PreviewStateMode.entries.forEach { mode ->
                            FilterChip(
                                selected = previewStateMode == mode,
                                onClick = { onSelectPreviewMode(mode) },
                                label = { Text(mode.name) }
                            )
                        }
                    }
                    when (previewStateMode) {
                        PreviewStateMode.LOADING -> LoadingStateView()
                        PreviewStateMode.EMPTY -> EmptyStateView(
                            subtitle = if (lang == AppLanguage.AR) "معاينة حالة القائمة الفارغة" else "Sample empty state preview",
                            onAction = { onSelectPreviewMode(PreviewStateMode.NORMAL) }
                        )
                        PreviewStateMode.ERROR -> ErrorStateView(
                            message = if (lang == AppLanguage.AR) "معاينة حالة خطأ شبكة قابل للاستعادة" else "Sample recoverable network error",
                            onRetry = { onSelectPreviewMode(PreviewStateMode.NORMAL) }
                        )
                        PreviewStateMode.LOCKED -> LockedStateView(
                            reason = if (lang == AppLanguage.AR) "يتطلب إكمال التحدي الموثّق الأول" else "Requires verified Challenge #1",
                            actionLabel = if (lang == AppLanguage.AR) "عودة" else "Unlock",
                            onAction = { onSelectPreviewMode(PreviewStateMode.NORMAL) }
                        )
                        PreviewStateMode.NORMAL -> StatusPill(
                            if (lang == AppLanguage.AR) "الحالة الحية النشطة" else "Active Live State",
                            StatusTone.VERIFIED,
                            Icons.Filled.CheckCircle
                        )
                    }
                }
            }
        }

        // Product Architecture, Flows & MVP Phase 1/2/3 Blueprint
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SkillBridgeColors.DeepNavy)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(strings.blueprintTitle, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = SkillBridgeColors.PureWhite)
                    Text(
                        if (lang == AppLanguage.AR) {
                            "• الحلقة الأساسية: فجوة المهارات ← التعلم ← التطبيق العملي ← التقييم ← دليل موثّق ← الفرصة\n" +
                                "• التنقل الرئيسي (5 تبويبات): الرئيسية، استكشف، المجتمع، الفرق، السجل (+ البحث، الإشعارات، المساعد الذكي، عربي/EN)\n" +
                                "• المرحلة 1: الهوية + الإعداد + تحليل المهارات + التحديات + الفرق + سجل الخبرة والشارات\n" +
                                "• المرحلة 2: المسابقات الحية + التصنيف + مركز الهندسة العكسية الأخلاقية + استوديو المرشد\n" +
                                "• المرحلة 3: توثيق الشركات المتدرج + سجل النقاط الثابت + خريطة الأدلة"
                        } else {
                            "• Core Loop: Skill Gap → Learning → Practical Experience → Evaluation → Verified Evidence → Opportunity\n" +
                                "• 5-Tab Navigation: Home, Discover, Community, Teams, Passport (+ Search, Notifications, Contextual AI Agents, AR/EN RTL/LTR)\n" +
                                "• MVP Phase 1: Auth + Onboarding + Skill Gap + Challenges + Teams + Experience Passport & 9 Badges\n" +
                                "• MVP Phase 2: Live Contests + Top-100 Leaderboards + Ethical Reverse Eng Hub + Mentor Studio\n" +
                                "• MVP Phase 3: Tiered Company Verification + Immutable Point Ledger + Skill Evidence Graph"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = SkillBridgeColors.InkOnDark
                    )
                    Text(
                        if (lang == AppLanguage.AR) {
                            "ضوابط الشفافية:\n" +
                                "١. الحساب الجديد يبدأ بـ 0 نقطة و0 شارات دون أي إنجازات وهمية.\n" +
                                "٢. التقييمات والمكافآت ترتبط بسجل Ledger حتمي غير قابل للتكرار.\n" +
                                "٣. مختبرات الهندسة العكسية محصورة في بيئات آمنة مع فحص الأسرار ومنع تسريب المفاتيح."
                        } else {
                            "Transparency Guardrails:\n" +
                                "1. Brand-new accounts strictly start at 0 points, 0 streak, Junior baseline, and 0 badges.\n" +
                                "2. Submissions and badges write idempotently to an immutable Firestore pointLedger and auditLogs trail.\n" +
                                "3. Reverse Engineering labs restrict artifacts to authorized sandbox specs with secret-leak scanning."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = SkillBridgeColors.SoftCyan
                    )
                }
            }
        }
        item { Spacer(Modifier.height(20.dp)) }
    }

    if (showSignOutConfirm) {
        AlertDialog(
            onDismissRequest = { showSignOutConfirm = false },
            title = { Text(strings.signOut) },
            text = {
                Text(
                    if (lang == AppLanguage.AR) "هل تريد تسجيل الخروج؟ ستبقى تفضيلات اللغة والمظهر محفوظة."
                    else "Sign out of SkillBridge? Your language and theme preferences remain preserved."
                )
            },
            confirmButton = {
                Button(onClick = {
                    showSignOutConfirm = false
                    onSignOut()
                }) { Text(strings.confirmLabel) }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutConfirm = false }) { Text(strings.cancelLabel) }
            }
        )
    }
}
