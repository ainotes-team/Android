package de.vincentscode.AINotes.Controls

import android.content.Context
import android.view.View
import android.widget.LinearLayout
import com.google.gson.Gson
import de.vincentscode.AINotes.App
import de.vincentscode.AINotes.Controls.InkCanvas.InkCanvas
import de.vincentscode.AINotes.Controls.InkCanvas.Pen
import de.vincentscode.AINotes.Controls.InkCanvas.PenModel
import de.vincentscode.AINotes.Helpers.Logger
import de.vincentscode.AINotes.Plugins.ImagePlugin
import de.vincentscode.AINotes.Plugins.base.Plugin
import de.vincentscode.AINotes.Plugins.TextPlugin
import de.vincentscode.AINotes.R
import kotlinx.android.synthetic.main.bottom_toolbar_editor_screen.view.*
import kotlinx.android.synthetic.main.eraser_advanced_options.view.*
import kotlinx.android.synthetic.main.pen_advanced_options.view.*
import kotlinx.android.synthetic.main.pen_advanced_options.view.thickness_slider

class EditorPluginOptionsBar(context: Context) : LinearLayout(context) {
    enum class Plugins {
        Text,
        Image
    }

    public lateinit var currentPen: Pen

    public var currentPluginType = Plugins.Text
    private val pens: ArrayList<Pen> = ArrayList()

    init {
        View.inflate(context, R.layout.bottom_toolbar_editor_screen, this)

        button_text.setOnClickListener {
            closeAdvancedOptions()
            currentPluginType = Plugins.Text

            App.mainActivity.editorScreen.inkCanvas.setMode(InkCanvas.InkCanvasMode.None)
        }

        button_image.setOnClickListener {
            closeAdvancedOptions()
            currentPluginType = Plugins.Image

            App.mainActivity.editorScreen.inkCanvas.setMode(InkCanvas.InkCanvasMode.None)
        }

        button_pen.setOnClickListener {
            closeAdvancedOptions()
            if (App.mainActivity.editorScreen.inkCanvas.currentMode == InkCanvas.InkCanvasMode.Pen) {
                onOpenPenAdvancedOptions()
            } else {
                App.mainActivity.editorScreen.inkCanvas.setMode(InkCanvas.InkCanvasMode.Pen)
            }
        }

        button_pen.setOnLongClickListener {
            if (!it.isSelected) {
                plugin_options.check(it.id)
            }

            App.mainActivity.editorScreen.inkCanvas.setMode(InkCanvas.InkCanvasMode.Pen)

            onOpenPenAdvancedOptions()

            true
        }

        button_eraser.setOnClickListener {
            closeAdvancedOptions()
            App.mainActivity.editorScreen.inkCanvas.setMode(InkCanvas.InkCanvasMode.Eraser)
        }

        button_eraser.setOnLongClickListener {
            if (!it.isSelected) {
                plugin_options.check(it.id)
            }

            App.mainActivity.editorScreen.inkCanvas.setMode(InkCanvas.InkCanvasMode.Eraser)

            onOpenEraserAdvancedOptions()

            true
        }

        button_lasso.setOnClickListener {
            savePens()
            closeAdvancedOptions()
            App.mainActivity.editorScreen.inkCanvas.setMode(InkCanvas.InkCanvasMode.Selection)
        }

        // pen item selected by default
        plugin_options.check(button_pen.id)

        loadPens()

        onOpenPenAdvancedOptions(false)
    }

    private fun loadPens() {
        val sharedPref = App.mainActivity.getPreferences(Context.MODE_PRIVATE)
        val x = sharedPref.getString("wtfarepens", null)
        Logger.log("loadPens", "loadPens")
        Logger.log("loadPens", "loadPens: $x")
        Logger.log(
            "loadPens",
            "Read sharedPref ${sharedPref.getString("myKey", " myKeydefault value :(")}"
        )

        Logger.log(
            "loadPens",
            "Read sharedPref ${sharedPref.getString("wtfarepens", "wtfarepens default value :(")}"
        )
        if (x != null) {
            val loadedPens = Gson().fromJson(x, Array<PenModel>::class.java)
            loadedPens.forEach {
                val pen = Pen(App.mainActivity, "#000000", 5f)
                pen.loadModel(it)
                pens.add(pen)
            }

            return
        }

        Logger.log("load Pens", "List is empty => create new pens")

        pens.add(Pen(App.mainActivity, "#000000", 5f))
        pens.add(Pen(App.mainActivity, "#FF0000", 5f))
        pens.add(Pen(App.mainActivity, "#00FF00", 5f))
        pens.add(Pen(App.mainActivity, "#0000FF", 5f))
    }

    private fun savePens() {
        val x = Gson().toJson(pens.map { pen -> pen.model }.toTypedArray())

        Logger.log("save Pens", x)
        val sharedPref = App.mainActivity.getPreferences(Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("wtfarepens", x)
            apply()
            Logger.log("save pens", "saved pens")
        }
    }

    private var eraserSecondaryOptions: View? = null

    private fun onOpenEraserAdvancedOptions() {
        openAdvancedOptions()
        secondary_options.removeAllViews()

        if (eraserSecondaryOptions == null) {
            val view = View.inflate(App.mainActivity, R.layout.eraser_advanced_options, null)

            view.eraser_width_slider.addOnChangeListener { _, value, _ ->
                App.mainActivity.editorScreen.inkCanvas.setEraserWidth(value * 2)
            }

            eraserSecondaryOptions = view
        }

        secondary_options.addView(eraserSecondaryOptions)
    }

    var penSecondaryOptions: View? = null

    private fun onOpenPenAdvancedOptions(show: Boolean = true) {
        if (show) openAdvancedOptions()
        secondary_options.removeAllViews()

        if (penSecondaryOptions == null) {
            // add content
            val options = View.inflate(App.mainActivity, R.layout.pen_advanced_options, null)

            currentPen = pens[0]

            fun loadPen(pen: Pen) {
                currentPen = pen
                options.thickness_slider.value = pen.getThickness()
                currentPen.select()
            }

            loadPen(currentPen)

            options.thickness_slider.addOnChangeListener { _, value, _ ->
                currentPen.setThickness(value)
                currentPen.select()
            }

            pens.forEach { pen: Pen ->
                options.pen_list.addView(pen)
                pen.setOnClickListener {
                    loadPen(it as Pen)
                }
            }

            penSecondaryOptions = options
        }

        secondary_options.addView(penSecondaryOptions)
    }

    private fun openAdvancedOptions() {
        secondary_options.visibility = View.VISIBLE
    }

    fun closeAdvancedOptions() {
        secondary_options.visibility = View.INVISIBLE
    }

    public fun getNewPlugin(): Plugin {
        return when (currentPluginType) {
            Plugins.Text -> {
                TextPlugin()
            }
            Plugins.Image -> {
                ImagePlugin()
            }
        }
    }
}