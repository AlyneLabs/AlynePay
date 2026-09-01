package com.alynelabs.systm.mesh

import android.util.Log
import com.alynelabs.systm.BleModule
import com.alynelabs.systm.InternetModule
import com.alynelabs.systm.WifiModule
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.PriorityQueue
import java.util.concurrent.ConcurrentHashMap

/**
 * Core Mesh & Routing Protocol Engine
 * Manages adjacency matrix and calculates paths using Dijkstra's algorithm.
 */
class MeshManager(
    val identity: NodeIdentity,
    private val ble: BleModule,
    private val wifi: WifiModule,
    private val internet: InternetModule
) {
    private val TAG = "MeshManager"
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    // Adjacency Matrix: Map<SourceNodeId, Map<NeighborNodeId, LinkWeight>>
    private val adjacencyTable = ConcurrentHashMap<Long, MutableMap<Long, Double>>()
    
    // Neighbor Metrics: Map<NodeId, Map<BearerType, MetricData>>
    private val neighborMetrics = ConcurrentHashMap<Long, MutableMap<Short, LinkMetrics>>()

    data class LinkMetrics(
        val rssi: Int = -100,
        val rtt: Long = 1000,
        val lossRate: Double = 0.0,
        val bearerType: Short
    )

    // Node ID to current known best IPvX mapping
    private val nodeToAddress = ConcurrentHashMap<Long, IPvXAddress>()
    
    // Node ID to Physical Hardware Address (MAC or IP)
    private val nodeToHardwareAddress = ConcurrentHashMap<Long, String>()

    private var is3NodeTestTriggered = java.util.concurrent.atomic.AtomicBoolean(false)

    // Exposed Mesh Topology for UI
    private val _meshTopology = MutableStateFlow<Map<Long, List<Long>>>(emptyMap())
    val meshTopology: StateFlow<Map<Long, List<Long>>> = _meshTopology.asStateFlow()

    // Payment callback for local IPC / AlynePay
    var onPaymentReceivedListener: ((org.json.JSONObject) -> Unit)? = null

    init {
        Log.i(TAG, "MeshManager Initialized for Node: ${identity.nodeId}")
        ble.localNodeId = identity.nodeId
        
        // 1. Map self node for topology
        updateTopology()

        // 2. Observe discovered devices from BLE
        ble.discoveredDevices.onEach { devices ->
            devices.forEach { device ->
                // Derive a stable Long ID from MAC address for visualization if NodeID not yet known
                val macLong = device.address.replace(":", "").toLong(16)
                // We use a high weight until we get real metrics/LSA
                updateNeighborMetric(macLong, LinkMetrics(bearerType = IPvXAddress.BEARER_BLE))
            }
            // Rapidly build topology on discovery
            if (devices.isNotEmpty()) {
                broadcastLSA()
                
                // --- AUTO TEST TRIGGER ---
                // If we are Phone A and we see Phone B, trigger test after a short delay
                val phoneA_Id = -2472472724180024548L
                val phoneB_Id = -8824097300144855975L
                val phoneC_Id = -9015913220544022712L
                
                if (identity.nodeId == phoneA_Id && nodeToAddress.containsKey(phoneB_Id) && is3NodeTestTriggered.compareAndSet(false, true)) {
                    scope.launch {
                        delay(10000)
                        Log.i(TAG, "[AUTO-TEST] Discovery stable. Triggering 3-node hop...")
                        start3NodeHopTest(phoneC_Id, phoneB_Id)
                    }
                }
            }
        }.launchIn(scope)

        // 3. Observe data from all modules
        ble.backendDataWithAddress.onEach { (hwAddr, data) -> handleIncomingRaw(data, hwAddr, IPvXAddress.BEARER_BLE) }.launchIn(scope)
        wifi.backendDataWithAddress.onEach { (hwAddr, data) -> handleIncomingRaw(data, hwAddr, IPvXAddress.BEARER_WIFI) }.launchIn(scope)
        internet.backendDataWithAddress.onEach { (hwAddr, data) -> handleIncomingRaw(data, hwAddr, IPvXAddress.BEARER_IP) }.launchIn(scope)

        // Periodically broadcast LSA (Link State Advertisement)
        scope.launch {
            while (isActive) {
                broadcastLSA()
                delay(5000)
            }
        }
    }

    fun startAll() {
        Log.i(TAG, "Mesh System Starting...")
        ble.startRadio()
        ble.startAdvertising("Systm-${identity.nodeId}")
        wifi.startRadio()
        internet.startRadio()
    }

    fun stopAll() {
        Log.i(TAG, "Mesh System Stopping...")
        ble.stopRadio()
        wifi.stopRadio()
        internet.stopRadio()
    }

    fun selfTest(): String {
        return "Node ID: ${identity.nodeId} | Active: ${ble.isConnected.value}"
    }

    fun runHoppingTest() {
        runDiagnosticHoppingTest()
    }

    fun start3NodeHopTest(targetNodeId: Long, relayNodeId: Long) {
        scope.launch {
            Log.i(TAG, "--- Starting Real 3-Node Hop Test ---")
            Log.i(TAG, "Source: ${identity.nodeId} -> Relay: $relayNodeId -> Target: $targetNodeId")
            
            val targetAddr = IPvXAddress(0x10FD, IPvXAddress.BEARER_BLE, targetNodeId, 0)
            
            val packet = MeshPacket(
                source = IPvXAddress(0, IPvXAddress.BEARER_IP, identity.nodeId, 0),
                destination = targetAddr,
                hopRoute = listOf(relayNodeId, targetNodeId),
                nextHopIndex = 0,
                payload = "REAL_3_NODE_HOP_SUCCESS".toByteArray(),
                type = MeshPacket.PacketType.DATA
            )

            val relayAddr = nodeToAddress[relayNodeId]
            if (relayAddr != null) {
                Log.i(TAG, "[TX] Dispatching packet to relay $relayNodeId...")
                sendToNode(relayNodeId, packet)
            } else {
                Log.e(TAG, "[TX] Cannot start test: Relay node $relayNodeId not connected/known.")
            }
        }
    }

    private fun runDiagnosticHoppingTest() {
        scope.launch {
            Log.i(TAG, "--- Starting Multi-Hop Relay Diagnostic ---")
            
            val sourceId = 1111111111L
            val targetId = 9999999999L
            val sourceAddr = IPvXAddress(0x10FD, IPvXAddress.BEARER_BLE, sourceId, 0)
            val targetAddr = IPvXAddress(0x10FD, IPvXAddress.BEARER_BLE, targetId, 0)
            
            nodeToAddress[targetId] = targetAddr
            updateLinkWeight(identity.nodeId, targetId, 2.0)
            
            val packet = MeshPacket(
                source = sourceAddr,
                destination = targetAddr,
                hopRoute = listOf(identity.nodeId, targetId),
                nextHopIndex = 0,
                payload = "MESSAGE_FOR_HOP_3".toByteArray(),
                type = MeshPacket.PacketType.DATA
            )

            Log.i(TAG, "[TEST] Simulating incoming packet from $sourceId for target $targetId...")
            delay(1500)
            handleIncomingRaw(packet.toBytes())
            Log.i(TAG, "--- Multi-Hop Relay Diagnostic Complete ---")
        }
    }

    private fun handleIncomingRaw(data: ByteArray, hwAddress: String? = null, bearerType: Short? = null) {
        try {
            val packet = MeshPacket.fromBytes(data)
            val myId = identity.nodeId
            
            // CRITICAL: Always refresh hardware mapping on every packet
            if (hwAddress != null) {
                val sourceId = packet.source.nodeHashId
                if (nodeToHardwareAddress[sourceId] != hwAddress) {
                    Log.i(TAG, "[MAP] Updating Hardware Mapping: Node $sourceId -> $hwAddress")
                    nodeToHardwareAddress[sourceId] = hwAddress
                }
                // Store the IPvX mapping, but override the bearerClass with what actually delivered it
                val DeliverableAddr = if (bearerType != null) {
                    packet.source.copy(bearerClass = bearerType)
                } else {
                    packet.source
                }
                if (nodeToAddress[sourceId] != DeliverableAddr) {
                    Log.i(TAG, "[MAP] Updating Address Mapping for Node $sourceId: $DeliverableAddr (Via Bearer: $bearerType)")
                    nodeToAddress[sourceId] = DeliverableAddr
                }
            }
            
            Log.d(TAG, "[MESH] Packet Rx: From=${packet.source.nodeHashId} To=${packet.destination.nodeHashId} Type=${packet.type} (Size: ${data.size})")

            if (packet.destination.nodeHashId == myId || packet.destination.nodeHashId == 0L) {
                handleConsumedPacket(packet)
            } else {
                Log.i(TAG, "[HOP] Relaying packet for node ${packet.destination.nodeHashId}")
                relayPacket(packet)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to process mesh packet: ${e.message}")
        }
    }

    private fun handleConsumedPacket(packet: MeshPacket) {
        when (packet.type) {
            MeshPacket.PacketType.LSA -> handleLSA(packet)
            MeshPacket.PacketType.DATA -> {
                val payloadStr = String(packet.payload, Charsets.UTF_8)
                Log.i(TAG, "[CONSUME] Data from ${packet.source.nodeHashId}: $payloadStr")
                try {
                    val json = org.json.JSONObject(payloadStr)
                    if (json.optString("type") == "PAYMENT") {
                        Log.i(TAG, "[PAYMENT RECEIVED] Dispatched from node ${packet.source.nodeHashId}")
                        onPaymentReceivedListener?.invoke(json)
                    }
                } catch (e: Exception) {
                    Log.d(TAG, "Data payload is not JSON payment: ${e.message}")
                }
            }
            else -> {}
        }
    }

    fun sendPaymentData(targetNodeId: Long, payloadJson: String): Boolean {
        return try {
            val targetAddr = nodeToAddress[targetNodeId] ?: IPvXAddress(0x10FD, IPvXAddress.BEARER_BLE, targetNodeId, 0)
            val path = calculatePath(targetNodeId) ?: listOf(targetNodeId)
            val packet = MeshPacket(
                source = IPvXAddress(0, IPvXAddress.BEARER_IP, identity.nodeId, 0),
                destination = targetAddr,
                hopRoute = path,
                nextHopIndex = 0,
                payload = payloadJson.toByteArray(Charsets.UTF_8),
                type = MeshPacket.PacketType.DATA
            )

            val nextHop = if (path.isNotEmpty()) path[0] else targetNodeId
            sendToNode(nextHop, packet)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send payment packet: ${e.message}")
            false
        }
    }

    private fun calculateLinkWeight(metrics: LinkMetrics): Double {
        val w1 = 0.4
        val w2 = 10.0
        val w3 = 50.0
        val bearerPenalty = when (metrics.bearerType) {
            IPvXAddress.BEARER_BLE -> 50.0
            IPvXAddress.BEARER_WIFI -> 10.0
            IPvXAddress.BEARER_IP -> 0.0
            else -> 100.0
        }
        val rssiFactor = (100.0 + metrics.rssi) / 10.0
        val invertedRssi = if (rssiFactor > 0) 1.0 / rssiFactor else 10.0
        return (w1 * metrics.rtt) + (w2 * invertedRssi) + (w3 * metrics.lossRate) + bearerPenalty
    }

    fun updateNeighborMetric(nodeId: Long, metrics: LinkMetrics) {
        val nodeLinks = neighborMetrics.getOrPut(nodeId) { ConcurrentHashMap() }
        nodeLinks[metrics.bearerType] = metrics
        val weight = calculateLinkWeight(metrics)
        updateLinkWeight(identity.nodeId, nodeId, weight)
    }

    private fun handleLSA(packet: MeshPacket) {
        val senderId = packet.source.nodeHashId
        // nodeToAddress mapping already handled in handleIncomingRaw
        val buffer = java.nio.ByteBuffer.wrap(packet.payload)
        if (buffer.remaining() < 1) return
        val numNeighbors = buffer.get().toInt()
        val neighbors = mutableMapOf<Long, Double>()
        repeat(numNeighbors) {
            if (buffer.remaining() >= 16) {
                val neighborId = buffer.long
                val weight = buffer.double
                neighbors[neighborId] = weight
            }
        }
        adjacencyTable[senderId] = neighbors
        updateTopology()
        Log.i(TAG, "LSA received: Node $senderId has $numNeighbors neighbors.")
    }

    private fun updateTopology() {
        val topology = adjacencyTable.mapValues { it.value.keys.toList() }
        _meshTopology.value = topology
    }

    private fun broadcastLSA() {
        val myNeighbors = adjacencyTable[identity.nodeId] ?: return
        if (myNeighbors.isEmpty()) return
        val buffer = java.nio.ByteBuffer.allocate(1 + (myNeighbors.size * 16))
        buffer.put(myNeighbors.size.toByte())
        myNeighbors.forEach { (id, weight) ->
            buffer.putLong(id)
            buffer.putDouble(weight)
        }
        val packet = MeshPacket(
            source = IPvXAddress(0, IPvXAddress.BEARER_IP, identity.nodeId, 0),
            destination = IPvXAddress(0, IPvXAddress.BEARER_IP, 0L, 0),
            hopRoute = emptyList(),
            nextHopIndex = 0,
            payload = buffer.array(),
            type = MeshPacket.PacketType.LSA
        )
        broadcastData(packet.toBytes())
    }

    private fun broadcastData(data: ByteArray) {
        ble.broadcastData(data)
        wifi.broadcastData(data)
    }

    private fun relayPacket(packet: MeshPacket) {
        val myId = identity.nodeId
        
        // 1. Advance the pointer if it's currently pointing to us
        if (packet.hopRoute.isNotEmpty() && 
            packet.nextHopIndex < packet.hopRoute.size && 
            packet.hopRoute[packet.nextHopIndex] == myId) {
            packet.nextHopIndex++
        }

        // 2. Get the actual next hop ID
        val nextHopId = if (packet.nextHopIndex < packet.hopRoute.size) {
            packet.hopRoute[packet.nextHopIndex]
        } else {
            // 3. No route or route exhausted -> Calculate a new one using Dijkstra
            Log.i(TAG, "[HOP] Route exhausted. Calculating new route to destination ${packet.destination.nodeHashId}...")
            val newRoute = calculatePath(packet.destination.nodeHashId)
            if (newRoute != null && newRoute.isNotEmpty()) {
                // For simplicity in this demo, we'll just take the first hop
                newRoute[0]
            } else {
                Log.w(TAG, "[HOP] No path found to ${packet.destination.nodeHashId}. Dropping packet.")
                null
            }
        }

        if (nextHopId != null) {
            Log.i(TAG, "[HOP] Forwarding packet to next hop: $nextHopId (Index: ${packet.nextHopIndex})")
            sendToNode(nextHopId, packet)
        }
    }

    private fun sendToNode(nodeId: Long, packet: MeshPacket) {
        val address = nodeToAddress[nodeId]
        val hwAddr = nodeToHardwareAddress[nodeId]
        
        if (address == null || hwAddr == null) {
            Log.w(TAG, "[TX] Cannot send to node $nodeId: Address or Hardware mapping missing. (Addr: $address, HW: $hwAddr)")
            return
        }

        val data = packet.toBytes()
        Log.i(TAG, "[TX] Sending ${data.size} bytes to $nodeId via ${hwAddr} (Bearer: ${address.bearerClass})")
        
        when (address.bearerClass) {
            IPvXAddress.BEARER_BLE -> ble.sendData(hwAddr, data)
            IPvXAddress.BEARER_WIFI -> wifi.sendData(hwAddr, data)
            IPvXAddress.BEARER_IP -> internet.connectAndSend(hwAddr, data)
        }
    }

    fun calculatePath(targetNodeId: Long): List<Long>? {
        val source = identity.nodeId
        val distances = mutableMapOf<Long, Double>().withDefault { Double.MAX_VALUE }
        val previous = mutableMapOf<Long, Long?>()
        val nodes = PriorityQueue<Pair<Long, Double>>(compareBy { it.second })
        distances[source] = 0.0
        nodes.add(source to 0.0)
        while (nodes.isNotEmpty()) {
            val (current, dist) = nodes.poll()!!
            if (current == targetNodeId) {
                val path = mutableListOf<Long>()
                var temp: Long? = current
                while (temp != null) {
                    path.add(temp)
                    temp = previous[temp]
                }
                return path.reversed().drop(1)
            }
            if (dist > distances.getValue(current)) continue
            adjacencyTable[current]?.forEach { (neighbor, weight) ->
                val alt = distances.getValue(current) + weight
                if (alt < distances.getValue(neighbor)) {
                    distances[neighbor] = alt
                    previous[neighbor] = current
                    nodes.add(neighbor to alt)
                }
            }
        }
        return null
    }

    fun updateLinkWeight(fromId: Long, toId: Long, weight: Double) {
        adjacencyTable.getOrPut(fromId) { ConcurrentHashMap() }[toId] = weight
        updateTopology()
    }
}
