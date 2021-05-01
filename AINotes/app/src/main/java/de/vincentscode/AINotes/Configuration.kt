package de.vincentscode.AINotes

import java.util.*

class Configuration {
    companion object {
        public var languages = hashMapOf<String, Locale>(
            "English" to Locale.ENGLISH,
            "German" to Locale.GERMAN
        )
    }
}