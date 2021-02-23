package net.starlegacy.explosionreversal.listener;

import net.starlegacy.explosionreversal.ExplosionReversalPlugin;
import net.starlegacy.explosionreversal.data.ExplodedEntityData;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.hanging.HangingBreakEvent;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.UUID;

public class EntityListener implements Listener {
    private final ExplosionReversalPlugin plugin;

    public EntityListener(ExplosionReversalPlugin plugin) {
        this.plugin = plugin;
    }

    private final HashMap<UUID, ExplodedEntityData> pendingDeathEntities = new HashMap<>();

    // put the data in beforehand because some entities such as armor stands lose items before the death event but
    // after this event
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDemise(EntityDamageEvent event) {
        Entity entity = event.getEntity();

        if (!isRegeneratedEntity(entity)) {
            return;
        }

        if (!isCausedByExplosion(event)) {
            return;
        }

        ExplodedEntityData explodedEntityData = getExplodedEntityData(entity);
        pendingDeathEntities.put(entity.getUniqueId(), explodedEntityData);
    }

    private ExplodedEntityData getExplodedEntityData(Entity entity) {
        double cap = plugin.getSettings().getDistanceDelayCap();
        double delay = plugin.getSettings().getDistanceDelay();
        long time = System.currentTimeMillis() + Math.round(cap * delay * 1000L);

        return new ExplodedEntityData(entity, time);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onItemDeath(ItemDespawnEvent event) {
        Item entity = event.getEntity();

        if (!isRegeneratedEntity(entity) || !isCausedByExplosion(entity.getLastDamageCause())) {
            return;
        }

        onEntityExplode(entity);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();

        if (!isRegeneratedEntity(entity) || !isCausedByExplosion(entity.getLastDamageCause())) {
            return;
        }

        onEntityExplode(entity);
        event.getDrops().clear();
    }

    private void onEntityExplode(Entity entity) {
        UUID id = entity.getUniqueId();
        ExplodedEntityData explodedEntityData;

        if (pendingDeathEntities.containsKey(id)) {
            explodedEntityData = pendingDeathEntities.remove(id);
        } else {
            explodedEntityData = getExplodedEntityData(entity);
        }

        World world = entity.getWorld();
        plugin.getWorldData().addEntity(world, explodedEntityData);
    }

    private boolean isRegeneratedEntity(Entity entity) {
        EntityType type = entity.getType();

        if (plugin.getSettings().getIgnoredEntities().contains(type)) {
            return false;
        }

        if (plugin.getSettings().getIncludedEntities().contains(type)) {
            return true;
        }


        switch (type) {
            case ARMOR_STAND:
            case DROPPED_ITEM:
            case PAINTING:
                return true;
            default:
                return false;
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

        ExplodedEntityData explodedEntityData = getExplodedEntityData(entity);

        plugin.getWorldData().addEntity(entity.getWorld(), explodedEntityData);

        entity.remove();
    }
}
