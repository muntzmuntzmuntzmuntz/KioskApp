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

    fun setWallpaperTexts(title: String, subtitle: String) {
        prefs.edit()
            .putString("wallpaper_title", title)
            .putString("wallpaper_subtitle", subtitle)
            .apply()
    }

    fun getWallpaperTitle(): String {
        return prefs.getString("wallpaper_title", "INSERT COIN!") ?: "INSERT COIN!"
    }

    fun getWallpaperSubtitle(): String {
        return prefs.getString("wallpaper_subtitle", "To continue playing") ?: "To continue playing"
    }

    fun setActivated(activated: Boolean, activationCode: String? = null) {
        prefs.edit()
            .putBoolean("device_activated", activated)
            .putString("activation_code", activationCode)
            .apply()
    }

    fun isActivated(): Boolean {
        return prefs.getBoolean("device_activated", false)
    }

    fun getActivationCode(): String? {
        return prefs.getString("activation_code", null)
    }

    fun setServerUrl(url: String) {
        prefs.edit { putString("server_url", url) }
    }

    fun getServerUrl(): String {
        return prefs.getString("server_url", "http://localhost:3000") ?: "http://localhost:3000"
    }
}