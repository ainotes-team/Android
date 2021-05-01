package de.vincentscode.AINotes.Helpers.Canvas

import android.graphics.Color
import android.net.Uri
import android.sax.RootElement
import android.util.Xml
import de.vincentscode.AINotes.App
import de.vincentscode.AINotes.Controls.InkCanvas.InkCanvas
import de.vincentscode.AINotes.MainActivity
import java.io.*

object SvgIO {
    fun saveSvg(canvas: InkCanvas) : String {
        // TODO: find a more efficient way
        val file = File(App.mainActivity.filesDir, "strokes")

        file.printWriter().use { out ->
            writeSvg(out, canvas.currentPaths, canvas.width, canvas.height)
            out.close()
        }

        return file.readText()
    }

    private fun writeSvg(writer: Writer, paths: Map<CustomPath, DrawingAttributes>, width: Int, height: Int) {
        writer.apply {
            write("<svg width=\"$width\" height=\"$height\" xmlns=\"http://www.w3.org/2000/svg\">")
            write("<rect width=\"$width\" height=\"$height\"/>")

            for ((key, value) in paths) {
                writePath(this, key, value)
            }
            write("</svg>")
        }
    }

    private fun writePath(writer: Writer, path: CustomPath, options: DrawingAttributes) {
        writer.apply {
            write("<path d=\"")
            path.actions.forEach {
                it.perform(this)
                write(" ")
            }

            write("\" fill=\"none\" stroke=\"")
            write(options.getColor())
            write("\" stroke-width=\"")
            write(options.strokeWidth.toString())
            write("\" stroke-linecap=\"round\"/>")
        }
    }

    fun loadSvg(activity: MainActivity, fileOrUri: Any, canvas: InkCanvas) {
        val svg = parseSvg(fileOrUri)

        canvas.clearCanvas()

        svg.paths.forEach {
            val path = CustomPath()
            path.readObject(it.data, activity)
            val options = DrawingAttributes(it.color, it.strokeWidth)

            canvas.addPath(path, options)
        }
    }

    private fun parseSvg(content: Any): SSvg {
        var inputStream: InputStream? = null
        val svg = SSvg()
        try {
            inputStream = when (content) {
                is File -> FileInputStream(content)
                is Uri -> App.mainActivity.contentResolver.openInputStream(content)
                is String -> ByteArrayInputStream(content.toByteArray(Charsets.UTF_8))
                else -> null
            }

            val ns = "http://www.w3.org/2000/svg"
            val root = RootElement(ns, "svg")
            val rectElement = root.getChild(ns, "rect")
            val pathElement = root.getChild(ns, "path")

            root.setStartElementListener { attributes ->
                val width = attributes.getValue("width").toInt()
                val height = attributes.getValue("height").toInt()
                svg.setSize(width, height)
            }

            rectElement.setStartElementListener { attributes ->
                val width = attributes.getValue("width").toInt()
                val height = attributes.getValue("height").toInt()
                if (svg.background != null)
                    throw UnsupportedOperationException("Unsupported SVG, should only have one <rect>.")

                svg.background = SRect(width, height)
            }

            pathElement.setStartElementListener { attributes ->
                val d = attributes.getValue("d")
                val width = attributes.getValue("stroke-width").toFloat()
                val stroke = attributes.getValue("stroke")
                val color = Color.parseColor(stroke)
                svg.paths.add(SPath(d, color, width, false))
            }

            Xml.parse(inputStream, Xml.Encoding.UTF_8, root.contentHandler)
        } finally {
            inputStream?.close()
        }
        return svg
    }

    private class SSvg : Serializable {
        var background: SRect? = null
        val paths: ArrayList<SPath> = ArrayList()
        private var width = 0
        private var height = 0

        internal fun setSize(w: Int, h: Int) {
            width = w
            height = h
        }
    }

    private class SRect(val width: Int, val height: Int) : Serializable

    private class SPath(var data: String, var color: Int, var strokeWidth: Float, var isEraser: Boolean) :
        Serializable
}