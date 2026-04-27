package com.kiosk.app.core.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.kiosk.app.ui.launcher.LauncherActivity
import com.kiosk.app.ui.locked.LockedActivity

class PowerManagementService : Service() {

    private val CHANNEL_ID = "PowerManagementServiceChannel"

    private val powerReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {

            when (intent.action) {
                Intent.ACTION_POWER_CONNECTED -> {
                    Log.d("PowerService", "Power Connected")
                    handlePowerState(true)
                }
                Intent.ACTION_POWER_DISCONNECTED -> {
                    Log.d("PowerService", "Power Disconnected")
                    handlePowerState(false)
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        val notification = createNotification()
        startForeground(1, notification)

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
        }
        registerReceiver(powerReceiver, filter)
        
        // Initial check
        checkInitialState()
    }

    private fun checkInitialState() {
        val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus = registerReceiver(null, intentFilter)
        val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) ?: -1
        val isCharging = status != 0
        handlePowerState(isCharging)
    }

    private fun handlePowerState(isCharging: Boolean) {
        Toast.makeText(this, "Power state changed: $isCharging", Toast.LENGTH_SHORT).show()

        if (isCharging) {
            val intent = Intent(this, LauncherActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            }
            startActivity(intent)
        } else {
            val intent = Intent(this, LockedActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            }
            startActivity(intent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(powerReceiver)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        val serviceChannel = NotificationChannel(
            CHANNEL_ID,
            "Power Management Service Channel",
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager?.createNotificationChannel(serviceChannel)
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Kiosk Power Monitoring")
            .setContentText("Monitoring charging state...")
            .setSmallIcon(android.R.drawable.ic_lock_idle_charging)
            .build()
    }
}
