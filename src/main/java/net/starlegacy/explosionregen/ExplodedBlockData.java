package net.starlegacy.explosionregen;

import org.bukkit.block.data.BlockData;

import javax.annotation.Nullable;

public class ExplodedBlockData {
    private int x;
    private int y;
    private int z;
    private long explodedTime;
    private BlockData blockData;
    @Nullable
    private byte[] tileData;

    public ExplodedBlockData(int x, int y, int z, long explodedTime, BlockData blockData, @Nullable byte[] tileData) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.explodedTime = explodedTime;
        this.blockData = blockData;
        this.tileData = tileData;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public long getExplodedTime() {
        return explodedTime;
    }

    public BlockData getBlockData() {
        return blockData;
    }

    @Nullable
    public byte[] getTileData() {
        return tileData;
    }
}
