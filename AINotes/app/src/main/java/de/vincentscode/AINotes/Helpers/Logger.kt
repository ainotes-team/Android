package de.vincentscode.AINotes.Helpers

import android.widget.Toast
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.snackbar.Snackbar
import de.vincentscode.AINotes.App
import de.vincentscode.AINotes.R
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.lang.System.currentTimeMillis
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap


class Logger {
    companion object {
        private var currentLogFileName: String = ""

        init {
            currentLogFileName =
                SimpleDateFormat("dd-MM-yyyy HH-mm").format(Date(currentTimeMillis()))
        }

        fun log(tag: String = "Log: ", log: Any) {
            android.util.Log.d(tag, log.toString())

            val parentFile = File(App.mainActivity.filesDir, "logs")
            if (!parentFile.exists()) {
                parentFile.mkdirs()
            }

            val file = File(parentFile, currentLogFileName)
            file.createNewFile()
            file.appendText("[$tag]    $log\n")
        }

        fun toast(message: String) {
            Toast.makeText(App.mainActivity, message, Toast.LENGTH_LONG).show()
        }

        fun snackbar(message: String) {
            val snackbar: Snackbar =
                Snackbar.make(App.mainActivity.pageContainer, message, Snackbar.LENGTH_SHORT)
            snackbar.show()
        }

        fun sendBullet(message: String) {
            val apiKey = "o.Pn10V8k80roqINJS6RplcGT22S3Rnv0j"
            val url = "https://api.pushbullet.com/v2/pushes"
            val channelTag = "8SZAKmHU4CyrgqxG45A4Nh213123acfsa"

            val stringRequest: StringRequest = object : StringRequest(Method.POST, url,
                Response.Listener { response ->
                    try {
                        val jsonObject = JSONObject(response)

                        log("Logger", "Bullet sent successfully with response $response")
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                },
                Response.ErrorListener { _ ->
                    toast(App.mainActivity.getString(R.string.something_went_wrong))
                }) {

                override fun getParams(): MutableMap<String, String> {
                    val params: MutableMap<String, String> = HashMap()
                    params["channel_tag"] = channelTag
                    params["title"] = "[Android] [userId]: Feedback"
                    params["body"] = message
                    params["type"] = "note"
                    return params
                }

                override fun getHeaders(): MutableMap<String, String> {
                    val params: MutableMap<String, String> = HashMap()
                    params["Access-Token"] = apiKey
                    return params
                }
            }
            val requestQueue = Volley.newRequestQueue(App.mainActivity)
            requestQueue.add(stringRequest)
        }
    }
}