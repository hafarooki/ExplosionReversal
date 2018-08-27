package net.starlegacy.explosionregen

import com.google.common.io.ByteStreams.newDataInput
import com.google.common.io.ByteStreams.newDataOutput
import com.google.gson.Gson
import com.google.gson.TypeAdapter
import com.google.gson.annotations.JsonAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import net.minecraft.server.v1_13_R1.BlockPosition
import net.minecraft.server.v1_13_R1.NBTReadLimiter
import net.minecraft.server.v1_13_R1.NBTTagCompound
import net.minecraft.server.v1_13_R1.TileEntity
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.block.BlockState
import org.bukkit.block.DoubleChest
import org.bukkit.block.data.BlockData
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.craftbukkit.v1_13_R1.CraftWorld
import org.bukkit.craftbukkit.v1_13_R1.block.CraftBlock
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockExplodeEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.world.WorldLoadEvent
import org.bukkit.inventory.DoubleChestInventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.plugin.java.JavaPlugin
import java.io.*
import java.lang.System.currentTimeMillis
import java.lang.System.nanoTime
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream



@Suppress("unused")
class ExplosionRegen : JavaPlugin(), Listener {
    private val thread = Executors.newSingleThreadExecutor()

    private val queues = mutableMapOf<World, ConcurrentLinkedQueue<ExplodedBlock>>()

    private fun World.getQueue() = queues.getOrPut(this) { ConcurrentLinkedQueue() }

    override fun onEnable() {
        loadAll()
        server.pluginManager.registerEvents(this, this)
        saveDefaultConfig()
        val regenTimeMillis = (config.getDouble("regenDelay") / 60 / 1000).toLong()
        val placementLimitNanos = TimeUnit.MILLISECONDS.toNanos(config.getInt("placementIntensity").toLong())
        server.scheduler.runTaskTimer(this, {
            queues.forEach { world, queue -> processRegeneration(world, queue, placementLimitNanos, regenTimeMillis) }
        }, 5, 5)
        server.scheduler.runTaskTimerAsynchronously(this, this::saveAll, 20 * 60, 20 * 60)
    }

    override fun onDisable() = saveAll()

    private fun loadAll() = Bukkit.getWorlds().forEach(::load)

    private fun saveAll() = queues.keys.forEach(::save)

    private data class ExplodedBlockList(val blocks: List<ExplodedBlock>)

    private fun load(world: World) {
        val file = world.explosionDataFile
        val queue = world.getQueue()
        if (file.exists()) InputStreamReader(GZIPInputStream(FileInputStream(file))).use { reader ->
            val blocks = Gson().fromJson(reader, ExplodedBlockList::class.java).blocks
            blocks.sortedBy { it.born }.forEach { queue.offer(it) }
        } else {
            file.parentFile.mkdirs()
            file.createNewFile()
            save(world)
        }
    }

    private fun save(world: World) = OutputStreamWriter(GZIPOutputStream(FileOutputStream(world.explosionDataFile))).use { writer ->
        Gson().toJson(ExplodedBlockList(world.getQueue().toList()), writer)
    }

    private fun processRegeneration(world: World, queue: ConcurrentLinkedQueue<ExplodedBlock>, timeoutNanos: Long, regenTimeMillis: Long) {
        val nmsWorld = (world as CraftWorld).handle
        val start = nanoTime()
        while (nanoTime() - start < timeoutNanos) {
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
            "regen" -> queues.forEach { world, queue -> processRegeneration(world, queue, TimeUnit.SECONDS.toNanos(15), 0) }
        }
        return true
    }

    @EventHandler
    fun onWorldLoad(event: WorldLoadEvent) = load(event.world)

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onBlockExplode(event: BlockExplodeEvent) = onExplode(event.blockList())

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onEntityExplode(event: EntityExplodeEvent) = onExplode(event.blockList())

    private fun onExplode(blocks: MutableList<Block>) {
        blocks.map { it as CraftBlock }.forEach { block ->
            val world = block.craftWorld
            val data = block.blockData
            val x = block.x
            val y = block.y
            val z = block.z
            val tile = world.handle.getTileEntity(BlockPosition(x, y, z))

            val explodedBlock = ExplodedBlock(
                    x, y, z,
                    data, tile.toBytes(),
                    currentTimeMillis()
            )

            thread.submit {
                val queue = world.getQueue()
                if (queue.none { explodedBlock.run { it.x == x && it.y == y && it.z == z } }) {
                    queue.offer(explodedBlock)
                }
            }
        }
        blocks.forEach {
            val state = it.getState(false)
            if (state is DoubleChest) {
                val inventory = state.inventory as DoubleChestInventory
                (if ((state.rightSide as BlockState).location == it.location) inventory.rightSide else inventory.leftSide).clear()
            } else (state as? InventoryHolder)?.inventory?.clear()
        }

        blocks.forEach { it.setType(Material.AIR, false) }

        blocks.clear()
    }

    private fun TileEntity?.toBytes() = this?.let {
        val nbt = NBTTagCompound()
        it.save(nbt)
        newDataOutput().apply(nbt::write).toByteArray()
    }

    private fun ByteArray?.toTileEntity() = this?.let { bytes ->
        val nbt = NBTTagCompound()
        newDataInput(bytes).let { nbt.load(it, 0, NBTReadLimiter.a) }
        TileEntity.create(nbt)
    }

    private val World.explosionDataFile get() = File(worldFolder, "data/explosionregen/explodedblocks.dat")

    @JsonAdapter(ExplodedBlockAdapter::class)
    data class ExplodedBlock(val x: Int, val y: Int, val z: Int, val blockData: BlockData, val tile: ByteArray?, val born: Long) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as ExplodedBlock

            if (x != other.x) return false
            if (y != other.y) return false
            if (z != other.z) return false
            if (blockData != other.blockData) return false
            if (!Arrays.equals(tile, other.tile)) return false
            if (born != other.born) return false

            return true
        }

        override fun hashCode() = x xor (z shl 12) xor (y shl 24)
    }

    class ExplodedBlockAdapter : TypeAdapter<ExplodedBlock>() {
        override fun write(writer: JsonWriter, value: ExplodedBlock) {
            writer.beginObject()
            writer.name("x").value(value.x)
            writer.name("y").value(value.y)
            writer.name("z").value(value.z)
            writer.name("data").value(value.blockData.asString)
            value.tile?.let {
                writer.name("tile").value(Base64.getEncoder().encodeToString(it))
            }
            writer.name("born").value(value.born)
            writer.endObject()
        }

        override fun read(reader: JsonReader): ExplodedBlock {
            reader.beginObject()
            var x: Int? = null
            var y: Int? = null
            var z: Int? = null
            var blockData: BlockData? = null
            var tile: ByteArray? = null
            var born: Long? = null
            while (reader.hasNext()) when (reader.nextName()) {
                "x" -> x = reader.nextInt()
                "y" -> y = reader.nextInt()
                "z" -> z = reader.nextInt()
                "data" -> blockData = Bukkit.createBlockData(reader.nextString())
                "tile" -> tile = Base64.getDecoder().decode(reader.nextString())
                "born" -> born = reader.nextLong()
            }
            reader.endObject()
            return ExplodedBlock(x!!, y!!, z!!, blockData!!, tile, born!!)
        }
    }
}