package com.kiosk.app.core.data

import android.content.Context
import androidx.core.content.edit

class PrefsManager(context: Context) {

    private val prefs = context.getSharedPreferences("KIOSK_PREFS", Context.MODE_PRIVATE)

    fun saveEnabledApps(set: Set<String>) {
        prefs.edit { putStringSet("enabled_apps", set) }
    }

    fun getEnabledApps(): Set<String> {
        return prefs.getStringSet("enabled_apps", emptySet()) ?: emptySet()
    }

    fun getPin(): String? {
        return prefs.getString("admin_pin", "1234")
    }

    fun savePin(newPin: String) {
        prefs.edit { putString("admin_pin", newPin) }
    }
}