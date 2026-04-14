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
        }.sortedBy { it.name.lowercase() }
    }

//    fun getAllApps(): List<AppItem> {
//        val pm = context.packageManager
//
//        return pm.getInstalledApplications(0).mapNotNull {
//            val launchIntent = pm.getLaunchIntentForPackage(it.packageName)
//            if (launchIntent != null) {
//                AppItem(
//                    name = pm.getApplicationLabel(it).toString(),
//                    packageName = it.packageName,
//                    icon = pm.getApplicationIcon(it)
//                )
//            } else null
//        }.sortedBy { it.name }
//    }

    fun getFilteredApps(): List<AppItem> {
        val prefs = PrefsManager(context)
        val enabled = prefs.getEnabledApps()

        val all = getAllApps()

        // ✅ If empty → return empty list (not all apps)
        if (enabled.isEmpty()) return emptyList()

        return all.filter { enabled.contains(it.packageName) }
    }
}