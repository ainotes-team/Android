package de.vincentscode.AINotes.Helpers.Preferences

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.Switch
import de.vincentscode.AINotes.App
import java.util.*

class BooleanPreference(private val displayName: String, val name: String, defaultValue: Boolean) : Preference {

    private val switch = Switch(App.mainActivity)

    init {
        val sharedPref = App.mainActivity.getPreferences(Context.MODE_PRIVATE)
        val checked = sharedPref.getBoolean(name.toLowerCase(Locale.ROOT), defaultValue)

        switch.setOnCheckedChangeListener { _, isChecked ->
            with (sharedPref.edit()) {
                putBoolean(name.toLowerCase(Locale.ROOT), isChecked)
                apply()
            }
        }

        switch.isChecked = checked
        switch.text = displayName
    }

    override fun getView(): View {
        switch.layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
        return switch
    }

    override fun getValue(): Boolean {
        return switch.isChecked
    }
}