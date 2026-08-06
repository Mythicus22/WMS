package com.example.myapplication.shared.features.settings.repository

interface DatabaseManager {
    suspend fun backupDatabase(destinationDirectoryUri: String): String
    suspend fun restoreDatabase(backupUri: String)
}
