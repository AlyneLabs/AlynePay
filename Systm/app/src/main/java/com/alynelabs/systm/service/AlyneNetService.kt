package com.alynelabs.systm.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import com.alynelabs.systm.BleModule
import com.alynelabs.systm.InternetModule
import com.alynelabs.systm.WifiModule
import com.alynelabs.systm.mesh.MeshManager
import com.alynelabs.systm.mesh.NodeIdentity

class AlyneNetService : Service() {

    private val TAG = "AlyneNetService"
    private val CHANNEL_ID = "AlyneNetMeshChannel"
    private val NOTIFICATION_ID = 1

    lateinit var meshManager: MeshManager
    private val binder = LocalBinder()

    inner class LocalBinder : Binder() {
        fun getService(): AlyneNetService = this@AlyneNetService
    }

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "Initializing Alyne Net System...")
        
        createNotificationChannel()
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(
                    NOTIFICATION_ID, 
                    createNotification(), 
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE
                )
            } else {
                startForeground(NOTIFICATION_ID, createNotification())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start foreground service: ${e.message}")
        }

        // Initialize core components. These don't require hardware permissions yet.
        val identity = NodeIdentity(applicationContext)
        val ble = BleModule(applicationContext)
        val wifi = WifiModule(applicationContext)
        val internet = InternetModule()
        
        meshManager = MeshManager(identity, ble, wifi, internet)
        Log.i(TAG, "Mesh Service Ready. Node ID: ${identity.nodeId}")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    private fun createNotificationChannel() {
        val name = "Alyne Net Mesh"
        val descriptionText = "Ensures mesh network connectivity"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
            setShowBadge(true)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }

    private fun createNotification(): Notification {
        val builder = Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("Alyne Net Mesh Active")
            .setContentText("Mesh network is running in the background")
            .setSmallIcon(android.R.drawable.stat_sys_data_bluetooth)
            .setOngoing(true)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            builder.setForegroundServiceBehavior(Notification.FOREGROUND_SERVICE_IMMEDIATE)
        }
        
        return builder.build()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Stopping Alyne Net System...")
        // Modules should be stopped via MeshManager or directly here
    }
}
