package de.vincentscode.AINotes

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.annotation.ColorRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get
import androidx.core.view.isNotEmpty
import androidx.room.Room
import de.vincentscode.AINotes.Controls.PageContent
import de.vincentscode.AINotes.Helpers.ImageEditing.ImageConstants
import de.vincentscode.AINotes.Helpers.Logger
import de.vincentscode.AINotes.Helpers.LoggingExceptionHandler
import de.vincentscode.AINotes.Helpers.ManagePermissions
import de.vincentscode.AINotes.Helpers.Preferences.Preferences
import de.vincentscode.AINotes.Helpers.database.FileDatabase
import de.vincentscode.AINotes.Helpers.database.FileHelper
import de.vincentscode.AINotes.Plugins.ImagePlugin
import de.vincentscode.AINotes.Screens.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import org.opencv.android.BaseLoaderCallback
import org.opencv.android.LoaderCallbackInterface
import org.opencv.android.OpenCVLoader
import java.io.InputStream


class MainActivity : AppCompatActivity() {

    lateinit var fileManagerScreen: FileManagerScreen
    lateinit var editorScreen: EditorScreen
    lateinit var settingsScreen: SettingsScreen
    lateinit var feedbackScreen: FeedbackScreen

    lateinit var fileDatabase: FileDatabase

    lateinit var pageContainer: LinearLayout
    lateinit var relativeLayout: RelativeLayout

    private val loaderCallback: BaseLoaderCallback = object : BaseLoaderCallback(this) {
        override fun onManagerConnected(status: Int) {
            when (status) {
                LoaderCallbackInterface.SUCCESS -> {
                    Logger.log("MainActivity", "Success loading OpenCV")
                }
                else -> {
                    Logger.log("MainActivity", "Failed loading OpenCV")
                }
            }
        }
    }

    init {
        App.mainActivity = this
    }

    fun getAppContext(): Context? {
        return this
    }

    private fun loadLanguage() {
        val sharedPref = App.mainActivity.getPreferences(Context.MODE_PRIVATE)
        val language = sharedPref.getString("language", Configuration.languages.keys.first())
        Preferences.setLanguage(language!!)
    }

    override fun onResume() {
        // opencv loader
        super.onResume()
        if (!OpenCVLoader.initDebug()) {
            Log.d(
                "OpenCV",
                "Internal OpenCV library not found. Using OpenCV Manager for initialization"
            )
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0, this, loaderCallback)
        } else {
            Log.d("OpenCV", "OpenCV library found inside package. Using it!")
            loaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Logger.log("MainActivity", "onCreate")

        loadLanguage()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window?.decorView?.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            window.statusBarColor = Color.WHITE
        }

        // for exception logging
        val globalExceptionHandler = LoggingExceptionHandler()

        setContentView(R.layout.activity_main)

        //window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)

        loadDatabase()

        fileManagerScreen = FileManagerScreen(this)
        editorScreen = EditorScreen(this)
        settingsScreen = SettingsScreen(this)
        feedbackScreen = FeedbackScreen(this)

        pageContainer = page_container

        relativeLayout = relative_layout
        Logger.log("MainActivity", "components loaded")

        // hide native toolbar
        supportActionBar?.hide()

        // start screen
        load(fileManagerScreen)

        floatingActionButton.setOnClickListener {
//            val intent = Intent(Intent.ACTION_PICK)
//            intent.type = "image/*"
//            startActivityForResult(intent, ImageConstants.GALLERY_IMAGE_LOADED)

            (page_container[0] as PageContent).onFabClicked()
        }

        navigation_left.setNavigationItemSelectedListener {
            // navigation drawer item click
            Logger.log("MainActivity", "left navigation drawer: navigate to ${it.title}")

            when (it.title) {
                getString(R.string.settings) -> {
                    load(settingsScreen)
                }
                getString(R.string.feedback) -> {
                    load(feedbackScreen)
                }
            }
            drawer_layout.closeDrawer(navigation_left)
            true
        }

        fileManagerScreen.reload()

        // permissions

        val list = listOf(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

        // Initialize a new instance of ManagePermissions class
        managePermissions = ManagePermissions(this, list, requestCode)
    }

    private fun loadDatabase() {
        fileDatabase =
            Room.databaseBuilder(applicationContext, FileDatabase::class.java, "database").build()
        Logger.log("MainActivity", "Database loaded")
    }

    // fab functions
    fun setFabIcon(resId: Int) {
        Logger.log("MainActivity", "Fab icon changed to $resId")
        floatingActionButton.setImageResource(resId)
    }

