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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Start the power management service if it's not running
        val intent = Intent(this, com.kiosk.app.core.service.PowerManagementService::class.java)
        startForegroundService(intent)
    }

    override fun onResume() {
        super.onResume()
        hideSystemUI()
    }

    override fun onPause() {
        super.onPause()
        Log.d("DDD", "Base Activity onPause!")
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
}