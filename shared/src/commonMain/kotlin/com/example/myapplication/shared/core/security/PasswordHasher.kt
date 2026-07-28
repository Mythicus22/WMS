package com.example.myapplication.shared.core.security

import java.security.MessageDigest
import java.security.SecureRandom
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 * SHA-256 password hashing utility.
 * Ensures passwords are never stored in plain text.
 */
object PasswordHasher {

    private const val DEFAULT_SALT = "WMS_RADIO_SHUTTLE_SALT_2026"

    /**
     * Hashes a raw password with a salt using SHA-256.
     */
    fun hashPassword(password: String, salt: String = DEFAULT_SALT): String {
        val bytes = (password + salt).toByteArray(Charsets.UTF_8)
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }

    /**
     * Verifies if a raw password matches the stored hash.
     */
    fun verifyPassword(password: String, storedHash: String, salt: String = DEFAULT_SALT): Boolean {
        val hash = hashPassword(password, salt)
        return hash.equals(storedHash, ignoreCase = true)
    }
}
