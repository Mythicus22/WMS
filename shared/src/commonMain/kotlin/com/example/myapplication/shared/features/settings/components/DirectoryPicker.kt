package com.example.myapplication.shared.features.settings.components

import androidx.compose.runtime.Composable

/**
 * A platform-agnostic directory picker launcher.
 */
interface DirectoryPickerLauncher {
    fun launch()
}

/**
 * Creates and remembers a directory picker launcher.
 *
 * @param onResult Callback invoked with the selected directory URI/path, or null if cancelled.
 */
@Composable
expect fun rememberDirectoryPicker(onResult: (String?) -> Unit): DirectoryPickerLauncher
