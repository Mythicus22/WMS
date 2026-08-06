package com.example.myapplication.shared.features.reports.repository

interface PlatformFileExporter {
    suspend fun exportFile(
        outputDirectoryUri: String,
        fileName: String,
        mimeType: String,
        content: ByteArray
    ): String
}
