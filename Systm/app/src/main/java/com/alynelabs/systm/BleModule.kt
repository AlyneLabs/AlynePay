package com.alynelabs.systm

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.*
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.ParcelUuid
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.nio.ByteBuffer
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Modern Production-Grade BLE Module
 * Supports concurrent multi-device connections, dual-role operations, 
 * and API 33+ compatibility.
 */
@SuppressLint("MissingPermission")
class
BleModule(private val context: Context) {

    private val TAG = "BleModule"
    var localNodeId: Long = 0L // Set by MeshManager

    // --- Types & Enums ---
    enum class TransmissionMode { GATT_WRITE, GATT_NOTIFY }
    data class DiscoveredDevice(val name: String, val address: String, val lastSeen: Long = System.currentTimeMillis())

    // --- Hardware References ---
    private val bluetoothManager by lazy { context.getSystemService(BluetoothManager::class.java) }
    private val bluetoothAdapter by lazy { bluetoothManager?.adapter }
    private val bleScanner by lazy { bluetoothAdapter?.bluetoothLeScanner }
    private val bleAdvertiser by lazy { bluetoothAdapter?.bluetoothLeAdvertiser }

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
    private val connectedGattDevices = ConcurrentHashMap<String, BluetoothGatt>()
    private val connectedServerDevices = ConcurrentHashMap<String, BluetoothDevice>()
    private val pendingConnections = Collections.newSetFromMap(ConcurrentHashMap<String, Boolean>())
    
    private var gattServer: BluetoothGattServer? = null
    private var isScanning = false
    private var isAdvertising = false
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // --- 1. Dual-Role Initialization ---

    /**
     * Convenience function to start both scanning and advertising.
     */
    fun startRadio() {
        Log.i(TAG, "Starting BLE Radio...")
        startBluetoothServer()
        startScanning()
        startAdvertising()
    }

    fun stopRadio() {
        Log.d(TAG, "Stopping Radio and Clearing Connections...")
        stopScanning()
        stopAdvertising()
        disconnectAll()
        gattServer?.close()
        gattServer = null
    }

    // --- 2. Discovery & Filtering ---

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device
            val scanRecord = result.scanRecord ?: return
            
            val name = scanRecord.deviceName ?: device.name ?: "Unknown"
            val address = device.address

            // Filter strictly by UUID or Name
            val uuids = scanRecord.serviceUuids?.map { it.uuid.toString().lowercase() } ?: emptyList()
            if (uuids.contains(SERVICE_UUID.lowercase()) || name.contains("Systm", true)) {
                _discoveredDevices.update { currentMap ->
                    currentMap + (address to DiscoveredDevice(name, address))
                }
                
                // Deterministic Peer Negotiation: Only initiate if our Node ID is HIGHER
                // This prevents "Dual Central" collisions and 30s disconnect loops.
                val remoteNodeId = name.filter { it.isDigit() || it == '-' }.toLongOrNull() ?: 0L
                
                val shouldInitiate = localNodeId > remoteNodeId
                
                val isAlreadyConnected = connectedGattDevices.containsKey(address) || 
                                       connectedServerDevices.containsKey(address) ||
                                       pendingConnections.contains(address)
                
                if (!isAlreadyConnected && shouldInitiate) {
                    Log.i(TAG, "Initiating Connection to $name ($address) [Higher Node ID Logic]")
                    connect(address)
                } else if (!isAlreadyConnected) {
                    Log.d(TAG, "Discovered $name ($address) but waiting for them to initiate [Lower Node ID Logic]")
                }
            }
        }
    }

    fun startScanning() {
        if (isScanning) return
        if (!hasPermission(Manifest.permission.BLUETOOTH_SCAN)) {
            Log.e(TAG, "Missing SCAN permission")
            return
        }
        
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()
            
        Log.i(TAG, "BLE Scanner Started")
        bleScanner?.startScan(null, settings, scanCallback)
        isScanning = true
    }

    fun stopScanning() {
        if (!isScanning) return
        try { 
            bleScanner?.stopScan(scanCallback)
            Log.d(TAG, "BLE Scanner Stopped")
        } catch (_: Exception) {}
        isScanning = false
    }

    fun startAdvertising(customName: String? = null) {
        if (isAdvertising) return
        if (!hasPermission(Manifest.permission.BLUETOOTH_ADVERTISE)) {
            Log.e(TAG, "Missing ADVERTISE permission")
            return
        }

        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setConnectable(true)
            .build()
            
        // Pack Node ID into Service Data (8 bytes)
        val nodeIdBytes = ByteBuffer.allocate(8).putLong(localNodeId).array()
        
        val data = AdvertiseData.Builder()
            .setIncludeDeviceName(true)
            .addServiceUuid(ParcelUuid.fromString(SERVICE_UUID))
            .addServiceData(ParcelUuid.fromString(SERVICE_UUID), nodeIdBytes)
            .build()
            
        Log.i(TAG, "BLE Advertiser Started with NodeID: $localNodeId")
        bleAdvertiser?.startAdvertising(settings, data, advertiseCallback)
        isAdvertising = true
    }

    fun stopAdvertising() {
        if (!isAdvertising) return
        try { 
            bleAdvertiser?.stopAdvertising(advertiseCallback)
            Log.d(TAG, "BLE Advertiser Stopped")
        } catch (_: Exception) {}
        isAdvertising = false
    }

    private fun startBluetoothServer() {
        if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
            Log.e(TAG, "Missing BLUETOOTH_CONNECT permission for GATT Server")
            return
        }
        try {
            gattServer = bluetoothManager?.openGattServer(context, gattServerCallback)
            val service = BluetoothGattService(UUID.fromString(SERVICE_UUID), BluetoothGattService.SERVICE_TYPE_PRIMARY)
            val props = BluetoothGattCharacteristic.PROPERTY_WRITE or BluetoothGattCharacteristic.PROPERTY_NOTIFY or BluetoothGattCharacteristic.PROPERTY_READ
            val perms = BluetoothGattCharacteristic.PERMISSION_WRITE or BluetoothGattCharacteristic.PERMISSION_READ
            val msgChar = BluetoothGattCharacteristic(UUID.fromString(MSG_CHAR_UUID), props, perms)
            msgChar.addDescriptor(BluetoothGattDescriptor(UUID.fromString(CLIENT_CONFIG), BluetoothGattDescriptor.PERMISSION_WRITE))
            service.addCharacteristic(msgChar)
            gattServer?.addService(service)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start GATT Server: ${e.message}")
        }
    }

    // --- 3. Concurrent Connection Management ---

    fun connect(address: String) {
        if (connectedGattDevices.containsKey(address) || pendingConnections.contains(address)) return
        
        pendingConnections.add(address)
        scope.launch {
            val device = bluetoothAdapter?.getRemoteDevice(address)
            device?.connectGatt(context, false, gattCallback, BluetoothDevice.TRANSPORT_LE)
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            val address = gatt.device.address
            pendingConnections.remove(address)
            
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                connectedGattDevices[address] = gatt
                _isConnected.value = true
                Log.i(TAG, "Connected to Central Tunnel: $address")
                scope.launch {
                    delay(800)
                    gatt.requestMtu(512)
                }
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                connectedGattDevices.remove(address)
                gatt.close()
                updateGlobalConnectionState()
                Log.i(TAG, "Disconnected from Central Tunnel: $address")
            }
        }

        override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
            Log.i(TAG, "MTU changed to $mtu for ${gatt.device.address} (Status: $status)")
            if (status == BluetoothGatt.GATT_SUCCESS) gatt.discoverServices()
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) setupNotifications(gatt)
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, value: ByteArray) {
            handleIncomingData(gatt.device.address, characteristic.uuid, value)
        }

        @Deprecated("Deprecated in Java")
        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            @Suppress("DEPRECATION")
            handleIncomingData(gatt.device.address, characteristic.uuid, characteristic.value)
        }
    }

    private val gattServerCallback = object : BluetoothGattServerCallback() {
        override fun onConnectionStateChange(device: BluetoothDevice, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                connectedServerDevices[device.address] = device
                _isConnected.value = true
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                connectedServerDevices.remove(device.address)
                updateGlobalConnectionState()
            }
        }

        override fun onCharacteristicWriteRequest(device: BluetoothDevice, requestId: Int, characteristic: BluetoothGattCharacteristic, preparedWrite: Boolean, responseNeeded: Boolean, offset: Int, value: ByteArray) {
            handleIncomingData(device.address, characteristic.uuid, value)
            if (responseNeeded) gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, value)
        }
    }

    // --- 4. Automated Data Transfer ---

    /**
     * Automatically selects the correct tunnel (Central Write or Server Notify)
     * based on how the targetAddress is connected.
     */
    fun sendData(targetAddress: String, data: ByteArray) {
        // 1. Check if we are connected as a Client (Central)
        connectedGattDevices[targetAddress]?.let { gatt ->
            val char = gatt.getService(UUID.fromString(SERVICE_UUID))
                ?.getCharacteristic(UUID.fromString(MSG_CHAR_UUID))
            
            if (char != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    gatt.writeCharacteristic(char, data, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT)
                } else {
                    @Suppress("DEPRECATION")
                    char.setValue(data)
                    gatt.writeCharacteristic(char)
                }
                Log.d(TAG, "Sent via GATT_WRITE to $targetAddress")
                return // Exit after successful send
            }
        }

        // 2. If not found in Clients, check if they are connected to our Server (Peripheral)
        connectedServerDevices[targetAddress]?.let { device ->
            val char = gattServer?.getService(UUID.fromString(SERVICE_UUID))
                ?.getCharacteristic(UUID.fromString(MSG_CHAR_UUID))
            
            if (char != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    gattServer?.notifyCharacteristicChanged(device, char, false, data)
                } else {
                    @Suppress("DEPRECATION")
                    char.setValue(data)
                    gattServer?.notifyCharacteristicChanged(device, char, false)
                }
                Log.d(TAG, "Sent via GATT_NOTIFY to $targetAddress")
            }
        }
    }

    /**
     * Sends data to EVERY connected device in the mesh, regardless of role.
     */
    fun broadcastData(data: ByteArray) {
        connectedGattDevices.keys().iterator().forEach { addr -> sendData(addr, data) }
        connectedServerDevices.keys().iterator().forEach { addr -> sendData(addr, data) }
    }

    // --- 5. Granular Disconnection ---

    fun disconnectDevice(address: String) {
        // Disconnect if we are the Client
        connectedGattDevices[address]?.let { gatt ->
            gatt.disconnect()
            gatt.close()
            connectedGattDevices.remove(address)
        }
        
        // Disconnect if we are the Server
        connectedServerDevices[address]?.let { device ->
            gattServer?.cancelConnection(device)
            connectedServerDevices.remove(address)
        }
        
        updateGlobalConnectionState()
    }

    fun disconnectAll() {
        connectedGattDevices.forEach { (_, gatt) ->
            gatt.disconnect()
            gatt.close()
        }
        connectedGattDevices.clear()
        
        connectedServerDevices.forEach { (_, device) ->
            gattServer?.cancelConnection(device)
        }
        connectedServerDevices.clear()
        
        _isConnected.value = false
    }

    // --- Helpers ---

    private fun handleIncomingData(address: String, uuid: UUID, data: ByteArray) {
        if (uuid.toString().equals(MSG_CHAR_UUID, ignoreCase = true)) {
            Log.d(TAG, "Incoming from $address: ${data.size} bytes")
            scope.launch { _backendDataWithAddress.emit(Pair(address, data)) }
        }
    }

    private fun setupNotifications(gatt: BluetoothGatt) {
        val service = gatt.getService(UUID.fromString(SERVICE_UUID))
        service?.getCharacteristic(UUID.fromString(MSG_CHAR_UUID))?.let { char ->
            gatt.setCharacteristicNotification(char, true)
            char.getDescriptor(UUID.fromString(CLIENT_CONFIG))?.let { desc ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    gatt.writeDescriptor(desc, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
                } else {
                    @Suppress("DEPRECATION")
                    desc.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
                    gatt.writeDescriptor(desc)
                }
            }
        }
    }

    private fun updateGlobalConnectionState() {
        _isConnected.value = connectedGattDevices.isNotEmpty() || connectedServerDevices.isNotEmpty()
    }

    private fun hasPermission(p: String): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.checkSelfPermission(p) == PackageManager.PERMISSION_GRANTED
        } else {
            context.checkSelfPermission(Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED
        }
    }

    private val advertiseCallback = object : AdvertiseCallback() {}

    companion object {
        const val SERVICE_UUID = "0000180D-0000-1000-8000-00805f9b34fb"
        const val MSG_CHAR_UUID = "00002A37-0000-1000-8000-00805f9b34fb"
        const val CLIENT_CONFIG = "00002902-0000-1000-8000-00805f9b34fb"
    }
}
