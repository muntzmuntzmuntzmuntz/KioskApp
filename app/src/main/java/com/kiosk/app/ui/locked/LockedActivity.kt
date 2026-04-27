package com.kiosk.app.ui.locked

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.res.ResourcesCompat
import com.kiosk.app.R
import com.kiosk.app.core.data.PrefsManager
import com.kiosk.app.ui.base.BaseActivity

class LockedActivity : BaseActivity() {

    private lateinit var prefs: PrefsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = PrefsManager(this)

        val typeFace = ResourcesCompat.getFont(this, R.font.orbitron)

        Log.d("Char Locked", ">>>>>>>>>>>>>")

        // ================= ROOT =================
        val root = FrameLayout(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }

        // ================= BACKGROUND IMAGE =================
        val bgImage = ImageView(this).apply {

            val file = java.io.File(filesDir, "lock_bg.jpg")

            if (file.exists()) {
                setImageURI(android.net.Uri.fromFile(file))
            } else {
                setImageResource(R.drawable.bg_gaming) // fallback
            }

            scaleType = ImageView.ScaleType.CENTER_CROP

            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }

        // ================= DARK OVERLAY =================
        val overlay = View(this).apply {
            setBackgroundColor(Color.parseColor("#99000000")) // adjust darkness

            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }

        // ================= CONTENT CONTAINER =================
        val container = FrameLayout(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            setPadding(dp(24), dp(24), dp(24), dp(24))
        }


        // ================= GLASS CARD =================
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(28), dp(28), dp(28), dp(28))

            layoutParams = FrameLayout.LayoutParams(
                1000,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER
            ).apply {
                marginStart = dp(16)
                marginEnd = dp(16)
            }

            elevation = dp(12).toFloat()
        }

        val titleText = prefs.getWallpaperTitle()

        // ================= TITLE =================
        val title = TextView(this).apply {
            text = titleText
            textSize = 100f
            setTypeface(typeFace, Typeface.BOLD)
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            letterSpacing = 0.05f
        }

        val descText = prefs.getWallpaperSubtitle()
        // ================= DESCRIPTION =================
        val desc = TextView(this).apply {
            text = descText
            setTypeface(typeFace)
            textSize = 30f
            setTextColor(Color.parseColor("#E0E0E0"))
            gravity = Gravity.CENTER
            setPadding(0, dp(12), 0, dp(20))
        }

        // ================= FOOTER =================
        val footer = TextView(this).apply {
            text = "Cebu Piso Tab and Rentals"
            textSize = 14f
            setTextColor(Color.parseColor("#B0BEC5"))
            gravity = Gravity.CENTER
        }

        // ================= BUILD =================
//        card.addView(icon)
        card.addView(title)
        card.addView(desc)
        card.addView(footer)

        container.addView(card)

        root.addView(bgImage)
        root.addView(overlay)
        root.addView(container)

        setContentView(root)
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}
