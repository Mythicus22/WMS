package com.example.myapplication

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import com.example.myapplication.shared.core.navigation.Navigator
import com.example.myapplication.shared.features.settings.model.AppSettings
import com.example.myapplication.shared.features.settings.model.ThemeMode
import com.example.myapplication.shared.features.settings.repository.SettingsRepository
import com.example.myapplication.shared.presentation.navigation.AppNavHost
import com.example.myapplication.shared.presentation.theme.AppTheme
import com.example.myapplication.shared.presentation.theme.buildTypography
import org.koin.core.context.GlobalContext

import androidx.compose.runtime.CompositionLocalProvider
import com.example.myapplication.shared.presentation.localization.LocalAppLanguage

@Composable
@Preview
fun App() {
    val settingsRepository = remember {
        GlobalContext.get().get<SettingsRepository>()
    }
    val settings by settingsRepository.getSettings().collectAsState(initial = AppSettings())

    val darkTheme = when (settings.general.themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
    }
    val typography = buildTypography(settings.general.fontSize)

    CompositionLocalProvider(LocalAppLanguage provides settings.general.language) {
        AppTheme(darkTheme = darkTheme, typography = typography) {
            val navigator = remember { Navigator() }
            AppNavHost(navigator = navigator)
        }
    }
}