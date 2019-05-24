package net.starlegacy.explosionregen;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.io.Files;
import org.bukkit.World;

import java.io.*;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class WorldData {
    private LoadingCache<World, List<ExplodedBlockData>> explodedBlocks = CacheBuilder.newBuilder()
            .weakKeys()
            .build(CacheLoader.from(this::load));

    private File getFile(World world) {
        return new File(world.getWorldFolder(), "data/explosionregen/explodedblocks.dat");
    }

    public List<ExplodedBlockData> get(World world) {
        return explodedBlocks.getUnchecked(world);
    }

    private List<ExplodedBlockData> load(World world) {
        File file = getFile(world);

        if (!file.exists()) {
            return new LinkedList<>();
        }

        boolean errorOccured = false;

        try (DataInputStream input = new DataInputStream(new FileInputStream(file))) {
            List<ExplodedBlockData> blocks = new LinkedList<>();

            int blockCount = input.readInt();
            for (int i = 0; i < blockCount; i++) {
                try {
                    int x = input.readInt();
                    int y = input.readInt();
                    int z = input.readInt();

                    long explodedTime = input.readLong();

                    byte[] blockDataBytes = new byte[input.readInt()];
                    input.read(blockDataBytes);
                    String blockDataString = new String(blockDataBytes);

                    byte[] tileEntityData = null;

                    if (input.readBoolean()) {
                        int tileEntitySize = input.readInt();
                        tileEntityData = new byte[tileEntitySize];
                        input.read(tileEntityData);
                    }

                    blocks.add(new ExplodedBlockData(x, y, z, explodedTime, blockDataString, tileEntityData));
                } catch (Exception exception) {
                    // if something goes wrong with a block, skip it but print the error,
                    // and flag that we need to save a backup copy of this file,
                    // in case data recovery is necessary later
                    exception.printStackTrace();
                    errorOccured = true;
                }
            }

            return blocks;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (errorOccured) {
                file.renameTo(new File(file.getAbsolutePath() + "_broken_" + System.currentTimeMillis() % 1000));
                save(world);
            }
        }
    }

    private void save(World world) {
        List<ExplodedBlockData> data = get(world);

        File file = getFile(world);
        file.getParentFile().mkdirs();

        File tmpFile = new File(file.getAbsolutePath() + "_tmp");

        try (DataOutputStream output = new DataOutputStream(new FileOutputStream(tmpFile))) {
            for (ExplodedBlockData block : data) {
                output.writeInt(block.getX());
                output.writeInt(block.getY());
                output.writeInt(block.getZ());

                output.writeLong(block.getExplodedTime());

                String blockDataString = block.getBlockDataString();
                output.writeInt(blockDataString.length());
                output.writeBytes(blockDataString);

                byte[] tileData = block.getTileData();
                if (tileData == null) {
                    output.writeBoolean(false);
                } else {
                    output.writeBoolean(true);
                    output.writeInt(tileData.length);
                    output.write(tileData);
                }
            }

            Files.move(tmpFile, file);
        } catch (IOException e) {
            e.printStackTrace();
            tmpFile.delete();
        }
    }

    public void addAll(World world, Collection<ExplodedBlockData> explodedBlockData) {
        get(world).addAll(explodedBlockData);
    }
}
