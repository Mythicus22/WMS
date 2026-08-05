package com.example.myapplication.shared.core.security

interface SecurityProvider {
    /**
     * Hashes a password securely using Argon2id.
     */
    fun hashPassword(password: String): String

    /**
     * Verifies a password against an Argon2id hash.
     */
    fun verifyPassword(password: String, hash: String): Boolean
}
