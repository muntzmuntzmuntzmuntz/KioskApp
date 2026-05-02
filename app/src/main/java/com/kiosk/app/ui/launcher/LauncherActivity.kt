package com.kiosk.app.ui.launcher

import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.Typeface
import android.media.AudioManager
import android.os.BatteryManager
import android.os.Bundle
import android.provider.Settings
import android.text.InputFilter
import android.text.InputType
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kiosk.app.R
import com.kiosk.app.core.data.ApiClient
import com.kiosk.app.core.data.AppRepository
import com.kiosk.app.core.data.PrefsManager
import com.kiosk.app.core.kiosk.KioskManager
import com.kiosk.app.core.model.AppItem
import com.kiosk.app.ui.base.BaseActivity
import com.kiosk.app.ui.pin.PinUnlockActivity
import kotlinx.coroutines.launch
import java.io.File
import java.util.Locale

class LauncherActivity : BaseActivity() {
    private lateinit var recycler: RecyclerView
    private lateinit var adapter: LauncherAdapter
    private lateinit var repo: AppRepository
    lateinit var content: FrameLayout
    private var tapCount = 0
    private lateinit var prefs: PrefsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prefs = PrefsManager(this)

        KioskManager.setupFeatures(this)
        KioskManager.startKiosk(this)
        KioskManager.setAsDefaultLauncher(this)

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
            setTextColor(Color.BLACK)
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

        // TODO: remove for deployments
//        val expireActivation = TextView(this).apply {
//            text = "EXP"
//            gravity = Gravity.CENTER
//            textSize = 10f
//            setTextColor(Color.WHITE)
//            background = android.graphics.drawable.GradientDrawable().apply {
//                setColor(Color.parseColor("#DC2626"))
//                cornerRadius = dp(8).toFloat()
//            }
//            layoutParams = LinearLayout.LayoutParams(dp(40), dp(40)).apply {
//                marginEnd = dp(8)
//            }
//
//            setOnClickListener {
//                prefs.expireActivationNow()
//                recreate()
//            }
//        }

        // TODO: remove for deployments
//        val checkActivation = TextView(this).apply {
//            text = "CHK"
//            gravity = Gravity.CENTER
//            textSize = 10f
//            setTextColor(Color.WHITE)
//            background = android.graphics.drawable.GradientDrawable().apply {
//                setColor(Color.parseColor("#2563EB"))
//                cornerRadius = dp(8).toFloat()
//            }
//            layoutParams = LinearLayout.LayoutParams(dp(40), dp(40)).apply {
//                marginEnd = dp(8)
//            }
//        }

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

        // TODO: remove this on deploy!
//        header.addView(checkActivation)
//        header.addView(expireActivation)
//        header.addView(closeApp)

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

        showActivationGate(typeFace, batteryLevel, apps)

//        checkActivation.setOnClickListener {
//            showActivationGate(typeFace, batteryLevel, apps, forceValidation = true)
//        }

        header.setOnClickListener {
            tapCount++
            if (tapCount >= 5 && prefs.isActivated()) {
                startActivity(Intent(this, PinUnlockActivity::class.java))
                tapCount = 0
            }
        }

        root.addView(header)
        root.addView(content)

