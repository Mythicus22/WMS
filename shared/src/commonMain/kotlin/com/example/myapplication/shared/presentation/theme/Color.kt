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
    primary = Color(0xFF60A5FA), // Electric blue accent
    primaryContainer = Color(0xFF1E3A8A),
    secondary = Color(0xFF38BDF8), // Sky blue accent
    secondaryContainer = Color(0xFF0369A1),
    tertiary = Color(0xFF818CF8),
    background = Color(0xFF0F172A), // Deep Slate Navy Dark background
    surface = Color(0xFF1E293B), // Slate Surface Card
    surfaceVariant = Color(0xFF334155), // Slate Container / Input Field
    outline = Color(0xFF475569), // Slate Border
    error = Color(0xFFF87171),
    onBackground = Color(0xFFF8FAFC), // Crisp Slate Text
    onSurface = Color(0xFFF8FAFC),
    onSurfaceVariant = Color(0xFF94A3B8), // Secondary Slate Text
    onError = Color(0xFF000000)
)

