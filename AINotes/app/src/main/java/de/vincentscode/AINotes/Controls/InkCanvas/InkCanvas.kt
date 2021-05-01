package de.vincentscode.AINotes.Controls.InkCanvas

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.*
import android.os.Parcelable
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.request.RequestOptions
import de.vincentscode.AINotes.App
import de.vincentscode.AINotes.Helpers.Canvas.CustomParcelable
import de.vincentscode.AINotes.Helpers.Canvas.CustomPath
import de.vincentscode.AINotes.Helpers.Canvas.DrawingAttributes
import de.vincentscode.AINotes.Helpers.Canvas.SvgIO
import de.vincentscode.AINotes.Helpers.Listeners.CanvasListener
import de.vincentscode.AINotes.Helpers.Logger
import de.vincentscode.AINotes.Helpers.MathHelper
import de.vincentscode.AINotes.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import java.util.concurrent.ExecutionException


class InkCanvas(context: Context) : View(context) {
    enum class InkCanvasMode {
        Pen,
        Eraser,
        Selection,
        None
    }

    private var eraserWidth = 60f

    public var currentPaths = LinkedHashMap<CustomPath, DrawingAttributes>()
    private var backgroundBitmap: Bitmap? = null
    private var canvasListener: CanvasListener? = null

    private var lastPaths = LinkedHashMap<CustomPath, DrawingAttributes>()
    private var lastBackgroundBitmap: Bitmap? = null
    private var undonePaths = LinkedHashMap<CustomPath, DrawingAttributes>()

    // drawing
    private var drawingPaint = Paint()
    private var drawingPath = CustomPath()
    private var drawingAttributes = DrawingAttributes()

    // erasing
    private var eraserPath = CustomPath()
    private var eraserPaint = Paint()

    // selection
    private var selectionPath = CustomPath()
    private var selectionPaint = Paint()

    private var currentX = 0f
    private var currentY = 0f
    private var startX = 0f
    private var startY = 0f
    private var brushSize = 0f
    private var allowZoom = true
    private var multitouch = false
    private var currentBackgroundColor = 0
    private var center: PointF? = null

    private var currentPoints: ArrayList<PointF> = arrayListOf()

    var currentMode: InkCanvasMode = InkCanvasMode.Pen

    private var scaleFactor = 1f

    init {
        drawingPaint.apply {
            color = drawingAttributes.color
            style = Paint.Style.STROKE
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
            strokeWidth = drawingAttributes.strokeWidth
            isAntiAlias = true
        }

        eraserPaint.apply {
            color = Color.LTGRAY
            style = Paint.Style.FILL
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
            isAntiAlias = true
        }

        selectionPaint.apply {
            color = Color.BLUE
            style = Paint.Style.STROKE
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
            strokeWidth = 10f
            isAntiAlias = true
            pathEffect = DashPathEffect(floatArrayOf(4f, 16f), 10f)
        }

        pathsUpdated()
    }

    fun undo() {
        if (currentPaths.isEmpty() && lastPaths.isNotEmpty()) {
            currentPaths = lastPaths.clone() as LinkedHashMap<CustomPath, DrawingAttributes>
            backgroundBitmap = lastBackgroundBitmap
            lastPaths.clear()
            pathsUpdated()
            invalidate()
            return
        }

        if (currentPaths.isEmpty()) {
            return
        }

        val lastPath = currentPaths.values.lastOrNull()
        val lastKey = currentPaths.keys.lastOrNull()

        currentPaths.remove(lastKey)
        if (lastPath != null && lastKey != null) {
            undonePaths[lastKey] = lastPath
            canvasListener?.toggleRedoVisibility(true)
        }
        pathsUpdated()
        invalidate()
    }

    fun redo() {
        if (undonePaths.keys.isEmpty()) {
            canvasListener?.toggleRedoVisibility(false)
            return
        }

        val lastKey = undonePaths.keys.last()
        addPath(lastKey, undonePaths.values.last())
        undonePaths.remove(lastKey)
        if (undonePaths.isEmpty()) {
            canvasListener?.toggleRedoVisibility(false)
        }
        invalidate()
    }

    public fun setEraserWidth(thickness: Float) {
        eraserWidth = thickness
    }

