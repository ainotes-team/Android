package de.vincentscode.AINotes.Helpers.Preferences

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import com.google.android.material.textfield.TextInputEditText
import de.vincentscode.AINotes.App
import java.util.*

class StringPreference(displayName: String, val name: String, defaultValue: String) : Preference {

    private val textField = TextInputEditText(App.mainActivity)

    init {
        val sharedPref = App.mainActivity.getPreferences(Context.MODE_PRIVATE)
        val text = sharedPref.getString(name.toLowerCase(Locale.ROOT), defaultValue)

        textField.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) { }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                with (sharedPref.edit()) {
                    putString(name.toLowerCase(Locale.ROOT), p0.toString())
                    apply()
                }
            }
        })

        textField.setText(text)
        textField.hint = displayName
    }

    override fun getView(): View {
        textField.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        return textField
    }

    override fun getValue(): String {
        return textField.text.toString()
    }
}