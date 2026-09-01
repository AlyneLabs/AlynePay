package com.alynelabs.systm.service

import android.util.Log
import com.alynelabs.systm.mesh.MeshManager
import com.alynelabs.systm.mesh.IPvXAddress
import com.alynelabs.systm.mesh.MeshPacket
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * Lightweight Embedded Localhost HTTP & IPC Bridge Server
 * Listens on 127.0.0.1:8765 to bridge the native Mesh Engine with React Native / AlynePay
 */
class SystmBridgeServer(private val meshManager: MeshManager) {
    private val TAG = "SystmBridgeServer"
    private val PORT = 8765
    private var serverSocket: ServerSocket? = null
    private var isRunning = false
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // Incoming payment queue for AlynePay to poll/consume
    private val incomingPaymentEvents = ConcurrentLinkedQueue<JSONObject>()

    init {
        // Register callback for incoming data packets from the mesh
        meshManager.onPaymentReceivedListener = { paymentJson ->
            Log.i(TAG, "[EVENT] Payment received from mesh: $paymentJson")
            incomingPaymentEvents.add(paymentJson)
        }
    }

    fun start() {
        if (isRunning) return
        isRunning = true

        scope.launch {
            try {
                serverSocket = ServerSocket().apply {
                    reuseAddress = true
                    bind(InetSocketAddress("127.0.0.1", PORT))
                }
                Log.i(TAG, "Systm IPC Bridge Server listening on http://127.0.0.1:$PORT")

                while (isActive && isRunning) {
                    try {
                        val clientSocket = serverSocket?.accept() ?: break
                        launch { handleClient(clientSocket) }
                    } catch (e: Exception) {
                        if (!isRunning) break
                        Log.e(TAG, "Error accepting client connection: ${e.message}")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to bind SystmBridgeServer on port $PORT: ${e.message}")
            }
        }
    }

    fun stop() {
        isRunning = false
        try {
            serverSocket?.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error closing server socket: ${e.message}")
        }
        scope.cancel()
    }

    private fun handleClient(socket: Socket) {
        socket.use { s ->
            try {
                val reader = BufferedReader(InputStreamReader(s.getInputStream()))
                val output = s.getOutputStream()

                val requestLine = reader.readLine() ?: return
                val parts = requestLine.split(" ")
                if (parts.size < 2) return

                val method = parts[0].uppercase()
                val path = parts[1]

                // Read headers
                var contentLength = 0
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    if (line.isNullOrEmpty()) break
                    val header = line!!
                    if (header.startsWith("Content-Length:", ignoreCase = true)) {
                        contentLength = header.substringAfter(":").trim().toIntOrNull() ?: 0
                    }
                }

                // Read request body if present
                val body = if (contentLength > 0) {
                    val charArray = CharArray(contentLength)
                    reader.read(charArray, 0, contentLength)
                    String(charArray)
                } else ""

                // Handle CORS preflight
                if (method == "OPTIONS") {
                    sendResponse(output, 200, "OK", "application/json", "{}")
                    return
                }

                // Route handlers
                when {
                    method == "GET" && path == "/api/status" -> handleStatus(output)
                    method == "GET" && path == "/api/peers" -> handlePeers(output)
                    method == "GET" && path == "/api/events" -> handleEvents(output)
                    method == "POST" && path == "/api/pay" -> handlePay(output, body)
                    else -> sendResponse(output, 404, "Not Found", "application/json", "{\"error\":\"Route not found\"}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error handling client request: ${e.message}")
            }
        }
    }

    private fun handleStatus(output: OutputStream) {
        val json = JSONObject().apply {
            put("status", "ONLINE")
            put("nodeId", meshManager.identity.nodeId)
            put("publicKey", "0x" + meshManager.identity.nodeId.toString(16).uppercase().padStart(16, '0'))
            put("meshActive", true)
            put("protocolVersion", "Alyne-Mesh v2.4")
        }
        sendResponse(output, 200, "OK", "application/json", json.toString())
    }

    private fun handlePeers(output: OutputStream) {
        val topology = meshManager.meshTopology.value
        val peersArray = JSONArray()

        val myNeighbors = topology[meshManager.identity.nodeId] ?: emptyList()
        myNeighbors.forEach { neighborId ->
            val peerObj = JSONObject().apply {
                put("id", "node-$neighborId")
                put("nodeId", neighborId)
                put("name", "Node " + (neighborId.toString().takeLast(4)))
                put("address", "0x" + neighborId.toString(16).uppercase().padStart(8, '0').take(6) + "..." + neighborId.toString(16).uppercase().takeLast(4))
                put("bearer", "BLE")
                put("connected", true)
            }
            peersArray.put(peerObj)
        }

        val response = JSONObject().apply {
            put("peers", peersArray)
            put("count", myNeighbors.size)
        }
        sendResponse(output, 200, "OK", "application/json", response.toString())
    }

    private fun handleEvents(output: OutputStream) {
        val eventsArray = JSONArray()
        while (incomingPaymentEvents.isNotEmpty()) {
            val event = incomingPaymentEvents.poll() ?: break
            eventsArray.put(event)
        }

        val response = JSONObject().apply {
            put("events", eventsArray)
        }
        sendResponse(output, 200, "OK", "application/json", response.toString())
    }

    private fun handlePay(output: OutputStream, body: String) {
        try {
            val json = JSONObject(body)
            val recipientNodeId = json.optLong("recipientNodeId", 0L)
            val recipientName = json.optString("recipientName", "Direct Peer")
            val amount = json.optDouble("amount", 0.0)

            if (amount <= 0) {
                sendResponse(output, 400, "Bad Request", "application/json", "{\"error\":\"Invalid amount\"}")
                return
            }

            val txId = "tx-" + System.currentTimeMillis()

            // Construct payload packet for mesh
            val paymentPayload = JSONObject().apply {
                put("type", "PAYMENT")
                put("txId", txId)
                put("fromNodeId", meshManager.identity.nodeId)
                put("fromName", "User")
                put("toNodeId", recipientNodeId)
                put("amount", amount)
                put("timestamp", System.currentTimeMillis())
            }

            val success = meshManager.sendPaymentData(recipientNodeId, paymentPayload.toString())

            val res = JSONObject().apply {
                put("success", success)
                put("txId", txId)
                put("amount", amount)
                put("recipientName", recipientName)
            }
            sendResponse(output, 200, "OK", "application/json", res.toString())
        } catch (e: Exception) {
            sendResponse(output, 500, "Internal Error", "application/json", "{\"error\":\"${e.message}\"}")
        }
    }

    private fun sendResponse(output: OutputStream, statusCode: Int, statusText: String, contentType: String, body: String) {
        val bodyBytes = body.toByteArray(Charsets.UTF_8)
        val headers = StringBuilder().apply {
            append("HTTP/1.1 $statusCode $statusText\r\n")
            append("Content-Type: $contentType; charset=utf-8\r\n")
            append("Content-Length: ${bodyBytes.size}\r\n")
            append("Access-Control-Allow-Origin: *\r\n")
            append("Access-Control-Allow-Methods: GET, POST, OPTIONS\r\n")
            append("Access-Control-Allow-Headers: Content-Type, Authorization\r\n")
            append("Connection: close\r\n\r\n")
        }.toString()

        output.write(headers.toByteArray(Charsets.UTF_8))
        output.write(bodyBytes)
        output.flush()
    }
}
