package com.kiosk.app.ui.admin

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.marginTop
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kiosk.app.R
import com.kiosk.app.core.data.AppRepository
import com.kiosk.app.core.data.PrefsManager
import com.kiosk.app.core.model.AppItem
import com.kiosk.app.ui.base.BaseActivity
import com.kiosk.app.ui.launcher.LauncherActivity
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class AdminActivity : BaseActivity() {

    private lateinit var allApps: List<AppItem>
    private lateinit var adapter: AdminAdapter
    private lateinit var prefs: PrefsManager

    private var bgTarget: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prefs = PrefsManager(this)

        val typeFace = ResourcesCompat.getFont(this, R.font.orbitron)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#F5F7FA"))
        }

        // ================= TITLE =================
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(16))
            setBackgroundColor(Color.WHITE)
        }

        val title = TextView(this).apply {
            text = "Administrator"
            textSize = 25f
            setTypeface(typeFace, Typeface.BOLD)
            setTextColor(Color.BLACK)
            letterSpacing = 0.09f

            layoutParams = LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
            )
        }

        val menuBtn = ImageView(this).apply {
            setImageResource(android.R.drawable.ic_menu_more)

            layoutParams = LinearLayout.LayoutParams(dp(36), dp(36))

            setOnClickListener {
                showAdminMenu(this)
            }
        }

        header.addView(title)
        // TODO: admin close button update logic
        header.addView(menuBtn);

        // ================= SEARCH =================
        val search = EditText(this).apply {
            hint = "Search apps..."
            setPadding(dp(16), dp(12), dp(16), dp(12))
            setBackgroundColor(Color.WHITE)
        }

        // ================= LIST =================
        val recycler = RecyclerView(this).apply {
            layoutManager = LinearLayoutManager(this@AdminActivity)
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        }

        val repo = AppRepository(this)
        val saved = prefs.getEnabledApps()

        allApps = repo.getAllApps().map { app ->
            app.copy(
                enabled = saved.contains(app.packageName)
            )
        }

        adapter = AdminAdapter(allApps)
        recycler.adapter = adapter

        // 🔍 SEARCH LOGIC
        search.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().lowercase()

                val filtered = allApps.filter {
                    it.name.lowercase().contains(query)
                }

                adapter.updateList(filtered)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // ================= ACTIONS =================
        val actions = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.END
            setPadding(dp(16), dp(12), dp(16), dp(12))
            setBackgroundColor(Color.WHITE)
        }

        val cancelBtn = Button(this).apply {
            text = "Cancel"
            setOnClickListener {
                startActivity(Intent(this@AdminActivity, LauncherActivity::class.java))
                finish()
            }
        }

        val saveBtn = Button(this).apply {
            text = "Save"
            setOnClickListener {
                saveChanges()
            }
        }

        actions.addView(cancelBtn)
        actions.addView(space())
        actions.addView(saveBtn)

        // ================= BUILD =================
        root.addView(header)
        root.addView(search)
        root.addView(recycler)
        root.addView(actions)

        setContentView(root)
    }

    private fun saveChanges() {
        val enabledApps = allApps
            .filter { it.enabled && it.packageName != packageName } // 🔥 exclude your app
            .map { it.packageName }
            .toSet()

        // ✅ Save for launcher UI
        prefs.saveEnabledApps(enabledApps)

        // ✅ Update system whitelist
        updateKioskWhitelist(enabledApps)

        // go back to launcher
        startActivity(Intent(this, LauncherActivity::class.java))
        finish()
    }

    private fun updateKioskWhitelist(enabledApps: Set<String>) {

        val dpm = getSystemService(android.app.admin.DevicePolicyManager::class.java)

        val admin = android.content.ComponentName(
            this,
            com.kiosk.app.core.admin.MyDeviceAdminReceiver::class.java
        )

        // 🔥 Use Set to avoid duplicates
        val allowed = mutableSetOf<String>()

        allowed.add(packageName)
        allowed.addAll(enabledApps)

        dpm.setLockTaskPackages(
            admin,
            allowed.toTypedArray()
        )
    }

    private fun space(): Space {
        return Space(this).apply {
            layoutParams = LinearLayout.LayoutParams(dp(12), dp(1))
        }
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }

    private fun closeApp() {
        finishAffinity()
        android.os.Process.killProcess(android.os.Process.myPid())
    }

    private fun showAdminMenu(anchor: View) {

        val popup = PopupMenu(this, anchor)

        popup.menu.add("Update PIN")
        popup.menu.add("Change Lock Background")
        popup.menu.add("Change Dashboard Background")
        popup.menu.add("Update Wallpaper Texts")
        popup.menu.add("Force Quit")
        popup.menu.add("About")

        popup.setOnMenuItemClickListener { item ->
            when (item.title) {
                "Update PIN" -> showUpdatePinDialog()
                "Change Lock Background" -> {
                    bgTarget = "lock"
                    openImagePicker()
                }
                "Change Dashboard Background" -> {
                    bgTarget = "launcher"
                    openImagePicker()
                }
                "Update Wallpaper Texts" -> {
                    showWallpaperTextDialog()
                }
                "Force Quit" -> {
                    closeApp()
                }
                "About" -> {
                    showAboutDialog()
                }
            }
            true
        }
        popup.show()
    }

    private fun showAboutDialog() {
        val dialog = android.app.Dialog(this)
        dialog.setCancelable(true)

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(24), dp(24), dp(24), dp(24))
            background = android.graphics.drawable.GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = dp(20).toFloat()
            }
        }

        val titleRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, 0, 0, dp(16))
        }

        val title = TextView(this).apply {
            text = "About"
            textSize = 20f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.BLACK)
            layoutParams = LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
            )
        }

        val closeButton = TextView(this).apply {
            text = "X"
            textSize = 18f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#6B7280"))
            gravity = Gravity.CENTER
            setPadding(dp(8), dp(4), dp(8), dp(4))
            setOnClickListener { dialog.dismiss() }
        }

        val versionLabel = buildAboutRow("App Version", "V1.0.0.1")
        val activationCodeLabel = buildAboutRow(
            "Activation Key",
            prefs.getActivationCode() ?: "Not activated"
        )
        val expirationLabel = buildAboutRow(
            "Expiration Date",
            formatActivationExpiration(prefs.getActivationExpiresAt())
        )

        titleRow.addView(title)
        titleRow.addView(closeButton)

        card.addView(titleRow)
        card.addView(versionLabel)
        card.addView(activationCodeLabel)
        card.addView(expirationLabel)

        dialog.setContentView(card)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.7f).toInt().coerceAtLeast(dp(320)),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.show()
    }

    private fun buildAboutRow(label: String, value: String): LinearLayout {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 0, 0, dp(16))
        }

        val labelView = TextView(this).apply {
            text = label
            textSize = 13f
            setTextColor(Color.GRAY)
        }

        val valueView = TextView(this).apply {
            text = value
            textSize = 16f
            setTextColor(Color.BLACK)
            setTypeface(Typeface.MONOSPACE)
            setPadding(0, dp(4), 0, 0)
        }

        row.addView(labelView)
        row.addView(valueView)
        return row
    }

    private fun formatActivationExpiration(value: String?): String {
        if (value.isNullOrBlank()) {
            return "No expiration set"
        }

        val inputFormats = listOf(
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX", Locale.US),
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX", Locale.US)
        ).onEach { it.timeZone = TimeZone.getTimeZone("UTC") }

        val outputFormat = SimpleDateFormat("MMMM d, yyyy h:mm a", Locale.US)

        for (format in inputFormats) {
            try {
                val parsedDate = format.parse(value) ?: continue
                return outputFormat.format(parsedDate)
            } catch (_: ParseException) {
            }
        }

        return value
    }

    private fun showWallpaperTextDialog() {

        val dialog = android.app.Dialog(this)
        dialog.setCancelable(true)

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(24), dp(24), dp(24), dp(24))
        }

        val titleInput = EditText(this).apply {
            hint = "Title (e.g. INSERT COIN!)"
            setText(prefs.getWallpaperTitle())
            setPadding(0, dp(0), 0, 10)
            textSize = 20f
        }

        val subtitleInput = EditText(this).apply {
            hint = "Subtitle"
            setText(prefs.getWallpaperSubtitle())
            setPadding(0, dp(0), 0, 15)
            textSize = 20f
        }

        val saveBtn = Button(this).apply {
            text = "Save"
            textSize = 15f
            setPadding(0,20,0,0)

            setOnClickListener {
                val title = titleInput.text.toString().trim()
                val subtitle = subtitleInput.text.toString().trim()

                prefs.setWallpaperTexts(title, subtitle)

                dialog.dismiss()
            }
        }

        container.addView(titleInput)
        container.addView(subtitleInput)
        container.addView(saveBtn)

        dialog.setContentView(container)
        dialog.window?.setLayout(
            400,
            350
        )
        dialog.show()
    }

    private fun showUpdatePinDialog() {

        val dialog = android.app.Dialog(this)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(24), dp(24), dp(24), dp(24))
            setBackgroundColor(Color.WHITE)
        }

        val title = TextView(this).apply {
            text = "Update PIN"
            textSize = 18f
            setPadding(0, 0, 0, dp(16))
        }

        // 🔢 NEW PIN
        val pinInput = EditText(this).apply {
            hint = "Enter 4-digit PIN"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or
                    android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD

            filters = arrayOf(android.text.InputFilter.LengthFilter(4))
            gravity = Gravity.CENTER
            textSize = 20f
        }

        // 🔁 CONFIRM PIN
        val confirmInput = EditText(this).apply {
            hint = "Confirm PIN"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or
                    android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD

            filters = arrayOf(android.text.InputFilter.LengthFilter(4))
            gravity = Gravity.CENTER
            textSize = 20f

//            setPadding(0, dp(12), 0, 0)
        }

        // 💾 SAVE BUTTON
        val save = Button(this).apply {
            text = "Save"

            setOnClickListener {

                val pin = pinInput.text.toString()
                val confirm = confirmInput.text.toString()

                // validation
                if (pin.length != 4 || confirm.length != 4) {
                    Toast.makeText(context, "PIN must be exactly 4 digits", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (pin != confirm) {
                    Toast.makeText(context, "PINs do not match", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                saveNewPin(pin)
                dialog.dismiss()
            }
        }

        root.addView(title)
        root.addView(pinInput)
        root.addView(confirmInput)
        root.addView(save)

        dialog.setContentView(root)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(
            350,
            350
        )

        dialog.show()
    }

    private fun saveNewPin(pin: String) {
        prefs.savePin(pin)

        Toast.makeText(this, "PIN updated", Toast.LENGTH_SHORT).show()
    }

    private val pickImageLauncher =
        registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.GetContent()) { uri ->

            if (uri != null) {
                saveBackgroundImage(uri)
            }
        }

    private fun openImagePicker() {
        pickImageLauncher.launch("image/*")
    }

    private fun saveBackgroundImage(uri: android.net.Uri) {

        val fileName = if (bgTarget == "lock") {
            "lock_bg.jpg"
        } else {
            "launcher_bg.jpg"
        }

        val file = java.io.File(filesDir, fileName)

        contentResolver.openInputStream(uri)?.use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        Toast.makeText(
            this,
            if (bgTarget == "lock") "Lock background updated"
            else "Dashboard background updated",
            Toast.LENGTH_SHORT
        ).show()
    }
}
