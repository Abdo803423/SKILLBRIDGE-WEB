package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

val PlusJakartaFamily = FontFamily(
    Font(R.font.plus_jakarta_sans, FontWeight.Normal),
    Font(R.font.plus_jakarta_sans, FontWeight.Medium),
    Font(R.font.plus_jakarta_sans, FontWeight.SemiBold),
    Font(R.font.plus_jakarta_sans, FontWeight.Bold)
)

val IbmPlexArabicFamily = FontFamily(
    Font(R.font.ibm_plex_sans_arabic, FontWeight.Normal),
    Font(R.font.ibm_plex_sans_arabic, FontWeight.Medium),
    Font(R.font.ibm_plex_sans_arabic, FontWeight.SemiBold),
    Font(R.font.ibm_plex_sans_arabic, FontWeight.Bold)
)

val SpaceGroteskFamily = FontFamily(
    Font(R.font.space_grotesk, FontWeight.Normal),
    Font(R.font.space_grotesk, FontWeight.Medium),
    Font(R.font.space_grotesk, FontWeight.SemiBold),
    Font(R.font.space_grotesk, FontWeight.Bold)
)

fun typographyForLocale(isArabic: Boolean): Typography {
    val headingFont = if (isArabic) IbmPlexArabicFamily else PlusJakartaFamily
    val bodyFont = if (isArabic) IbmPlexArabicFamily else PlusJakartaFamily

    return Typography(
        displaySmall = TextStyle(
            fontFamily = headingFont,
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp,
            lineHeight = if (isArabic) 42.sp else 38.sp,
            letterSpacing = if (isArabic) 0.sp else (-0.4).sp
        ),
        headlineLarge = TextStyle(
            fontFamily = headingFont,
            fontWeight = FontWeight.Bold,
            fontSize = 26.sp,
            lineHeight = if (isArabic) 36.sp else 32.sp,
            letterSpacing = if (isArabic) 0.sp else (-0.25).sp
        ),
        headlineMedium = TextStyle(
            fontFamily = headingFont,
            fontWeight = FontWeight.Bold,
            fontSize = 21.sp,
            lineHeight = if (isArabic) 30.sp else 27.sp
        ),
        titleLarge = TextStyle(
            fontFamily = headingFont,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            lineHeight = if (isArabic) 26.sp else 24.sp
        ),
        titleMedium = TextStyle(
            fontFamily = bodyFont,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            lineHeight = if (isArabic) 24.sp else 22.sp
        ),
        bodyLarge = TextStyle(
            fontFamily = bodyFont,
            fontWeight = FontWeight.Normal,
            fontSize = 15.sp,
            lineHeight = if (isArabic) 24.sp else 22.sp
        ),
        bodyMedium = TextStyle(
            fontFamily = bodyFont,
            fontWeight = FontWeight.Normal,
            fontSize = 13.5.sp,
            lineHeight = if (isArabic) 22.sp else 20.sp
        ),
        labelLarge = TextStyle(
            fontFamily = bodyFont,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            lineHeight = 20.sp
        ),
        labelMedium = TextStyle(
            fontFamily = bodyFont,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            lineHeight = 16.sp
        ),
        labelSmall = TextStyle(
            fontFamily = bodyFont,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            lineHeight = 14.sp
        )
    )
}

val Typography = typographyForLocale(isArabic = false)
