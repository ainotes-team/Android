package de.vincentscode.AINotes.Controls

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout

class CustomScrollContent : FrameLayout {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {

    }
}