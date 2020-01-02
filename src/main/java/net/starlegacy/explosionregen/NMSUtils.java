package net.starlegacy.explosionregen;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.block.Block;

import net.minecraft.server.v1_15_R1.*;
import org.bukkit.craftbukkit.v1_15_R1.*;

import javax.annotation.Nullable;

public class NMSUtils {
    @Nullable
    public static byte[] getTileEntity(Block block) {
        try {
            return completeGetTileEntity(block);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // separate method for version compatibility
    private static byte[] completeGetTileEntity(Block block) throws Exception {
        BlockPosition blockPosition = new BlockPosition(block.getX(), block.getY(), block.getZ());
        WorldServer worldServer = ((CraftWorld) block.getWorld()).getHandle();
        TileEntity tileEntity = worldServer.getTileEntity(blockPosition);
        if (tileEntity == null) {
            return null;
        }
        NBTTagCompound nbt = new NBTTagCompound();
        tileEntity.save(nbt);
        ByteArrayDataOutput output = ByteStreams.newDataOutput();
        nbt.write(output);
        return output.toByteArray();
    }

    public static void setTileEntity(Block block, byte[] bytes) {
        try {
            completeSetTileEntity(block, bytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void completeSetTileEntity(Block block, byte[] bytes) throws Exception {
        NBTTagCompound nbt = new NBTTagCompound();
        ByteArrayDataInput input = ByteStreams.newDataInput(bytes);
        NBTTagCompound.a.b(input, 0, new NBTReadLimiter(bytes.length * 10));
        BlockPosition blockPosition = new BlockPosition(block.getX(), block.getY(), block.getZ());
        WorldServer worldServer = ((CraftWorld) block.getWorld()).getHandle();
        TileEntity tileEntity = TileEntity.create(nbt);
        worldServer.setTileEntity(blockPosition, tileEntity);
    }
}
