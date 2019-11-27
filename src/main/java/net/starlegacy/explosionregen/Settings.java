package net.starlegacy.explosionregen;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;

import java.util.Objects;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

class Settings {
    private static Logger log = Logger.getLogger(Settings.class.getName());

    /**
     * Time in minutes after explosion blocks should regenerate
     */
    private double regenDelay;
    /**
     * Maximum time in milliseconds that should be spent per tick regenerating exploded blocks
     */
    private double placementIntensity;
    /**
     * Entities to ignore the explosions of
     */
    private Set<EntityType> ignoredEntities;
    /**
     * Types of blocks to not regenerate
     */
    private Set<Material> ignoredMaterials;
    /**
     * Types of blocks to regenerate. When empty, include all blocks
     */
    private Set<Material> includedMaterials;

    Settings(FileConfiguration config) {
        regenDelay = config.getDouble("regen_delay", 5.0);
        placementIntensity = config.getDouble("placement_intensity", 5.0);
        ignoredEntities = config.getStringList("ignored_entities").stream()
                .map(this::parseEntityType)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        ignoredMaterials = config.getStringList("ignored_materials").stream()
                .map(this::parseMaterial)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        includedMaterials = config.getStringList("included_materials").stream()
                .map(this::parseMaterial)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    void save(FileConfiguration config) {
        config.set("regen_delay", regenDelay);
        config.set("placement_intensity", placementIntensity);
        config.set("ignored_entities", ignoredEntities.stream().map(Enum::name).collect(Collectors.toList()));
        config.set("ignored_materials", ignoredMaterials.stream().map(Enum::name).collect(Collectors.toList()));
        config.set("included_materials", includedMaterials.stream().map(Enum::name).collect(Collectors.toList()));
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

    public double getPlacementIntensity() {
        return placementIntensity;
    }

    public Set<EntityType> getIgnoredEntities() {
        return ignoredEntities;
    }

    public Set<Material> getIgnoredMaterials() {
        return ignoredMaterials;
    }

    public Set<Material> getIncludedMaterials() {
        return includedMaterials;
    }
}
