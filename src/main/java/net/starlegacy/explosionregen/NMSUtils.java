package net.starlegacy.explosionregen;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;

import net.minecraft.server.v1_15_R1.*;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.v1_15_R1.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.util.Objects;

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
        WorldServer worldServer = ((CraftWorld) block.getWorld()).getHandle();

        BlockPosition blockPosition = new BlockPosition(block.getX(), block.getY(), block.getZ());

        TileEntity tileEntity = worldServer.getTileEntity(blockPosition);
        if (tileEntity == null) {
            return null;
        }

        NBTTagCompound nbt = new NBTTagCompound();
        tileEntity.save(nbt);

        ByteArrayDataOutput output = ByteStreams.newDataOutput();
        NBTCompressedStreamTools.a(nbt, output);

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
        WorldServer worldServer = ((CraftWorld) block.getWorld()).getHandle();

        BlockPosition blockPosition = new BlockPosition(block.getX(), block.getY(), block.getZ());

        ByteArrayDataInput input = ByteStreams.newDataInput(bytes);
        NBTTagCompound nbt = NBTCompressedStreamTools.a(input, new NBTReadLimiter(bytes.length * 10));

        TileEntity tileEntity = Objects.requireNonNull(TileEntity.create(nbt));
        tileEntity.setPosition(blockPosition);

        worldServer.removeTileEntity(blockPosition);
        worldServer.setTileEntity(blockPosition, tileEntity);
    }
}
