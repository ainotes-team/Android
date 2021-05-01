package de.vincentscode.AINotes.Controls.InkCanvas

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.vincentscode.AINotes.App
import de.vincentscode.AINotes.Helpers.Adapters.ColorGridAdapter

// TODO: improve design quality
class ColorMenu : RecyclerView {
    constructor(context: Context) : super(context) {
        load()
    }

    constructor(context: Context, attrs : AttributeSet) : super(context,attrs) {
        load()
    }

    constructor(context: Context,  attrs: AttributeSet , defStyleAttr : Int) : super(context, attrs, defStyleAttr) {
        load()
    }

    private val colors: List<String> = arrayListOf(
        "#FFC114",
        "#F6630D",
        "#FF0066",
        "#E71225",
        "#5B2D90",
        "#AB008B",
        "#CC0066",
        "#004F8B",
        "#00A0D7",
        "#33CCFF",
        "#008C3A",
        "#66CC00",
        "#000000",
        "#333333",
        "#849398",
        "#FFFFFF"
    )

    private fun load() {
        layoutManager = GridLayoutManager(App.mainActivity, 5)
        adapter = ColorGridAdapter(colors)
    }
}