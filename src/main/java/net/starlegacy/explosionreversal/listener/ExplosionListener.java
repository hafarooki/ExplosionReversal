package net.starlegacy.explosionreversal.listener;

import net.starlegacy.explosionreversal.ExplosionReversalPlugin;
import net.starlegacy.explosionreversal.Settings;
import net.starlegacy.explosionreversal.data.ExplodedBlockData;
import net.starlegacy.explosionreversal.nms.NMSUtils;
import org.bukkit.Location;
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
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import javax.annotation.Nullable;
import java.util.*;

public class ExplosionListener implements Listener {
    private final ExplosionReversalPlugin plugin;

    public ExplosionListener(ExplosionReversalPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    void onEntityExplode(EntityExplodeEvent event) {
        if (plugin.getSettings().getIgnoredEntityExplosions().contains(event.getEntity().getType())) {
            return;
        }

        processExplosion(event.getEntity().getWorld(), event.getLocation(), event.blockList());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    void onBlockExplode(BlockExplodeEvent event) {
        processExplosion(event.getBlock().getWorld(), event.getBlock().getLocation(), event.blockList());
    }

    private void processExplosion(World world, Location explosionLocation, List<Block> list) {
        if (plugin.getSettings().getIgnoredWorlds().contains(world.getName())) {
            return;
        }

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
        long explodedTime = plugin.getExplodedTime(eX, eY, eZ, x, y, z);

        @Nullable byte[] tileEntity = NMSUtils.getTileEntity(block);

        if (tileEntity != null) {
            processTileEntity(explodedBlockDataList, block, explodedTime);
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

    private void processTileEntity(List<ExplodedBlockData> explodedBlockDataList, Block block, long explodedTime) {
        BlockState state = block.getState();

        if (state instanceof InventoryHolder) {
            Inventory inventory = ((InventoryHolder) state).getInventory();
            // Double chests are weird so you have to get the state (as a holder)'s inventory's holder to cast to DoubleChest
            InventoryHolder inventoryHolder = inventory.getHolder();

            if (inventoryHolder instanceof DoubleChest) {
                boolean isRight = ((Chest) block.getBlockData()).getType() == Chest.Type.RIGHT;
                DoubleChest doubleChest = (DoubleChest) inventoryHolder;
                processDoubleChest(explodedBlockDataList, isRight, doubleChest, explodedTime);
            }

            inventory.clear();
        }
    }

    private void processDoubleChest(List<ExplodedBlockData> explodedBlockDataList, boolean isRight,
                                    DoubleChest doubleChest, long explodedTime) {
        DoubleChestInventory inventory = (DoubleChestInventory) doubleChest.getInventory();
        Inventory otherInventory = isRight ? inventory.getRightSide() : inventory.getLeftSide();
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
