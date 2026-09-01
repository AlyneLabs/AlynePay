package com.alynelabs.systm.mesh

import org.junit.Assert.*
import org.junit.Test
import java.nio.ByteBuffer

class MeshSystemTest {

    @Test
    fun testIPvXAddressSerialization() {
        val original = IPvXAddress(
            prefix = 0xFE80.toShort(),
            bearerClass = IPvXAddress.BEARER_BLE,
            nodeHashId = 0x123456789ABCDEF0L,
            interfaceIndex = 1
        )
        
        val bytes = original.toBytes()
        assertEquals(16, bytes.size)
        
        val decoded = IPvXAddress.fromBytes(bytes)
        assertEquals(original, decoded)
        assertEquals("FE80:0001:123456789ABCDEF0:00000001", decoded.toString())
    }

    @Test
    fun testMeshPacketSerialization() {
        val source = IPvXAddress(0, 1, 100, 0)
        val dest = IPvXAddress(0, 1, 200, 0)
        val payload = "Hello Mesh".toByteArray()
        val hopRoute = listOf(101L, 102L)
        
        val packet = MeshPacket(
            source = source,
            destination = dest,
            hopRoute = hopRoute,
            nextHopIndex = 0,
            payload = payload,
            type = MeshPacket.PacketType.DATA
        )
        
        val bytes = packet.toBytes()
        val decoded = MeshPacket.fromBytes(bytes)
        
        assertEquals(packet.source, decoded.source)
        assertEquals(packet.destination, decoded.destination)
        assertEquals(packet.hopRoute, decoded.hopRoute)
        assertArrayEquals(packet.payload, decoded.payload)
    }
}
