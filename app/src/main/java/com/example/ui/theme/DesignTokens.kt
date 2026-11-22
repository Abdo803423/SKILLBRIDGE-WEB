package com.example.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

object SkillBridgeColors {
    // Primary Brand Identity (from SkillBridge Brand Showcase Poster)
    val DeepNavy = Color(0xFF0B1F38)
    val DeepNavyElevated = Color(0xFF132D4F)
    val BridgeBlue = Color(0xFF00B4DB)
    val BridgeBlueDark = Color(0xFF008CAE)
    val SoftCyan = Color(0xFF7EE8F5)
    val SoftCyanTint = Color(0xFFE0F9FD)
    val LimeGreen = Color(0xFFA6E22E)
    val LimeGreenDark = Color(0xFF558B00)
    val LimeGreenTint = Color(0xFFF1FCE0)

    // Neutral Canvas & Surfaces
    val OffWhiteCanvas = Color(0xFFF7FAFC)
    val PureWhite = Color(0xFFFFFFFF)
    val SurfaceSubtle = Color(0xFFEDF2F7)
    val SurfaceContainerHigh = Color(0xFFE2E8F0)
    val BorderLight = Color(0xFFD5E0EC)
    val BorderDark = Color(0xFF213C61)

    // Typography Neutrals (WCAG AA+ Contrast)
    val InkPrimary = Color(0xFF0B1F38)
    val InkSecondary = Color(0xFF3E526A)
    val InkMuted = Color(0xFF64748B)
    val InkOnDark = Color(0xFFF4F9FC)
    val InkMutedOnDark = Color(0xFFA7BED3)

    // Semantic Status Tokens (Verified, Pending, Review, Paid, Disputed, Error, Locked)
    val VerifiedGreen = Color(0xFF15803D)
    val VerifiedBg = Color(0xFFDCFCE7)
    val PendingAmber = Color(0xFFB45309)
    val PendingBg = Color(0xFFFEF3C7)
    val ReviewBlue = Color(0xFF0369A1)
    val ReviewBg = Color(0xFFE0F2FE)
    val PaidTeal = Color(0xFF0F766E)
    val PaidBg = Color(0xFFCCFBF1)
    val DisputedRed = Color(0xFFBE123C)
    val DisputedBg = Color(0xFFFFE4E6)
    val LockedSlate = Color(0xFF475569)
    val LockedBg = Color(0xFFE2E8F0)
    val EthicalPurple = Color(0xFF5B21B6)
    val EthicalBg = Color(0xFFEDE9FE)
}

object SkillBridgeSpacing {
    val xxs = 4.dp
    val xs = 8.dp
    val sm = 12.dp
    val md = 16.dp
    val lg = 20.dp
    val xl = 24.dp
    val xxl = 32.dp
    val screenHorizontal = 16.dp
    val cardPadding = 16.dp
    val touchTargetMin = 48.dp
}

object SkillBridgeRadius {
    val xs = 8.dp
    val sm = 12.dp
    val md = 16.dp
    val lg = 20.dp
    val xl = 28.dp
    val pill = 999.dp
}

object SkillBridgeElevation {
    val none = 0.dp
    val card = 2.dp
    val sheet = 6.dp
    val hero = 8.dp
}
