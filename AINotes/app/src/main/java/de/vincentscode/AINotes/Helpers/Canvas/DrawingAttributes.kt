package de.vincentscode.AINotes.Helpers.Canvas

import android.graphics.Color

data class DrawingAttributes(var color: Int = Color.BLACK,
                             var strokeWidth: Float = 5f) {

    fun getColor() = "#${Integer.toHexString(color).substring(2)}"
}