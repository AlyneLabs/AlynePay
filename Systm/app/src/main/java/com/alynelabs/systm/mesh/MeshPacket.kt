package com.alynelabs.systm.mesh

import android.util.Log
import java.nio.ByteBuffer

/**
 * Alyne Mesh Data Frame
 * Handles serialization of packets including the source-routed hop path.
 */
data class MeshPacket(
    val source: IPvXAddress,
    val destination: IPvXAddress,
    val hopRoute: List<Long>, // List of Node Hash IDs for source routing
    var nextHopIndex: Int,
    val payload: ByteArray,
    val type: PacketType = PacketType.DATA
) {
    enum class PacketType(val value: Byte) {
        DATA(0x01),
        CONTROL(0x02),
        PING(0x03),
        LSA(0x04)
    }

    fun toBytes(): ByteArray {
        val size = 16 + 16 + 1 + (hopRoute.size * 8) + 4 + payload.size + 1
        val buffer = ByteBuffer.allocate(size)
        
        buffer.put(type.value)
        buffer.put(source.toBytes())
        buffer.put(destination.toBytes())
        
        buffer.put(hopRoute.size.toByte())
        hopRoute.forEach { buffer.putLong(it) }
        
        buffer.putInt(nextHopIndex)
        buffer.put(payload)
        
        return buffer.array()
    }

    companion object {
        fun fromBytes(bytes: ByteArray): MeshPacket {
            try {
                val buffer = ByteBuffer.wrap(bytes)
                val typeValue = buffer.get()
                val type = PacketType.entries.find { it.value == typeValue } ?: PacketType.DATA
                
                val sourceBytes = ByteArray(16)
                if (buffer.remaining() < 16) throw Exception("Buffer too small for Source Address (Remaining: ${buffer.remaining()})")
                buffer.get(sourceBytes)
                val source = IPvXAddress.fromBytes(sourceBytes)
                
                val destBytes = ByteArray(16)
                if (buffer.remaining() < 16) throw Exception("Buffer too small for Destination Address (Remaining: ${buffer.remaining()})")
                buffer.get(destBytes)
                val destination = IPvXAddress.fromBytes(destBytes)
                
                val routeSize = buffer.get().toInt()
                val hopRoute = mutableListOf<Long>()
                if (buffer.remaining() < routeSize * 8) throw Exception("Buffer too small for Hop Route (RouteSize: $routeSize, Remaining: ${buffer.remaining()})")
                repeat(routeSize) { hopRoute.add(buffer.long) }
                
                if (buffer.remaining() < 4) throw Exception("Buffer too small for Next Hop Index")
                val nextHopIndex = buffer.getInt()
                
                val payloadSize = buffer.remaining()
                val payload = ByteArray(payloadSize)
                buffer.get(payload)
                
                return MeshPacket(source, destination, hopRoute, nextHopIndex, payload, type)
            } catch (e: Exception) {
                val hex = bytes.joinToString("") { "%02x".format(it) }
                Log.e("MeshPacket", "Deserialization failed: ${e.message}. Data: $hex")
                throw e
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MeshPacket) return false
        if (source != other.source) return false
        if (destination != other.destination) return false
        if (hopRoute != other.hopRoute) return false
        if (nextHopIndex != other.nextHopIndex) return false
        if (!payload.contentEquals(other.payload)) return false
        if (type != other.type) return false
        return true
    }

    override fun hashCode(): Int {
        var result = source.hashCode()
        result = 31 * result + destination.hashCode()
        result = 31 * result + hopRoute.hashCode()
        result = 31 * result + nextHopIndex
        result = 31 * result + payload.contentHashCode()
        result = 31 * result + type.hashCode()
        return result
    }
}
