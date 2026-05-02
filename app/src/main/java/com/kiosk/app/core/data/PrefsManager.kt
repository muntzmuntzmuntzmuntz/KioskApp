package com.kiosk.app.core.data

import android.content.Context
import androidx.core.content.edit
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class PrefsManager(context: Context) {

    private val prefs = context.getSharedPreferences("KIOSK_PREFS", Context.MODE_PRIVATE)
    private val localDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val isoFormats = listOf(
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX", Locale.US),
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX", Locale.US),
    ).onEach { it.timeZone = TimeZone.getTimeZone("UTC") }

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

    fun setActivated(activated: Boolean, activationCode: String? = null, expiresAt: String? = null) {
        prefs.edit {
            putBoolean("device_activated", activated)

            if (activated) {
                putString("activation_code", activationCode)
                putString("activation_expires_at", expiresAt)
            } else {
                remove("activation_code")
                remove("activation_expires_at")
                remove("activation_last_validated_date")
                remove("activation_last_validated_at")
            }
        }
    }

    fun isActivated(): Boolean {
        return prefs.getBoolean("device_activated", false)
    }

    fun getActivationCode(): String? {
        return prefs.getString("activation_code", null)
    }

    fun getActivationExpiresAt(): String? {
        return prefs.getString("activation_expires_at", null)
    }

    fun markActivationValidatedToday(now: Long = System.currentTimeMillis()) {
        prefs.edit {
            putString("activation_last_validated_date", localDateFormat.format(Date(now)))
            remove("activation_last_validated_at")
        }
    }

    fun shouldValidateActivation(now: Long = System.currentTimeMillis()): Boolean {
        val lastValidatedDate = prefs.getString("activation_last_validated_date", null)
        val today = localDateFormat.format(Date(now))
        return lastValidatedDate != today
    }

    fun isActivationExpired(now: Long = System.currentTimeMillis()): Boolean {
        val expiresAt = getActivationExpiresAt() ?: return false
        val expiresAtMs = parseIsoTimestamp(expiresAt) ?: return false
        return now >= expiresAtMs
    }

    fun expireActivationNow() {
        if (!isActivated()) {
            return
        }

        val expiredAt = isoFormats.first().format(java.util.Date())
        prefs.edit { putString("activation_expires_at", expiredAt) }
    }

    private fun parseIsoTimestamp(value: String): Long? {
        for (format in isoFormats) {
            try {
                return format.parse(value)?.time
            } catch (_: ParseException) {
            }
        }

        return null
    }
}