    fun deleteSelected() {
        currentPaths.keys.toTypedArray().forEach {
            if (it.isSelected) {
                currentPaths.remove(it)
            }
        }
        invalidate()
    }

    fun setMode(mode: InkCanvasMode) {
        currentMode = mode
        invalidate()
    }

    fun setColor(newColor: Int) {
        drawingAttributes.color = newColor
    }

    fun updateBackgroundColor(newColor: Int) {
        currentBackgroundColor = newColor
        setBackgroundColor(newColor)
        backgroundBitmap = null
    }

    fun setBrushSize(newBrushSize: Float) {
        brushSize = newBrushSize
        drawingAttributes.strokeWidth = resources.getDimension(R.dimen.full_brush_size) * (newBrushSize / scaleFactor / 100f)
    }

    fun setAllowZooming(allowZooming: Boolean) {
        allowZoom = allowZooming
    }

    fun getBitmap(): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)
        draw(canvas)
        return bitmap
    }

    fun drawBitmap(activity: Activity, path: Any) {
        Thread {
            val size = Point()
            activity.windowManager.defaultDisplay.getSize(size)
            val options = RequestOptions()
                .format(DecodeFormat.PREFER_ARGB_8888)
                .disallowHardwareConfig()
                .fitCenter()

            try {
                val builder = Glide.with(context)
                    .asBitmap()
                    .load(path)
                    .apply(options)
                    .into(size.x, size.y)

                backgroundBitmap = builder.get()
                activity.runOnUiThread {
                    invalidate()
                }
            } catch (e: ExecutionException) {
                val errorMsg = context.getString(R.string.failed_to_load)
                Logger.toast(errorMsg)
            }
        }.start()
    }

    fun addPath(path: CustomPath, options: DrawingAttributes) {
        currentPaths[path] = options
        pathsUpdated()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.save()

        if (center == null) {
            center = PointF(width / 2f, height / 2f)
        }

        canvas.scale(scaleFactor, scaleFactor, center!!.x, center!!.y)

        if (backgroundBitmap != null) {
            val left = (width - backgroundBitmap!!.width) / 2
            val top = (height - backgroundBitmap!!.height) / 2
            canvas.drawBitmap(backgroundBitmap!!, left.toFloat(), top.toFloat(), null)
        }

        // adding each path to the canvas
        for ((key, value) in currentPaths) {
            changePaint(value)
            canvas.drawPath(key, drawingPaint)
        }

        if (currentMode == InkCanvasMode.Eraser) {
            canvas.drawPath(eraserPath, eraserPaint)
        }

        changePaint(drawingAttributes)
        canvas.drawPath(drawingPath, drawingPaint)
        canvas.restore()

        if (wasEraser) {
            wasEraser = false
            currentMode = InkCanvasMode.Eraser
        }

        if (currentMode == InkCanvasMode.Selection) {
            canvas.drawPath(selectionPath, selectionPaint)
        }
    }

    private fun changePaint(paintOptions: DrawingAttributes) {
        drawingPaint.color = paintOptions.color
        drawingPaint.strokeWidth = paintOptions.strokeWidth
    }

    fun clearCanvas() {
        lastPaths = currentPaths.clone() as LinkedHashMap<CustomPath, DrawingAttributes>
        lastBackgroundBitmap = backgroundBitmap
        backgroundBitmap = null
        drawingPath.reset()
        currentPaths.clear()
        pathsUpdated()
        invalidate()
    }

    private fun actionDown(x: Float, y: Float) {
        when (currentMode) {
            InkCanvasMode.Pen -> {
                drawingPath.reset()
                drawingPath.moveTo(x, y)
                currentX = x
                currentY = y
            }
            InkCanvasMode.Eraser -> {
                eraserPath.reset()
                return
            }
            InkCanvasMode.Selection -> {
                selectionPath.reset()
                selectionPath.moveTo(x, y)
                currentX = x
                currentY = y
            }
        }
    }

    private fun actionMove(x: Float, y: Float) {
        when (currentMode) {
            InkCanvasMode.Pen -> {
                drawingPath.quadTo(currentX, currentY, (x + currentX) / 2, (y + currentY) / 2)
                currentX = x
                currentY = y
            }
            InkCanvasMode.Eraser -> {
                erase(x, y)
            }
            InkCanvasMode.Selection -> {
                selectionPath.quadTo(currentX, currentY, (x + currentX) / 2, (y + currentY) / 2)
                currentX = x
                currentY = y
            }
        }
    }

    private var wasEraser = false

    private fun actionUp() {
        when (currentMode) {
            InkCanvasMode.Pen -> {
                if (!multitouch) {
                    drawingPath.lineTo(currentX, currentY)

                    // draw a dot on click
                    if (startX == currentX && startY == currentY) {
                        drawingPath.lineTo(currentX, currentY + 2)
                        drawingPath.lineTo(currentX + 1, currentY + 2)
                        drawingPath.lineTo(currentX + 1, currentY)
                    }
                }


                drawingPath.setPoints(currentPoints.toTypedArray())

                currentPaths[drawingPath] = drawingAttributes
                pathsUpdated()
                drawingPath = CustomPath()
                drawingAttributes = DrawingAttributes(drawingAttributes.color, drawingAttributes.strokeWidth)
            }
            InkCanvasMode.Eraser -> {
                eraserPath.reset()
                currentMode = InkCanvasMode.Pen
                wasEraser = true
                return
            }
            InkCanvasMode.Selection -> {
                selectionPath.reset()

                selectByPolyline(currentPoints.toTypedArray())
            }
            else -> {}
        }
    }

    private fun selectByPolyline(polyline: Array<PointF>) : RectF {
        currentPaths.keys.forEach {
            if (MathHelper.isPointInPolygon(polyline,
                    PointF(drawingPath.strokeBounds.left, drawingPath.strokeBounds.top))) {
                it.isSelected = true
            }
        }

        return RectF()
    }

    private fun erase(x: Float, y: Float) {
        eraserPath.reset()
        eraserPath.addCircle(x, y, eraserWidth, Path.Direction.CW)
        currentPaths.keys.toTypedArray().forEach {
            val path = it
            if (path.strokeBounds.intersect(RectF(x - eraserWidth, y - eraserWidth,
                    x + eraserWidth, y + eraserWidth))) {
                currentPaths.remove(path)
            }
        }
    }

    private fun pathsUpdated() {
        canvasListener?.toggleUndoVisibility(currentPaths.isNotEmpty() || lastPaths.isNotEmpty())
    }

    fun getDrawingHashCode() = currentPaths.hashCode().toLong() + (backgroundBitmap?.hashCode()?.toLong() ?: 0L)

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        App.mainActivity.editorScreen.optionsBar?.closeAdvancedOptions()

        var x = event.x
        var y = event.y

        if (scaleFactor != 1f) {
            val fullWidth = width / scaleFactor
            var curTouchX = fullWidth * x / width
            curTouchX -= (fullWidth / 2) * (1 - scaleFactor)
            x = curTouchX

            val fullHeight = height / scaleFactor
            var curTouchY = fullHeight * y / height
            curTouchY -= (fullHeight / 2) * (1 - scaleFactor)
            y = curTouchY
        }

        if (currentMode == InkCanvasMode.None) {
            invalidate()
            return false
        }

        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                multitouch = false
                startX = x
                startY = y
                actionDown(x, y)
                currentPoints.clear()
                currentPoints.add(PointF(x, y))
                undonePaths.clear()
                canvasListener?.toggleRedoVisibility(false)
            }
            MotionEvent.ACTION_MOVE -> {
                if (!allowZoom || (event.pointerCount == 1 && !multitouch)) {
                    currentPoints.add(PointF(x, y))
                    actionMove(x, y)
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                currentPoints.add(PointF(x, y))
                actionUp()
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                multitouch = true
            }
        }

        invalidate()
        return true
    }

    fun load(content: String) {
        CoroutineScope(IO).launch {
            SvgIO.loadSvg(App.mainActivity, content, this@InkCanvas)
            invalidate()
        }
    }

    fun save() : String {
        return SvgIO.saveSvg(this)
    }

    public override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        val savedState =
            CustomParcelable(superState!!)
        savedState.paths = currentPaths
        return savedState
    }

    public override fun onRestoreInstanceState(state: Parcelable) {
        if (state !is CustomParcelable) {
            super.onRestoreInstanceState(state)
            return
        }

        super.onRestoreInstanceState(state.superState)
        currentPaths = state.paths
        pathsUpdated()
    }
}