package com.alynelabs.systm.mesh

import java.nio.ByteBuffer

/**
 * Multi-IPvX Hierarchical Addressing Engine
 * Structure: [Prefix (16b)] | [Bearer Class (16b)] | [Node Hash ID (64b)] | [Interface Index (32b)]
 */
data class IPvXAddress(
    val prefix: Short,
    val bearerClass: Short,
    val nodeHashId: Long,
    val interfaceIndex: Int
) {
    companion object {
        const val BEARER_BLE: Short = 0x0001
        const val BEARER_WIFI: Short = 0x0002
        const val BEARER_IP: Short = 0x0003

        fun fromBytes(bytes: ByteArray): IPvXAddress {
            require(bytes.size == 16) { "IPvX address must be 128-bit (16 bytes)" }
            val buffer = ByteBuffer.wrap(bytes)
            return IPvXAddress(
                prefix = buffer.short,
                bearerClass = buffer.short,
                nodeHashId = buffer.long,
                interfaceIndex = buffer.int
            )
        }
    }

    fun toBytes(): ByteArray {
        return ByteBuffer.allocate(16)
            .putShort(prefix)
            .putShort(bearerClass)
            .putLong(nodeHashId)
            .putInt(interfaceIndex)
            .array()
    }

    override fun toString(): String {
        val p = String.format("%04X", prefix)
        val b = String.format("%04X", bearerClass)
        val n = String.format("%016X", nodeHashId)
        val i = String.format("%08X", interfaceIndex)
        return "$p:$b:$n:$i"
    }
}
