package de.vincentscode.AINotes.Controls

import android.content.Context
import android.graphics.Point
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.EdgeEffect
import android.widget.FrameLayout
import android.widget.OverScroller
import android.widget.ScrollView
import androidx.core.view.ViewCompat

class CustomScrollView : FrameLayout {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {

    }

//    private val AXIS_Y_MIN: Float = 0f
//    private val AXIS_Y_MAX: Float = 10000f
//    private val AXIS_X_MAX: Float = 10000f
//    private val AXIS_X_MIN: Float = 0f
//
//    // The current viewport. This rectangle represents the currently visible
//// chart domain and range. The viewport is the part of the app that the
//// user manipulates via touch gestures.
//    private val mCurrentViewport = RectF(AXIS_X_MIN, AXIS_Y_MIN, AXIS_X_MAX, AXIS_Y_MAX)
//
//    // The current destination rectangle (in pixel coordinates) into which the
//// chart data should be drawn.
//    private lateinit var mContentRect: Rect
//
//    private lateinit var mScroller: OverScroller
//    private lateinit var mScrollerStartViewport: RectF
//
//    private val mGestureListener = object : GestureDetector.SimpleOnGestureListener() {
//
//        override fun onDown(e: MotionEvent): Boolean {
//            mScrollerStartViewport.set(mCurrentViewport)
//            // Aborts any active scroll animations and invalidates.
//            mScroller.forceFinished(true)
//            ViewCompat.postInvalidateOnAnimation(this@CustomScrollView)
//            return true
//        }
//
//        override fun onFling(
//            e1: MotionEvent,
//            e2: MotionEvent,
//            velocityX: Float,
//            velocityY: Float
//        ): Boolean {
//            fling((-velocityX).toInt(), (-velocityY).toInt())
//            return true
//        }
//    }
//
//    private fun fling(velocityX: Int, velocityY: Int) {
//        // Flings use math in pixels (as opposed to math based on the viewport).
//        val surfaceSize: Point = computeScrollSurfaceSize()
//        val (startX: Int, startY: Int) = mScrollerStartViewport.run {
//            set(mCurrentViewport)
//            (surfaceSize.x * (left - AXIS_X_MIN) / (AXIS_X_MAX - AXIS_X_MIN)).toInt() to
//                    (surfaceSize.y * (AXIS_Y_MAX - bottom) / (AXIS_Y_MAX - AXIS_Y_MIN)).toInt()
//        }
//        // Before flinging, aborts the current animation.
//        mScroller.forceFinished(true)
//        // Begins the animation
//        mScroller.fling(
//            // Current scroll position
//            startX,
//            startY,
//            velocityX,
//            velocityY,
//            /*
//             * Minimum and maximum scroll positions. The minimum scroll
//             * position is generally zero and the maximum scroll position
//             * is generally the content size less the screen size. So if the
//             * content width is 1000 pixels and the screen width is 200
//             * pixels, the maximum scroll offset should be 800 pixels.
//             */
//            0, surfaceSize.x - mContentRect.width(),
//            0, surfaceSize.y - mContentRect.height(),
//            // The edges of the content. This comes into play when using
//            // the EdgeEffect class to draw "glow" overlays.
//            mContentRect.width() / 2,
//            mContentRect.height() / 2
//        )
//        // Invalidates to trigger computeScroll()
//        ViewCompat.postInvalidateOnAnimation(this)
//    }
//
//    // Edge effect / overscroll tracking objects.
//    private lateinit var mEdgeEffectTop: EdgeEffect
//    private lateinit var mEdgeEffectBottom: EdgeEffect
//    private lateinit var mEdgeEffectLeft: EdgeEffect
//    private lateinit var mEdgeEffectRight: EdgeEffect
//
//    private var mEdgeEffectTopActive: Boolean = false
//    private var mEdgeEffectBottomActive: Boolean = false
//    private var mEdgeEffectLeftActive: Boolean = false
//    private var mEdgeEffectRightActive: Boolean = false
//
//    override fun computeScroll() {
//        super.computeScroll()
//
//        var needsInvalidate = false
//
//        // The scroller isn't finished, meaning a fling or programmatic pan
//        // operation is currently active.
//        if (mScroller.computeScrollOffset()) {
//            val surfaceSize: Point = computeScrollSurfaceSize()
//            val currX: Int = mScroller.currX
//            val currY: Int = mScroller.currY
//
//            val (canScrollX: Boolean, canScrollY: Boolean) = mCurrentViewport.run {
//                (left > AXIS_X_MIN || right < AXIS_X_MAX) to (top > AXIS_Y_MIN || bottom < AXIS_Y_MAX)
//            }
//
//            /*
//             * If you are zoomed in and currX or currY is
//             * outside of bounds and you are not already
//             * showing overscroll, then render the overscroll
//             * glow edge effect.
//             */
//            if (canScrollX
//                && currX < 0
//                && mEdgeEffectLeft.isFinished
//                && !mEdgeEffectLeftActive) {
//                mEdgeEffectLeft.onAbsorb(mScroller.currVelocity.toInt())
//                mEdgeEffectLeftActive = true
//                needsInvalidate = true
//            } else if (canScrollX
//                && currX > surfaceSize.x - mContentRect.width()
//                && mEdgeEffectRight.isFinished
//                && !mEdgeEffectRightActive) {
//                mEdgeEffectRight.onAbsorb(mScroller.currVelocity.toInt())
//                mEdgeEffectRightActive = true
//                needsInvalidate = true
//            }
//
//            if (canScrollY
//                && currY < 0
//                && mEdgeEffectTop.isFinished
//                && !mEdgeEffectTopActive) {
//                mEdgeEffectTop.onAbsorb(mScroller.currVelocity.toInt())
//                mEdgeEffectTopActive = true
//                needsInvalidate = true
//            } else if (canScrollY
//                && currY > surfaceSize.y - mContentRect.height()
//                && mEdgeEffectBottom.isFinished
//                && !mEdgeEffectBottomActive) {
//                mEdgeEffectBottom.onAbsorb(mScroller.currVelocity.toInt())
//                mEdgeEffectBottomActive = true
//                needsInvalidate = true
//            }
//        }
//    }
//
//    private fun computeScrollSurfaceSize(): Point {
//        return Point(
//            (mContentRect.width() * (AXIS_X_MAX - AXIS_X_MIN) / mCurrentViewport.width()).toInt(),
//            (mContentRect.height() * (AXIS_Y_MAX - AXIS_Y_MIN) / mCurrentViewport.height()).toInt()
//        )
//    }

}