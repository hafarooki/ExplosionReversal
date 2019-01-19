package net.starlegacy.explosionregen;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

class ExplosionListener implements Listener {
    private ExplosionRegenPlugin plugin;

    ExplosionListener(ExplosionRegenPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    void onEntityExplode(EntityExplodeEvent event) {
        if (plugin.getSettings().getIgnoredEntities().contains(event.getEntityType())) {
            return;
        }

        processBlockList(event.getEntity().getWorld(), event.blockList());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    void onBlockExplode(BlockExplodeEvent event) {
        processBlockList(event.getBlock().getWorld(), event.blockList());
    }

    private void processBlockList(World world, List<Block> list) {
        if (list.isEmpty()) {
            return;
        }

        List<ExplodedBlockData> explodedBlockDataList = new ArrayList<>();

        for (Iterator<Block> iterator = list.iterator(); iterator.hasNext(); ) {
            Block block = iterator.next();
            BlockData blockData = block.getBlockData();

            if (plugin.getSettings().getIgnoredMaterials().contains(blockData.getMaterial())) {
                continue;
            }

            // TODO: get tile entity data

            int x = block.getX();
            int y = block.getY();
            int z = block.getZ();
            explodedBlockDataList.add(new ExplodedBlockData(x, y, z, blockData, null));
            iterator.remove();
        }

        // if no blocks were handled by the plugin at all (for example, every block's type is ignored)
        if (explodedBlockDataList.isEmpty()) {
            return;
        }

        plugin.getWorldData().addAll(world, explodedBlockDataList);
    }
}
