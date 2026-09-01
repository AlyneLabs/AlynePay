package com.alynelabs.systm

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.alynelabs.systm.service.AlyneNetService
import com.alynelabs.systm.ui.theme.SystmTheme
import androidx.compose.runtime.State

class MainActivity : ComponentActivity() {
    private var meshService: AlyneNetService? = null
    private var isBound = false

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as AlyneNetService.LocalBinder
            meshService = binder.getService()
            isBound = true
            Log.i("MainActivity", "Bound to AlyneNetService")
            Log.i("MainActivity", "Service Self-Test: ${meshService?.meshManager?.selfTest()}")
            
            // 3-Node Hop Test Configuration (Automated Retry)
            val phoneA_Id = -2472472724180024548L
            val phoneB_Id = -8824097300144855975L
            val phoneC_Id = -9015913220544022712L

            val myId = meshService?.meshManager?.identity?.nodeId

            if (myId == phoneA_Id) {
                Log.i("MainActivity", "[SYSTEM] Device is Phone A. Tap username to start hop test.")
            } else if (myId == phoneB_Id) {
                Log.i("MainActivity", "[SYSTEM] Device is Phone B. Standing by as Relay.")
            } else if (myId == phoneC_Id) {
                Log.i("MainActivity", "[SYSTEM] Device is Phone C. Standing by as Destination.")
            }
            
            // If permissions are already granted, start the mesh
            if (hasRequiredPermissions()) {
                meshService?.meshManager?.startAll()
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isBound = false
            meshService = null
        }
    }

    private fun hasRequiredPermissions(): Boolean {
        val permissions = getRequiredPermissions()
        return permissions.all { checkSelfPermission(it) == android.content.pm.PackageManager.PERMISSION_GRANTED }
    }

    private fun getRequiredPermissions(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_ADVERTISE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.POST_NOTIFICATIONS
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_ADVERTISE,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        } else {
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Start and bind to mesh service
        Intent(this, AlyneNetService::class.java).also { intent ->
            startForegroundService(intent)
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }

        setContent {
            MainScreen(
                onSystmClick = { active ->
                    Log.d("MainActivity", "SYSTM Clicked, active: $active")
                    if (active) meshService?.meshManager?.startAll() else meshService?.meshManager?.stopAll()
                },
                onMeshClick = { active ->
                    Log.d("MainActivity", "MESH Clicked, active: $active")
                },
                onInternetRoutingClick = { active ->
                    Log.d("MainActivity", "Internet Routing Clicked, active: $active")
                },
                onWifiTunnelsClick = { active ->
                    Log.d("MainActivity", "Wi-Fi Tunnels Clicked, active: $active")
                }
            )
        }

        val permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val allGranted = permissions.entries.all { it.value }
            if (allGranted) {
                Log.d("MainActivity", "Permissions granted, starting mesh...")
                meshService?.meshManager?.startAll()
            }
        }

        permissionLauncher.launch(getRequiredPermissions())
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isBound) {
            unbindService(connection)
            isBound = false
        }
    }
}

@Composable
fun MainScreen(
    meshService: AlyneNetService? = null,
    onSystmClick: (Boolean) -> Unit = {},
    onMeshClick: (Boolean) -> Unit = {},
    onInternetRoutingClick: (Boolean) -> Unit = {},
    onWifiTunnelsClick: (Boolean) -> Unit = {}
) {
    var currentScreen by remember { mutableStateOf(SystmScreen.Home) }
    val uiScale = 1.1f

    var isSystmActive by remember { mutableStateOf(false) }
    var isMeshActive by remember { mutableStateOf(false) }
    var isInternetActive by remember { mutableStateOf(false) }
    var isWifiActive by remember { mutableStateOf(false) }

    val meshTopologyState = meshService?.meshManager?.meshTopology?.collectAsState()
    val meshTopology = meshTopologyState?.value ?: emptyMap<Long, List<Long>>()

    SystmTheme(sizeMultiplier = uiScale) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .statusBarsPadding()
                .padding(horizontal = 16.dp * uiScale),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Section (Shared)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LogoCard(sizeMultiplier = 0.85f)
                StatusBox(sizeMultiplier = 0.75f)
            }

            Spacer(modifier = Modifier.height(20.dp * uiScale))

            // Dynamic Content
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.TopCenter) {
                when (currentScreen) {
                    SystmScreen.Home -> HomeScreenContent(
                        uiScale = uiScale,
                        meshTopology = meshTopology,
                        isSystmActive = isSystmActive,
                        isMeshActive = isMeshActive,
                        isInternetActive = isInternetActive,
                        isWifiActive = isWifiActive,
                        onSystmClick = { 
                            isSystmActive = !isSystmActive
                            onSystmClick(isSystmActive) 
                        },
                        onMeshClick = { 
                            isMeshActive = !isMeshActive
                            onMeshClick(isMeshActive) 
                        },
                        onInternetRoutingClick = { 
                            isInternetActive = !isInternetActive
                            onInternetRoutingClick(isInternetActive) 
                        },
                        onWifiTunnelsClick = { 
                            isWifiActive = !isWifiActive
                            onWifiTunnelsClick(isWifiActive) 
                        },
                        onTestClick = {
                            val phoneB_Id = -8824097300144855975L
                            val phoneC_Id = -9015913220544022712L
                            meshService?.meshManager?.start3NodeHopTest(phoneC_Id, phoneB_Id)
                        }
                    )
                    SystmScreen.Account -> AccountScreenContent(uiScale)
                    else -> Box(modifier = Modifier.fillMaxSize())
                }
            }

            Spacer(modifier = Modifier.height(16.dp * uiScale))

            // Navigation Bar
            NavigationBar(
                sizeMultiplier = uiScale,
                selectedScreen = currentScreen,
                onScreenSelected = { currentScreen = it }
            )
            Spacer(modifier = Modifier.height(16.dp * uiScale))
        }
    }
}

