package com.kiosk.app.ui.pin

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Space
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import com.kiosk.app.R
import com.kiosk.app.core.data.PrefsManager
import com.kiosk.app.ui.admin.AdminActivity
import com.kiosk.app.ui.base.BaseActivity
import com.kiosk.app.ui.launcher.LauncherActivity

class PinUnlockActivity : BaseActivity() {

    private var input = ""
    private var correctPin = "1234"
    private lateinit var pinSlots: List<TextView>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        correctPin = PrefsManager(this).getPin().toString()
        val typeFace = ResourcesCompat.getFont(this, R.font.orbitron)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#F5F7FA"))
        }

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(16))
            setBackgroundColor(Color.WHITE)
        }

        val title = TextView(this).apply {
            text = "Enter PIN"
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

        val closeBtn = ImageView(this).apply {
            setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
            layoutParams = LinearLayout.LayoutParams(dp(36), dp(36))
            setPadding(dp(6), dp(6), dp(6), dp(6))
            setBackgroundResource(android.R.drawable.list_selector_background)
            setOnClickListener { goBack() }
        }

        header.addView(title)
        header.addView(closeBtn)

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
            setPadding(dp(24), dp(24), dp(24), dp(24))
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER
            )
        }

        val keyMargin = dp(6)
        val availableWidth = resources.displayMetrics.widthPixels - dp(48)
        val keySize = ((availableWidth - (keyMargin * 6)) / 3).coerceIn(dp(72), dp(88))
        val keypadWidth = keySize * 3 + keyMargin * 6

        val pinDisplay = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(dp(24), dp(20), dp(24), dp(20))
            background = android.graphics.drawable.GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = dp(28).toFloat()
                setStroke(dp(1), Color.parseColor("#D1D5DB"))
            }
            layoutParams = LinearLayout.LayoutParams(
                keypadWidth.coerceAtLeast(dp(260)),
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dp(32)
                gravity = Gravity.CENTER_HORIZONTAL
            }
        }

        pinSlots = List(4) { index ->
            TextView(this).apply {
                text = ""
                gravity = Gravity.CENTER
                textSize = 26f
                setTypeface(typeFace, Typeface.BOLD)
                setTextColor(Color.parseColor("#111827"))
                layoutParams = LinearLayout.LayoutParams(0, dp(44), 1f).apply {
                    if (index < 3) {
                        marginEnd = dp(8)
                    }
                }
            }
        }

        pinSlots.forEach(pinDisplay::addView)
        updateDisplay()


        val grid = GridLayout(this).apply {
            columnCount = 3
            useDefaultMargins = false
            layoutParams = LinearLayout.LayoutParams(
                keypadWidth,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.CENTER_HORIZONTAL
            }
        }

        fun key(text: String): Button {
            return Button(this).apply {
                this.text = text
                textSize = if (text == "DEL") 16f else 24f
                setTypeface(typeFace, Typeface.BOLD)
                setTextColor(Color.parseColor("#111827"))
                isAllCaps = false
                background = android.graphics.drawable.GradientDrawable().apply {
                    setColor(Color.WHITE)
                    cornerRadius = dp(18).toFloat()
                    setStroke(dp(1), Color.parseColor("#D1D5DB"))
                }
                elevation = dp(2).toFloat()
                layoutParams = ViewGroup.MarginLayoutParams(keySize, keySize).apply {
                    setMargins(keyMargin, keyMargin, keyMargin, keyMargin)
                }

                setOnClickListener {
                    if (text == "DEL") {
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
                            Toast.makeText(
                                context,
                                "Wrong PIN. Please contact Administrator.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        }

        for (i in 1..9) {
            grid.addView(key(i.toString()))
        }

        grid.addView(Space(this).apply {
            layoutParams = ViewGroup.MarginLayoutParams(keySize, keySize).apply {
                setMargins(keyMargin, keyMargin, keyMargin, keyMargin)
            }
        })
        grid.addView(key("0"))
        grid.addView(key("DEL"))

        container.addView(pinDisplay)
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
        pinSlots.forEachIndexed { index, textView ->
            val filled = index < input.length
            textView.text = if (filled) "•" else ""
            textView.background = android.graphics.drawable.GradientDrawable().apply {
                setColor(if (index == input.length && input.length < 4) Color.parseColor("#F3F4F6") else Color.TRANSPARENT)
                cornerRadius = dp(14).toFloat()
                setStroke(
                    dp(1),
                    if (filled) Color.parseColor("#111827") else Color.parseColor("#D1D5DB")
                )
            }
        }
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}
