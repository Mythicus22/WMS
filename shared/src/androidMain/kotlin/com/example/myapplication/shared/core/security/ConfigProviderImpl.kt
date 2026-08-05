package com.example.myapplication.shared.core.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class ConfigProviderImpl(private val context: Context) : ConfigProvider {
    
    private val PREFS_NAME = "secure_wms_prefs"
    private val KEY_USERNAME = "admin_username"
    private val KEY_HASH = "admin_hash"
    
    private val masterKey: MasterKey by lazy {
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }
    
    private val sharedPreferences by lazy {
        EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    override fun isConfigured(): Boolean {
        return sharedPreferences.contains(KEY_USERNAME) && sharedPreferences.contains(KEY_HASH)
    }

    override fun saveConfig(username: String, hash: String) {
        sharedPreferences.edit()
            .putString(KEY_USERNAME, username)
            .putString(KEY_HASH, hash)
            .apply()
    }

    override fun readConfig(): Pair<String, String>? {
        val username = sharedPreferences.getString(KEY_USERNAME, null)
        val hash = sharedPreferences.getString(KEY_HASH, null)
        if (username != null && hash != null) {
            return Pair(username, hash)
        }
        return null
    }
}
