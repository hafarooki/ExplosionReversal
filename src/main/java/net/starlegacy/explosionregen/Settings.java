package net.starlegacy.explosionregen;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class Settings {
    private static final Logger log = Logger.getLogger(Settings.class.getName());

    /**
     * Time in minutes after explosion blocks should regenerate
     */
    private final double regenDelay;
    /**
     * Time in seconds of additional delay for each block away (taxicab distance)
     */
    private final double distanceDelay;
    /**
     * Maximum distance to consider
     */
    private final int distanceDelayCap;
    /**
     * Maximum time in milliseconds that should be spent per tick regenerating exploded blocks
     */
    private final double placementIntensity;
    /**
     * Worlds to ignore explosions in
     */
    private final Set<String> ignoredWorlds;
    /**
     * Entities to ignore the explosions of
     */
    private final Set<EntityType> ignoredEntityExplosions;
    /**
     * Entities to not regenerate
     */
    private final Set<EntityType> ignoredEntities;
    /**
     * Additional types of entities to regenerate.
     */
    private final Set<EntityType> includedEntities;
    /**
     * Types of blocks to not regenerate
     */
    private final Set<Material> ignoredMaterials;
    /**
     * Types of blocks to regenerate. When empty, include all blocks
     */
    private final Set<Material> includedMaterials;

    Settings(FileConfiguration config) {
        regenDelay = config.getDouble("regen_delay", 5.0);
        distanceDelay = config.getDouble("distance_delay", 2.0);
        distanceDelayCap = config.getInt("distance_delay_cap", 6);
        placementIntensity = config.getDouble("placement_intensity", 5.0);
        ignoredWorlds = getStringSet(config, "ignored_worlds");
        ignoredEntityExplosions = getEntityTypes(config, "ignored_entity_explosions");
        ignoredEntities = getEntityTypes(config, "ignored_entities");
        ignoredMaterials = getMaterials(config, "ignored_materials");
        includedMaterials = getMaterials(config, "included_materials");
        includedEntities = getEntityTypes(config, "included_entities");
    }

    private Set<String> getStringSet(FileConfiguration config, String path) {
        return new HashSet<>(config.getStringList(path));
    }

    private Set<EntityType> getEntityTypes(FileConfiguration config, String path) {
        return getStringSet(config, path).stream()
                .map(this::parseEntityType)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private Set<Material> getMaterials(FileConfiguration config, String path) {
        return getStringSet(config, path).stream()
                .map(this::parseMaterial)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private EntityType parseEntityType(String string) {
        try {
            return EntityType.valueOf(string.toUpperCase());
        } catch (Exception exception) {
            log.severe("Failed to parse entity type " + string + "! Make sure it is a valid entity type. " +
                    "Valid entity types can be viewed at https://papermc.io/javadocs/org/bukkit/entity/EntityType.html");
            return null;
        }
    }

    private Material parseMaterial(String string) {
        try {
            Material material = Material.valueOf(string);
            if (!material.isBlock()) {
                log.severe(material + " is not a block!");
                return null;
            }
            return material;
        } catch (Exception exception) {
            log.severe("Failed to parse material " + string + "! Make sure it is a valid material. " +
                    "Valid materials can be viewed at https://papermc.io/javadocs/org/bukkit/Material.html");
            return null;
        }
    }

    public double getRegenDelay() {
        return regenDelay;
    }

    public double getDistanceDelay() {
        return distanceDelay;
    }

    public double getDistanceDelayCap() {
        return distanceDelayCap;
    }

    public double getPlacementIntensity() {
        return placementIntensity;
    }

    public Set<String> getIgnoredWorlds() {
        return ignoredWorlds;
    }

    public Set<EntityType> getIgnoredEntityExplosions() {
        return ignoredEntityExplosions;
    }

    public Set<EntityType> getIgnoredEntities() {
        return ignoredEntities;
    }

    public Set<EntityType> getIncludedEntities() {
        return includedEntities;
    }

    public Set<Material> getIgnoredMaterials() {
        return ignoredMaterials;
    }

    public Set<Material> getIncludedMaterials() {
        return includedMaterials;
    }
}
