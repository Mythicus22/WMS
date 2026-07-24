package com.example.myapplication.shared.data.local.database

/**
 * Platform-agnostic placeholder for a database driver factory.
 * Concrete platform-specific implementations should be provided later.
 */
open class DatabaseDriverFactory {
    open fun createDriver(): Any {
        throw NotImplementedError("Platform-specific database driver not implemented yet")
    }
}

