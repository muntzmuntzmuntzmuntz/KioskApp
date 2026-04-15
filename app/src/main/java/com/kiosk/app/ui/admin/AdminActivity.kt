package com.kiosk.app.ui.admin

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kiosk.app.core.data.AppRepository
import com.kiosk.app.core.data.PrefsManager
import com.kiosk.app.core.model.AppItem
import com.kiosk.app.ui.base.BaseActivity
import com.kiosk.app.ui.launcher.LauncherActivity

class AdminActivity : BaseActivity() {

    private lateinit var allApps: List<AppItem>
    private lateinit var adapter: AdminAdapter
    private lateinit var prefs: PrefsManager

    private var bgTarget: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prefs = PrefsManager(this)

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
            text = "Admin Panel"
            textSize = 20f
            setTextColor(Color.BLACK)

            layoutParams = LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
            )
        }

        val closeBtn = ImageView(this).apply {
            setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
            layoutParams = LinearLayout.LayoutParams(dp(32), dp(32))

            setOnClickListener {
                closeApp()
            }
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
        header.addView(closeBtn)
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
            }
            true
        }
        popup.show()
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
        val prefs = PrefsManager(this)
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