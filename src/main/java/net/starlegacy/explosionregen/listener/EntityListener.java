package net.starlegacy.explosionregen.listener;

import net.starlegacy.explosionregen.ExplosionRegenPlugin;
import net.starlegacy.explosionregen.data.ExplodedEntityData;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.hanging.HangingBreakEvent;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.UUID;

public class EntityListener implements Listener {
    private final ExplosionRegenPlugin plugin;

    public EntityListener(ExplosionRegenPlugin plugin) {
        this.plugin = plugin;
    }

    private HashMap<UUID, ExplodedEntityData> pendingDeathEntities = new HashMap<>();

    // put the data in beforehand because some entities such as armor stands lose items before the death event but
    // after this event
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDemise(EntityDamageEvent event) {
        Entity entity = event.getEntity();
        if (isRegeneratedEntity(entity) && isCausedByExplosion(event)) {
            ExplodedEntityData explodedEntityData = new ExplodedEntityData(entity);
            pendingDeathEntities.put(entity.getUniqueId(), explodedEntityData);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (!isRegeneratedEntity(entity) || !isCausedByExplosion(entity.getLastDamageCause())) {
            return;
        }
        UUID id = entity.getUniqueId();
        ExplodedEntityData explodedEntityData;
        if (pendingDeathEntities.containsKey(id)) {
            explodedEntityData = pendingDeathEntities.remove(id);
        } else {
            explodedEntityData = new ExplodedEntityData(entity);
        }
        World world = entity.getWorld();
        plugin.getWorldData().addEntity(world, explodedEntityData);
        event.getDrops().clear();
    }

    private boolean isRegeneratedEntity(Entity entity) {
        if (plugin.getSettings().getIgnoredEntities().contains(entity.getType())) {
            return false;
        } else if (plugin.getSettings().getIncludedEntities().contains(entity.getType())) {
            return true;
        } else {
            switch (entity.getType()) {
                case ARMOR_STAND:
                case PAINTING:
                    return true;
                default:
                    return false;
            }
        }
    }

    private boolean isCausedByExplosion(@Nullable EntityDamageEvent event) {
        if (event == null) {
            return false;
        }
        switch (event.getCause()) {
            case BLOCK_EXPLOSION:
            case ENTITY_EXPLOSION:
                return true;
            default:
                return false;
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPaintingBreak(HangingBreakEvent event) {
        if (event.getCause() != HangingBreakEvent.RemoveCause.EXPLOSION) {
            return;
        }

        Hanging entity = event.getEntity();

        if (!isRegeneratedEntity(entity)) {
            return;
        }

        event.setCancelled(true);

        World world = entity.getWorld();
        ExplodedEntityData explodedEntityData = new ExplodedEntityData(entity);
        plugin.getWorldData().addEntity(world, explodedEntityData);

        entity.remove();
    }
}
