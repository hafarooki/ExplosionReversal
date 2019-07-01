package net.starlegacy.explosionregen;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.minecraft.server.v1_13_R2.*;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_13_R2.CraftWorld;

import javax.annotation.Nullable;
import java.io.IOException;

public class NMSUtils {
    @Nullable
    public static byte[] getTileEntity(Block block) {
        BlockPosition blockPosition = new BlockPosition(block.getX(), block.getY(), block.getZ());
        WorldServer worldServer = ((CraftWorld) block.getWorld()).getHandle();
        TileEntity tileEntity = worldServer.getTileEntity(blockPosition);
        if (tileEntity == null) {
            return null;
        }
        NBTTagCompound nbt = new NBTTagCompound();
        tileEntity.save(nbt);
        ByteArrayDataOutput output = ByteStreams.newDataOutput();
        try {
            nbt.write(output);
            return output.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void setTileEntity(Block block, byte[] bytes) {
        NBTTagCompound nbt = new NBTTagCompound();
        ByteArrayDataInput input = ByteStreams.newDataInput(bytes);
        try {
            nbt.load(input, 0, new NBTReadLimiter(bytes.length * 10));
        } catch (IOException e) {
            e.printStackTrace();
        }
        BlockPosition blockPosition = new BlockPosition(block.getX(), block.getY(), block.getZ());
        WorldServer worldServer = ((CraftWorld) block.getWorld()).getHandle();
        TileEntity tileEntity = TileEntity.create(nbt, worldServer);
        worldServer.setTileEntity(blockPosition, tileEntity);
    }
}
