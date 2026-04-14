package com.kiosk.app.ui.pin

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*
import com.kiosk.app.ui.admin.AdminActivity
import com.kiosk.app.ui.base.BaseActivity
import com.kiosk.app.ui.launcher.LauncherActivity

class PinUnlockActivity : BaseActivity() {

    private var input = ""
    private val correctPin = "1234"

    private lateinit var display: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#F5F7FA"))
        }

        // ================= HEADER =================
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(16))
        }

        val title = TextView(this).apply {
            text = "Enter PIN"
            textSize = 18f
            setTextColor(Color.BLACK)

            layoutParams = LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
            )
        }

        val closeBtn = ImageView(this).apply {
            setImageResource(android.R.drawable.ic_menu_close_clear_cancel)

            layoutParams = LinearLayout.LayoutParams(dp(36), dp(36))

            setOnClickListener {
                goBack()
            }

            // optional touch feedback
            setPadding(dp(6), dp(6), dp(6), dp(6))
            setBackgroundResource(android.R.drawable.list_selector_background)
        }

        header.addView(title)
        header.addView(closeBtn)

        // ================= CONTENT =================
        val content = FrameLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        }

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER

            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER
            )
        }

        // PIN DISPLAY
        display = TextView(this).apply {
            textSize = 36f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.BLACK)
            setPadding(0, 0, 0, dp(32))
        }

        updateDisplay()

        // GRID
        val grid = GridLayout(this).apply {
            columnCount = 3
        }

        fun key(text: String): Button {
            return Button(this).apply {

                this.text = text

                layoutParams = ViewGroup.MarginLayoutParams(dp(80), dp(80)).apply {
                    setMargins(dp(10), dp(10), dp(10), dp(10))
                }

                setOnClickListener {
                    if (text == "⌫") {
                        if (input.isNotEmpty()) {
                            input = input.dropLast(1)
                        }
                    } else {
                        input += text
                    }

                    updateDisplay()

                    if (input.length == 4) {
                        if (input == correctPin) {
                            startActivity(Intent(this@PinUnlockActivity, AdminActivity::class.java))
                            finish()
                        } else {
                            input = ""
                            updateDisplay()
                        }
                    }
                }
            }
        }

        for (i in 1..9) grid.addView(key(i.toString()))

        grid.addView(Space(this).apply {
            layoutParams = ViewGroup.LayoutParams(dp(80), dp(80))
        })

        grid.addView(key("0"))
        grid.addView(key("⌫"))

        container.addView(display)
        container.addView(grid)

        content.addView(container)

        root.addView(header)
        root.addView(content)

        setContentView(root)
    }

    private fun goBack() {
        startActivity(Intent(this, LauncherActivity::class.java))
        finish()
    }

    private fun updateDisplay() {
        display.text = "●".repeat(input.length)
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}