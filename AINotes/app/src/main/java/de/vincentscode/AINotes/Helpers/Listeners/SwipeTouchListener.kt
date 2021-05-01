package de.vincentscode.AINotes.Helpers.Listeners

import android.annotation.SuppressLint
import android.content.Context
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.widget.ListView
import kotlin.math.abs


class OnSwipeTouchListener : OnTouchListener {
    var list: ListView? = null
    private var gestureDetector: GestureDetector? = null
    private var context: Context? = null

    constructor(ctx: Context?, list: ListView?) {
        gestureDetector = GestureDetector(ctx, GestureListener())
        context = ctx
        this.list = list
    }

    constructor() : super() {}

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        return gestureDetector!!.onTouchEvent(event)
    }

    fun onSwipeRight(pos: Int) {
        //Do what you want after swiping left to right
    }

    fun onSwipeLeft(pos: Int) {

        //Do what you want after swiping right to left
    }

    private inner class GestureListener : SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent): Boolean {
            return true
        }

        private fun getPosition(e1: MotionEvent): Int {
            return list?.pointToPosition(e1.x.toInt(), e1.y.toInt())!!
        }

        override fun onFling(
            e1: MotionEvent, e2: MotionEvent,
            velocityX: Float, velocityY: Float
        ): Boolean {
            val distanceX = e2.x - e1.x
            val distanceY = e2.y - e1.y
            if (abs(distanceX) > abs(distanceY) && abs(distanceX) > 100 && abs(
                    velocityX
                ) > 100
            ) {
                if (distanceX > 0) onSwipeRight(getPosition(e1)) else onSwipeLeft(getPosition(e1))
                return true
            }
            return false
        }
    }
}