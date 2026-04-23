package com.kiosk.app.core.kiosk

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.util.Log
import com.kiosk.app.core.admin.MyDeviceAdminReceiver

object KioskManager {

    private const val TAG = "KioskManager"

    private fun getDpm(context: Context): DevicePolicyManager {
        return context.getSystemService(DevicePolicyManager::class.java)
    }

    private fun getAdmin(context: Context): ComponentName {
        return ComponentName(
            context,
            MyDeviceAdminReceiver::class.java
        )
    }

    fun isDeviceOwner(context: Context): Boolean {
        return getDpm(context).isDeviceOwnerApp(context.packageName)
    }

    // 🔒 Safe LockTask start
    fun startKiosk(context: Context) {
        try {
            if (context is android.app.Activity && isDeviceOwner(context)) {
                context.startLockTask()
            }
        } catch (e: Exception) {
            Log.e(TAG, "startKiosk failed", e)
        }
    }

    // 🔓 Safe LockTask stop
    fun stopKiosk(context: Context) {
        try {
            if (context is android.app.Activity) {
                context.stopLockTask()
            }
        } catch (e: Exception) {
            Log.e(TAG, "stopKiosk failed", e)
        }
    }

    // ⚙️ Safe feature setup
    fun setupFeatures(context: Context) {
        try {
            val dpm = getDpm(context)

            if (!isDeviceOwner(context)) return

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                dpm.setLockTaskFeatures(
                    getAdmin(context),
                    DevicePolicyManager.LOCK_TASK_FEATURE_HOME or
                            DevicePolicyManager.LOCK_TASK_FEATURE_SYSTEM_INFO
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "setupFeatures failed", e)
        }
    }

    fun setAsDefaultLauncher(context: Context) {
        try {
            val dpm = getDpm(context)

            if (!isDeviceOwner(context)) return

            val filter = IntentFilter(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                addCategory(Intent.CATEGORY_DEFAULT)
            }

            dpm.addPersistentPreferredActivity(
                getAdmin(context),
                filter,
                android.content.ComponentName(
                    context,
                    com.kiosk.app.ui.launcher.LauncherActivity::class.java
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "setupFeatures failed", e)
        }
    }
}