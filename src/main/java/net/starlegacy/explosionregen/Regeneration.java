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

    public static int regenerate(ExplosionRegenPlugin plugin, boolean instant) {
        long millisecondDelay = (long) (plugin.getSettings().getRegenDelay() * 60L * 1_000L);
        long maxNanos = (long) (plugin.getSettings().getPlacementIntensity() * 1_000_000L);

        long start = System.nanoTime();

        int regenerated = 0;

        for (World world : Bukkit.getWorlds()) {
            List<ExplodedBlockData> blocks = plugin.getWorldData().get(world);

            for (Iterator<ExplodedBlockData> iterator = blocks.iterator(); iterator.hasNext(); ) {
                if (!instant && System.nanoTime() - start > maxNanos) {
                    return regenerated;
                }

                ExplodedBlockData data = iterator.next();
                if (!instant && System.currentTimeMillis() - data.getExplodedTime() < millisecondDelay) {
                    continue;
                }

                iterator.remove();
                Block block = world.getBlockAt(data.getX(), data.getY(), data.getZ());
                BlockData blockData = data.getBlockData();
                block.setBlockData(blockData, false);

                @Nullable byte[] tileData = data.getTileData();
                if (tileData != null) {
                    NMSUtils.setTileEntity(block, tileData);
                }

                regenerated++;
            }
        }

        return regenerated;
    }
}
