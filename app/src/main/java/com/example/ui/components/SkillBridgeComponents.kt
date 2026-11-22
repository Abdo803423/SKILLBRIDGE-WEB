package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.AppLanguage
import com.example.core.LocalAppLanguage
import com.example.core.LocalAppStrings
import com.example.core.localizedCareerLevel
import com.example.ui.theme.SkillBridgeColors
import com.example.ui.theme.SkillBridgeRadius
import com.example.ui.theme.SkillBridgeSpacing

/**
 * Official SkillBridge isometric S-ribbon mark matching the SkillBridge Brand Showcase Poster.
 */
@Composable
fun SkillBridgeEmblem(
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    darkBackground: Boolean = false
) {
    Surface(
        modifier = modifier.size(size),
        shape = RoundedCornerShape((size.value * 0.24f).dp),
        color = androidx.compose.ui.graphics.Color(0xFF0B233B),
        border = BorderStroke(
            width = 1.dp,
            color = if (darkBackground) SkillBridgeColors.BridgeBlue.copy(alpha = 0.45f) else SkillBridgeColors.BorderLight
        )
    ) {
        androidx.compose.foundation.Image(
            painter = androidx.compose.ui.res.painterResource(id = com.example.core.AssetRegistry.BRAND_LOGO_RASTER),
            contentDescription = "SkillBridge Logo",
            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun SkillBridgeLogo(
    modifier: Modifier = Modifier,
    darkBackground: Boolean = false,
    compactOnly: Boolean = false,
    showTagline: Boolean = false
) {
    val strings = LocalAppStrings.current
    val wordColor = if (darkBackground) SkillBridgeColors.PureWhite else MaterialTheme.colorScheme.onSurface
    val subColor = if (darkBackground) SkillBridgeColors.SoftCyan else MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(if (compactOnly) 0.dp else 10.dp)
    ) {
        SkillBridgeEmblem(
            size = if (compactOnly) 30.dp else 40.dp,
            darkBackground = darkBackground
        )
        if (!compactOnly) {
            Column {
                Text(
                    text = strings.appName,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.3).sp
                    ),
                    color = wordColor
                )
                if (showTagline) {
                    Text(
                        text = strings.tagline,
                        style = MaterialTheme.typography.labelSmall,
                        color = subColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun CircularSkillMatchGauge(
    percent: Int,
    label: String,
    modifier: Modifier = Modifier,
    size: Dp = 112.dp
) {
    val clamped = percent.coerceIn(0, 100)
    val animatedProgress by animateFloatAsState(
        targetValue = clamped / 100f,
        animationSpec = tween(durationMillis = 700),
        label = "skill_match_progress"
    )
    val trackColor = MaterialTheme.colorScheme.surfaceContainerHigh

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(size)
        ) {
            Canvas(modifier = Modifier.size(size)) {
                val strokeWidth = 11.dp.toPx()
                drawArc(
                    color = trackColor,
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
                drawArc(
                    brush = Brush.sweepGradient(
                        listOf(
                            SkillBridgeColors.BridgeBlue,
                            SkillBridgeColors.SoftCyan,
                            SkillBridgeColors.LimeGreen,
                            SkillBridgeColors.BridgeBlue
                        )
                    ),
                    startAngle = -90f,
                    sweepAngle = 360f * animatedProgress,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$clamped%",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        val lang = LocalAppLanguage.current
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(4.dp))
            StatusPill(
                text = when {
                    clamped >= 85 -> if (lang == AppLanguage.AR) "جاهزية عالية" else "High Readiness"
                    clamped >= 60 -> if (lang == AppLanguage.AR) "على المسار الصحيح" else "On Track"
                    else -> if (lang == AppLanguage.AR) "مسار التطوير نشط" else "Bridge Active"
                },
                tone = when {
                    clamped >= 85 -> StatusTone.VERIFIED
                    clamped >= 60 -> StatusTone.REVIEW
                    else -> StatusTone.PENDING
                }
            )
        }
    }
}

@Composable
fun SkillCategoryBar(
    label: String,
    percent: Int,
    modifier: Modifier = Modifier,
    barColor: Color = SkillBridgeColors.BridgeBlue
) {
    val clamped = percent.coerceIn(0, 100)
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "$clamped%",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = SkillBridgeColors.BridgeBlueDark
            )
        }
        Spacer(Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { clamped / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(SkillBridgeRadius.pill)),
            color = barColor,
            trackColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    }
}

enum class StatusTone {
    VERIFIED, PENDING, REVIEW, PAID, DISPUTED, LOCKED, ETHICAL, NEUTRAL
}

@Composable
fun StatusPill(
    text: String,
    tone: StatusTone = StatusTone.NEUTRAL,
    icon: ImageVector? = null,
    modifier: Modifier = Modifier
) {
    val (bg, fg) = when (tone) {
        StatusTone.VERIFIED -> SkillBridgeColors.VerifiedBg to SkillBridgeColors.VerifiedGreen
        StatusTone.PENDING -> SkillBridgeColors.PendingBg to SkillBridgeColors.PendingAmber
        StatusTone.REVIEW -> SkillBridgeColors.ReviewBg to SkillBridgeColors.ReviewBlue
        StatusTone.PAID -> SkillBridgeColors.PaidBg to SkillBridgeColors.PaidTeal
        StatusTone.DISPUTED -> SkillBridgeColors.DisputedBg to SkillBridgeColors.DisputedRed
        StatusTone.LOCKED -> SkillBridgeColors.LockedBg to SkillBridgeColors.LockedSlate
        StatusTone.ETHICAL -> SkillBridgeColors.EthicalBg to SkillBridgeColors.EthicalPurple
        StatusTone.NEUTRAL -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(SkillBridgeRadius.pill),
        color = bg
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = fg,
                    modifier = Modifier.size(14.dp)
                )
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = fg,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun EvidenceProgressStepper(
    activeStepIndex: Int,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = strings.loopTitle,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            strings.loopSteps.forEachIndexed { idx, stepLabel ->
                val done = idx <= activeStepIndex
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(SkillBridgeRadius.pill))
                            .background(
                                if (done) SkillBridgeColors.BridgeBlue
                                else MaterialTheme.colorScheme.surfaceContainerHigh
                            )
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = stepLabel,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
                        fontWeight = if (idx == activeStepIndex) FontWeight.Bold else FontWeight.Medium,
                        color = if (done) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LevelAndPlanBadgeRow(
    careerLevel: String,
    planTier: String,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val lang = LocalAppLanguage.current
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            StatusPill(
                text = localizedCareerLevel(careerLevel, lang),
                tone = StatusTone.VERIFIED,
                icon = Icons.Filled.Verified
            )
            StatusPill(
                text = planTier,
                tone = StatusTone.NEUTRAL,
                icon = Icons.Filled.Shield
            )
        }
        Text(
            text = strings.levelDisclaimer,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AiExplainabilityCard(
    explanation: String,
    inputsUsed: List<String>,
    confidencePercent: Int,
    onEditPreferences: () -> Unit,
    onAppeal: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val lang = LocalAppLanguage.current
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)),
        border = BorderStroke(1.dp, SkillBridgeColors.BridgeBlue.copy(alpha = 0.35f)),
        shape = RoundedCornerShape(SkillBridgeRadius.md)
    ) {
        Column(
            modifier = Modifier.padding(SkillBridgeSpacing.cardPadding),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Filled.AutoAwesome,
                        contentDescription = null,
                        tint = SkillBridgeColors.BridgeBlueDark,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = strings.aiExplainTitle,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                StatusPill(
                    text = if (lang == AppLanguage.AR) "ثقة $confidencePercent%" else "$confidencePercent% conf.",
                    tone = StatusTone.REVIEW
                )
            }
            Text(
                text = explanation,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (inputsUsed.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    inputsUsed.forEach { input ->
                        Surface(
                            shape = RoundedCornerShape(SkillBridgeRadius.pill),
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Text(
                                text = input,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    onClick = onEditPreferences,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("ai_edit_preferences_btn")
                ) {
                    Text(strings.appealOrAdjust, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Button(
                    onClick = onAppeal,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SkillBridgeColors.DeepNavy,
                        contentColor = SkillBridgeColors.PureWhite
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("ai_prove_skill_btn")
                ) {
                    Text(strings.proveThisSkill, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}

@Composable
fun SectionHeaderRow(
    title: String,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f)
        )
        if (actionText != null && onAction != null) {
            Text(
                text = actionText,
                style = MaterialTheme.typography.labelLarge,
                color = SkillBridgeColors.BridgeBlueDark,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .clip(RoundedCornerShape(SkillBridgeRadius.sm))
                    .clickable(onClick = onAction)
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            )
        }
    }
}

@Composable
fun LoadingStateView(
    message: String = LocalAppStrings.current.loadingText,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CircularProgressIndicator(color = SkillBridgeColors.BridgeBlue)
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun EmptyStateView(
    title: String = LocalAppStrings.current.emptyTitle,
    subtitle: String,
    actionLabel: String = LocalAppStrings.current.emptyAction,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = SkillBridgeColors.SoftCyanTint,
                modifier = Modifier.size(52.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.Inbox,
                        contentDescription = null,
                        tint = SkillBridgeColors.BridgeBlueDark
                    )
                }
            }
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            if (onAction != null) {
                Button(
                    onClick = onAction,
                    modifier = Modifier.height(48.dp)
                ) {
                    Text(actionLabel)
                }
            }
        }
    }
}

@Composable
fun ErrorStateView(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SkillBridgeColors.DisputedBg),
        border = BorderStroke(1.dp, SkillBridgeColors.DisputedRed.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.ErrorOutline,
                contentDescription = null,
                tint = SkillBridgeColors.DisputedRed,
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = SkillBridgeColors.DisputedRed,
                textAlign = TextAlign.Center
            )
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = SkillBridgeColors.DisputedRed),
                modifier = Modifier.height(48.dp)
            ) {
                Text(strings.errorRetry)
            }
        }
    }
}

@Composable
fun LockedStateView(
    title: String = LocalAppStrings.current.lockedTitle,
    reason: String,
    actionLabel: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SkillBridgeColors.LockedBg),
        border = BorderStroke(1.dp, SkillBridgeColors.LockedSlate.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = null,
                tint = SkillBridgeColors.LockedSlate,
                modifier = Modifier.size(28.dp)
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(reason, style = MaterialTheme.typography.bodyMedium, color = SkillBridgeColors.LockedSlate)
            }
            OutlinedButton(
                onClick = onAction,
                modifier = Modifier.height(48.dp)
            ) {
                Text(actionLabel)
            }
        }
    }
}

@Composable
fun InfoNoteBanner(
    text: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(SkillBridgeRadius.sm),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Info,
                contentDescription = null,
                tint = SkillBridgeColors.BridgeBlueDark,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
