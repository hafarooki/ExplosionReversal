@file:Suppress("UnstableApiUsage")

package com.miclesworkshop.explosionregen

import com.google.common.io.ByteStreams.newDataInput
import com.google.common.io.ByteStreams.newDataOutput
import net.minecraft.server.v1_13_R2.BlockPosition
import net.minecraft.server.v1_13_R2.NBTReadLimiter
import net.minecraft.server.v1_13_R2.NBTTagCompound
import net.minecraft.server.v1_13_R2.TileEntity
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.block.BlockState
import org.bukkit.block.DoubleChest
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.craftbukkit.v1_13_R2.CraftWorld
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockExplodeEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.world.WorldLoadEvent
import org.bukkit.inventory.DoubleChestInventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.plugin.java.JavaPlugin
import java.lang.System.currentTimeMillis
import java.lang.System.nanoTime
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@Suppress("unused")
class ExplosionRegenPlugin : JavaPlugin(), Listener {
    private val thread = Executors.newSingleThreadExecutor()


    private lateinit var settings: ExplosionRegenSettings

    override fun onEnable() {
        ExplodedBlockData.loadAll()
        server.pluginManager.registerEvents(this, this)
        loadSettings()
        scheduleTasks()
    }

    private fun loadSettings() {
        saveDefaultConfig()
        config.options().copyDefaults(true)
        saveConfig()
        settings = ExplosionRegenSettings(config)

        // overwrite config to update comments
        saveResource("config.yml", true)
        reloadConfig()
        settings.save(config)
        saveConfig()
    }

    private fun scheduleTasks() {
        val regenTimeMillis: Long = (settings.regenDelay * 60 * 1000).toLong()
        val placementLimitNanos: Long = TimeUnit.MILLISECONDS.toNanos(settings.placementIntensity.toLong())

        // process regeneration queue every 5 milliseconds
        server.scheduler.runTaskTimer(this, {
            ExplodedBlockData.queues.forEach { world: World, queue: ConcurrentLinkedQueue<ExplodedBlock> ->
                processRegeneration(world, queue, placementLimitNanos, regenTimeMillis)
            }
        }, 5, 5)

        // save every 20 seconds
        server.scheduler.runTaskTimerAsynchronously(this, ExplodedBlockData::saveAll, 20 * 60, 20 * 60)
    }

    override fun onDisable() = ExplodedBlockData.saveAll()

    private fun processRegeneration(world: World, queue: ConcurrentLinkedQueue<ExplodedBlock>, timeoutNanos: Long, regenTimeMillis: Long) {
        val nmsWorld = (world as CraftWorld).handle
        val start = nanoTime()

        while (nanoTime() - start < timeoutNanos) { // timeout so not too much time is spent placing blocks
            val explodedBlock = queue.peek() ?: break
            if (currentTimeMillis() - explodedBlock.born < regenTimeMillis) break
            val x = explodedBlock.x
            val y = explodedBlock.y
            val z = explodedBlock.z
            world.getBlockAt(x, y, z).setBlockData(explodedBlock.blockData, false)
            explodedBlock.tile.toTileEntity()?.let { nmsWorld.setTileEntity(BlockPosition(x, y, z), it) }
            queue.remove()
        }
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        when (command.name) {
            "regen" -> ExplodedBlockData.queues.forEach { world: World, queue: ConcurrentLinkedQueue<ExplodedBlock> ->
                processRegeneration(world, queue, TimeUnit.SECONDS.toNanos(15), regenTimeMillis = 0)
            }
        }
        return true
    }

    @EventHandler
    fun onWorldLoad(event: WorldLoadEvent) = ExplodedBlockData.load(event.world)

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onBlockExplode(event: BlockExplodeEvent) = onExplode(event.blockList())

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onEntityExplode(event: EntityExplodeEvent) {
        if (!settings.ignoredEntities.contains(event.entityType)) {
            onExplode(event.blockList())
        }
    }

    private fun onExplode(blocks: MutableList<Block>) {
        // put block data in
        blocks.forEach { block: Block ->
            val world = block.world
            val data = block.blockData
            val x = block.x
            val y = block.y
            val z = block.z
            val tile = getTileEntityData(world, x, y, z)

            val explodedBlock = ExplodedBlock(
                    x, y, z,
                    data, tile,
                    currentTimeMillis()
            )

            thread.submit {
                val queue = ExplodedBlockData.getQueue(world)

                if (queue.none { explodedBlock.run { it.x == x && it.y == y && it.z == z } }) {
                    queue.offer(explodedBlock)
                }
            }
        }

        blocks.forEach { block: Block ->
            val state = block.state

            // clear inventories so they don't drop items

            if (state is DoubleChest) {
                val inventory = state.inventory as DoubleChestInventory
                (if ((state.rightSide as BlockState).location == block.location) {
                    inventory.rightSide
                } else {
                    inventory.leftSide
                }).clear()
            } else (state as? InventoryHolder)?.inventory?.clear()
        }

        blocks.forEach { it.setType(Material.AIR, false) }

        blocks.clear()
    }

    private fun getTileEntityData(world: World, x: Int, y: Int, z: Int): ByteArray? {
        return (world as CraftWorld).handle.getTileEntity(BlockPosition(x, y, z))?.toBytes()
    }

    private fun TileEntity?.toBytes(): ByteArray? = this?.let {
        val nbt = NBTTagCompound()
        it.save(nbt)
        return@let newDataOutput().apply(nbt::write).toByteArray()
    }

    private fun ByteArray?.toTileEntity(): TileEntity? = this?.let { bytes ->
        val nbt = NBTTagCompound()
        newDataInput(bytes).let { nbt.load(it, 0, NBTReadLimiter.a) }
        return@let TileEntity.create(nbt)
    }
}