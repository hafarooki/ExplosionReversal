package net.starlegacy.explosionregen;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.List;

public class Regeneration {
    private static LoadingCache<String, BlockData> blockDataCache = CacheBuilder.newBuilder().build(CacheLoader.from(Bukkit::createBlockData));

    public static void regenerate(ExplosionRegenPlugin plugin) {
        long millisecondDelay = (long) (plugin.getSettings().getRegenDelay() * 1_000L);
        long maxNanos = (long) (plugin.getSettings().getPlacementIntensity() * 1_000_000L);

        long start = System.nanoTime();

        for (World world : Bukkit.getWorlds()) {
            List<ExplodedBlockData> blocks = plugin.getWorldData().get(world);

            for (Iterator<ExplodedBlockData> iterator = blocks.iterator(); iterator.hasNext(); ) {
                if (System.nanoTime() - start > maxNanos) {
                    return;
                }

                ExplodedBlockData data = iterator.next();
                if (System.currentTimeMillis() - data.getExplodedTime() < millisecondDelay) {
                    continue;
                }

                iterator.remove();
                Block block = world.getBlockAt(data.getX(), data.getY(), data.getZ());
                BlockData blockData = blockDataCache.getUnchecked(data.getBlockDataString());
                block.setBlockData(blockData);

                @Nullable byte[] tileData = data.getTileData();
                if (tileData != null) {
                    NMSUtils.setTileEntity(block, tileData);
                }
            }
        }
    }
}
