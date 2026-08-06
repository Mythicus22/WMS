package com.example.myapplication.shared.features.settings.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun rememberDirectoryPicker(onResult: (String?) -> Unit): DirectoryPickerLauncher {
    return remember {
        object : DirectoryPickerLauncher {
            override fun launch() {
                // JVM dummy fallback
                onResult("/mock/jvm/path")
            }
        }
    }
}
