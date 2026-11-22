package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = SkillBridgeColors.BridgeBlueDark,
    onPrimary = SkillBridgeColors.PureWhite,
    primaryContainer = SkillBridgeColors.SoftCyanTint,
    onPrimaryContainer = SkillBridgeColors.DeepNavy,
    secondary = SkillBridgeColors.DeepNavy,
    onSecondary = SkillBridgeColors.PureWhite,
    secondaryContainer = SkillBridgeColors.SurfaceSubtle,
    onSecondaryContainer = SkillBridgeColors.DeepNavy,
    tertiary = SkillBridgeColors.LimeGreenDark,
    onTertiary = SkillBridgeColors.PureWhite,
    tertiaryContainer = SkillBridgeColors.LimeGreenTint,
    onTertiaryContainer = SkillBridgeColors.DeepNavy,
    background = SkillBridgeColors.OffWhiteCanvas,
    onBackground = SkillBridgeColors.InkPrimary,
    surface = SkillBridgeColors.PureWhite,
    onSurface = SkillBridgeColors.InkPrimary,
    surfaceVariant = SkillBridgeColors.SurfaceSubtle,
    onSurfaceVariant = SkillBridgeColors.InkSecondary,
    surfaceContainerLow = SkillBridgeColors.PureWhite,
    surfaceContainer = Color(0xFFF1F6FA),
    surfaceContainerHigh = SkillBridgeColors.SurfaceContainerHigh,
    outline = SkillBridgeColors.BorderLight,
    outlineVariant = Color(0xFFE5EDF5),
    error = SkillBridgeColors.DisputedRed,
    onError = SkillBridgeColors.PureWhite,
    errorContainer = SkillBridgeColors.DisputedBg,
    onErrorContainer = Color(0xFF881337)
)

private val DarkColorScheme = darkColorScheme(
    primary = SkillBridgeColors.BridgeBlue,
    onPrimary = SkillBridgeColors.DeepNavy,
    primaryContainer = SkillBridgeColors.DeepNavyElevated,
    onPrimaryContainer = SkillBridgeColors.SoftCyan,
    secondary = SkillBridgeColors.SoftCyan,
    onSecondary = SkillBridgeColors.DeepNavy,
    secondaryContainer = Color(0xFF17355C),
    onSecondaryContainer = SkillBridgeColors.InkOnDark,
    tertiary = SkillBridgeColors.LimeGreen,
    onTertiary = SkillBridgeColors.DeepNavy,
    tertiaryContainer = Color(0xFF283D08),
    onTertiaryContainer = SkillBridgeColors.LimeGreenTint,
    background = SkillBridgeColors.DeepNavy,
    onBackground = SkillBridgeColors.InkOnDark,
    surface = Color(0xFF102846),
    onSurface = SkillBridgeColors.InkOnDark,
    surfaceVariant = SkillBridgeColors.DeepNavyElevated,
    onSurfaceVariant = SkillBridgeColors.InkMutedOnDark,
    surfaceContainerLow = Color(0xFF0E2541),
    surfaceContainer = SkillBridgeColors.DeepNavyElevated,
    surfaceContainerHigh = Color(0xFF1A3A63),
    outline = SkillBridgeColors.BorderDark,
    outlineVariant = Color(0xFF1A3253),
    error = Color(0xFFFB7185),
    onError = SkillBridgeColors.DeepNavy,
    errorContainer = Color(0xFF4C0519),
    onErrorContainer = Color(0xFFFFE4E6)
)

private val AppShapes = Shapes(
    small = RoundedCornerShape(SkillBridgeRadius.sm),
    medium = RoundedCornerShape(SkillBridgeRadius.md),
    large = RoundedCornerShape(SkillBridgeRadius.lg),
    extraLarge = RoundedCornerShape(SkillBridgeRadius.xl)
)

@Composable
fun SkillBridgeTheme(
    darkTheme: Boolean = false,
    isArabic: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val typography = typographyForLocale(isArabic = isArabic)

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        shapes = AppShapes,
        content = content
    )
}
