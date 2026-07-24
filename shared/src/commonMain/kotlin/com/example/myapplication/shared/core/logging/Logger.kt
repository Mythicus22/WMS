package com.example.myapplication.shared.core.logging

import io.github.aakira.napier.Napier
import io.github.aakira.napier.DebugAntilog

object Logger {
    fun init() {
        // Initialize napier for multiplatform logging. Platform-specific configuration may be added later.
        Napier.base(DebugAntilog())
    }
}