        setContentView(root)
    }

    private fun showActivationPrompt(typeFace: Typeface?, reason: String?, prefs: PrefsManager) {
        val apiClient = ApiClient()
        clearContent()

        val wrapper = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(24), dp(24), dp(24), dp(24))
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(24), dp(24), dp(24), dp(24))
            background = android.graphics.drawable.GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = dp(12).toFloat()
                setStroke(dp(1), Color.parseColor("#E5E7EB"))
            }
            elevation = dp(4).toFloat()
            layoutParams = LinearLayout.LayoutParams(
                (resources.displayMetrics.widthPixels * 0.55f).toInt()
                    .coerceIn(dp(320), dp(520)),
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        val title = TextView(this).apply {
            text = "Device Activation"
            textSize = 24f
            setTypeface(typeFace, Typeface.BOLD)
            setTextColor(Color.BLACK)
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, dp(8))
        }

        val message = TextView(this).apply {
            text = "Enter your activation code to unlock this kiosk."
            textSize = 14f
            setTextColor(Color.GRAY)
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, dp(20))
        }

        val codeInput = EditText(this).apply {
            hint = "Activation code"
            textSize = 18f
            setTextColor(Color.BLACK)
            setHintTextColor(Color.GRAY)
            gravity = Gravity.CENTER
            setSingleLine(true)
            setTypeface(Typeface.MONOSPACE)
//            filters = arrayOf(InputFilter.AllCaps())
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
            setPadding(dp(16), dp(14), dp(16), dp(14))
            background = android.graphics.drawable.GradientDrawable().apply {
                setColor(Color.parseColor("#F8F9FA"))
                cornerRadius = dp(8).toFloat()
                setStroke(dp(1), Color.parseColor("#D1D5DB"))
            }
        }

        val statusText = TextView(this).apply {
            textSize = 13f
            setTextColor(Color.GRAY)
            gravity = Gravity.CENTER
            setPadding(0, dp(12), 0, 0)
            visibility = View.GONE
        }

        val progressBar = ProgressBar(this).apply {
            visibility = View.GONE
        }

        val activateButton = Button(this).apply {
            text = "Verify Code"
            textSize = 16f
            setTextColor(Color.WHITE)
            setPadding(dp(24), dp(14), dp(24), dp(14))
            background = android.graphics.drawable.GradientDrawable().apply {
                setColor(Color.parseColor("#111827"))
                cornerRadius = dp(8).toFloat()
            }
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(16)
            }
        }

        fun showStatus(message: String, color: Int) {
            statusText.text = message
            statusText.setTextColor(color)
            statusText.visibility = if (message.isEmpty()) View.GONE else View.VISIBLE
        }

        fun setLoading(loading: Boolean) {
            progressBar.visibility = if (loading) View.VISIBLE else View.GONE
            activateButton.isEnabled = !loading
            codeInput.isEnabled = !loading
        }

        if (reason != null) {
            showStatus(activationFailureMessage(reason), Color.RED)
        }

        fun verifyCode() {
            val code = codeInput.text.toString();

            if (code.isEmpty()) {
                showStatus("Please enter an activation code.", Color.RED)
                return
            }

            if (code.length < 6) {
                showStatus("Activation code must be at least 6 characters.", Color.RED)
                return
            }

            val deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)

            setLoading(true)
            showStatus("", Color.GRAY)

            lifecycleScope.launch {
                val result = apiClient.activateDevice(code, deviceId)

                result.onSuccess { response ->
                    if (response.valid) {
                        Log.d("DDD", response.toString())
                        prefs.setActivated(true, code, response.expiresAt)
                        prefs.markActivationValidatedToday()
                        showStatus("Device activated successfully.", Color.rgb(22, 163, 74))

                        android.os.Handler(mainLooper).postDelayed({
                            recreate()
                        }, 700)
                    } else {
                        showStatus(activationFailureMessage(response.reason), Color.RED)
                    }
                }.onFailure { error ->
                    Log.e("LauncherActivity", "Activation failed", error)
                    showStatus(
                        "Could not verify code. Check the network and try again.",
                        Color.RED
                    )
                }

                setLoading(false)
            }
        }

        activateButton.setOnClickListener { verifyCode() }

        card.addView(title)
        card.addView(message)
        card.addView(codeInput)
        card.addView(statusText)
        card.addView(progressBar)
        card.addView(activateButton)
        wrapper.addView(card)
        content.addView(wrapper)


        val isExpired = prefs.isActivationExpired()

        if (isExpired) {
            showStatus("asdfasdfasdfsdf", Color.RED)
            return
        }
    }

