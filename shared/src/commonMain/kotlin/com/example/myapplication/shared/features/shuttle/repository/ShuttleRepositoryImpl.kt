package com.example.myapplication.shared.features.shuttle.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOne
import com.example.myapplication.shared.database.AppDatabase
import com.example.myapplication.shared.features.shuttle.model.Shuttle
import com.example.myapplication.shared.features.shuttle.model.ShuttleCounts
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class ShuttleRepositoryImpl(
    private val database: AppDatabase
) : ShuttleRepository {

    private val queries = database.appDatabaseQueries

    override fun getAllShuttles(): Flow<List<Shuttle>> {
        return queries.getAllShuttles().asFlow().mapToList(Dispatchers.IO).map { entities ->
            entities.map { entity ->
                Shuttle(
                    id = entity.id,
                    name = entity.name,
                    description = entity.description,
                    plcIpAddress = entity.plcIpAddress,
                    mqttPublishTopic = entity.mqttPublishTopic,
                    mqttSubscribeTopic = entity.mqttSubscribeTopic,
                    communicationTimeout = entity.communicationTimeout.toInt(),
                    heartbeatInterval = entity.heartbeatInterval.toInt(),
                    firmwareVersion = entity.firmwareVersion,
                    isEnabled = entity.isEnabled != 0L,
                    createdAt = entity.createdAt,
                    updatedAt = entity.updatedAt
                )
            }
        }
    }

    override fun getShuttleCounts(): Flow<ShuttleCounts> {
        return queries.getShuttleCounts().asFlow().mapToOne(Dispatchers.IO).map { count ->
            ShuttleCounts(
                total = count.total,
                enabled = count.enabled ?: 0L,
                disabled = count.disabled ?: 0L
            )
        }
    }

    override suspend fun getShuttleById(id: String): Shuttle? = withContext(Dispatchers.IO) {
        queries.getShuttleById(id).executeAsOneOrNull()?.let { entity ->
            Shuttle(
                id = entity.id,
                name = entity.name,
                description = entity.description,
                plcIpAddress = entity.plcIpAddress,
                mqttPublishTopic = entity.mqttPublishTopic,
                mqttSubscribeTopic = entity.mqttSubscribeTopic,
                communicationTimeout = entity.communicationTimeout.toInt(),
                heartbeatInterval = entity.heartbeatInterval.toInt(),
                firmwareVersion = entity.firmwareVersion,
                isEnabled = entity.isEnabled != 0L,
                createdAt = entity.createdAt,
                updatedAt = entity.updatedAt
            )
        }
    }

    override suspend fun addShuttle(shuttle: Shuttle) = withContext(Dispatchers.IO) {
        queries.insertShuttle(
            id = shuttle.id,
            name = shuttle.name,
            description = shuttle.description,
            plcIpAddress = shuttle.plcIpAddress,
            mqttPublishTopic = shuttle.mqttPublishTopic,
            mqttSubscribeTopic = shuttle.mqttSubscribeTopic,
            communicationTimeout = shuttle.communicationTimeout.toLong(),
            heartbeatInterval = shuttle.heartbeatInterval.toLong(),
            firmwareVersion = shuttle.firmwareVersion,
            isEnabled = if (shuttle.isEnabled) 1L else 0L,
            createdAt = shuttle.createdAt,
            updatedAt = shuttle.updatedAt
        )
    }

    override suspend fun updateShuttle(shuttle: Shuttle) = withContext(Dispatchers.IO) {
        addShuttle(shuttle) // insertShuttle uses INSERT OR REPLACE
    }

    override suspend fun updateShuttleStatus(id: String, isEnabled: Boolean) = withContext(Dispatchers.IO) {
        queries.updateShuttleStatus(
            isEnabled = if (isEnabled) 1L else 0L,
            updatedAt = com.example.myapplication.shared.core.utils.getCurrentTimeMillis(),
            id = id
        )
    }

    override suspend fun deleteShuttle(id: String) = withContext(Dispatchers.IO) {
        queries.deleteShuttleById(id)
    }
}
