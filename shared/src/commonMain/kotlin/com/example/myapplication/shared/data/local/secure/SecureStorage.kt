package com.example.myapplication.shared.data.local.secure

interface SecureStorage {
    fun saveSessionId(sessionId: String)
    fun getSessionId(): String?
    fun clearSessionId()
}
