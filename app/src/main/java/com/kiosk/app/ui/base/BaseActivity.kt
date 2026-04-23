package com.kiosk.app.ui.base

import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.kiosk.app.ui.launcher.LauncherActivity
import com.kiosk.app.ui.locked.LockedActivity

abstract class BaseActivity : AppCompatActivity() {

    private var lastChargingState = false
    private val handler = Handler(Looper.getMainLooper())

    private val powerCheck = object : Runnable {
        override fun run() {

            val charging = isCharging()

            if (charging != lastChargingState) {
                lastChargingState = charging


                when (this@BaseActivity) {
                    is LauncherActivity -> {
                        if (!charging) goToLocked()
                    }

                    is LockedActivity -> {
                        if (charging) goToLauncher()
                    }
                }

            }

            handler.postDelayed(this, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        handler.post(powerCheck)
        super.onCreate(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        hideSystemUI()
    }

    override fun onPause() {
        super.onPause()
        handler.post(powerCheck)
        Log.d("DDD", "Base Activity back onPause!")
    }

    private fun hideSystemUI() {
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
    }

    override fun onBackPressed() {
        // disable back
        Log.d("DDD", "Base Activity back pressed!")
    }

    // 🔋 Reliable power detection
    private fun isCharging(): Boolean {
        val intent = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            ?: return false

        val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0)
        val isCharging = plugged != 0

        Log.d("Char POWER_DEBUG", "plugged=$plugged charging=$isCharging")
        return isCharging
    }

    // 🔁 Navigation helpers
    private fun goToLocked() {
        if (this is LockedActivity) return

        val intent = Intent(this, LockedActivity::class.java).apply {
            addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
            )
        }
        startActivity(intent)
    }

    private fun goToLauncher() {
        if (this is LauncherActivity) return
        startActivity(Intent(this, LauncherActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        })
//        finish()
    }
}