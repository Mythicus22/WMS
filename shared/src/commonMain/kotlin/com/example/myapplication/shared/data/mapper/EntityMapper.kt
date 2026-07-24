package com.example.myapplication.shared.data.mapper

// Mapper placeholder - will be populated when entity/DTO mapping is needed
interface EntityMapper<Entity, Domain> {
    fun toDomain(entity: Entity): Domain
    fun toEntity(domain: Domain): Entity
}

