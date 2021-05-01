package de.vincentscode.AINotes.Screens

import android.annotation.SuppressLint
import android.content.Context
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.core.view.children
import androidx.core.view.contains
import de.vincentscode.AINotes.App
import de.vincentscode.AINotes.Controls.EditorPluginOptionsBar
import de.vincentscode.AINotes.Controls.InkCanvas.InkCanvas
import de.vincentscode.AINotes.Controls.PageContent
import de.vincentscode.AINotes.Helpers.database.FileHelper
import de.vincentscode.AINotes.Helpers.Logger
import de.vincentscode.AINotes.Models.FileModel
import de.vincentscode.AINotes.Plugins.base.Plugin
import de.vincentscode.AINotes.R
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.editor_screen.view.*
import kotlinx.android.synthetic.main.toolbar_editor_screen.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import java.lang.System.currentTimeMillis


class EditorScreen(context: Context) : PageContent(context) {
    public var optionsBar: EditorPluginOptionsBar? = null
    public var inkCanvas: InkCanvas
    private var root: LinearLayout
    var currentFileModel: FileModel

    private var toolbarContent: View = View.inflate(context, R.layout.toolbar_editor_screen, null)

    init {
        View.inflate(context, R.layout.editor_screen, this)
        loadComponents()

        root = canvas_root

        inkCanvas = InkCanvas(context)
        root.addView(inkCanvas)

        // initializing currentFileModel to prevent exception
        currentFileModel = FileModel()

        Logger.log("EditorScreen", "Initialization finished")
    }

    fun loadFile(fileModel: FileModel) {
        // do not reload same file
        if (currentFileModel.fileId == fileModel.fileId) return

        inkCanvas.clearCanvas()
        currentFileModel = fileModel

        if (fileModel.strokeContent == "") {
            // is new file => loading not required
            return
        }

        inkCanvas.load(fileModel.strokeContent)

        CoroutineScope(IO).launch {
            Logger.log("EditorScreen", "Loading plugins")
            for (pluginModel in fileModel.getPluginModels()) {
                Logger.log("EditorScreen", "Loading plugin model $pluginModel")
                CoroutineScope(Main).launch {
                    val plugin = pluginModel.toPlugin()

                    val lp = FrameLayout.LayoutParams(
                        pluginModel.sizeX.toInt(),
                        pluginModel.sizeY.toInt()
                    )
                    lp.leftMargin = pluginModel.posX.toInt()
                    lp.topMargin = pluginModel.posY.toInt()

                    plugin.layoutParams = lp

                    plugin.loadPluginModel(pluginModel)

                    plugin_container.addView(plugin, lp)
                }
            }
        }
    }

    private fun saveFile() {
        currentFileModel.strokeContent = inkCanvas.save()
        currentFileModel.lastChangedDate = currentTimeMillis()
        CoroutineScope(IO).launch {
            FileHelper.updateFile(currentFileModel)
        }

        val children = plugin_container.children.iterator()

        CoroutineScope(IO).launch {
            children.forEach {
                Logger.log("EditorScreen", "Saving Plugin $it")
                if (it is Plugin) {
                    it.savePluginModel(currentFileModel.fileId!!)
                }
            }
        }

        Logger.log("EditorScreen", "Saving file with id ${currentFileModel.fileId}")
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun loadComponents() {
        plugin_container.setOnTouchListener { v, event ->
            onTouch(v as FrameLayout, event)
            true
        }
    }

    private fun onTouch(frameLayout: FrameLayout, event: MotionEvent) {
        if (event.action != MotionEvent.ACTION_DOWN) return

        val x = event.x.toInt()
        val y = event.y.toInt()

        val plugin = optionsBar?.getNewPlugin()!!

        plugin.onPlace(x, y, frameLayout)
    }

    fun removePlugin(plugin: Plugin?) {
        if (plugin == null) return
        if (plugin_container.contains(plugin)) {
            plugin_container.removeView(plugin)
            CoroutineScope(IO).launch {
                if (plugin.pluginId != -1) FileHelper.deletePlugin(plugin.pluginId)
            }
        }
    }

    private fun setToolbar() {
        toolbarContent = View.inflate(context, R.layout.toolbar_editor_screen, null)

        App.mainActivity.toolbar.removeAllViews()
        App.mainActivity.toolbar.addView(toolbarContent)

        toolbarContent.button_back.setOnClickListener {
            App.mainActivity.load(App.mainActivity.fileManagerScreen)
            //App.mainActivity.fileManagerScreen.reload()
        }

        toolbarContent.button_undo.setOnClickListener {
            inkCanvas.undo()
        }

        toolbarContent.button_redo.setOnClickListener {
            inkCanvas.redo()
        }

        toolbarContent.button_menu_editor.setOnClickListener {
            inkCanvas.invalidate()
        }


        Logger.log("EditorScreen", "Toolbar loaded successfully")
    }

    override fun onLoad() {
        Logger.log("EditorScreen", "onLoad")

        plugin_container.removeAllViews()

        setToolbar()
        App.mainActivity.setFabVisibility(false)
        App.mainActivity.isToolbarDividerVisible(true)

        App.mainActivity.deselectAllNavigationItems()

        if (optionsBar == null) optionsBar = EditorPluginOptionsBar(context)

        val lp = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)

        App.mainActivity.relativeLayout.addView(optionsBar, lp)
    }

    override fun onUnload() {
        Logger.log("EditorScreen", "onUnload")

        App.mainActivity.toolbar.removeAllViews()
        App.mainActivity.relativeLayout.removeView(optionsBar)
        saveFile()
    }
}