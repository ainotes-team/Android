package de.vincentscode.AINotes.Controls.InkCanvas

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.core.view.setPadding
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import de.vincentscode.AINotes.App
import de.vincentscode.AINotes.R
import java.io.Serializable

class Pen(c: Context,
          private var color: String,
          private var thickness: Float) : androidx.appcompat.widget.AppCompatImageButton(c) {

    var model: PenModel

    init {
        model = PenModel(color, thickness)
        setImageResource(R.drawable.ic_pen)
        setColor(color)
        setThickness(thickness)

        background = null
        setPadding(8)
    }

    fun setColor(newColor: String) {
        color = newColor
        model.color = color
        setColorFilter(Color.parseColor(color))
    }

    fun getColor() : String {
        return color
    }

    fun setThickness(newThickness: Float) {
        thickness = newThickness
        model.thickness = thickness
    }

    fun getThickness() : Float {
        return thickness
    }

    // this method has to be called in order to apply the attributes to the canvas
    fun select() {
        App.mainActivity.editorScreen.inkCanvas.setBrushSize(thickness)
        App.mainActivity.editorScreen.inkCanvas.setColor(Color.parseColor(color))
    }

    fun serialize() : String {
        return Gson().toJson(model)
    }

    fun deserialize(serialized: String) {
        model =  Gson().fromJson(serialized, PenModel::class.java)
        setThickness(model.thickness)
        setColor(model.color)
    }

    fun loadModel(m: PenModel) {
        model = m
        setColor(model.color)
        setThickness(model.thickness)
    }
}

data class PenModel(
    public var color: String,
    public var thickness: Float
)