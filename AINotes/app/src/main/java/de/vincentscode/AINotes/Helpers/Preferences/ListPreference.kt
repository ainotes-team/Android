package de.vincentscode.AINotes.Helpers.Preferences

import android.app.AlertDialog
import android.content.Context
import android.text.Layout
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.TextView
import androidx.core.view.marginStart
import androidx.core.view.setPadding
import de.vincentscode.AINotes.App
import de.vincentscode.AINotes.Configuration
import de.vincentscode.AINotes.R
import java.util.*

class ListPreference(private val displayName: String, val name: String, private val options: List<String>, defaultValue: Int) : Preference {

    private val textView = TextView(App.mainActivity)
    var selectedString: String = ""

    init {
        val sharedPref = App.mainActivity.getPreferences(Context.MODE_PRIVATE)

        textView.setOnClickListener {
            val selectedItem = sharedPref.getString(name.toLowerCase(Locale.ROOT), options[defaultValue])

            selectedString = selectedItem!!

            val builder = AlertDialog.Builder(App.mainActivity)
            builder.setTitle("Choose language")

            builder.setSingleChoiceItems(options.toTypedArray(), options.indexOf(selectedItem)) { dialog, which ->
                with (sharedPref.edit()) {
                    selectedString = options[which]
                    textView.text = selectedString
                    putString(name.toLowerCase(Locale.ROOT),
                        selectedString
                    )
                    apply()
                }

                dialog.dismiss()
            }

            builder.setNegativeButton(App.mainActivity.getString(R.string.cancel), null)

            val dialog = builder.create()
            dialog.show()
        }

        textView.text = sharedPref.getString(name.toLowerCase(Locale.ROOT), options[defaultValue])
        textView.textSize = 20f
        textView.isClickable = true
        textView.layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
        textView.setPadding(20)
    }

    override fun getView(): View {
        return textView
    }

    override fun getValue(): String {
        return selectedString
    }
}