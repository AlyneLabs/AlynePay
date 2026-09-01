package com.alynelabs.systm

import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.ConcurrentHashMap

/**
 * Minimalist Internet IP Module
 * Handles sending data to specific IPs and receiving incoming data.
 */
class InternetModule {

    private val TAG = "InternetModule"
    private val DEFAULT_PORT = 9999

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _backendData = MutableSharedFlow<ByteArray>()
    val backendData: SharedFlow<ByteArray> = _backendData.asSharedFlow()

    private val _backendDataWithAddress = MutableSharedFlow<Pair<String, ByteArray>>()
    val backendDataWithAddress: SharedFlow<Pair<String, ByteArray>> = _backendDataWithAddress.asSharedFlow()

    private val activeSockets = ConcurrentHashMap<String, Socket>()
    private var serverSocket: ServerSocket? = null
    private var isRunning = false
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun startRadio() {
        if (isRunning) return
        isRunning = true
        startServer()
    }

    fun stopRadio() {
        isRunning = false
        disconnectAll()
    }

    private fun startServer() {
        scope.launch {
            try {
                serverSocket = ServerSocket(DEFAULT_PORT)
                Log.d(TAG, "Listening for incoming data on port $DEFAULT_PORT")
                while (isRunning) {
                    val socket = serverSocket?.accept() ?: break
                    val ip = socket.inetAddress.hostAddress ?: "unknown"
                    activeSockets[ip] = socket
                    _isConnected.value = true
                    handleSocket(ip, socket)
                }
            } catch (e: Exception) {
                if (isRunning) Log.e(TAG, "Server error: ${e.message}")
            }
        }
    }

    fun connectAndSend(ip: String, data: ByteArray, port: Int = DEFAULT_PORT) {
        scope.launch {
            try {
                val socket = activeSockets[ip] ?: Socket().apply {
                    connect(InetSocketAddress(ip, port), 5000)
                    activeSockets[ip] = this
                    _isConnected.value = true
                    handleSocket(ip, this)
                }
                
                withContext(Dispatchers.IO) {
                    socket.getOutputStream().write(data)
                    socket.getOutputStream().flush()
                }
                Log.d(TAG, "Sent ${data.size} bytes to $ip")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to send to $ip: ${e.message}")
                disconnect(ip)
            }
        }
    }

    private fun handleSocket(ip: String, socket: Socket) {
        scope.launch(Dispatchers.IO) {
            val buffer = ByteArray(1024 * 64)
            try {
                val input = socket.getInputStream()
                while (isRunning && !socket.isClosed) {
                    val bytesRead = input.read(buffer)
                    if (bytesRead == -1) break
                    val data = buffer.copyOfRange(0, bytesRead)
                    _backendData.emit(data)
                    _backendDataWithAddress.emit(Pair(ip, data))
                }
            } catch (_: Exception) {
                Log.d(TAG, "Connection lost with $ip")
            } finally {
                disconnect(ip)
            }
        }
    }

    fun disconnect(ip: String) {
        activeSockets.remove(ip)?.let {
            try { it.close() } catch (_: Exception) {}
        }
        _isConnected.value = activeSockets.isNotEmpty()
    }

    fun disconnectAll() {
        activeSockets.forEach { (ip, socket) ->
            try { socket.close() } catch (_: Exception) {}
        }
        activeSockets.clear()
        try { serverSocket?.close() } catch (_: Exception) {}
        serverSocket = null
        _isConnected.value = false
    }
}
