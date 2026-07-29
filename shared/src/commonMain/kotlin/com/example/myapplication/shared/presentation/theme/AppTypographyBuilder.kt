package com.example.myapplication.shared.presentation.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.myapplication.shared.features.settings.model.AppFontSize

/**
 * Builds a scaled [Typography] object based on the user's chosen font size preference.
 * Font family is a future enhancement that requires platform-specific font loading.
 */
fun buildTypography(fontSize: AppFontSize): Typography {
    val scale = fontSize.scaleFactor
    return Typography(
        displayLarge = TextStyle(
            fontSize = (32 * scale).sp,
            fontWeight = FontWeight.Bold,
            lineHeight = (40 * scale).sp,
            letterSpacing = 0.sp
        ),
        displayMedium = TextStyle(
            fontSize = (28 * scale).sp,
            fontWeight = FontWeight.Bold,
            lineHeight = (36 * scale).sp,
            letterSpacing = 0.sp
        ),
        displaySmall = TextStyle(
            fontSize = (24 * scale).sp,
            fontWeight = FontWeight.Bold,
            lineHeight = (32 * scale).sp,
            letterSpacing = 0.sp
        ),
        headlineLarge = TextStyle(
            fontSize = (22 * scale).sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = (28 * scale).sp,
            letterSpacing = 0.sp
        ),
        headlineMedium = TextStyle(
            fontSize = (18 * scale).sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = (24 * scale).sp,
            letterSpacing = 0.sp
        ),
        headlineSmall = TextStyle(
            fontSize = (16 * scale).sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = (22 * scale).sp,
            letterSpacing = 0.sp
        ),
        bodyLarge = TextStyle(
            fontSize = (16 * scale).sp,
            fontWeight = FontWeight.Normal,
            lineHeight = (24 * scale).sp,
            letterSpacing = 0.5.sp
        ),
        bodyMedium = TextStyle(
            fontSize = (14 * scale).sp,
            fontWeight = FontWeight.Normal,
            lineHeight = (20 * scale).sp,
            letterSpacing = 0.25.sp
        ),
        bodySmall = TextStyle(
            fontSize = (12 * scale).sp,
            fontWeight = FontWeight.Normal,
            lineHeight = (16 * scale).sp,
            letterSpacing = 0.4.sp
        ),
        labelLarge = TextStyle(
            fontSize = (14 * scale).sp,
            fontWeight = FontWeight.Medium,
            lineHeight = (20 * scale).sp,
            letterSpacing = 0.1.sp
        ),
        labelMedium = TextStyle(
            fontSize = (12 * scale).sp,
            fontWeight = FontWeight.Medium,
            lineHeight = (16 * scale).sp,
            letterSpacing = 0.5.sp
        ),
        labelSmall = TextStyle(
            fontSize = (11 * scale).sp,
            fontWeight = FontWeight.Medium,
            lineHeight = (16 * scale).sp,
            letterSpacing = 0.5.sp
        ),
        titleLarge = TextStyle(
            fontSize = (22 * scale).sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = (28 * scale).sp,
            letterSpacing = 0.sp
        ),
        titleMedium = TextStyle(
            fontSize = (16 * scale).sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = (24 * scale).sp,
            letterSpacing = 0.15.sp
        ),
        titleSmall = TextStyle(
            fontSize = (14 * scale).sp,
            fontWeight = FontWeight.Medium,
            lineHeight = (20 * scale).sp,
            letterSpacing = 0.1.sp
        )
    )
}
