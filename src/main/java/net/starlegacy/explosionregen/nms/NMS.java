package net.starlegacy.explosionregen.nms;

import org.bukkit.block.Block;
import org.bukkit.entity.Entity;

import javax.annotation.Nullable;

public interface NMS {
    @Nullable
    byte[] getTileEntity(Block block);

    void setTileEntity(Block block, byte[] data);

    @Nullable
    byte[] getEntityData(Entity entity);

    void restoreEntityData(Entity entity, byte[] data);
}
