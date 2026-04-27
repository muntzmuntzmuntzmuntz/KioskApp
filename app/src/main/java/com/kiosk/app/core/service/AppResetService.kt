package com.kiosk.app.core.service

import android.app.Service
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.kiosk.app.core.admin.MyDeviceAdminReceiver
import com.kiosk.app.core.data.PrefsManager

class AppResetService : Service() {

    private var countDownTimer: CountDownTimer? = null
    private val countDownTime: Long = 5_000

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        if (action == ACTION_CANCEL_RESET) {
            Log.d("AppResetService", "Reset cancelled")
            stopResetTimer()
            stopSelf()
        } else {
            Log.d("AppResetService", "Reset started")
            startResetTimer()
        }
        return START_NOT_STICKY
    }

    private fun startResetTimer() {
        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(countDownTime, countDownTime) {
            override fun onTick(millisUntilFinished: Long) {
                // Not really needed for a 5s timer with 5s interval, but following original logic
                Toast.makeText(this@AppResetService, "Clearing login data in 5 seconds", Toast.LENGTH_SHORT).show()
            }

            override fun onFinish() {
                Log.d("AppResetService", "Reset timer finished, performing reset")
                performReset()
                stopSelf()
            }
        }.start()
    }

    private fun stopResetTimer() {
        countDownTimer?.cancel()
        countDownTimer = null
    }

    private fun performReset() {
        val dpm = getSystemService(DevicePolicyManager::class.java)
        val admin = ComponentName(this, MyDeviceAdminReceiver::class.java)
        val prefs = PrefsManager(this)
        val apps = prefs.getEnabledApps()

        if (apps.isEmpty()) {
            Log.d("AppResetService", "No apps to reset")
            return
        }

        val executor = ContextCompat.getMainExecutor(this)
        var completed = 0
        val targetApps = apps.filter { it != packageName && !SKIP_PACKAGES.contains(it) }
        val total = targetApps.size

        if (total == 0) {
            Log.d("AppResetService", "No target apps to reset (only self or skipped)")
            return
        }

        targetApps.forEach { pkg ->
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    dpm.clearApplicationUserData(admin, pkg, executor) { _, _ ->
                        completed++
                        if (completed == total) {
                            Toast.makeText(this, "Apps reset complete", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("AppResetService", "Failed to reset $pkg", e)
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        stopResetTimer()
        super.onDestroy()
    }

    companion object {
        const val ACTION_CANCEL_RESET = "com.kiosk.app.ACTION_CANCEL_RESET"
        val SKIP_PACKAGES = listOf(
            "com.mobile.legends"
        )
    }
}
