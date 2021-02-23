package net.starlegacy.explosionreversal.data;

import org.bukkit.block.data.BlockData;

import javax.annotation.Nullable;

public class ExplodedBlockData {
    private final int x;
    private final int y;
    private final int z;
    private final long explodedTime; // the time to start the regenerate timer, NOT necessarily the time that it exploded
    private final BlockData blockData;
    @Nullable
    private final byte[] tileData;

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
