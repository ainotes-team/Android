package de.vincentscode.AINotes.Helpers.Preferences

import android.content.Context
import android.content.res.Configuration
import de.vincentscode.AINotes.App
import java.util.*
import kotlin.collections.HashMap


class Preferences {
    companion object {
        fun setLanguage(language: String) {
            val locale = de.vincentscode.AINotes.Configuration.languages[language]
            Locale.setDefault(locale!!)
            val config = Configuration()
            config.setLocale(locale)

            App.mainActivity.resources.updateConfiguration(config,
                App.mainActivity.baseContext.resources.displayMetrics)

            val sharedPref = App.mainActivity.getPreferences(Context.MODE_PRIVATE)
            with (sharedPref.edit()) {
                putString("language", language)
                apply()
            }
        }

        // general
        val languagePreference = ListPreference("Language", "language", de.vincentscode.AINotes.Configuration.languages.keys.toList(), 0)

        // sidebar
        var gradePreference = IntegerPreference("Grade", "grade", 0)
        var maxRecentFilesPreference = IntegerPreference("Max. Files Count in Recent Files View", "recent_files_count", 10)

        var preferences: HashMap<String, List<Preference>> = hashMapOf(
            "General" to listOf(
                languagePreference
            ),
            "Sidebar" to listOf(
                gradePreference,
                maxRecentFilesPreference
            ),
            "File Manager" to listOf(

            ),
            "Editor" to listOf(

            ),
            "Handwriting" to listOf(

            ),
            "Shortcuts" to listOf(

            ),
            "Labels" to listOf(

            ),
            "User Plugins" to listOf(

            ),
            "Advanced" to listOf(

            ),
            "Advanced Theming" to listOf(

            ),
            "Developer Options" to listOf(

            )
        )
    }
}