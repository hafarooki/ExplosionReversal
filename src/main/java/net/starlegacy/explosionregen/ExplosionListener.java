package net.starlegacy.explosionregen;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.Location;
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
import java.util.*;
import java.lang.Math;

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

        processBlockList(event.getEntity().getWorld(), event.getLocation(), event.blockList());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    void onBlockExplode(BlockExplodeEvent event) {
        processBlockList(event.getBlock().getWorld(), event.getBlock().getLocation(), event.blockList());
    }

    private void processBlockList(World world, Location explosionLocation, List<Block> list) {
        if (list.isEmpty()) {
            return;
        }

        List<ExplodedBlockData> explodedBlockDataList = new LinkedList<>();

        double eX = explosionLocation.getX(), eY = explosionLocation.getY(), eZ = explosionLocation.getZ();

        for (Iterator<Block> iterator = list.iterator(); iterator.hasNext(); ) {
            processBlock(explodedBlockDataList, eX, eY, eZ, iterator);
        }

        // if no blocks were handled by the plugin at all (for example, every block's type is ignored)
        if (explodedBlockDataList.isEmpty()) {
            return;
        }

        plugin.getWorldData().addAll(world, explodedBlockDataList);
    }

    private void processBlock(List<ExplodedBlockData> explodedBlockDataList, double eX, double eY, double eZ,
                              Iterator<Block> iterator) {
        Block block = iterator.next();
        BlockData blockData = block.getBlockData();

        if (ignoreMaterial(blockData.getMaterial())) {
            return;
        }

        int x = block.getX(), y = block.getY(), z = block.getZ();
        long explodedTime = getExplodedTime(eX, eY, eZ, x, y, z);

        @Nullable byte[] tileEntity = NMSUtils.getTileEntity(block);

        if (tileEntity != null) {
            processTileEntity(explodedBlockDataList, block, blockData, explodedTime);
        }


        ExplodedBlockData explodedBlockData = new ExplodedBlockData(x, y, z, explodedTime, blockData, tileEntity);
        explodedBlockDataList.add(explodedBlockData);

        // break the block manually
        iterator.remove();
        block.setType(Material.AIR, false);
    }

    private boolean ignoreMaterial(Material material) {
        Settings settings = plugin.getSettings();
        Set<Material> includedMaterials = settings.getIncludedMaterials();
        return material == Material.AIR ||
                settings.getIgnoredMaterials().contains(material) ||
                !includedMaterials.isEmpty() && !includedMaterials.contains(material);
    }

    private long getExplodedTime(double eX, double eY, double eZ, int x, int y, int z) {
        long now = System.currentTimeMillis();
        double distance = Math.abs(eX - x) + Math.abs(eY - y) + Math.abs(eZ - z);
        long offset = Math.round((16 - distance) * plugin.getSettings().getDistanceDelay() * 1000);
        return now + offset;
    }

    private void processTileEntity(List<ExplodedBlockData> explodedBlockDataList,
                                   Block block, BlockData blockData, long explodedTime) {
        BlockState state = block.getState();

        if (state instanceof InventoryHolder) {
            Inventory inventory = ((InventoryHolder) state).getInventory();
            // Double chests are weird so you have to get the state (as a holder)'s inventory's holder to cast to DoubleChest
            InventoryHolder inventoryHolder = inventory.getHolder();

            if (inventoryHolder instanceof DoubleChest) {
                processDoubleChest(explodedBlockDataList, (Chest) blockData, (DoubleChest) inventoryHolder, explodedTime);
            }

            inventory.clear();
        }
    }

    private void processDoubleChest(List<ExplodedBlockData> explodedBlockDataList, Chest blockData,
                                    DoubleChest doubleChest, long explodedTime) {
        boolean isRight = blockData.getType() == Chest.Type.RIGHT;
        InventoryHolder otherHolder = isRight ? doubleChest.getLeftSide() : doubleChest.getRightSide();
        if (otherHolder != null) {
            Inventory otherInventory = otherHolder.getInventory();
            Location otherInventoryLocation = Objects.requireNonNull(otherInventory.getLocation());
            Block other = otherInventoryLocation.getBlock();

            int otherX = other.getX(), otherY = other.getY(), otherZ = other.getZ();
            BlockData otherBlockData = other.getBlockData();
            byte[] otherTile = NMSUtils.getTileEntity(other);

            explodedBlockDataList.add(new ExplodedBlockData(otherX, otherY, otherZ, explodedTime, otherBlockData, otherTile));

            otherInventory.clear();
            other.setType(Material.AIR, false);
        }
    }
}
