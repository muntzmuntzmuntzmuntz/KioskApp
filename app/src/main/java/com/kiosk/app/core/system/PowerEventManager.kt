package com.kiosk.app.core.system

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter

class PowerEventManager(
    private val context: Context,
    private val listener: Listener
) {

    interface Listener {
        fun onPowerConnected()
        fun onPowerDisconnected()
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {

            when (intent?.action) {
                Intent.ACTION_POWER_CONNECTED -> listener.onPowerConnected()
                Intent.ACTION_POWER_DISCONNECTED -> listener.onPowerDisconnected()
            }
        }
    }

    fun register() {
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
        }
        context.registerReceiver(receiver, filter)
    }

    fun unregister() {
        context.unregisterReceiver(receiver)
    }
}