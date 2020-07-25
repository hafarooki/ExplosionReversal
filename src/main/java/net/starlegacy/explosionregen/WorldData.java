package net.starlegacy.explosionregen;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import net.starlegacy.explosionregen.data.ExplodedBlockData;
import net.starlegacy.explosionregen.data.ExplodedEntityData;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.EntityType;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;

public class WorldData {
    private final LoadingCache<World, List<ExplodedBlockData>> explodedBlocks = CacheBuilder.newBuilder()
            .weakKeys()
            .build(CacheLoader.from(this::loadBlocks));

    private final LoadingCache<World, List<ExplodedEntityData>> explodedEntities = CacheBuilder.newBuilder()
            .weakKeys()
            .build(CacheLoader.from(this::loadEntities));

    private File getBlocksFile(World world) {
        return new File(world.getWorldFolder(), "data/explosion_regen_blocks.dat");
    }

    private File getEntitiesFile(World world) {
        return new File(world.getWorldFolder(), "data/explosion_regen_entities.dat");
    }

    public List<ExplodedBlockData> getBlocks(World world) {
        return explodedBlocks.getUnchecked(world);
    }

    public List<ExplodedEntityData> getEntities(World world) {
        return explodedEntities.getUnchecked(world);
    }

    public void save(World world) {
        saveBlocks(world);
        saveEntities(world);
    }

    private List<ExplodedBlockData> loadBlocks(World world) {
        File file = getBlocksFile(world);

        if (!file.exists()) {
            return new LinkedList<>();
        }

        List<ExplodedBlockData> blocks = new LinkedList<>();

        try (DataInputStream input = new DataInputStream(new FileInputStream(file))) {
            List<BlockData> palette = readPalette(input);
            readBlocks(blocks, input, palette);
        } catch (Exception e) {
            e.printStackTrace();
            file.renameTo(new File(file.getParentFile(), file.getName() + "_broken_" + System.currentTimeMillis() % 1000));
            saveBlocks(world);
        }

        return blocks;
    }

    private List<BlockData> readPalette(DataInputStream input) throws IOException {
        int paletteSize = input.readInt();
        List<BlockData> palette = new ArrayList<>();
        for (int i = 0; i < paletteSize; i++) {
            byte[] blockDataBytes = new byte[input.readInt()];
            input.read(blockDataBytes);
            String blockDataString = new String(blockDataBytes);
            palette.add(Bukkit.createBlockData(blockDataString));
        }
        return palette;
    }

    private void readBlocks(List<ExplodedBlockData> blocks, DataInputStream input, List<BlockData> palette) throws IOException {
        int blockCount = input.readInt();
        for (int i = 0; i < blockCount; i++) {
            int x = input.readInt();
            int y = input.readInt();
            int z = input.readInt();

            long explodedTime = input.readLong();

            BlockData blockData = palette.get(input.readInt());

            byte[] tileEntityData = null;

            if (input.readBoolean()) {
                int tileEntitySize = input.readInt();
                tileEntityData = new byte[tileEntitySize];
                input.read(tileEntityData);
            }

            blocks.add(new ExplodedBlockData(x, y, z, explodedTime, blockData, tileEntityData));
        }
    }

