package com.kiosk.app.ui.locked

import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.BatteryManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.kiosk.app.R
import com.kiosk.app.ui.base.BaseActivity
import com.kiosk.app.ui.launcher.LauncherActivity

class LockedActivity : BaseActivity() {

    private val handler = Handler(Looper.getMainLooper())

    private val checkPowerRunnable = object : Runnable {
        override fun run() {
            if (isCharging()) {
                goToLauncher()
                return
            }
            handler.postDelayed(this, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ================= ROOT =================
        val root = FrameLayout(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }

        // ================= BACKGROUND IMAGE =================
        val bgImage = ImageView(this).apply {
            setImageResource(R.drawable.bg_gaming) // 🔥 your image here
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
                700,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER
            ).apply {
                marginStart = dp(16)
                marginEnd = dp(16)
            }

//            background = GradientDrawable().apply {
//                setColor(Color.parseColor("#1AFFFFFF")) // glass effect
//                cornerRadius = dp(20).toFloat()
//            }

            elevation = dp(12).toFloat()
        }

        // ================= ICON =================
        val icon = ImageView(this).apply {
            setImageResource(R.drawable.ic_peso)
            setColorFilter(Color.parseColor("#80D8FF"))

            layoutParams = LinearLayout.LayoutParams(dp(56), dp(56)).apply {
                bottomMargin = dp(16)
            }
        }

        // ================= TITLE =================
        val title = TextView(this).apply {
            text = "INSERT COIN"
            textSize = 40f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
        }

        // ================= DESCRIPTION =================
        val desc = TextView(this).apply {
            text = "Connect charger to continue"
            textSize = 15f
            setTextColor(Color.parseColor("#B0BEC5"))
            gravity = Gravity.CENTER
            setPadding(0, dp(10), 0, dp(20))
        }

        // ================= FOOTER =================
        val footer = TextView(this).apply {
            text = "Kiosk System"
            textSize = 13f
            setTextColor(Color.parseColor("#78909C"))
            gravity = Gravity.CENTER
        }

        // ================= BUILD =================
        card.addView(icon)
        card.addView(title)
        card.addView(desc)
        card.addView(footer)

        container.addView(card)

        root.addView(bgImage)
        root.addView(overlay)
        root.addView(container)

        setContentView(root)
    }

    override fun onResume() {
        super.onResume()
        handler.post(checkPowerRunnable)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(checkPowerRunnable)
    }

    private fun goToLauncher() {
        startActivity(Intent(this, LauncherActivity::class.java))
        finish()
    }

    private fun isCharging(): Boolean {
        val intent = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        return status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}