    fun setFabVisibility(isVisible: Boolean) {
        Logger.log("MainActivity", "Fab visibility changed to $isVisible")
        if (isVisible) {
            if (!floatingActionButton.isShown) floatingActionButton.show()
        } else {
            if (floatingActionButton.isShown) floatingActionButton.hide()
        }
    }

    fun load(pageContent: PageContent) {
        Logger.log("MainActivity", "loading $pageContent")
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )

        if (page_container.isNotEmpty()) {
            val oldContent = (page_container[0] as PageContent)
            oldContent.onUnload()
        }
        page_container.removeAllViews()
        page_container.addView(pageContent, params)

        pageContent.onLoad()

        hideSoftKeyboard()
    }

    private fun getPageContent(): PageContent {
        return page_container[0] as PageContent
    }

    override fun onBackPressed() {
        Logger.log("MainActivity", "onBackPressed")
        if (getPageContent() == fileManagerScreen) {
            if (fileManagerScreen.searching) {
                fileManagerScreen.stopSearching()
                return
            }

            if (fileManagerScreen.isSelectionMode) {
                fileManagerScreen.clearSelection()
                fileManagerScreen.onSelectionChanged()
                return
            }

            if (fileManagerScreen.currentDirectoryId != -1) {
                CoroutineScope(IO).launch {
                    val newParentDirectoryId: Int =
                        FileHelper.getDirectory(fileManagerScreen.currentDirectoryId)?.parentDirectoryId!!
                    fileManagerScreen.currentDirectoryId = newParentDirectoryId
                    fileManagerScreen.reload()
                }
            } else {
                super.onBackPressed()
            }
        } else {
            load(fileManagerScreen)
        }
    }

    fun isToolbarDividerVisible(isVisible: Boolean) {
        Logger.log("MainActivity", "toolbar visible: $isVisible")

        if (isVisible) {
            toolbar_divider.visibility = View.VISIBLE
        } else {
            toolbar_divider.visibility = View.INVISIBLE
        }
    }

    fun hideSoftKeyboard() {
        hideKeyboard()
        Logger.log("MainActivity", "hiding SoftKeyboard")
    }

    private fun Activity.hideKeyboard() {
        val currentFocus = this.currentFocus
        if (currentFocus != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus.windowToken, 0)
        }
        // else {
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        // }
    }

    fun deselectAllNavigationItems() {
        Logger.log("MainActivity", "deselecting all navigation drawer items")
        navigation_left.checkedItem?.isChecked = false
    }

    private var selectedImage: Uri? = null
    private var selectedBitmap: Bitmap? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == ImageConstants.IMAGE_SEARCH_GALLERY_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                currentImagePlugin?.contentImage?.setImageURI(data?.data)
                currentImagePlugin = null

                currentImagePlugin?.onSourceChanged()
            } else {
                editorScreen.removePlugin(currentImagePlugin)
            }
        }

        if (requestCode == ImageConstants.IMAGE_FROM_CAMERA_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                val imageBitmap = data?.extras?.get("data") as Bitmap
                currentImagePlugin?.contentImage?.setImageBitmap(imageBitmap)
                currentImagePlugin = null

                currentImagePlugin?.onSourceChanged()
            } else {
                editorScreen.removePlugin(currentImagePlugin)
            }
        }

        if (requestCode == ImageConstants.GALLERY_IMAGE_LOADED && resultCode == RESULT_OK && data != null) {
            selectedImage = data.data

            val inputStream: InputStream? = contentResolver.openInputStream(selectedImage!!)
            selectedBitmap = BitmapFactory.decodeStream(inputStream)

            ImageConstants.selectedImageBitmap = selectedBitmap

            val editor = ImageEditorScreen(this)
            load(editor)
        }
    }

    var currentImagePlugin: ImagePlugin? = null

    private val requestCode = 100
    private lateinit var managePermissions: ManagePermissions

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            this.requestCode -> {
                val isPermissionsGranted = managePermissions
                    .processPermissionsResult(requestCode, permissions, grantResults)

                if (isPermissionsGranted) {
                    Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
                        takePictureIntent.resolveActivity(App.mainActivity.packageManager)?.also {
                            App.mainActivity.startActivityForResult(
                                takePictureIntent,
                                ImageConstants.IMAGE_FROM_CAMERA_REQUEST
                            )
                        }
                    }
                } else {
                    editorScreen.removePlugin(currentImagePlugin)
                }

                return
            }
        }
    }
}
