package com.example.myapplication.shared.core.datastore

// DataStore abstraction placeholder for preferences
interface AppDataStore {
    suspend fun <T> read(key: String, default: T): T
    suspend fun <T> write(key: String, value: T)
}

