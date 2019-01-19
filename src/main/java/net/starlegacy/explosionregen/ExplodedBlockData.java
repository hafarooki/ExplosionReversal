package net.starlegacy.explosionregen;

import org.bukkit.block.data.BlockData;

public class ExplodedBlockData {
    private int x;
    private int y;
    private int z;
    private String blockData;
    private byte[] tileData;

    public ExplodedBlockData(int x, int y, int z, BlockData blockData, byte[] tileData) {
        setX(x);
        setY(y);
        setZ(z);
        setBlockData(blockData);
        setTileData(tileData);
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getZ() {
        return z;
    }

    public void setZ(int z) {
        this.z = z;
    }

    public String getBlockDataString() {
        return blockData;
    }

    public void setBlockData(BlockData blockData) {
        this.blockData = blockData.getAsString();
    }

    public byte[] getTileData() {
        return tileData;
    }

    public void setTileData(byte[] tileData) {
        this.tileData = tileData;
    }
}
