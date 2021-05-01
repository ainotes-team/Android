package de.vincentscode.AINotes.Controls

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout


open class PageContent : LinearLayout {
    var isFabEnabled: Boolean = false

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        orientation = VERTICAL
    }

    // when view is attached to root window
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
    }

    open fun onLoad() {

    }

    open fun onUnload() {

    }

    open fun onFabClicked() {

    }

    open fun onHideKeyboard() {

    }
}