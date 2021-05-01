package de.vincentscode.AINotes.Controls

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.widget.LinearLayout

@SuppressLint("ViewConstructor")
class AnalyzedImage(context: Context, val source: Bitmap) : LinearLayout(context) {
    var processing: Boolean = false

    var autoCropped: Bitmap? = null

    init {

    }
}