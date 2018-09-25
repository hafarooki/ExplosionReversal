package com.miclesworkshop.explosionregen

import com.google.gson.TypeAdapter
import com.google.gson.annotations.JsonAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import org.bukkit.Bukkit
import org.bukkit.block.data.BlockData
import java.util.*


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