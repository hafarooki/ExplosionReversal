package com.miclesworkshop.explosionregen

import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.entity.EntityType

class ExplosionRegenSettings(
        val regenDelay: Double,
        val placementIntensity: Double,
        val ignoredEntities: Set<EntityType>
) {
    constructor(config: FileConfiguration) : this(
            config.getDouble("regenDelay"),
            config.getDouble("placementIntensity"),
            config.getStringList("ignoredEntities").asSequence().map { EntityType.valueOf(it) }.toSet()
    )

    fun save(config: FileConfiguration) {
        config["regenDelay"] = regenDelay
        config["placementIntensity"] = placementIntensity
        config["ignoredEntities"] = ignoredEntities.map { it.name }
    }
}