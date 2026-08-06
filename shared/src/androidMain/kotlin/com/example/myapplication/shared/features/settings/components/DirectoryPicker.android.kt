package com.example.myapplication.shared.features.settings.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun rememberDirectoryPicker(onResult: (String?) -> Unit): DirectoryPickerLauncher {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        onResult(uri?.toString())
    }

    return remember(launcher) {
        object : DirectoryPickerLauncher {
            override fun launch() {
                launcher.launch(null)
            }
        }
    }
}
