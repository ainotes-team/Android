package de.vincentscode.AINotes.Plugins

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.RectF
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.FrameLayout
import de.vincentscode.AINotes.App
import de.vincentscode.AINotes.Helpers.Logger
import de.vincentscode.AINotes.Helpers.database.FileHelper
import de.vincentscode.AINotes.Models.PluginModel
import de.vincentscode.AINotes.Plugins.base.Plugin
import kotlinx.android.synthetic.main.text_plugin_layout.view.*


@SuppressLint("ViewConstructor")
class TextPlugin(pluginId: Int = -1, context: Context = App.mainActivity) : Plugin(context, pluginId) {

    val layout = EditText(context)

    //private val editor: RichTextEditor = layout.editor

    init {
        movable = true
        resizable = false
        resizableToRight = true

//        editor.setOnFocusChangeListener { v, hasFocus ->
//            Logger.toast("Focus changed with ${editor.getCachedHtml()}")
//            if (editor.getCachedHtml().isEmpty() && !hasFocus) {
//                delete()
//            }
//        }

//        editor.addHtmlChangedListener {
//            CoroutineScope(Main).launch {
//                layoutParams.height = (editor.contentHeight * 2.1 + 100).toInt()
//                requestLayout()
//            }
//        }

        content.addView(layout)

        layout.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {

            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                layout.measure(0, 0)
                val height: Int = layout.measuredHeight
                layoutParams.height = height + 2 * 50

                Logger.log("TextPlugin", height)

                val x = layout.layoutParams
                x.height = height

                layout.setLayoutParams(x)

                //requestLayout()
            }
        })
    }

    override fun onPlace(x: Int, y: Int, layout: FrameLayout) {
        layoutParams = LayoutParams(100, 100)
        layoutParams.leftMargin = x
        layoutParams.topMargin = y

        super.onPlace(x, y, layout)
    }

    override fun loadPluginModel(pluginModel: PluginModel): Boolean {
        Logger.log("TextPlugin", "Load Plugin Model")
        pluginId = pluginModel.pluginId!!
        val content = pluginModel.content

        layout.setText(content)

        showNobs()

        return true
    }

    override fun savePluginModel(fileId: Int): Boolean {
        Logger.log("TextPlugin", "Saving TextPlugin content with pluginId $pluginId")

        FileHelper.insertOrReplacePlugin(PluginModel().apply {
            pluginId = if (this@TextPlugin.pluginId != -1) this@TextPlugin.pluginId else null
            type = PluginModel.pluginTypes.keys.toTypedArray()[PluginModel.pluginTypes.values.toList().indexOfFirst {
                it == this@TextPlugin::class.java
            }]
            posX = x.toDouble()
            posY = y.toDouble()
            sizeX = width.toDouble()
            sizeY = height.toDouble()
            content = layout.text.toString()
            this.fileId = fileId
        })

        return true
    }
}