package com.kiosk.app.core.system

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.kiosk.app.ui.launcher.LauncherActivity
import com.kiosk.app.ui.locked.LockedActivity

class ChargingReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        val target = when (intent.action) {
            Intent.ACTION_POWER_CONNECTED -> LauncherActivity::class.java
            Intent.ACTION_POWER_DISCONNECTED -> LockedActivity::class.java
            else -> return
        }

        context.startActivity(
            Intent(context, target).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
        )
    }
}