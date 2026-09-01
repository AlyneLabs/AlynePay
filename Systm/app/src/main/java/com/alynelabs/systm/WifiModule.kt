package com.alynelabs.systm

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.NetworkInfo
import android.net.wifi.p2p.*
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest
import android.os.Build
import android.os.Looper
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.ConcurrentHashMap

/**
 * Modern Production-Grade WiFi Module (Wifi-Direct)
 * Matches the API and lifecycle of BleModule for consistent core logic.
 */
@SuppressLint("MissingPermission")
class WifiModule(private val context: Context) {

    private val TAG = "WifiModule"

    // --- Types ---
    data class DiscoveredDevice(val name: String, val address: String, val lastSeen: Long = System.currentTimeMillis())

    // --- Hardware References ---
    private val manager: WifiP2pManager? by lazy { context.getSystemService(Context.WIFI_P2P_SERVICE) as? WifiP2pManager }
    private val channel: WifiP2pManager.Channel? by lazy { manager?.initialize(context, Looper.getMainLooper(), null) }

    // --- State Management (Thread Safe) ---
    private val _discoveredDevices = MutableStateFlow<Map<String, DiscoveredDevice>>(emptyMap())
    val discoveredDevices: StateFlow<List<DiscoveredDevice>> = _discoveredDevices
        .map { it.values.toList() }
        .stateIn(CoroutineScope(Dispatchers.IO), SharingStarted.Eagerly, emptyList())

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _backendData = MutableSharedFlow<ByteArray>()
    val backendData: SharedFlow<ByteArray> = _backendData.asSharedFlow()

    private val _backendDataWithAddress = MutableSharedFlow<Pair<String, ByteArray>>()
    val backendDataWithAddress: SharedFlow<Pair<String, ByteArray>> = _backendDataWithAddress.asSharedFlow()

    // Connection Pools
    private val activeSockets = ConcurrentHashMap<String, Socket>()
    
    private var isScanning = false
    private var isAdvertising = false
    private var isRadioActive = false
    private var serverSocket: ServerSocket? = null
    
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // --- 1. Lifecycle Initialization ---

    fun startRadio() {
        if (isRadioActive) return
        Log.i(TAG, "Starting WiFi Radio...")
        isRadioActive = true
        
        val filter = IntentFilter().apply {
            addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            context.registerReceiver(receiver, filter)
        }

        startScanning()
        startAdvertising()
    }

    fun stopRadio() {
        Log.d(TAG, "Stopping WiFi Radio...")
        isRadioActive = false
        stopScanning()
        stopAdvertising()
        disconnectAll()
        try { context.unregisterReceiver(receiver) } catch (_: Exception) {}
    }

    // --- 2. Discovery (Scanning & Advertising) ---

    fun startScanning() {
        if (isScanning) return
        Log.i(TAG, "WiFi Scanning Started")
        
        manager?.addServiceRequest(channel, WifiP2pDnsSdServiceRequest.newInstance(), null)
        manager?.setDnsSdResponseListeners(channel, { instanceName, _, device ->
            if (instanceName == "_systm") {
                _discoveredDevices.update { currentMap ->
                    currentMap + (device.deviceAddress to DiscoveredDevice(device.deviceName, device.deviceAddress))
                }
            }
        }, null)
        
        manager?.discoverServices(channel, null)
        manager?.discoverPeers(channel, null)
        isScanning = true
    }

    fun stopScanning() {
        if (!isScanning) return
        manager?.stopPeerDiscovery(channel, null)
        manager?.clearServiceRequests(channel, null)
        isScanning = false
    }

    fun startAdvertising(customName: String? = null) {
        if (isAdvertising) return
        Log.d(TAG, "WiFi Advertising Started")
        
        val record = mapOf("name" to (customName ?: Build.MODEL))
        val serviceInfo = WifiP2pDnsSdServiceInfo.newInstance("_systm", "_presence._tcp", record)
        
        manager?.addLocalService(channel, serviceInfo, object : WifiP2pManager.ActionListener {
            override fun onSuccess() { isAdvertising = true }
            override fun onFailure(reason: Int) { Log.e(TAG, "Failed to start WiFi Ads: $reason") }
        })
    }

    fun stopAdvertising() {
        if (!isAdvertising) return
        manager?.clearLocalServices(channel, null)
        isAdvertising = false
    }

