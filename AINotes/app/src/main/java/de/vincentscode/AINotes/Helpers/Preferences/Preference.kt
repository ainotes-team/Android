package de.vincentscode.AINotes.Helpers.Preferences

import android.view.View

interface Preference {
    fun getView() : View
    fun getValue() : Any?
}