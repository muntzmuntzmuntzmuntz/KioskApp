package com.kiosk.app.ui.admin

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.*
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.kiosk.app.core.model.AppItem

class AdminAdapter(
    private var apps: List<AppItem>
) : RecyclerView.Adapter<AdminAdapter.VH>() {

    inner class VH(val container: FrameLayout) : RecyclerView.ViewHolder(container) {
        val icon: ImageView = container.findViewWithTag("icon")
        val name: TextView = container.findViewWithTag("name")
        val toggle: Switch = container.findViewWithTag("toggle")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val ctx = parent.context

        val outer = FrameLayout(ctx).apply {
            layoutParams = ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(dp(ctx, 16), dp(ctx, 8), dp(ctx, 16), dp(ctx, 8))
            }
        }

        val bg = GradientDrawable().apply {
            setColor(Color.WHITE)
            cornerRadius = dp(ctx, 16).toFloat()
            setStroke(dp(ctx, 1), Color.parseColor("#E0E0E0"))
        }

        val row = LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(ctx, 16), dp(ctx, 16), dp(ctx, 16), dp(ctx, 16))
            background = bg
        }

        val icon = ImageView(ctx).apply {
            tag = "icon"
            layoutParams = LinearLayout.LayoutParams(dp(ctx, 40), dp(ctx, 40)).apply {
                marginEnd = dp(ctx, 12)
            }
        }

        val name = TextView(ctx).apply {
            tag = "name"
            textSize = 16f
            setTextColor(Color.BLACK)

            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply {
                marginEnd = dp(ctx, 16)
            }
        }

        val toggle = Switch(ctx).apply {
            tag = "toggle"
        }

        row.addView(icon)
        row.addView(name)
        row.addView(toggle)

        outer.addView(row)

        return VH(outer)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val app = apps[position]

        holder.icon.setImageDrawable(app.icon)
        holder.name.text = app.name

        holder.toggle.setOnCheckedChangeListener(null)
        holder.toggle.isChecked = app.enabled

        holder.toggle.setOnCheckedChangeListener { _, isChecked ->
            app.enabled = isChecked
        }
    }

    override fun getItemCount() = apps.size

    fun updateList(newList: List<AppItem>) {
        apps = newList
        notifyDataSetChanged()
    }

    private fun dp(ctx: android.content.Context, value: Int): Int {
        return (value * ctx.resources.displayMetrics.density).toInt()
    }
}