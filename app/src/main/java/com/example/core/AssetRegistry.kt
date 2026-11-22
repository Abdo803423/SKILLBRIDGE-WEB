package com.example.core

import androidx.annotation.DrawableRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Verified
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.R
import com.example.ui.theme.SkillBridgeColors

/**
 * Single documented Asset & Brand Registry aligned strictly with the SkillBridge Brand Showcase Poster:
 * - Palette: Deep Navy (#0B1F38), Bridge Blue (#00B4DB), Soft Cyan (#7EE8F5), Lime Spark (#A6E22E)
 * - Typography: Plus Jakarta Sans (EN), IBM Plex Sans Arabic (AR), Space Grotesk (Numerals)
 * - Themes & Layout: Light + Deep Navy Dark Mode, RTL (AR) & LTR (EN) mirrored navigation.
 */
object AssetRegistry {
    @DrawableRes
    val HERO_BRIDGE_BANNER: Int = R.drawable.img_hero_bridge_1794605869240

    @DrawableRes
    val AI_ORBIT_VISUAL: Int = R.drawable.img_coach_orbit_1794605869758

    @DrawableRes
    val BRAND_LOGO_RASTER: Int = R.drawable.skillbridge_app_icon_1794769666078

    data class BadgeVisualSpec(
        val badgeId: String,
        val icon: ImageVector,
        val accentLight: Color,
        val accentDark: Color
    )

    private val defaultBadgeVisual = BadgeVisualSpec(
        badgeId = "default",
        icon = Icons.Filled.Verified,
        accentLight = SkillBridgeColors.BridgeBlueDark,
        accentDark = SkillBridgeColors.SoftCyan
    )

    val badgeVisuals: Map<String, BadgeVisualSpec> = listOf(
        BadgeVisualSpec("first_challenge", Icons.Filled.Code, SkillBridgeColors.BridgeBlueDark, SkillBridgeColors.SoftCyan),
        BadgeVisualSpec("problem_solver", Icons.Filled.Psychology, SkillBridgeColors.VerifiedGreen, SkillBridgeColors.LimeGreen),
        BadgeVisualSpec("team_player", Icons.Filled.Groups, SkillBridgeColors.BridgeBlue, SkillBridgeColors.SoftCyan),
        BadgeVisualSpec("community_helper", Icons.Filled.Forum, SkillBridgeColors.PaidTeal, SkillBridgeColors.LimeGreen),
        BadgeVisualSpec("reverse_eng_explorer", Icons.Filled.Security, SkillBridgeColors.EthicalPurple, SkillBridgeColors.SoftCyan),
        BadgeVisualSpec("consistent_builder", Icons.Filled.LocalFireDepartment, SkillBridgeColors.PendingAmber, SkillBridgeColors.LimeGreen),
        BadgeVisualSpec("verified_skill", Icons.Filled.Verified, SkillBridgeColors.VerifiedGreen, SkillBridgeColors.LimeGreen),
        BadgeVisualSpec("mentor_contributor", Icons.Filled.AutoAwesome, SkillBridgeColors.BridgeBlueDark, SkillBridgeColors.SoftCyan),
        BadgeVisualSpec("top_mena_performer", Icons.Filled.EmojiEvents, SkillBridgeColors.DeepNavy, SkillBridgeColors.LimeGreen)
    ).associateBy { it.badgeId }

    fun badgeVisualFor(badgeId: String): BadgeVisualSpec = badgeVisuals[badgeId] ?: defaultBadgeVisual
}
