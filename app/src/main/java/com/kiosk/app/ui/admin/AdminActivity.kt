package com.kiosk.app.ui.admin

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
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

        header.addView(title)
        header.addView(closeBtn)

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
            .filter { it.enabled }
            .map { it.packageName }
            .toSet()

        prefs.saveEnabledApps(enabledApps)

        startActivity(Intent(this, LauncherActivity::class.java))
        finish()
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
}