//    private fun clearExpiredActivationIfNeeded() {
//        if (prefs.isActivated() && prefs.isActivationExpired()) {
//            prefs.setActivated(false)
//        }
//    }

    private fun showActivationGate(
        typeFace: Typeface?,
        batteryLevel: Int,
        apps: List<AppItem>,
        forceValidation: Boolean = false
    ) {
        if (prefs.isActivationExpired()) {
            prefs.setActivated(false)
            showActivationPrompt(typeFace, null, prefs)
            return
        }
        if (!prefs.isActivated()) {
            showActivationPrompt(typeFace,null , prefs)
            return
        }

        if (!forceValidation && !prefs.shouldValidateActivation()) {
            showHappyLauncher(typeFace, batteryLevel, apps)
            return
        }

        val activationCode = prefs.getActivationCode() ?: run {
            prefs.setActivated(false)
            showActivationPrompt(typeFace, null, prefs)
            return
        }

        showActivationLoading(typeFace)

        val deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        val apiClient = ApiClient()

        lifecycleScope.launch {
            val result = apiClient.validateActivationCode(activationCode, deviceId)

            result.onSuccess { response ->
                if (response.valid) {
                    prefs.setActivated(
                        true,
                        activationCode,
                        response.expiresAt
                    )
                    prefs.markActivationValidatedToday()
                    showHappyLauncher(typeFace, batteryLevel, apps)
                    return@onSuccess
                }

                prefs.setActivated(false)
                showActivationPrompt(typeFace, response.reason, prefs)
            }.onFailure { error ->
                Log.w("LauncherActivity", "Could not refresh activation status", error)
                showHappyLauncher(typeFace, batteryLevel, apps)
            }
        }
    }

    private fun showHappyLauncher(
        typeFace: Typeface?,
        batteryLevel: Int,
        apps: List<AppItem>
    ) {
        clearContent()

        if (batteryLevel < 50) {
            val hibernating = TextView(this).apply {
                text = "Not enough battery level. Hibernating..."
                textSize = 70f
                setTypeface(typeFace, Typeface.BOLD)
                setTextColor(Color.BLACK)
                gravity = Gravity.CENTER
                letterSpacing = 0.05f
            }
            content.addView(hibernating)
            return
        }

        if (apps.isEmpty()) {
            val empty = TextView(this).apply {
                text = "No apps available. Please contact Administrator."
                textSize = 18f
                setTextColor(Color.GRAY)
                gravity = Gravity.CENTER
            }
            content.addView(empty)
            return
        }

        recycler.adapter = adapter
        adapter.submitList(apps)
        content.addView(recycler)
    }

    private fun showActivationLoading(typeFace: Typeface?) {
        clearContent()

        val wrapper = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(24), dp(24), dp(24), dp(24))
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        val title = TextView(this).apply {
            text = "Checking activation..."
            textSize = 24f
            setTypeface(typeFace, Typeface.BOLD)
            setTextColor(Color.BLACK)
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, dp(16))
        }

        val progressBar = ProgressBar(this)

        wrapper.addView(title)
        wrapper.addView(progressBar)
        content.addView(wrapper)
    }

    private fun clearContent() {
        content.removeAllViews()
    }

    private fun activationFailureMessage(reason: String?): String {
        return when (reason) {
            "not_found" -> "Activation code was not found."
            "revoked" -> "Activation code has been revoked."
            // TODO: add business number
            "expired" -> "Activation code has expired. Please contact 09434540240"
            "device_mismatch" -> "Activation code is assigned to another device."
            else -> "Activation code is invalid."
        }
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
            setBackgroundColor(Color.TRANSPARENT)
        }

        // 🔥 CARD CONTAINER
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(24), dp(24), dp(24), dp(24))

            background = android.graphics.drawable.GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = dp(20).toFloat()
            }
            clipToOutline = true

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
        dialog.window?.setDimAmount(0.4f)
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
