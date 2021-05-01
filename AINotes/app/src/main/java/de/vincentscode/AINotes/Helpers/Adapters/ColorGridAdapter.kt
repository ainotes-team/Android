package de.vincentscode.AINotes.Helpers.Adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import de.vincentscode.AINotes.App
import de.vincentscode.AINotes.R

class ColorGridAdapter(private var colors: List<String>) : RecyclerView.Adapter<ColorItem>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorItem {
        val item = LayoutInflater.from(App.mainActivity).inflate(R.layout.color_item_layout, parent, false)

        return ColorItem(
            item
        )
    }

    override fun getItemCount(): Int {
        return colors.size
    }

    override fun onBindViewHolder(holder: ColorItem, position: Int) {
        if (position > colors.size) return
        holder.bindData(colors[position])
    }

}

class ColorItem(private val item: View) : RecyclerView.ViewHolder(item) {
    fun bindData(color: String) {

        item.setBackgroundColor(Color.parseColor(color))
        item.setOnClickListener {
            App.mainActivity.editorScreen.optionsBar?.currentPen?.setColor(color)
            App.mainActivity.editorScreen.optionsBar?.currentPen?.select()
        }
    }
}