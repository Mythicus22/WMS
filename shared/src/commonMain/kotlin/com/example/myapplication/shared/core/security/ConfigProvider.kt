package com.example.myapplication.shared.core.security

/**
 * Provides access to the secure configuration of the application.
 */
interface ConfigProvider {
    /**
     * Checks if the configuration exists and the app has been initialized.
     */
    fun isConfigured(): Boolean

    /**
     * Saves the initial admin credentials securely.
     */
    fun saveConfig(username: String, hash: String)

    /**
     * Reads the encrypted configuration, returning the username and password hash.
     * Returns null if not configured.
     */
    fun readConfig(): Pair<String, String>?

    /**
     * Saves the logged-in session ID securely.
     */
    fun saveSessionId(sessionId: String)

    /**
     * Retrieves the stored session ID, or null if no active session exists.
     */
    fun getSessionId(): String?

    /**
     * Clears the stored session ID.
     */
    fun clearSessionId()
}
