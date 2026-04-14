package com.kiosk.app.core.data

import android.content.Context

class PrefsManager(context: Context) {

    private val prefs = context.getSharedPreferences("kiosk_prefs", Context.MODE_PRIVATE)

    fun saveEnabledApps(set: Set<String>) {
        prefs.edit().putStringSet("enabled_apps", set).apply()
    }

    fun getEnabledApps(): Set<String> {
        return prefs.getStringSet("enabled_apps", emptySet()) ?: emptySet()
    }

    fun getPin(): String {
        return prefs.getString("pin", "1234") ?: "1234"
    }
}