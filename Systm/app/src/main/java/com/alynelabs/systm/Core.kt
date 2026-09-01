package com.alynelabs.systm

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class Core(
    private val context: Context,
    private val ble: BleModule,
    private val wifi: WifiModule,
    private val internet: InternetModule
) {

    fun start() {
        Log.d("Core", "Starting hardware modules...")
        ble.startRadio()
        wifi.startRadio()
        internet.startRadio()
    }

    fun stop() {
        Log.d("Core", "Stopping hardware modules...")
        ble.stopRadio()
        wifi.stopRadio()
        internet.stopRadio()
    }
}