    // --- 3. Connection Management ---

    fun connect(address: String) {
        if (!isRadioActive) return
        val config = WifiP2pConfig().apply { 
            deviceAddress = address
            groupOwnerIntent = 0 // Prefer to be client if connecting TO someone
        }
        manager?.connect(channel, config, null)
    }

    fun disconnectDevice(address: String) {
        activeSockets[address]?.let { socket ->
            scope.launch {
                try { socket.close() } catch (_: Exception) {}
                activeSockets.remove(address)
                updateGlobalConnectionState()
            }
        }
    }

    fun disconnectAll() {
        scope.launch {
            activeSockets.forEach { (_, socket) ->
                try { socket.close() } catch (_: Exception) {}
            }
            activeSockets.clear()
            serverSocket?.close()
            serverSocket = null
            manager?.removeGroup(channel, null)
            _isConnected.value = false
        }
    }

    // --- 4. Automated Data Transfer ---

    fun sendData(targetAddress: String, data: ByteArray) {
        scope.launch(Dispatchers.IO) {
            try {
                activeSockets[targetAddress]?.getOutputStream()?.let { out ->
                    out.write(data)
                    out.flush()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to send WiFi data to $targetAddress: ${e.message}")
                disconnectDevice(targetAddress)
            }
        }
    }

    fun broadcastData(data: ByteArray) {
        activeSockets.keys().iterator().forEach { addr -> sendData(addr, data) }
    }

    // --- Internal Logic ---

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {
                    @Suppress("DEPRECATION")
                    val networkInfo = intent.getParcelableExtra<NetworkInfo>(WifiP2pManager.EXTRA_NETWORK_INFO)
                    if (networkInfo?.isConnected == true) {
                        manager?.requestConnectionInfo(channel) { info ->
                            startSocketCommunication(info)
                        }
                    } else {
                        disconnectAll()
                    }
                }
                WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> {
                    manager?.requestPeers(channel) { peers ->
                        peers.deviceList.forEach { dev ->
                            _discoveredDevices.update { it + (dev.deviceAddress to DiscoveredDevice(dev.deviceName, dev.deviceAddress)) }
                        }
                    }
                }
            }
        }
    }

    private fun startSocketCommunication(info: WifiP2pInfo) {
        scope.launch(Dispatchers.IO) {
            try {
                if (info.isGroupOwner) {
                    if (serverSocket == null || serverSocket!!.isClosed) {
                        serverSocket = ServerSocket(8888)
                        while (isRadioActive) {
                            val socket = serverSocket?.accept() ?: break
                            // In a real scenario, we'd handshake to get the MAC address.
                            // Here we use the IP as the key for simplicity in Wifi mode.
                            val remoteAddr = socket.inetAddress.hostAddress ?: "Unknown"
                            activeSockets[remoteAddr] = socket
                            _isConnected.value = true
                            handleIncomingSocket(remoteAddr, socket)
                        }
                    }
                } else {
                    val socket = Socket()
                    socket.connect(InetSocketAddress(info.groupOwnerAddress, 8888), 10000)
                    val remoteAddr = info.groupOwnerAddress.hostAddress ?: "GO"
                    activeSockets[remoteAddr] = socket
                    _isConnected.value = true
                    handleIncomingSocket(remoteAddr, socket)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Socket Error: ${e.message}")
            }
        }
    }

    private fun handleIncomingSocket(address: String, socket: Socket) {
        scope.launch(Dispatchers.IO) {
            val buffer = ByteArray(1024 * 64)
            try {
                val input = socket.getInputStream()
                while (isRadioActive && !socket.isClosed) {
                    val bytesRead = input.read(buffer)
                    if (bytesRead == -1) break
                    val data = buffer.copyOfRange(0, bytesRead)
                    _backendData.emit(data)
                    _backendDataWithAddress.emit(Pair(address, data))
                }
            } catch (_: Exception) {} finally {
                disconnectDevice(address)
            }
        }
    }

    private fun updateGlobalConnectionState() {
        _isConnected.value = activeSockets.isNotEmpty()
    }

    private fun hasPermission(p: String): Boolean {
        return context.checkSelfPermission(p) == PackageManager.PERMISSION_GRANTED
    }
}
