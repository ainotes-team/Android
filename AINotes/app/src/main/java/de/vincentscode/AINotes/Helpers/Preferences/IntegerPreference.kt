package de.vincentscode.AINotes.Helpers.Preferences

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import com.google.android.material.textfield.TextInputEditText
import de.vincentscode.AINotes.App
import java.util.*

class IntegerPreference(displayName: String, val name: String, defaultValue: Int) : Preference {
    private val textField = TextInputEditText(App.mainActivity)

    init {
        val sharedPref = App.mainActivity.getPreferences(Context.MODE_PRIVATE)
        val text = sharedPref.getString(name.toLowerCase(Locale.ROOT), defaultValue.toString())

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
            MATCH_PARENT,
            WRAP_CONTENT
        )
        return textField
    }

    override fun getValue(): Int {
        return textField.text.toString().toInt()
    }
}