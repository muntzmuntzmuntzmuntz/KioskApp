package com.kiosk.app.ui.launcher

import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.Typeface
import android.media.AudioManager
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kiosk.app.core.data.AppRepository
import com.kiosk.app.core.system.PowerEventManager
import com.kiosk.app.ui.base.BaseActivity
import com.kiosk.app.ui.locked.LockedActivity
import com.kiosk.app.ui.pin.PinUnlockActivity
import androidx.core.graphics.toColorInt
import com.kiosk.app.R
import com.kiosk.app.core.kiosk.KioskManager
import java.io.File

class LauncherActivity : BaseActivity() {

    private lateinit var recycler: RecyclerView
    private lateinit var adapter: LauncherAdapter
    private lateinit var repo: AppRepository
    lateinit var content: FrameLayout
    private var tapCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        KioskManager.setupFeatures(this)
        KioskManager.startKiosk(this)
        KioskManager.setAsDefaultLauncher(this)

        Log.d("Char Launch", ">>>>>>>>>>>>>")

        repo = AppRepository(this)

        val typeFace = ResourcesCompat.getFont(this, R.font.orbitron)

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
            text = "Dashboard"
            textSize = 25f
            setTypeface(typeFace, Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            letterSpacing = 0.09f
        }

        val settings = ImageView(this).apply {
            setImageResource(android.R.drawable.ic_menu_more)
            layoutParams = LinearLayout.LayoutParams(dp(40), dp(40))

            setOnClickListener {
                requestWriteSettingsPermission()
                showSettingsDialog()
            }
        }

        val closeApp = ImageView(this).apply {
            setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
            layoutParams = LinearLayout.LayoutParams(dp(40), dp(40))

            setOnClickListener {
                closeApp()
            }
        }

        // ================= BATTERY =================
        val batteryRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, 15, 20, dp(0))
        }

        val batteryIcon = TextView(this).apply {
            text = "🔋"
            textSize = 18f
            setPadding(0, 0, dp(0), 0)
        }

        val batteryLevel = getBatteryPercentage()
        val batteryText = TextView(this).apply {
            text = "$batteryLevel%"
            textSize = 10f
            setTextColor(Color.BLACK)
        }

        batteryRow.addView(batteryIcon)
        batteryRow.addView(batteryText)


        header.addView(title)
        header.addView(batteryRow)
        header.addView(settings)

//        // TODO: remove this on deploy!
        header.addView(closeApp)

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

        if (batteryLevel < 50) {
            val hibernating = TextView(this).apply {
                text = "Not enough battery level. Hibernating..."
                textSize = 70f
//                setTextColor(Color.parseColor("#E0E0E0"))
                setTypeface(typeFace, Typeface.BOLD)
                setTextColor(Color.BLACK)
                gravity = Gravity.CENTER
                letterSpacing = 0.05f
            }
            content.addView(hibernating)
        } else {
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

    override fun onBackPressed() {
        super.onBackPressed()
        Log.d("DDD", "Launcher Activity back pressed!")

    }

    override fun onStart() {
        super.onStart()
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onPause() {
        super.onPause()
        Log.d("DDD", "Launcher Activity paused!")
    }

    override fun onResume() {
        super.onResume()

        if (KioskManager.isDeviceOwner(this)) {
            enableKioskMode()
        }
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

    private fun getBatteryPercentage(): Int {
        val intent = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            ?: return -1

        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)

        return if (level >= 0 && scale > 0) {
            (level * 100 / scale.toFloat()).toInt()
        } else {
            -1
        }
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
            KioskManager.startKiosk(this)
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

    // TODO: For development only.
    private fun closeApp() {
        throw RuntimeException("Test crash triggered manually")
    }
}