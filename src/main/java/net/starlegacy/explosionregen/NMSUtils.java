package net.starlegacy.explosionregen;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.minecraft.server.v1_16_R2.*;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_16_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R2.entity.CraftEntity;
import org.bukkit.entity.Entity;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Objects;

public class NMSUtils {
    private static boolean nmsEnabled = true;

    static {
        try {
            Class.forName("net.minecraft.server.v1_16_R1.WorldServer");
        } catch (Exception e) {
            nmsEnabled = false;
            e.printStackTrace();
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    private static byte[] serialize(NBTTagCompound nbt) throws IOException {
        ByteArrayDataOutput output = ByteStreams.newDataOutput();
        NBTCompressedStreamTools.a(nbt, output);

        return output.toByteArray();
    }

    @SuppressWarnings("UnstableApiUsage")
    private static NBTTagCompound deserialize(byte[] bytes) throws IOException {
        ByteArrayDataInput input = ByteStreams.newDataInput(bytes);
        NBTReadLimiter readLimiter = new NBTReadLimiter(bytes.length * 10);
        return NBTCompressedStreamTools.a(input, readLimiter);
    }

    // separate method for graceful failure on version incompatibility
    @Nullable
    public static byte[] getTileEntity(Block block) {
        if (!nmsEnabled) {
            return null;
        }

        try {
            return completeGetTileEntity(block);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    private static byte[] completeGetTileEntity(Block block) throws Exception {
        WorldServer worldServer = ((CraftWorld) block.getWorld()).getHandle();

        BlockPosition blockPosition = new BlockPosition(block.getX(), block.getY(), block.getZ());

        TileEntity tileEntity = worldServer.getTileEntity(blockPosition);
        if (tileEntity == null) {
            return null;
        }

        NBTTagCompound nbt = tileEntity.save(new NBTTagCompound());

        return serialize(nbt);
    }

    public static void setTileEntity(Block block, byte[] bytes) {
        if (!nmsEnabled) {
            return;
        }

        try {
            completeSetTileEntity(block, bytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void completeSetTileEntity(Block block, byte[] bytes) throws IOException {
        WorldServer worldServer = ((CraftWorld) block.getWorld()).getHandle();

        BlockPosition blockPosition = new BlockPosition(block.getX(), block.getY(), block.getZ());

        NBTTagCompound nbt = deserialize(bytes);

        IBlockData blockData = worldServer.getType(blockPosition);

        TileEntity tileEntity = Objects.requireNonNull(TileEntity.create(blockData, nbt));
        tileEntity.setPosition(blockPosition);

        worldServer.removeTileEntity(blockPosition);
        worldServer.setTileEntity(blockPosition, tileEntity);
    }

    private static net.minecraft.server.v1_16_R2.Entity getNMSEntity(Entity entity) {
        CraftEntity craftEntity = (CraftEntity) entity;
        return craftEntity.getHandle();
    }

    @Nullable
    public static byte[] getEntityData(Entity entity) {
        if (!nmsEnabled) {
            return null;
        }

        try {
            return completeGetEntityData(entity);
        } catch (Exception exception) {
            exception.printStackTrace();
            return null;
        }
    }

    private static byte[] completeGetEntityData(Entity entity) throws IOException {
        net.minecraft.server.v1_16_R2.Entity nmsEntity = getNMSEntity(entity);
        NBTTagCompound nbt = nmsEntity.save(new NBTTagCompound());
        return serialize(nbt);
    }

    public static void restoreEntityData(Entity entity, byte[] entityData) {
        if (!nmsEnabled) {
            return;
        }

        try {
            completeRestoreEntityData(entity, entityData);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private static void completeRestoreEntityData(Entity entity, byte[] entityData) throws IOException {
        net.minecraft.server.v1_16_R2.Entity nmsEntity = getNMSEntity(entity);
        NBTTagCompound nbt = deserialize(entityData);
        nmsEntity.load(nbt);
    }
}
