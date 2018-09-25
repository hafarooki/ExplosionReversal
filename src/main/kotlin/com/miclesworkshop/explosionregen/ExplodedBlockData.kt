package com.miclesworkshop.explosionregen

import com.google.gson.Gson
import org.bukkit.Bukkit
import org.bukkit.World
import java.io.*
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

object ExplodedBlockData {
    val queues = mutableMapOf<World, ConcurrentLinkedQueue<ExplodedBlock>>()

    fun getQueue(world: World) = queues.getOrPut(world) { ConcurrentLinkedQueue() }

    fun loadAll() = Bukkit.getWorlds().forEach(::load)

    fun saveAll() = queues.keys.forEach(::save)

    private data class ExplodedBlockList(val blocks: List<ExplodedBlock>)

    private val World.explosionDataFile get() = File(worldFolder, "data/explosionregen/explodedblocks.dat")

    fun load(world: World) {
        val file = world.explosionDataFile
        val queue = getQueue(world)

        if (file.exists()) InputStreamReader(GZIPInputStream(FileInputStream(file))).use { reader ->
            val blocks = Gson().fromJson(reader, ExplodedBlockList::class.java).blocks
            blocks.sortedBy { it.born }.forEach { queue.offer(it) }
        } else {
            file.parentFile.mkdirs()
            file.createNewFile()
            save(world)
        }
    }

    private fun save(world: World) = OutputStreamWriter(GZIPOutputStream(FileOutputStream(world.explosionDataFile)))
            .use { Gson().toJson(ExplodedBlockList(getQueue(world).toList()), it) }
}