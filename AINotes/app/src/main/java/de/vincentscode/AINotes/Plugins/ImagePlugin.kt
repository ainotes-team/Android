package de.vincentscode.AINotes.Plugins

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Environment
import android.util.Log
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.annotation.NonNull
import de.vincentscode.AINotes.App
import de.vincentscode.AINotes.Controls.CustomImageView
import de.vincentscode.AINotes.Fragments.BottomSheetImageSearchOptions
import de.vincentscode.AINotes.Helpers.ImageEditing.ImageConstants
import de.vincentscode.AINotes.Helpers.database.FileHelper
import de.vincentscode.AINotes.Helpers.Logger
import de.vincentscode.AINotes.Models.PluginModel
import de.vincentscode.AINotes.Plugins.base.Plugin
import de.vincentscode.AINotes.R
import de.vincentscode.AINotes.Screens.ImageEditorScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import java.io.*


@SuppressLint("ViewConstructor")
class ImagePlugin(pluginId: Int = -1, context: Context = App.mainActivity) :
    Plugin(context, pluginId) {

    var contentImage = CustomImageView(context)
    private var savingPath: String? = null

    private val savingDir = "image_plugin_content"

    init {
        movable = true
        resizable = true
        resizableToRight = false

        contentImage.scaleType = ImageView.ScaleType.FIT_XY

        content.addView(contentImage)

        contentImage.setOnItemClickedListener { selectedItem ->
            when (selectedItem) {
                App.mainActivity.getString(R.string.remove) -> {
                    delete()
                }
                App.mainActivity.getString(R.string.copy) -> {
                    // TODO
                }
                App.mainActivity.getString(R.string.cut) -> {
                    // TODO
                }
                App.mainActivity.getString(R.string.to_foreground) -> {
                    // TODO
                }
                App.mainActivity.getString(R.string.to_background) -> {
                    // TODO
                }
                App.mainActivity.getString(R.string.crop) -> {
                    crop()
                }
                App.mainActivity.getString(R.string.rotate90left) -> {
                    // TODO
                }
                App.mainActivity.getString(R.string.rotate90right) -> {
                    // TODO
                }
                else -> {
                    Logger.log("CustomImageView", "Context menu String resource not found")
                }
            }
        }

        initialize()
    }

    private fun initialize() {

    }

    private fun crop() {
        ImageConstants.selectedImageBitmap = getBitmapFromImage()

        val editor = ImageEditorScreen(App.mainActivity)
        App.mainActivity.load(editor)
    }

    fun onSourceChanged() {
        val bitmap = (contentImage.drawable as BitmapDrawable).bitmap

        layoutParams.width = bitmap.width
        layoutParams.height = bitmap.height

        requestLayout()
    }

    override fun onPlace(x: Int, y: Int, layout: FrameLayout) {
        layoutParams =
            LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        layoutParams.leftMargin = x
        layoutParams.topMargin = y

        minWidth = 200f
        minHeight = 200f

        openImageSearchOptions()

        super.onPlace(x, y, layout)
    }

    private fun openImageSearchOptions() {
        val fragment = BottomSheetImageSearchOptions(this)
        fragment.show(App.mainActivity.supportFragmentManager, "bottom_sheet")
    }

    override fun loadPluginModel(pluginModel: PluginModel): Boolean {
        Logger.log("ImagePlugin", "loadPluginModel")

        CoroutineScope(Main).launch {
            pluginId = pluginModel.pluginId!!
            val content = pluginModel.content

            val bitmap = ImageSaver(context).setFileName(content).setDirectoryName(savingDir).load()

            contentImage.setImageBitmap(bitmap)
        }

        showNobs()

        return true
    }

    override fun savePluginModel(fileId: Int): Boolean {
        Logger.log("ImagePlugin", "Saving ImagePlugin content with pluginId $pluginId")

        ImageSaver(context).setFileName(getSavingName()).setDirectoryName(savingDir)
            .save(getBitmapFromImage())

        FileHelper.insertOrReplacePlugin(PluginModel().apply {
            pluginId = if (this@ImagePlugin.pluginId != -1) this@ImagePlugin.pluginId else null
            type =
                PluginModel.pluginTypes.keys.toTypedArray()[PluginModel.pluginTypes.values.toList()
                    .indexOfFirst {
                        it == this@ImagePlugin::class.java
                    }]
            posX = x.toDouble()
            posY = y.toDouble()
            sizeX = width.toDouble()
            sizeY = height.toDouble()
            content = savingPath!!
            this.fileId = fileId
        })

        return true
    }

    private fun getBitmapFromImage(): Bitmap {
        return (contentImage.drawable as BitmapDrawable).bitmap
    }

    private fun setPluginId() {
        // TODO: fix
        if (pluginId == -1) {
            pluginId = FileHelper.insertOrReplacePlugin(PluginModel().apply {
                pluginId = null
                type =
                    PluginModel.pluginTypes.keys.toTypedArray()[PluginModel.pluginTypes.values.toList()
                        .indexOfFirst {
                            it == this@ImagePlugin::class.java
                        }]
            })

            Logger.log("ImagePlugin", "Set Plugin Id to $pluginId")
        }
    }

    private fun getSavingName(): String {
        setPluginId()
        if (savingPath == null) savingPath = "$pluginId.png"
        return savingPath!!
    }
}

// TODO: replace with non deprecated methods
@Suppress("DEPRECATION")
class ImageSaver(private val context: Context) {
    private var directoryName = "images"
    private var fileName = "image.png"
    private var external = false

    fun setFileName(fileName: String): ImageSaver {
        this.fileName = fileName
        return this
    }

    fun setExternal(external: Boolean): ImageSaver {
        this.external = external
        return this
    }

    fun setDirectoryName(directoryName: String): ImageSaver {
        this.directoryName = directoryName
        return this
    }

    fun save(bitmapImage: Bitmap) {
        var fileOutputStream: FileOutputStream? = null
        try {
            fileOutputStream = FileOutputStream(createFile())
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                fileOutputStream?.close()
                Logger.log("ImagePlugin", "Saving succeeded")
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    @NonNull
    private fun createFile(): File {
        val directory: File = if (external) {
            getAlbumStorageDir(directoryName)
        } else {
            context.getDir(directoryName, Context.MODE_PRIVATE)
        }
        if (!directory.exists() && !directory.mkdirs()) {
            Log.e("ImageSaver", "Error creating directory $directory")
        }
        return File(directory, fileName)
    }

    private fun getAlbumStorageDir(albumName: String): File {
        return File(
            Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES
            ), albumName
        )
    }

    fun load(): Bitmap? {
        var inputStream: FileInputStream? = null
        try {
            inputStream = FileInputStream(createFile())
            return BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                inputStream?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return null
    }

    companion object {
        val isExternalStorageWritable: Boolean
            get() {
                val state: String = Environment.getExternalStorageState()
                return Environment.MEDIA_MOUNTED == state
            }

        val isExternalStorageReadable: Boolean
            get() {
                val state: String = Environment.getExternalStorageState()
                return Environment.MEDIA_MOUNTED == state ||
                        Environment.MEDIA_MOUNTED_READ_ONLY == state
            }
    }

}