package de.vincentscode.AINotes.Plugins.base

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PointF
import android.graphics.RectF
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import de.vincentscode.AINotes.App
import de.vincentscode.AINotes.Models.PluginModel
import de.vincentscode.AINotes.R
import de.vincentscode.AINotes.Screens.EditorScreen
import kotlinx.android.synthetic.main.editor_screen.view.*
import kotlinx.android.synthetic.main.plugin_background.view.*

@SuppressLint("ViewConstructor")
open class Plugin(context: Context, var pluginId: Int) : FrameLayout(context) {
    protected var editorScreen: EditorScreen = App.mainActivity.editorScreen
    protected var pluginContainer: FrameLayout = App.mainActivity.editorScreen.plugin_container

    protected var movable: Boolean = true
    protected var resizable: Boolean = true
    protected var resizableToRight: Boolean = false

    private var moveNob: View? = null
    private var resizeNob: View? = null
    private var resizeToRightNob: View? = null

    protected var minWidth: Float = 200f
    protected var minHeight: Float = 200f

    private var pluginBackground = View.inflate(context, R.layout.plugin_background, null)

    public var pluginSelected: Boolean = false

    public lateinit var layoutParams: LayoutParams

    protected var content = FrameLayout(context)

    private val margin = 50

    init {
        loadComponents()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun loadComponents() {
        addView(content)

        val params = content.layoutParams as MarginLayoutParams
        params.setMargins(margin, margin, margin, margin)

        content.layoutParams = params

        addView(pluginBackground)

        moveNob = pluginBackground.move_nob
        resizeNob = pluginBackground.resize_nob
        resizeToRightNob = pluginBackground.resize_to_right_nob

        var dX = -1f
        var dY = -1f

        moveNob?.setOnTouchListener { _, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    dX = x - event.rawX
                    dY = y - event.rawY
                }
                MotionEvent.ACTION_MOVE -> {
                    layoutParams.leftMargin = if (event.rawX + dX < 0) {
                        0
                    } else {
                        (event.rawX + dX).toInt()
                    }

                    layoutParams.topMargin = if (event.rawY + dY < 0) {
                        0
                    } else {
                        (event.rawY + dY).toInt()
                    }

                    requestLayout()
                }
            }
            true
        }

        var lX = -1f
        var lY = -1f

        resizeToRightNob?.setOnTouchListener { _, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    lX = width - event.rawX
                }
                MotionEvent.ACTION_MOVE -> {
                    if (event.rawX + lX < minWidth){
                        layoutParams.width = minWidth.toInt()
                    } else {
                        layoutParams.width = (event.rawX + lX).toInt()
                    }

                    requestLayout()
                }
                MotionEvent.ACTION_UP -> {
                    lX = -1f
                }
            }
            true
        }

        resizeNob?.setOnTouchListener { _, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    lX = width - event.rawX
                    lY = height - event.rawY
                }
                MotionEvent.ACTION_MOVE -> {
                    if (event.rawX + lX < minWidth){
                        layoutParams.width = minWidth.toInt()
                    } else {
                        layoutParams.width = (event.rawX + lX).toInt()
                    }

                    if (event.rawY + lY < minHeight){
                        layoutParams.height = minHeight.toInt()
                    } else {
                        layoutParams.height = (event.rawY + lY).toInt()
                    }

                    requestLayout()
                }
                MotionEvent.ACTION_UP -> {
                    lX = -1f
                    lY = -1f
                }
            }
            true
        }
    }

    open fun onSelect() {
        pluginSelected = true
    }

    open fun onDeselect() {
        pluginSelected = false
    }

    open fun onBoundsChanged(newBounds: RectF) {

    }

    protected fun openContextMenu(position: PointF) {
        // TODO
    }

    open fun delete() {
        App.mainActivity.editorScreen.removePlugin(this)
    }

    open fun copy() {
        // TODO
    }

    open fun cut() {
        // TODO
    }

    // layout params have to be initialized before calling the base method
    open fun onPlace(x: Int, y: Int, layout: FrameLayout) {
        if (layoutParams.width < minWidth) layoutParams.width = minWidth.toInt()
        if (layoutParams.height < minHeight) layoutParams.height = minHeight.toInt()

        layout.addView(this, layoutParams)

        showNobs()
    }

    // changing the plugins bounds
    fun setBounds(bounds: RectF) {
        layoutParams.leftMargin = bounds.left.toInt()
        layoutParams.topMargin = bounds.top.toInt()

        layoutParams.width = bounds.width().toInt()
        layoutParams.height = bounds.height().toInt()

        setLayoutParams(layoutParams)
    }

    protected fun showNobs() {
        if (moveNob != null) {
            moveNob?.visibility = View.VISIBLE
        }

        if (resizeNob != null) {
            resizeNob?.visibility = View.VISIBLE
        }

        if (resizeToRightNob != null) {
            resizeToRightNob?.visibility = View.VISIBLE
        }
    }

    protected fun hideNobs() {
        if (moveNob != null) {
            moveNob?.visibility = View.INVISIBLE
        }

        if (resizeNob != null) {
            resizeNob?.visibility = View.INVISIBLE
        }

        if (resizeToRightNob != null) {
            resizeToRightNob?.visibility = View.INVISIBLE
        }
    }

    open fun savePluginModel(fileId: Int) : Boolean {
        return true
    }

    open fun loadPluginModel(pluginModel: PluginModel) : Boolean {
        return true
    }
}