@Composable
fun HomeScreenContent(
    uiScale: Float,
    meshTopology: Map<Long, List<Long>> = emptyMap(),
    isSystmActive: Boolean = false,
    isMeshActive: Boolean = false,
    isInternetActive: Boolean = false,
    isWifiActive: Boolean = false,
    onSystmClick: () -> Unit = {},
    onMeshClick: () -> Unit = {},
    onInternetRoutingClick: () -> Unit = {},
    onWifiTunnelsClick: () -> Unit = {},
    onTestClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp * uiScale),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))

        // User Section
        UserBox(sizeMultiplier = uiScale, onTestClick = onTestClick)

        Spacer(modifier = Modifier.weight(1f))

        // Toggles Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp * uiScale)
        ) {
            ToggleCard(
                text = "SYSTM",
                glowColor = Color(0xFF00FF95),
                sizeMultiplier = 1.1f,
                modifier = Modifier.weight(1f),
                isActive = isSystmActive,
                onClick = onSystmClick
            ) {
                SystmIcon(sizeMultiplier = 1.25f)
            }
            ToggleCard(
                text = "MESH",
                glowColor = Color(0xFF00CCFF),
                sizeMultiplier = 1.1f,
                modifier = Modifier.weight(1f),
                isActive = isMeshActive,
                onClick = onMeshClick
            ) {
                MeshIcon(sizeMultiplier = 1.25f)
            }
        }

        // Quick Actions Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp * uiScale)
        ) {
            QuickActionPanel(sizeMultiplier = 1.1f, modifier = Modifier.weight(1f)) {
                ActionButton(
                    text = "Internet\nRouting",
                    glowColor = Color(0xFF9747FF),
                    sizeMultiplier = 1.1f,
                    isActive = isInternetActive,
                    onClick = onInternetRoutingClick
                ) { InternetIcon(sizeMultiplier = 1.4f) }
                ActionButton(
                    text = "Wi-Fi\nTunnels",
                    glowColor = Color(0xFFFFA629),
                    sizeMultiplier = 1.1f,
                    isActive = isWifiActive,
                    onClick = onWifiTunnelsClick
                ) { WifiIcon(sizeMultiplier = 1.25f) }
            }
            QuickActionPanel(sizeMultiplier = 1.1f, modifier = Modifier.weight(1f)) {
                ActionButton(text = "Energy\nSaver", glowColor = Color(0xFF00B309), sizeMultiplier = 1.1f) { EnergyIcon(sizeMultiplier = 1.4f) }
                ActionButton(text = "Limit\nDevices", glowColor = Color(0xFFF24822), sizeMultiplier = 1.1f) { LimitIcon(sizeMultiplier = 1.5f) }
            }
        }

        // Nodes Panel
        NodesPanel(sizeMultiplier = 1.1f, topology = meshTopology)
    }
}

@Composable
fun AccountScreenContent(uiScale: Float) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp * uiScale)
    ) {
        // Fixed Section
        ProfileCard(sizeMultiplier = uiScale)
        
        BackupActionsRow(sizeMultiplier = uiScale)

        // Scrollable List Section
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp * uiScale),
            contentPadding = PaddingValues(bottom = 20.dp * uiScale)
        ) {
            item { AccountListItem(name = "Solid Ammo", lastUsed = "12 July 2026", sizeMultiplier = uiScale, modifier = Modifier.fillMaxWidth()) }
            item { AccountListItem(name = "Gas Ammo", lastUsed = "15 July 2026", sizeMultiplier = uiScale, modifier = Modifier.fillMaxWidth()) }
            item { AccountListItem(name = "Plasma Ammo", lastUsed = "12 July 2026", sizeMultiplier = uiScale, showVoiceIcon = false, modifier = Modifier.fillMaxWidth()) }
            item { AccountListItem(name = "Void Ammo", lastUsed = "12 July 2026", sizeMultiplier = uiScale, showMessageIcon = false, modifier = Modifier.fillMaxWidth()) }
            item { AccountListItem(name = "Lead Ammo", lastUsed = "10 July 2026", sizeMultiplier = uiScale, modifier = Modifier.fillMaxWidth()) }
            item { AccountListItem(name = "Steel Ammo", lastUsed = "08 July 2026", sizeMultiplier = uiScale, modifier = Modifier.fillMaxWidth()) }
            item { AccountListItem(name = "Iron Ammo", lastUsed = "05 July 2026", sizeMultiplier = uiScale, modifier = Modifier.fillMaxWidth()) }
            item { AccountListItem(name = "Copper Ammo", lastUsed = "01 July 2026", sizeMultiplier = uiScale, modifier = Modifier.fillMaxWidth()) }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    MainScreen()
}
