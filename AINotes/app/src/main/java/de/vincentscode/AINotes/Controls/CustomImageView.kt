package de.vincentscode.AINotes.Controls

import android.content.Context
import android.content.DialogInterface
import android.view.Gravity
import android.view.View
import android.widget.PopupMenu
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.vincentscode.AINotes.App
import de.vincentscode.AINotes.R

class CustomImageView(context: Context) : androidx.appcompat.widget.AppCompatImageView(context) {
    private var itemClickListener: ((String) -> Unit)? = null

    init {
        this.setOnLongClickListener {
            showMenu(this)
            true
        }
    }

    private fun showMenu(anchor: View?): Boolean {
//        val popup = PopupMenu(App.mainActivity, anchor, Gravity.FILL_VERTICAL)
//        popup.menuInflater.inflate(R.menu.image_context_menu, popup.menu)
//
//        popup.show()
//
//        popup.setOnMenuItemClickListener {
//            itemClickListener?.invoke(it.title.toString())
//            true
//        }

        val items = arrayOf(
            resources.getString(R.string.remove),
            resources.getString(R.string.copy),
            resources.getString(R.string.cut),
            resources.getString(R.string.to_foreground),
            resources.getString(R.string.to_background),
            resources.getString(R.string.crop),
            resources.getString(R.string.rotate90left),
            resources.getString(R.string.rotate90right)
        )

        MaterialAlertDialogBuilder(context)
            .setTitle("Options")
            .setItems(items) { _, which ->
                itemClickListener?.invoke(items[which])
            }
            .show()

        return true
    }

    fun setOnItemClickedListener(listener: (String) -> Unit) {
        itemClickListener = listener
    }
}