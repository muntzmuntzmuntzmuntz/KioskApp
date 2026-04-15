package com.kiosk.app.ui.launcher

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.media.AudioManager
import android.os.BatteryManager
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kiosk.app.core.data.AppRepository
import com.kiosk.app.core.system.PowerEventManager
import com.kiosk.app.ui.base.BaseActivity
import com.kiosk.app.ui.locked.LockedActivity
import com.kiosk.app.ui.pin.PinUnlockActivity
import androidx.core.graphics.toColorInt
import java.io.File

class LauncherActivity : BaseActivity() {

    private lateinit var recycler: RecyclerView
    private lateinit var adapter: LauncherAdapter
    private lateinit var repo: AppRepository
    private lateinit var powerManager: PowerEventManager
    lateinit var content: FrameLayout

    private var tapCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!isCharging()) {
            startActivity(Intent(this, LockedActivity::class.java))
            finish()
            return
        }
        repo = AppRepository(this)

        powerManager = PowerEventManager(this, object : PowerEventManager.Listener {
            override fun onPowerConnected() {}
            override fun onPowerDisconnected() {
                startActivity(Intent(this@LauncherActivity, LockedActivity::class.java))
                finish()
            }
        })

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#F5F7FA"))
        }

        // ================= HEADER =================
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(Color.WHITE)
            setPadding(dp(24), dp(16), dp(24), dp(16))
        }

        val title = TextView(this).apply {
            text = "Cebu Piso Tab"
            textSize = 20f
            setTypeface(null, Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }

        val settings = ImageView(this).apply {
            setImageResource(android.R.drawable.ic_menu_manage)
            layoutParams = LinearLayout.LayoutParams(dp(40), dp(40))

            setOnClickListener {
                requestWriteSettingsPermission()
                showSettingsDialog()
            }
        }

//        val closeBtn = ImageView(this).apply {
//            setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
//            layoutParams = LinearLayout.LayoutParams(dp(32), dp(32))
//
//            setOnClickListener {
//                closeApp()
//            }
//        }

        header.addView(title)
        header.addView(settings)
//        header.addView(closeBtn)

        // ================= CONTENT =================
        content = FrameLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        }

        recycler = RecyclerView(this).apply {
            layoutManager = GridLayoutManager(this@LauncherActivity, calculateSpanCount())
            setPadding(dp(24), dp(24), dp(24), dp(24))
        }

        adapter = LauncherAdapter {
            val intent = packageManager.getLaunchIntentForPackage(it.packageName)
            if (intent != null) startActivity(intent)
        }

        val apps = repo.getFilteredApps()

        if (apps.isEmpty()) {
            val empty = TextView(this).apply {
                text = "No apps available. Please contact Administrator."
                textSize = 18f
                setTextColor(Color.GRAY)
                gravity = Gravity.CENTER
            }
            content.addView(empty)
        } else {
            recycler.adapter = adapter
            adapter.submitList(apps)
            content.addView(recycler)
        }

        root.setOnClickListener {
            tapCount++
            if (tapCount >= 5) {
                startActivity(Intent(this, PinUnlockActivity::class.java))
                tapCount = 0
            }
        }

        root.addView(header)
        root.addView(content)

        setContentView(root)
    }

    override fun onStart() {
        super.onStart()
        powerManager.register()
    }

    override fun onStop() {
        super.onStop()
        powerManager.unregister()
    }

    override fun onResume() {
        super.onResume()

        enableKioskMode()
        applyLauncherBackground()
    }

    // ================= SETTINGS MODAL =================
    private fun showSettingsDialog() {

        val dialog = android.app.Dialog(this)
        dialog.setCancelable(true)

        val root = FrameLayout(this).apply {
            setBackgroundColor(Color.parseColor("#66000000")) // dim background
        }

        // 🔥 CARD CONTAINER
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(24), dp(24), dp(24), dp(24))

            background = android.graphics.drawable.GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = dp(20).toFloat()
            }

            elevation = dp(12).toFloat()

            layoutParams = FrameLayout.LayoutParams(
                (resources.displayMetrics.widthPixels * 0.5).toInt(),
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER
            )
        }

        // TITLE
        val title = TextView(this).apply {
            text = "Settings"
            textSize = 18f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.BLACK)
            setPadding(0, 0, 0, dp(16))
        }

        // ================= VOLUME =================
        val volumeRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val volumeIcon = TextView(this).apply {
            text = "🔊"
            textSize = 18f
            setPadding(0, 0, dp(12), 0)
        }

        val audioManager = getSystemService(AudioManager::class.java)

        val volumeSeek = SeekBar(this).apply {
            max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            progress = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }

        volumeSeek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        volumeRow.addView(volumeIcon)
        volumeRow.addView(volumeSeek)

        // ================= BRIGHTNESS =================
        val brightnessRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(16), 0, 0)
        }

        val brightnessIcon = TextView(this).apply {
            text = "☀️"
            textSize = 18f
            setPadding(0, 0, dp(12), 0)
        }

        val brightnessSeek = SeekBar(this).apply {
            max = 255
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }

        try {
            brightnessSeek.progress = Settings.System.getInt(
                contentResolver,
                Settings.System.SCREEN_BRIGHTNESS
            )
        } catch (e: Exception) {
            brightnessSeek.progress = 125
        }

        brightnessSeek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {

                if (Settings.System.canWrite(this@LauncherActivity)) {

                    Settings.System.putInt(
                        contentResolver,
                        Settings.System.SCREEN_BRIGHTNESS_MODE,
                        Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
                    )

                    Settings.System.putInt(
                        contentResolver,
                        Settings.System.SCREEN_BRIGHTNESS,
                        progress
                    )
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        brightnessRow.addView(brightnessIcon)
        brightnessRow.addView(brightnessSeek)

        // CLOSE BUTTON
        val close = TextView(this).apply {
            text = "Close"
            textSize = 14f
            setTextColor(Color.GRAY)
            gravity = Gravity.END
            setPadding(0, dp(20), 0, 0)

            setOnClickListener { dialog.dismiss() }
        }

        // BUILD
        card.addView(title)
        card.addView(volumeRow)
        card.addView(brightnessRow)
        card.addView(close)

        root.addView(card)

        dialog.setContentView(root)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }

    private fun requestWriteSettingsPermission() {
        if (!Settings.System.canWrite(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_WRITE_SETTINGS,
                android.net.Uri.parse("package:$packageName")
            )
            startActivity(intent)
        }
    }

    private fun isCharging(): Boolean {
        val intent = registerReceiver(null, android.content.IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        return status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL
    }

    private fun calculateSpanCount(): Int {
        val screenWidth = resources.displayMetrics.widthPixels
        val tileMinWidth = dp(140)
        return (screenWidth / tileMinWidth).coerceAtLeast(3)
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }

    private fun enableKioskMode() {
        val dpm = getSystemService(android.app.admin.DevicePolicyManager::class.java)
        val admin = android.content.ComponentName(
            this,
            com.kiosk.app.core.admin.MyDeviceAdminReceiver::class.java
        )

        // 🔥 get saved apps
        val prefs = com.kiosk.app.core.data.PrefsManager(this)
        val enabledApps = prefs.getEnabledApps()

        val allowed = mutableListOf(packageName)
        allowed.addAll(enabledApps)

        dpm.setLockTaskPackages(admin, allowed.toTypedArray())

        if (!isInLockTaskMode()) {
            startLockTask()
        }
    }

    private fun isInLockTaskMode(): Boolean {
        val am = getSystemService(android.app.ActivityManager::class.java)
        return am.lockTaskModeState != android.app.ActivityManager.LOCK_TASK_MODE_NONE
    }

    private fun applyLauncherBackground() {

        val file = File(filesDir, "launcher_bg.jpg")

        if (file.exists()) {
            val bitmap = android.graphics.BitmapFactory.decodeFile(file.absolutePath)
            val drawable = android.graphics.drawable.BitmapDrawable(resources, bitmap)
            content.background = drawable
        } else {
            content.setBackgroundColor(Color.parseColor("#F5F7FA"))
        }
    }

    private fun closeApp() {
        finishAffinity()
        android.os.Process.killProcess(android.os.Process.myPid())
    }
}