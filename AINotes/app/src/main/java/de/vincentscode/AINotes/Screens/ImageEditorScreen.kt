package de.vincentscode.AINotes.Screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.PointF
import android.graphics.RectF
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import com.google.android.material.bottomnavigation.BottomNavigationView
import de.vincentscode.AINotes.App
import de.vincentscode.AINotes.Controls.PageContent
import de.vincentscode.AINotes.Helpers.ImageEditing.ImageConstants
import de.vincentscode.AINotes.Helpers.ImageEditing.OpenCV
import de.vincentscode.AINotes.Helpers.ImageEditing.CroppingPolygon
import de.vincentscode.AINotes.Helpers.Logger
import de.vincentscode.AINotes.R
import de.vincentscode.AINotes.Screens.ImageEditorScreen.ImageEditMode.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.image_editor_screen.view.*
import kotlinx.android.synthetic.main.toolbar_image_editor.view.*
import org.opencv.core.MatOfPoint2f
import java.util.*

open class ImageEditorScreen(context: Context) : PageContent(context) {

    enum class ImageEditMode {
        None,
        Cropping,
        PerspectiveCropping,
        Filtering
    }

    private var mode: ImageEditMode = None

    // toolbar
    private var toolbarContent: View = View.inflate(context, R.layout.toolbar_image_editor, null)

    // views
    private lateinit var currentBitmap: Bitmap
    private lateinit var currentBitmapCopy: Bitmap

    private var selectedImageBitmap: Bitmap? = null

    private var imageHolder: FrameLayout? = null
    private var editorImageView: ImageView? = null
    private var croppingPolygon: CroppingPolygon? = null

    // opencv object
    private var openCV = OpenCV()

    private fun updateButtons() {
        when (mode) {
            PerspectiveCropping, Cropping, Filtering -> {
                Logger.toast("Perspective Cropping")
                toolbarContent.image_editor_save.visibility = View.GONE
                toolbarContent.image_editor_done.visibility = View.VISIBLE
            }
            None -> {
                toolbarContent.image_editor_save.visibility = View.VISIBLE
                toolbarContent.image_editor_done.visibility = View.GONE
            }
        }
    }

