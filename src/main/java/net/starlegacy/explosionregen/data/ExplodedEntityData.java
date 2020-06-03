package net.starlegacy.explosionregen.data;

import net.starlegacy.explosionregen.NMSUtils;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import javax.annotation.Nullable;

public class ExplodedEntityData {
    private EntityType entityType;
    private double x, y, z;
    private float pitch, yaw;
    private long explodedTime;
    @Nullable
    private byte[] nmsData;

    public ExplodedEntityData(EntityType entityType, double x, double y, double z, float pitch, float yaw,
                              long explodedTime, @Nullable byte[] nmsData) {
        this.entityType = entityType;
        this.x = x;
        this.y = y;
        this.z = z;
        this.pitch = pitch;
        this.yaw = yaw;
        this.explodedTime = explodedTime;
        this.nmsData = nmsData;
    }

    public ExplodedEntityData(Entity entity) {
        this(entity.getType(), entity.getLocation().getX(), entity.getLocation().getY(), entity.getLocation().getZ(),
                entity.getLocation().getPitch(), entity.getLocation().getYaw(),
                System.currentTimeMillis(), NMSUtils.getEntityData(entity));
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public float getPitch() {
        return pitch;
    }

    public float getYaw() {
        return yaw;
    }

    public long getExplodedTime() {
        return explodedTime;
    }

    @Nullable
    public byte[] getNmsData() {
        return nmsData;
    }
}
