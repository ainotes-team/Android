package de.vincentscode.AINotes.Screens

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.LinearLayout
import de.vincentscode.AINotes.App
import de.vincentscode.AINotes.Controls.PageContent
import de.vincentscode.AINotes.Helpers.Logger
import de.vincentscode.AINotes.R
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.back_arrow_toolbar.view.*
import kotlinx.android.synthetic.main.feedback_screen.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream


class FeedbackScreen(context: Context) : PageContent(context) {
    private var toolbarContent: View = View.inflate(context, R.layout.back_arrow_toolbar, null)

    private var root: LinearLayout?

    init {
        View.inflate(context, R.layout.feedback_screen, this)

        root = feedback_root

        loadComponents()
    }

    private fun loadComponents() {
        feedback_button.setOnClickListener {
            if (!feedback_text_field.text.isNullOrBlank()) {
                sendFeedback()
            }
        }

        play_store_feedback.setOnClickListener {
            openAppRating()
        }
    }

    private fun openAppRating() {
        // you can also use BuildConfig.APPLICATION_ID
        val appId = context.packageName
        val rateIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("market://details?id=$appId")
        )
        var marketFound = false

        // find all applications able to handle our rateIntent
        val otherApps = context.packageManager
            .queryIntentActivities(rateIntent, 0)
        for (otherApp in otherApps) {
            // look for Google Play application
            if (otherApp.activityInfo.applicationInfo.packageName
                == "com.android.vending"
            ) {
                val otherAppActivity = otherApp.activityInfo
                val componentName = ComponentName(
                    otherAppActivity.applicationInfo.packageName,
                    otherAppActivity.name
                )
                // make sure it does NOT open in the stack of your activity
                rateIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                // task reparenting if needed
                rateIntent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
                // if the Google Play was already open in a search result
                //  this make sure it still go to the app page you requested
                rateIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                // this make sure only the Google Play app is allowed to
                // intercept the intent
                rateIntent.component = componentName
                context.startActivity(rateIntent)
                marketFound = true
                break
            }
        }

        // if GP not present on device, open web browser
        if (!marketFound) {
            val webIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=$appId")
            )
            context.startActivity(webIntent)
        }
    }

    private fun sendFeedback() {
        if (include_logs_checkbox.isChecked) {
            val parentFile = File(App.mainActivity.filesDir, "logs")
            val saveFile = File(App.mainActivity.filesDir, "logs.zip")
            val files = parentFile.listFiles() ?: return
            if (files.isEmpty()) return

            // create zip
            Logger.log("FeedbackScreen", "Creating .zip")

            ZipOutputStream(BufferedOutputStream(FileOutputStream(saveFile))).use { out ->
                for (file in files) {
                    FileInputStream(file).use { fi ->
                        BufferedInputStream(fi).use { origin ->
                            val entry = ZipEntry(file.path.substring(file.path.lastIndexOf("/")))
                            out.putNextEntry(entry)
                            origin.copyTo(out, 1024)
                        }
                    }
                }
            }

            uploadLogs(saveFile, feedback_text_field.text.toString())
            Logger.log("FeedbackScreen", "Logs uploaded")

            parentFile.listFiles()?.forEach {
                it.delete()
            }
            Logger.log("FeedbackScreen", "Sent logs were deleted")
        } else {
            Logger.sendBullet(feedback_text_field.text.toString())
        }

        feedback_text_field.setText("")
        feedback_text_field.hint = "Thanks for your Feedback"
        include_logs_checkbox.isChecked = false
    }

    private fun setToolbar() {
        App.mainActivity.toolbar.removeAllViews()
        App.mainActivity.toolbar.addView(toolbarContent)

        toolbarContent.button_back.setOnClickListener {
            App.mainActivity.onBackPressed()
        }
    }

    override fun onLoad() {
        App.mainActivity.isToolbarDividerVisible(true)
        App.mainActivity.setFabVisibility(false)

        setToolbar()
    }

    override fun onUnload() {

    }

    private fun uploadLogs(file: File, message: String) {
        CoroutineScope(IO).launch {
            val client = OkHttpClient()
            val url = "https://file.io/?expires=3m"

            val requestBody = MultipartBody.Builder().setType(MultipartBody.FORM).addFormDataPart(
                "file",
                file.name,
                file.asRequestBody(getMimeType(file.absolutePath).toMediaTypeOrNull())
            ).build()

            val request: Request = Request.Builder().url(url).post(requestBody).build()

            try {
                val response = client.newCall(request)
                response.enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        e.printStackTrace()
                    }

                    override fun onResponse(call: Call, response: Response) {
                        response.use {
                            val link = JSONObject(it.body!!.string()).getString("link")
                            Logger.sendBullet("$message\n$link")
                        }
                    }
                })
            } catch (ex: IOException) {
                // ignore
            }
        }
    }

    private fun getMimeType(path: String?): String {
        var type: String? = null
        val extension = MimeTypeMap.getFileExtensionFromUrl(path)
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        }
        return type.toString()
    }
}