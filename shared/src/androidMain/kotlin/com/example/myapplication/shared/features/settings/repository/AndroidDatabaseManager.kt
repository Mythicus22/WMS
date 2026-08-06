package com.example.myapplication.shared.features.settings.repository

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class AndroidDatabaseManager(private val context: Context) : DatabaseManager {
    
    override suspend fun backupDatabase(destinationDirectoryUri: String): String = withContext(Dispatchers.IO) {
        try {
            val dbFile = context.getDatabasePath("app.db")
            if (!dbFile.exists()) {
                throw IllegalStateException("Database file not found.")
            }
            
            val dirUri = Uri.parse(destinationDirectoryUri)
            val documentFile = DocumentFile.fromTreeUri(context, dirUri)
                ?: throw IllegalArgumentException("Invalid output directory URI")

            val timestamp = System.currentTimeMillis().toString()
            val fileName = "wms_backup_$timestamp.db"
            val file = documentFile.createFile("application/vnd.sqlite3", fileName)
                ?: throw IllegalStateException("Could not create backup file in the selected directory")

            FileInputStream(dbFile).use { input ->
                context.contentResolver.openOutputStream(file.uri)?.use { output ->
                    input.copyTo(output)
                    output.flush()
                } ?: throw IllegalStateException("Could not open output stream for backup file")
            }

            return@withContext file.uri.toString()
        } catch (e: Exception) {
            Napier.e("Failed to backup database", e, "AndroidDatabaseManager")
            throw e
        }
    }

    override suspend fun restoreDatabase(backupUri: String) = withContext(Dispatchers.IO) {
        try {
            val uri = Uri.parse(backupUri)
            val dbFile = context.getDatabasePath("app.db")
            
            // Note: In a real app, you must close all database connections before replacing the file.
            // For this prototype, we'll replace the file directly.
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(dbFile).use { output ->
                    input.copyTo(output)
                    output.flush()
                }
            } ?: throw IllegalStateException("Could not open input stream for backup file")

            Napier.d("Database restored from $backupUri", tag = "AndroidDatabaseManager")
        } catch (e: Exception) {
            Napier.e("Failed to restore database", e, "AndroidDatabaseManager")
            throw e
        }
    }
}
