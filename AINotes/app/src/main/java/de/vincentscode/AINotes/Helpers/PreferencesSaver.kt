package de.vincentscode.AINotes.Helpers

import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import de.vincentscode.AINotes.App

class PreferencesSaver {
    companion object {
        public val sharedPreferencesSettings: SharedPreferences = App.mainActivity.getSharedPreferences("AINotes Settings", MODE_PRIVATE)
        public val settingsEditor: SharedPreferences.Editor = sharedPreferencesSettings.edit()

        fun save() {

        }
    }
}