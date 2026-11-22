package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.unit.dp
import com.example.core.AppLanguage
import com.example.core.LocalAppLanguage
import com.example.core.LocalAppStrings
import com.example.data.model.AiAgentContextMode
import com.example.data.model.AiAgentPendingAction
import com.example.ui.theme.SkillBridgeColors
import com.example.ui.theme.SkillBridgeRadius
import com.example.ui.viewmodel.AiAgentUiState

@Composable
fun OfflineStatusBanner(
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    Surface(
        modifier = modifier.fillMaxWidth().testTag("offline_status_banner"),
        color = SkillBridgeColors.PendingBg,
        border = BorderStroke(1.dp, SkillBridgeColors.PendingAmber.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Info,
                contentDescription = null,
                tint = SkillBridgeColors.PendingAmber,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = strings.offlineBannerText,
                style = MaterialTheme.typography.labelMedium,
                color = SkillBridgeColors.DeepNavy
            )
        }
    }
}

@Composable
fun GuestPreviewBanner(
    onSignInClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    Surface(
        modifier = modifier.fillMaxWidth().testTag("guest_preview_banner"),
        color = SkillBridgeColors.SoftCyanTint,
        border = BorderStroke(1.dp, SkillBridgeColors.BridgeBlue.copy(alpha = 0.45f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = strings.guestBannerTitle,
                style = MaterialTheme.typography.labelMedium,
                color = SkillBridgeColors.DeepNavy,
                modifier = Modifier.weight(1f)
            )
            TextButton(
                onClick = onSignInClick,
                modifier = Modifier.testTag("guest_banner_signin_btn")
            ) {
                Text(strings.signInGoogle, fontWeight = FontWeight.Bold, color = SkillBridgeColors.BridgeBlueDark)
            }
        }
    }
}

