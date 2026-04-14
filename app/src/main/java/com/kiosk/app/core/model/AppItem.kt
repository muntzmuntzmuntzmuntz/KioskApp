package com.kiosk.app.core.model

import android.graphics.drawable.Drawable

data class AppItem(
    val name: String,
    val packageName: String,
    val icon: Drawable,
    var enabled: Boolean = true
)