    private val optionsItemSelected =
        BottomNavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.adjust_corners_button -> {
                    initializePerspectiveCropping()
                    updateButtons()
                }
                R.id.crop_button -> {
                    initializeCropping()
                    updateButtons()
                }
                R.id.filter_button -> {
                    initializeFilters()
                    updateButtons()
                }
            }

            image_editor_bottom_options.visibility = View.GONE

            false
        }

    override fun onLoad() {
        View.inflate(context, R.layout.image_editor_screen, this)

        imageHolder = findViewById(R.id.imageContainer)
        editorImageView = findViewById(R.id.editorImageView)
        croppingPolygon = findViewById(R.id.croppingRectangleView)

        imageHolder?.post {
            selectedImageBitmap = ImageConstants.selectedImageBitmap
            ImageConstants.selectedImageBitmap = null

            updateBitmap(getScaledBitmap(
                selectedImageBitmap,
                imageHolder!!.width,
                imageHolder!!.height
            ))
        }

        image_editor_bottom_options.setOnNavigationItemSelectedListener(optionsItemSelected)
        // make bottom bar uncheckable
        image_editor_bottom_options.menu.setGroupCheckable(0, false, true)

        setToolbar()

        updateButtons()

        App.mainActivity.isToolbarDividerVisible(false)
    }

    private fun setToolbar() {
        //App.mainActivity.toolbar.layoutParams.height = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 64f, resources.displayMetrics).toInt()
        App.mainActivity.toolbar.requestLayout()

        App.mainActivity.toolbar.removeAllViews()
        App.mainActivity.toolbar.addView(toolbarContent)

        toolbarContent.image_editor_cancel.setOnClickListener {
            App.mainActivity.load(App.mainActivity.editorScreen)
        }

        toolbarContent.image_editor_done.setOnClickListener {
            App.mainActivity.load(App.mainActivity.editorScreen)

            App.mainActivity.editorScreen.loadFile(App.mainActivity.editorScreen.currentFileModel)

            // TODO: set new Image source
        }

        toolbarContent.image_editor_save.setOnClickListener {
            Logger.toast("wshjfiosj")
        }
    }

    override fun onUnload() {
        App.mainActivity.isToolbarDividerVisible(true)

        //App.mainActivity.toolbar.layoutParams.height = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 64f, resources.displayMetrics).toInt()
        App.mainActivity.toolbar.requestLayout()
    }

    private fun updateBitmap(newBitmap: Bitmap) {
        currentBitmap = newBitmap
        currentBitmapCopy = currentBitmap.copy(Bitmap.Config.ARGB_8888, true)

        editorImageView!!.setImageBitmap(newBitmap)
    }

    private fun initializeFilters() {
        mode = Filtering
    }

    private fun initializeCropping() {
        mode = Cropping
    }

    private fun initializePerspectiveCropping() {
        mode = PerspectiveCropping

        val pointFs = getEdgePoints(currentBitmapCopy)

        croppingPolygon?.points = pointFs
        croppingPolygon?.visibility = View.VISIBLE

        val padding = resources.getDimension(R.dimen.scanPadding).toInt()

        val layoutParams = FrameLayout.LayoutParams(
            currentBitmapCopy.width + 2 * padding,
            currentBitmapCopy.height + 2 * padding
        )

        layoutParams.gravity = Gravity.CENTER
        croppingPolygon?.layoutParams = layoutParams
    }

    private val croppedImage: Bitmap
        get() {
            val points: Map<Int, PointF> = croppingPolygon?.points!!

            val xRatio = selectedImageBitmap!!.width.toFloat() / editorImageView!!.width
            val yRatio = selectedImageBitmap!!.height.toFloat() / editorImageView!!.height

            val x1 = points.getValue(0).x * xRatio
            val x2 = points.getValue(1).x * xRatio
            val x3 = points.getValue(2).x * xRatio
            val x4 = points.getValue(3).x * xRatio
            val y1 = points.getValue(0).y * yRatio
            val y2 = points.getValue(1).y * yRatio
            val y3 = points.getValue(2).y * yRatio
            val y4 = points.getValue(3).y * yRatio

            return openCV.getScannedBitmap(
                selectedImageBitmap,
                x1,
                y1,
                x2,
                y2,
                x3,
                y3,
                x4,
                y4
            )
        }

    private fun getScaledBitmap(bitmap: Bitmap?, width: Int, height: Int): Bitmap {
        Log.v("ImageEditorScreen", "scaledBitmap")
        Log.v("ImageEditorScreen", "$width $height")
        val m = Matrix()
        m.setRectToRect(
            RectF(0f, 0f, bitmap!!.width.toFloat(), bitmap.height.toFloat()),
            RectF(0f, 0f, width.toFloat(), height.toFloat()),
            Matrix.ScaleToFit.CENTER
        )
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, m, true)
    }

    private fun getEdgePoints(tempBitmap: Bitmap): Map<Int, PointF> {
        Log.v("ImageEditorScreen", "getEdgePoints")
        val pointFs = getContourEdgePoints(tempBitmap)
        return orderedValidEdgePoints(tempBitmap, pointFs)
    }

    private fun getContourEdgePoints(tempBitmap: Bitmap): List<PointF> {
        Log.v("ImageEditorScreen", "getContourEdgePoints")
        val point2f: MatOfPoint2f? = openCV?.getPoint(tempBitmap)
        val points = if (point2f != null) listOf(*point2f.toArray()) else mutableListOf()
        val result: MutableList<PointF> = ArrayList()
        for (i in points.indices) {
            result.add(PointF(points[i].x.toFloat(), points[i].y.toFloat()))
        }
        return result
    }

    private fun getOutlinePoints(tempBitmap: Bitmap): Map<Int, PointF> {
        Log.v("ImageEditorScreen", "getOutlinePoints")
        val outlinePoints: MutableMap<Int, PointF> =
            HashMap()
        outlinePoints[0] = PointF(0f, 0f)
        outlinePoints[1] = PointF(tempBitmap.width.toFloat(), 0f)
        outlinePoints[2] = PointF(0f, tempBitmap.height.toFloat())
        outlinePoints[3] = PointF(
            tempBitmap.width.toFloat(), tempBitmap.height.toFloat()
        )
        return outlinePoints
    }

    private fun orderedValidEdgePoints(
        tempBitmap: Bitmap,
        pointFs: List<PointF>
    ): Map<Int, PointF> {
        Log.v("ImageEditorScreen", "orderedValidEdgePoints")
        var orderedPoints: Map<Int, PointF> =
            croppingPolygon?.getOrderedPoints(pointFs)!!
        if (croppingPolygon?.isValidShape(orderedPoints)!!) {
            orderedPoints = getOutlinePoints(tempBitmap)
        }
        return orderedPoints
    }
}