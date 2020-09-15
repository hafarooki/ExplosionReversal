package net.starlegacy.explosionregen.data;

import net.starlegacy.explosionregen.nms.NMSUtils;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import javax.annotation.Nullable;

public class ExplodedEntityData {
    private final EntityType entityType;
    private final double x;
    private final double y;
    private final double z;
    private final float pitch;
    private final float yaw;
    private final long explodedTime;
    @Nullable
    private final byte[] nmsData;

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

    public ExplodedEntityData(Entity entity, long explosionTime) {
        this(entity.getType(), entity.getLocation().getX(), entity.getLocation().getY(), entity.getLocation().getZ(),
                entity.getLocation().getPitch(), entity.getLocation().getYaw(),
                explosionTime, NMSUtils.getEntityData(entity));
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
