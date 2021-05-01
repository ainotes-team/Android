package de.vincentscode.AINotes.Screens

import android.content.Context
import android.view.View
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import de.vincentscode.AINotes.App
import de.vincentscode.AINotes.Controls.PageContent
import de.vincentscode.AINotes.Helpers.Preferences.Preferences
import de.vincentscode.AINotes.R
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.back_arrow_toolbar.view.*
import kotlinx.android.synthetic.main.settings_screen.view.*
import kotlinx.android.synthetic.main.toolbar_editor_screen.view.*
import kotlinx.android.synthetic.main.toolbar_editor_screen.view.button_back

class SettingsScreen(context: Context) : PageContent(context) {
    private var toolbarContent: View = View.inflate(context, R.layout.back_arrow_toolbar, null)

    private var root: LinearLayout?

    init {
        View.inflate(context, R.layout.settings_screen, this)

        root = settings_root

        loadComponents()
    }

    private fun loadComponents() {
        Preferences.preferences.forEach { pair ->
            val innerList = LinearLayout(App.mainActivity)
            innerList.orientation = VERTICAL

            pair.value.forEach {
                innerList.addView(it.getView())
            }

            if (pair.value.isEmpty()) return@forEach
            root?.addView(TextView(App.mainActivity).apply {
                textSize = 24f
                text = pair.key
            })

            root?.addView(innerList)
        }
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
}