@Composable
fun ContextualAiAgentBanner(
    mode: AiAgentContextMode,
    onOpenAgent: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lang = LocalAppLanguage.current
    val strings = LocalAppStrings.current
    Card(
        modifier = modifier.fillMaxWidth().testTag("contextual_ai_agent_banner"),
        colors = CardDefaults.cardColors(containerColor = SkillBridgeColors.DeepNavy)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.AutoAwesome,
                contentDescription = null,
                tint = SkillBridgeColors.LimeGreen,
                modifier = Modifier.size(26.dp)
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "${strings.aiAgentTitle} • ${mode.title.get(lang)}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = SkillBridgeColors.PureWhite
                )
                Text(
                    text = mode.purpose.get(lang),
                    style = MaterialTheme.typography.labelSmall,
                    color = SkillBridgeColors.SoftCyan
                )
            }
            Button(
                onClick = onOpenAgent,
                colors = ButtonDefaults.buttonColors(
                    containerColor = SkillBridgeColors.BridgeBlue,
                    contentColor = SkillBridgeColors.DeepNavy
                ),
                shape = RoundedCornerShape(SkillBridgeRadius.pill),
                modifier = Modifier.height(44.dp).testTag("open_ai_agent_btn")
            ) {
                Text(if (lang == AppLanguage.AR) "اسأل AI" else "Ask AI", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SkillBridgeAiAgentSheet(
    state: AiAgentUiState,
    allowPersonalization: Boolean,
    allowContextSharing: Boolean,
    isOffline: Boolean,
    onSendPrompt: (String) -> Unit,
    onRetry: () -> Unit,
    onRateMessage: (String, Boolean) -> Unit,
    onReportMessage: (String, String) -> Unit,
    onRequestActionConfirm: (AiAgentPendingAction) -> Unit,
    onConfirmAction: () -> Unit,
    onDismissAction: () -> Unit,
    onDismiss: () -> Unit
) {
    if (!state.isOpen) return
    val lang = LocalAppLanguage.current
    val strings = LocalAppStrings.current
    var prompt by remember { mutableStateOf("") }
    var reportTargetId by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = SkillBridgeColors.BridgeBlueDark)
                    Column {
                        Text(
                            text = state.mode.title.get(lang),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = state.mode.purpose.get(lang),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                StatusPill(
                    text = if (isOffline) {
                        if (lang == AppLanguage.AR) "غير متصل" else "Offline"
                    } else if (allowPersonalization && allowContextSharing) {
                        if (lang == AppLanguage.AR) "مربوط بالسياق" else "Grounded"
                    } else {
                        if (lang == AppLanguage.AR) "عام" else "General"
                    },
                    tone = if (isOffline) StatusTone.DISPUTED else StatusTone.VERIFIED
                )
            }

            // Transparent Boundaries & Data Used Disclosure
            Surface(
                shape = RoundedCornerShape(SkillBridgeRadius.sm),
                color = SkillBridgeColors.SoftCyanTint.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "${strings.aiAgentDataUsedPrefix} " + if (allowPersonalization && allowContextSharing) {
                            if (lang == AppLanguage.AR) "ملفك وسياق شاشة ${state.screenLabel}" else "Profile + ${state.screenLabel} context"
                        } else {
                            if (lang == AppLanguage.AR) "إرشادات عامة فقط (مشاركة السياق متوقفة)" else "General guidelines only (context sharing disabled)"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = SkillBridgeColors.DeepNavy
                    )
                    Text(
                        text = "${strings.aiAgentBoundariesPrefix} ${state.mode.boundaries.get(lang)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = SkillBridgeColors.InkSecondary
                    )
                }
            }

            // Quick suggested prompts per context mode
            val quickChips = remember(state.mode, lang) {
                when (state.mode) {
                    AiAgentContextMode.CHALLENGE_MENTOR -> if (lang == AppLanguage.AR) {
                        listOf("ما أهم الحالات الحدية؟", "كيف أراجع معايير التقييم؟")
                    } else {
                        listOf("Key edge cases to test?", "Review rubric checklist")
                    }
                    AiAgentContextMode.TEAM_SPRINT_PLANNER -> if (lang == AppLanguage.AR) {
                        listOf("قسّم مهام السباق للفريق", "ما المخاطر التقنية الآن؟")
                    } else {
                        listOf("Break down sprint tasks", "Top sprint risks?")
                    }
                    AiAgentContextMode.REVERSE_ENG_GUIDE -> if (lang == AppLanguage.AR) {
                        listOf("اشرح أثر السجل الآمن", "بنية تقرير الفهم الإلزامي")
                    } else {
                        listOf("Explain sandbox trace", "Understanding Report outline")
                    }
                    AiAgentContextMode.COMMUNITY_COACH -> if (lang == AppLanguage.AR) {
                        listOf("حسّن صياغة سؤالي التقني", "تأكد من عدم وجود أسرار")
                    } else {
                        listOf("Sharpen my technical question", "Check for leaked secrets")
                    }
                    AiAgentContextMode.PASSPORT_REVIEWER -> if (lang == AppLanguage.AR) {
                        listOf("ما أقرب شارة لي الآن؟", "كيف أرفع مستواي لـ Mid-level؟")
                    } else {
                        listOf("Closest next badge?", "How do I reach Mid-level?")
                    }
                    AiAgentContextMode.SKILL_ADVISOR -> if (lang == AppLanguage.AR) {
                        listOf("اقترح تحديًا لسد الفجوة", "ما المهارة الأعلى أولوية؟")
                    } else {
                        listOf("Best challenge for my gap?", "Top priority skill?")
                    }
                }
            }

            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                quickChips.forEach { chipText ->
                    FilterChip(
                        selected = false,
                        onClick = { onSendPrompt(chipText) },
                        label = { Text(chipText, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 140.dp, max = 280.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.messages, key = { it.id }) { msg ->
                    val isUser = msg.sender == "USER"
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isUser) SkillBridgeColors.DeepNavy else MaterialTheme.colorScheme.surface
                        ),
                        border = if (isUser) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = msg.content,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isUser) SkillBridgeColors.PureWhite else MaterialTheme.colorScheme.onSurface
                            )
                            if (!isUser) {
                                msg.pendingAction?.let { pending ->
                                    OutlinedButton(
                                        onClick = { onRequestActionConfirm(pending) },
                                        modifier = Modifier.fillMaxWidth().testTag("ai_proposed_action_btn")
                                    ) {
                                        Text(pending.description.get(lang), fontWeight = FontWeight.SemiBold)
                                    }
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        IconButton(onClick = { onRateMessage(msg.id, true) }, modifier = Modifier.size(36.dp)) {
                                            Icon(
                                                Icons.Filled.ThumbUp,
                                                contentDescription = "Helpful",
                                                tint = if (msg.feedbackPositive == true) SkillBridgeColors.VerifiedGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        IconButton(onClick = { onRateMessage(msg.id, false) }, modifier = Modifier.size(36.dp)) {
                                            Icon(
                                                Icons.Filled.ThumbDown,
                                                contentDescription = "Not helpful",
                                                tint = if (msg.feedbackPositive == false) SkillBridgeColors.DisputedRed else MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                    TextButton(
                                        onClick = { reportTargetId = msg.id },
                                        enabled = !msg.isReported
                                    ) {
                                        Icon(Icons.Filled.Flag, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            if (msg.isReported) (if (lang == AppLanguage.AR) "تم البلاغ" else "Reported")
                                            else strings.aiAgentReportLabel,
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                if (state.isStreaming) {
                    item {
                        Card(colors = CardDefaults.cardColors(containerColor = SkillBridgeColors.SoftCyanTint)) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                Text(
                                    text = state.streamingText.ifBlank { if (lang == AppLanguage.AR) "جارٍ التحليل..." else "Thinking..." },
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }

                state.errorMessage?.let { err ->
                    item {
                        Card(colors = CardDefaults.cardColors(containerColor = SkillBridgeColors.DisputedBg)) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(err, color = SkillBridgeColors.DisputedRed, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                                TextButton(onClick = onRetry, modifier = Modifier.testTag("ai_agent_retry_btn")) {
                                    Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text(strings.errorRetry)
                                }
                            }
                        }
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = prompt,
                    onValueChange = { prompt = it },
                    placeholder = { Text(strings.aiAgentAskHint, maxLines = 1) },
                    modifier = Modifier.weight(1f).testTag("ai_agent_input")
                )
                IconButton(
                    onClick = {
                        onSendPrompt(prompt)
                        prompt = ""
                    },
                    enabled = prompt.isNotBlank() && !state.isStreaming,
                    modifier = Modifier.testTag("ai_agent_send_btn")
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
                }
            }
            Spacer(Modifier.height(14.dp))
        }
    }

    state.pendingActionConfirmation?.let { pending ->
        AlertDialog(
            onDismissRequest = onDismissAction,
            title = { Text(strings.aiAgentConfirmActionTitle) },
            text = { Text(pending.description.get(lang)) },
            confirmButton = {
                Button(onClick = onConfirmAction, modifier = Modifier.testTag("ai_agent_confirm_write_btn")) {
                    Text(strings.confirmLabel)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissAction) {
                    Text(strings.cancelLabel)
                }
            }
        )
    }

    reportTargetId?.let { id ->
        var reason by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { reportTargetId = null },
            title = { Text(strings.aiAgentReportLabel) },
            text = {
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text(if (lang == AppLanguage.AR) "سبب البلاغ" else "Reason") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = {
                    onReportMessage(id, reason)
                    reportTargetId = null
                }) { Text(strings.confirmLabel) }
            },
            dismissButton = {
                TextButton(onClick = { reportTargetId = null }) { Text(strings.cancelLabel) }
            }
        )
    }
}
