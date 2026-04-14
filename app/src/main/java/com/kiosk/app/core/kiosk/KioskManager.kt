package com.kiosk.app.core.kiosk

import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.ComponentName

class KioskManager(private val activity: Activity) {

    fun startKiosk() {
        val dpm = activity.getSystemService(DevicePolicyManager::class.java)
        val admin = ComponentName(activity, DeviceAdminReceiver::class.java)

        dpm.setLockTaskPackages(admin, arrayOf(activity.packageName))
        activity.startLockTask()
    }
}