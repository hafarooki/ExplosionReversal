package net.starlegacy.explosionregen;

import javax.annotation.Nullable;

public class ExplodedBlockData {
    private int x;
    private int y;
    private int z;
    private long explodedTime;
    private String blockDataString;
    @Nullable
    private byte[] tileData;

    public ExplodedBlockData(int x, int y, int z, long explodedTime, String blockDataString, @Nullable byte[] tileData) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.explodedTime = explodedTime;
        this.blockDataString = blockDataString;
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

    public String getBlockDataString() {
        return blockDataString;
    }

    @Nullable
    public byte[] getTileData() {
        return tileData;
    }
}