    private void saveBlocks(World world) {
        File file = getBlocksFile(world);
        file.getParentFile().mkdirs();

        File tmpFile = new File(file.getParentFile(), file.getName() + "_tmp");

        try (DataOutputStream output = new DataOutputStream(new FileOutputStream(tmpFile))) {
            List<ExplodedBlockData> data = getBlocks(world);
            Map<BlockData, Integer> palette = writePalette(output, data);
            writeBlocks(output, data, palette);
        } catch (Exception e) {
            e.printStackTrace();
            tmpFile.delete();
            return;
        }

        try {
            Files.move(tmpFile.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Map<BlockData, Integer> writePalette(DataOutputStream output, List<ExplodedBlockData> data) throws IOException {
        List<BlockData> palette = data.stream().map(ExplodedBlockData::getBlockData).distinct().collect(Collectors.toList());
        Map<BlockData, Integer> values = new HashMap<>(palette.size());

        output.writeInt(palette.size());

        for (int i = 0; i < palette.size(); i++) {
            BlockData value = palette.get(i);
            String valueString = value.getAsString(true);
            output.writeInt(valueString.length());
            output.writeBytes(valueString);
            values.put(value, i);
        }
        return values;
    }

    private void writeBlocks(DataOutputStream output, List<ExplodedBlockData> data, Map<BlockData, Integer> palette) throws IOException {
        output.writeInt(data.size());

        for (ExplodedBlockData block : data) {
            output.writeInt(block.getX());
            output.writeInt(block.getY());
            output.writeInt(block.getZ());

            output.writeLong(block.getExplodedTime());

            output.writeInt(palette.get(block.getBlockData()));

            byte[] tileData = block.getTileData();
            if (tileData == null) {
                output.writeBoolean(false);
            } else {
                output.writeBoolean(true);
                output.writeInt(tileData.length);
                output.write(tileData);
            }
        }
    }

    private List<ExplodedEntityData> loadEntities(World world) {
        File file = getEntitiesFile(world);

        if (!file.exists()) {
            return new LinkedList<>();
        }

        List<ExplodedEntityData> entities = new LinkedList<>();

        try (DataInputStream input = new DataInputStream(new FileInputStream(file))) {
            int entityCount = input.readInt();
            for (int i = 0; i < entityCount; i++) {
                ExplodedEntityData explodedEntityData = readEntityData(input);
                entities.add(explodedEntityData);
            }
        } catch (Exception e) {
            e.printStackTrace();
            file.renameTo(new File(file.getParentFile(), file.getName() + "_broken_" + System.currentTimeMillis() % 1000));
            saveEntities(world);
        }
        return entities;
    }

    private ExplodedEntityData readEntityData(DataInputStream input) throws IOException {
        EntityType entityType = EntityType.values()[input.readInt()];
        double x = input.readDouble();
        double y = input.readDouble();
        double z = input.readDouble();
        float pitch = input.readFloat();
        float yaw = input.readFloat();
        long explodedTime = input.readLong();

        byte[] entityData = null;

        if (input.readBoolean()) {
            int entityDataSize = input.readInt();
            entityData = new byte[entityDataSize];
            input.read(entityData);
        }

        return new ExplodedEntityData(entityType, x, y, z, pitch, yaw, explodedTime, entityData);
    }

    private void saveEntities(World world) {
        File file = getEntitiesFile(world);
        file.getParentFile().mkdirs();

        File tmpFile = new File(file.getParentFile(), file.getName() + "_tmp");

        try (DataOutputStream output = new DataOutputStream(new FileOutputStream(tmpFile))) {
            List<ExplodedEntityData> data = getEntities(world);
            output.writeInt(data.size());
            for (ExplodedEntityData entity : data) {
                writeEntity(output, entity);
            }
        } catch (Exception e) {
            e.printStackTrace();
            tmpFile.delete();
            return;
        }

        try {
            Files.move(tmpFile.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeEntity(DataOutputStream output, ExplodedEntityData entity) throws IOException {
        output.writeInt(entity.getEntityType().ordinal());
        output.writeDouble(entity.getX());
        output.writeDouble(entity.getY());
        output.writeDouble(entity.getZ());
        output.writeFloat(entity.getPitch());
        output.writeFloat(entity.getYaw());
        output.writeLong(entity.getExplodedTime());

        byte[] entityData = entity.getNmsData();
        if (entityData == null) {
            output.writeBoolean(false);
        } else {
            output.writeBoolean(true);
            output.writeInt(entityData.length);
            output.write(entityData);
        }
    }

    public void addAll(World world, Collection<ExplodedBlockData> explodedBlockData) {
        getBlocks(world).addAll(explodedBlockData);
    }

    public void addEntity(World world, ExplodedEntityData explodedEntityData) {
        getEntities(world).add(explodedEntityData);
    }
}
