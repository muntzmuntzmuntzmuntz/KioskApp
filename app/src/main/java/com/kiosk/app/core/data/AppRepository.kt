package com.kiosk.app.core.data

import android.content.Context
import android.content.Intent
import com.kiosk.app.core.model.AppItem

class AppRepository(private val context: Context) {
    fun getAllApps(): List<AppItem> {
        val pm = context.packageManager

        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolveInfos = pm.queryIntentActivities(intent, 0)

        return resolveInfos.map {
            val label = it.loadLabel(pm).toString()
            val packageName = it.activityInfo.packageName
            val icon = it.loadIcon(pm)

            AppItem(
                name = label,
                packageName = packageName,
                icon = icon,
                enabled = false
            )
        }.filter { it.packageName != context.packageName }
            .sortedBy { it.name.lowercase() }
    }

    fun getFilteredApps(): List<AppItem> {
        val prefs = PrefsManager(context)
        val enabled = prefs.getEnabledApps()

        val all = getAllApps()

        // ✅ If empty → return empty list (not all apps)
        if (enabled.isEmpty()) return emptyList()

        return all.filter { enabled.contains(it.packageName) }
    }
}