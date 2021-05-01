package de.vincentscode.AINotes.Helpers.ImageEditing

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.content.ContextCompat
import de.vincentscode.AINotes.App
import de.vincentscode.AINotes.R
import java.util.*
import kotlin.math.abs

open class CroppingPolygon : FrameLayout {
    private var ctx: Context = App.mainActivity
    private var paint: Paint? = null
    private var pointer1: ImageView? = null
    private var pointer2: ImageView? = null
    private var pointer3: ImageView? = null
    private var pointer4: ImageView? = null
    private var midPointer13: ImageView? = null
    private var midPointer12: ImageView? = null
    private var midPointer34: ImageView? = null
    private var midPointer24: ImageView? = null
    private var polygonView: CroppingPolygon? = null

    constructor(context: Context) : super(context) {
        this.ctx = context
        init()
    }

    constructor(
        context: Context,
        attrs: AttributeSet?
    ) : super(context, attrs) {
        this.ctx = context
        init()
    }

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr) {
        this.ctx = context
        init()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun init() {
        polygonView = this
        pointer1 = getImageView(0, 0)
        pointer2 = getImageView(width, 0)
        pointer3 = getImageView(0, height)
        pointer4 = getImageView(width, height)
        midPointer13 = getImageView(0, height / 2)
        midPointer13!!.setOnTouchListener(MidPointTouchListenerImpl(pointer1, pointer3))
        midPointer12 = getImageView(0, width / 2)
        midPointer12!!.setOnTouchListener(MidPointTouchListenerImpl(pointer1, pointer2))
        midPointer34 = getImageView(0, height / 2)
        midPointer34!!.setOnTouchListener(MidPointTouchListenerImpl(pointer3, pointer4))
        midPointer24 = getImageView(0, height / 2)
        midPointer24!!.setOnTouchListener(MidPointTouchListenerImpl(pointer2, pointer4))
        addView(pointer1)
        addView(pointer2)
        addView(midPointer13)
        addView(midPointer12)
        addView(midPointer34)
        addView(midPointer24)
        addView(pointer3)
        addView(pointer4)
        initPaint()
    }

    private fun initPaint() {
        paint = Paint()
        paint!!.color = ContextCompat.getColor(context, R.color.blue)
        paint!!.strokeWidth = 1f
        paint!!.isAntiAlias = true
    }

    var points: Map<Int, PointF>
        get() {
            val points: MutableList<PointF> = ArrayList()
            points.add(PointF(pointer1!!.x, pointer1!!.y))
            points.add(PointF(pointer2!!.x, pointer2!!.y))
            points.add(PointF(pointer3!!.x, pointer3!!.y))
            points.add(PointF(pointer4!!.x, pointer4!!.y))
            return getOrderedPoints(points)
        }
        set(pointFMap) {
            if (pointFMap.size == 4) {
                setPointsCoordinates(pointFMap)
            }
        }

    fun getOrderedPoints(points: List<PointF>): Map<Int, PointF> {
        val centerPoint = PointF()
        val size = points.size
        for (pointF in points) {
            centerPoint.x += pointF.x / size
            centerPoint.y += pointF.y / size
        }
        val orderedPoints: MutableMap<Int, PointF> =
            HashMap()
        for (pointF in points) {
            var index = -1
            if (pointF.x < centerPoint.x && pointF.y < centerPoint.y) {
                index = 0
            } else if (pointF.x > centerPoint.x && pointF.y < centerPoint.y) {
                index = 1
            } else if (pointF.x < centerPoint.x && pointF.y > centerPoint.y) {
                index = 2
            } else if (pointF.x > centerPoint.x && pointF.y > centerPoint.y) {
                index = 3
            }
            orderedPoints[index] = pointF
        }
        return orderedPoints
    }

    private fun setPointsCoordinates(pointFMap: Map<Int, PointF>) {
        pointer1!!.x = (pointFMap[0] ?: error("")).x
        pointer1!!.y = (pointFMap[0] ?: error("")).y
        pointer2!!.x = (pointFMap[1] ?: error("")).x
        pointer2!!.y = (pointFMap[1] ?: error("")).y
        pointer3!!.x = (pointFMap[2] ?: error("")).x
        pointer3!!.y = (pointFMap[2] ?: error("")).y
        pointer4!!.x = (pointFMap[3] ?: error("")).x
        pointer4!!.y = (pointFMap[3] ?: error("")).y
    }

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)
        canvas.drawLine(
            pointer1!!.x + pointer1!!.width / 2,
            pointer1!!.y + pointer1!!.height / 2,
            pointer3!!.x + pointer3!!.width / 2,
            pointer3!!.y + pointer3!!.height / 2,
            paint!!
        )
        canvas.drawLine(
            pointer1!!.x + pointer1!!.width / 2,
            pointer1!!.y + pointer1!!.height / 2,
            pointer2!!.x + pointer2!!.width / 2,
            pointer2!!.y + pointer2!!.height / 2,
            paint!!
        )
        canvas.drawLine(
            pointer2!!.x + pointer2!!.width / 2,
            pointer2!!.y + pointer2!!.height / 2,
            pointer4!!.x + pointer4!!.width / 2,
            pointer4!!.y + pointer4!!.height / 2,
            paint!!
        )
        canvas.drawLine(
            pointer3!!.x + pointer3!!.width / 2,
            pointer3!!.y + pointer3!!.height / 2,
            pointer4!!.x + pointer4!!.width / 2,
            pointer4!!.y + pointer4!!.height / 2,
            paint!!
        )
        midPointer13!!.x = pointer3!!.x - (pointer3!!.x - pointer1!!.x) / 2
        midPointer13!!.y = pointer3!!.y - (pointer3!!.y - pointer1!!.y) / 2
        midPointer24!!.x = pointer4!!.x - (pointer4!!.x - pointer2!!.x) / 2
        midPointer24!!.y = pointer4!!.y - (pointer4!!.y - pointer2!!.y) / 2
        midPointer34!!.x = pointer4!!.x - (pointer4!!.x - pointer3!!.x) / 2
        midPointer34!!.y = pointer4!!.y - (pointer4!!.y - pointer3!!.y) / 2
        midPointer12!!.x = pointer2!!.x - (pointer2!!.x - pointer1!!.x) / 2
        midPointer12!!.y = pointer2!!.y - (pointer2!!.y - pointer1!!.y) / 2
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun getImageView(x: Int, y: Int): ImageView {
        val imageView = ImageView(context)
        val layoutParams = LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        imageView.layoutParams = layoutParams
        imageView.setImageResource(R.drawable.circle)
        imageView.x = x.toFloat()
        imageView.y = y.toFloat()
        imageView.setOnTouchListener(TouchListenerImpl())
        return imageView
    }

    private inner class MidPointTouchListenerImpl(
        private val mainPointer1: ImageView?,
        private val mainPointer2: ImageView?
    ) : OnTouchListener {
        var downPosition = PointF() // Record Mouse Position When Pressed Down
        var startPosition = PointF() // Record Start Position of 'img'

        @SuppressLint("ClickableViewAccessibility")
        override fun onTouch(v: View, event: MotionEvent): Boolean {
            when (event.action) {
                MotionEvent.ACTION_MOVE -> {
                    val mv = PointF(event.x - downPosition.x, event.y - downPosition.y)
                    if (abs(mainPointer1!!.x - mainPointer2!!.x) > abs(
                            mainPointer1.y - mainPointer2.y
                        )
                    ) {
                        if (mainPointer2.y + mv.y + v.height < polygonView!!.height && mainPointer2.y + mv.y > 0) {
                            v.x = (startPosition.y + mv.y)
                            startPosition = PointF(v.x, v.y)
                            mainPointer2.y = (mainPointer2.y + mv.y)
                        }
                        if (mainPointer1.y + mv.y + v.height < polygonView!!.height && mainPointer1.y + mv.y > 0) {
                            v.x = (startPosition.y + mv.y)
                            startPosition = PointF(v.x, v.y)
                            mainPointer1.y = (mainPointer1.y + mv.y)
                        }
                    } else {
                        if (mainPointer2.x + mv.x + v.width < polygonView!!.width && mainPointer2.x + mv.x > 0) {
                            v.x = (startPosition.x + mv.x)
                            startPosition = PointF(v.x, v.y)
                            mainPointer2.x = (mainPointer2.x + mv.x)
                        }
                        if (mainPointer1.x + mv.x + v.width < polygonView!!.width && mainPointer1.x + mv.x > 0) {
                            v.x = (startPosition.x + mv.x)
                            startPosition = PointF(v.x, v.y)
                            mainPointer1.x = (mainPointer1.x + mv.x)
                        }
                    }
                }
                MotionEvent.ACTION_DOWN -> {
                    downPosition.x = event.x
                    downPosition.y = event.y
                    startPosition = PointF(v.x, v.y)
                }
                MotionEvent.ACTION_UP -> {
                    var color = 0
                    color = if (isValidShape(points)) {
                        resources.getColor(R.color.blue)
                    } else {
                        resources.getColor(R.color.orange)
                    }
                    paint!!.color = color
                }
                else -> {
                }
            }
            polygonView!!.invalidate()
            return true
        }

    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return super.onTouchEvent(event)
    }

    fun isValidShape(pointFMap: Map<Int, PointF>): Boolean {
        return pointFMap.size == 4
    }

    private inner class TouchListenerImpl : OnTouchListener {
        var downPosition = PointF() // Record Mouse Position When Pressed Down
        var startPosition = PointF() // Record Start Position of 'img'

        @SuppressLint("ClickableViewAccessibility")
        override fun onTouch(v: View, event: MotionEvent): Boolean {
            when (event.action) {
                MotionEvent.ACTION_MOVE -> {
                    val mv = PointF(event.x - downPosition.x, event.y - downPosition.y)
                    if (startPosition.x + mv.x + v.width < polygonView!!.width && startPosition.y + mv.y + v.height < polygonView!!.height && startPosition.x + mv.x > 0 && startPosition.y + mv.y > 0) {
                        v.x = (startPosition.x + mv.x)
                        v.y = (startPosition.y + mv.y)
                        startPosition = PointF(v.x, v.y)
                    }
                }
                MotionEvent.ACTION_DOWN -> {
                    downPosition.x = event.x
                    downPosition.y = event.y
                    startPosition = PointF(v.x, v.y)
                }
                MotionEvent.ACTION_UP -> {
                    val color: Int = if (isValidShape(points)) {
                        resources.getColor(R.color.blue)
                    } else {
                        resources.getColor(R.color.orange)
                    }
                    paint!!.color = color
                }
                else -> {
                }
            }
            polygonView!!.invalidate()
            return true
        }
    }
}