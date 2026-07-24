package com.example.myapplication.shared.presentation.theme

import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color

object AppColors {
    // Primary brand colors
    val Primary = Color(0xFF6200EE)
    val PrimaryVariant = Color(0xFF3700B3)
    val Secondary = Color(0xFF03DAC6)
    val SecondaryVariant = Color(0xFF018786)
    val Tertiary = Color(0xFF03DAC6)

    // Surface colors
    val Background = Color(0xFFFAFAFA)
    val Surface = Color(0xFFFFFFFF)
    val Error = Color(0xFFB00020)

    // Neutral colors
    val OnBackground = Color(0xFF1F1F1F)
    val OnSurface = Color(0xFF1F1F1F)
    val OnError = Color(0xFFFFFFFF)

    // Functional colors
    val Success = Color(0xFF4CAF50)
    val Warning = Color(0xFFFFC107)
    val Info = Color(0xFF2196F3)
}

val LightColorScheme = lightColorScheme(
    primary = AppColors.Primary,
    primaryContainer = AppColors.PrimaryVariant,
    secondary = AppColors.Secondary,
    secondaryContainer = AppColors.SecondaryVariant,
    tertiary = AppColors.Tertiary,
    background = AppColors.Background,
    surface = AppColors.Surface,
    error = AppColors.Error,
    onBackground = AppColors.OnBackground,
    onSurface = AppColors.OnSurface,
    onError = AppColors.OnError
)

val DarkColorScheme = darkColorScheme(
    primary = AppColors.Primary,
    primaryContainer = AppColors.PrimaryVariant,
    secondary = AppColors.Secondary,
    secondaryContainer = AppColors.SecondaryVariant,
    tertiary = AppColors.Tertiary,
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    error = AppColors.Error,
    onBackground = Color(0xFFE0E0E0),
    onSurface = Color(0xFFE0E0E0),
    onError = AppColors.OnError
)

