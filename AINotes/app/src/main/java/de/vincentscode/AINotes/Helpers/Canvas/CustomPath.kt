package de.vincentscode.AINotes.Helpers.Canvas

import android.app.Activity
import android.graphics.Path
import android.graphics.PointF
import android.graphics.RectF
import de.vincentscode.AINotes.App
import de.vincentscode.AINotes.Helpers.Canvas.Actions.Action
import de.vincentscode.AINotes.Helpers.Canvas.Actions.Line
import de.vincentscode.AINotes.Helpers.Canvas.Actions.Move
import de.vincentscode.AINotes.Helpers.Canvas.Actions.Quad
import de.vincentscode.AINotes.Helpers.Logger
import de.vincentscode.AINotes.R
import java.io.ObjectInputStream
import java.io.Serializable
import java.io.Writer
import java.security.InvalidParameterException
import java.util.*
import kotlin.collections.ArrayList

class CustomPath : Path(), Serializable {
    val actions = LinkedList<Action>()
    var strokeBounds = RectF()
    var isSelected = false

    var currentPoints: Array<PointF> = arrayOf()

    private fun readObject(inputStream: ObjectInputStream) {
        inputStream.defaultReadObject()

        val copiedActions = actions.map { it }
        copiedActions.forEach {
            it.perform(this)
        }
    }

    fun readObject(pathData: String, activity: Activity) {
        val tokens = pathData.split("\\s+".toRegex()).dropLastWhile(String::isEmpty).toTypedArray()
        var i = 0
        try {
            while (i < tokens.size) {
                when (tokens[i][0]) {
                    'M' -> addAction(
                        Move(
                            tokens[i]
                        )
                    )
                    'L' -> addAction(
                        Line(
                            tokens[i]
                        )
                    )
                    'Q' -> {
                        // Quad actions are of the following form:
                        // "Qx1,y1 x2,y2"
                        if (i + 1 >= tokens.size)
                            throw InvalidParameterException("Error parsing the data for a Quad.")

                        addAction(
                            Quad(
                                tokens[i] + " " + tokens[i + 1]
                            )
                        )
                        ++i
                    }
                }
                ++i
            }
            updateBounds()
        } catch (e: Exception) {
            Logger.toast(App.mainActivity.getString(R.string.unknown_error))
        }
    }

    override fun reset() {
        actions.clear()
        super.reset()
    }

    private fun addAction(action: Action) {
        when (action) {
            is Move -> moveTo(action.x, action.y)
            is Line -> lineTo(action.x, action.y)
            is Quad -> quadTo(action.x1, action.y1, action.x2, action.y2)
        }
    }

    override fun moveTo(x: Float, y: Float) {
        actions.add(Move(x, y))
        super.moveTo(x, y)
        updateBounds()
    }

    override fun lineTo(x: Float, y: Float) {
        actions.add(Line(x, y))
        super.lineTo(x, y)
        updateBounds()
    }

    override fun quadTo(x1: Float, y1: Float, x2: Float, y2: Float) {
        actions.add(
            Quad(
                x1, y1,
                x2, y2
            )
        )
        super.quadTo(x1, y1, x2, y2)
        updateBounds()
    }

    private fun updateBounds() {
        this.computeBounds(strokeBounds, true)
    }

    public fun setPoints(points: Array<PointF>) {
        currentPoints = points
    }
}