package com.kiosk.app.ui.launcher

import android.content.Context
import android.view.*
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.kiosk.app.core.model.AppItem

class LauncherAdapter(
    private val onClick: (AppItem) -> Unit
) : ListAdapter<AppItem, LauncherAdapter.VH>(DIFF) {

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<AppItem>() {
            override fun areItemsTheSame(a: AppItem, b: AppItem) = a.packageName == b.packageName
            override fun areContentsTheSame(a: AppItem, b: AppItem) = a == b
        }
    }

    inner class VH(val card: CardView) : RecyclerView.ViewHolder(card) {
        val icon: ImageView = card.findViewWithTag("icon")
//        val name: TextView = card.findViewWithTag("name")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val ctx = parent.context

        val card = CardView(ctx).apply {
            radius = 20f
            cardElevation = 6f
            layoutParams = ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(ctx, 160)
            ).apply { setMargins(16, 16, 16, 16) }
        }

        val layout = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
        }

        val icon = ImageView(ctx).apply {
            tag = "icon"
        }

        val name = TextView(ctx).apply {
            tag = "name"
        }

        layout.addView(icon)
//        layout.addView(name)
        card.addView(layout)

        return VH(card)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val app = getItem(position)

        holder.icon.setImageDrawable(app.icon)
//        holder.name.text = app.name
        holder.card.setOnClickListener {
            onClick(app)
        }
    }

    private fun dp(ctx: Context, v: Int) =
        (v * ctx.resources.displayMetrics.density).toInt()
}