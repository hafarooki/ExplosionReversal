package net.starlegacy.explosionregen;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Chest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.LinkedList;
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

        List<ExplodedBlockData> explodedBlockDataList = new LinkedList<>();

        for (Iterator<Block> iterator = list.iterator(); iterator.hasNext(); ) {
            Block block = iterator.next();
            BlockData blockData = block.getBlockData();

            if (plugin.getSettings().getIgnoredMaterials().contains(blockData.getMaterial())) {
                continue;
            }

            @Nullable byte[] tileEntity = NMSUtils.getTileEntity(block);

            int x = block.getX();
            int y = block.getY();
            int z = block.getZ();

            String string = blockData.getAsString(true); // true for hideUnspecified, to reduce unnecessary size

            long now = System.currentTimeMillis();
            ExplodedBlockData explodedBlockData = new ExplodedBlockData(x, y, z, now, string, tileEntity);
            explodedBlockDataList.add(explodedBlockData);
            iterator.remove();

            if (tileEntity != null) {
                BlockState state = block.getState();

                if (state instanceof InventoryHolder) {
                    Inventory inventory = ((InventoryHolder) state).getInventory();
                    // Double chests are weird so you have to get the state (as a holder)'s inventory's holder to cast to DoubleChest
                    InventoryHolder inventoryHolder = inventory.getHolder();

                    if (inventoryHolder instanceof DoubleChest) {
                        DoubleChest doubleChest = (DoubleChest) inventoryHolder;
                        boolean isRight = ((Chest) blockData).getType() == Chest.Type.RIGHT;
                        Inventory otherInventory = (isRight ? doubleChest.getLeftSide() : doubleChest.getRightSide()).getInventory();
                        Block other = otherInventory.getLocation().getBlock();
                        int otherX = other.getX();
                        int otherY = other.getY();
                        int otherZ = other.getZ();
                        String otherString = other.getBlockData().getAsString(true);
                        byte[] otherTile = NMSUtils.getTileEntity(other);
                        explodedBlockDataList.add(new ExplodedBlockData(otherX, otherY, otherZ, now, otherString, otherTile));

                        otherInventory.clear();
                        other.setType(Material.AIR, false);
                    }

                    inventory.clear();
                }
            }

            block.setType(Material.AIR, false);

        }

        // if no blocks were handled by the plugin at all (for example, every block's type is ignored)
        if (explodedBlockDataList.isEmpty()) {
            return;
        }

        plugin.getWorldData().addAll(world, explodedBlockDataList);
    }
}
