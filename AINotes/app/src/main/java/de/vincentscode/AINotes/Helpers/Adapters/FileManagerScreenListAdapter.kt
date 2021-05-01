package de.vincentscode.AINotes.Helpers.Adapters

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.style.BackgroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import de.vincentscode.AINotes.App
import de.vincentscode.AINotes.Fragments.BottomSheetDirectoryOptions
import de.vincentscode.AINotes.Fragments.BottomSheetFileOptions
import de.vincentscode.AINotes.Helpers.database.FileHelper
import de.vincentscode.AINotes.Helpers.FileManagerScreenListItem
import de.vincentscode.AINotes.Helpers.ListItemMode
import de.vincentscode.AINotes.R
import java.text.SimpleDateFormat
import java.util.*


class FileManagerScreenListAdapter(private val context: Activity, private val listItems: ArrayList<FileManagerScreenListItem>)
    : RecyclerView.Adapter<FileManagerItem>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileManagerItem {
        val item = LayoutInflater.from(context).inflate(R.layout.file_manager_screen_list_item, parent, false)

        return FileManagerItem(
            item
        )
    }

    override fun getItemCount(): Int {
        return listItems.size
    }

    override fun onBindViewHolder(holder: FileManagerItem, position: Int) {
        if (position > listItems.size) return
        holder.bindData(listItems[position])
        // static animations here
    }
}

public class FileManagerItem(private val item: View) : RecyclerView.ViewHolder(item) {
    private var fileManagerScreenListItem: FileManagerScreenListItem? = null
    public var selected: Boolean = false

    public fun delete() {
        if (fileManagerScreenListItem?.listItemMode == ListItemMode.Directory) {
            FileHelper.deleteDirectory(fileManagerScreenListItem?.directoryModel!!)
        } else {
            FileHelper.deleteFile(fileManagerScreenListItem?.fileModel!!)
        }
    }

    private fun setHighLightedText(textView: TextView, textToHighlight: String) {
        if (textToHighlight == "") {
            return
        }

        val searchableText = textView.text.toString().toLowerCase(Locale.ROOT)

        var ofe = searchableText.indexOf(textToHighlight, 0)
        val wordToSpan: Spannable = SpannableString(textView.text)
        var ofs = 0
        while (ofs < searchableText.length && ofe != -1) {
            ofe = searchableText.indexOf(textToHighlight, ofs)
            if (ofe == -1) break else {
                wordToSpan.setSpan(
                    BackgroundColorSpan(Color.parseColor("#97B7CF")),
                    ofe,
                    ofe + textToHighlight.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                textView.setText(wordToSpan, TextView.BufferType.SPANNABLE)
            }
            ofs = ofe + 1
        }
    }

    public fun setOptionsButtonVisibility(isVisible: Boolean) {
        val menuItem: ImageButton = item.findViewById(R.id.file_manager_screen_list_item_menu)
        if (isVisible) {
            menuItem.visibility = View.VISIBLE
        } else {
            menuItem.visibility = View.INVISIBLE
        }
    }

    public fun select() {
        selected = true
        item.setBackgroundResource(R.drawable.file_manager_item_background)
        if (fileManagerScreenListItem?.listItemMode == ListItemMode.Directory) {
            App.mainActivity.fileManagerScreen.selectedDirectoryItems.add(fileManagerScreenListItem?.directoryModel!!)
        } else {
            App.mainActivity.fileManagerScreen.selectedFileItems.add(fileManagerScreenListItem?.fileModel!!)
        }
        val icon: ImageView = item.findViewById(R.id.file_manager_screen_list_icon)
        icon.setImageResource(R.drawable.ic_check_colored)

        App.mainActivity.fileManagerScreen.onSelectionChanged()
    }

    public fun deselect() {
        selected = false
        item.background = null
        val icon: ImageView = item.findViewById(R.id.file_manager_screen_list_icon)
        if (fileManagerScreenListItem?.listItemMode == ListItemMode.Directory) {
            icon.setImageResource(R.drawable.ic_folder)
            App.mainActivity.fileManagerScreen.selectedDirectoryItems.remove(fileManagerScreenListItem?.directoryModel)
        } else {
            icon.setImageResource(R.drawable.ic_file)
            App.mainActivity.fileManagerScreen.selectedFileItems.remove(fileManagerScreenListItem?.fileModel)
        }

        App.mainActivity.fileManagerScreen.onSelectionChanged()
    }

    @SuppressLint("SimpleDateFormat", "SetTextI18n")
    fun bindData(model: FileManagerScreenListItem) {
        fileManagerScreenListItem = model
        App.mainActivity.fileManagerScreen.currentListViewHolders.add(this)

        val itemTitle: TextView = item.findViewById(R.id.file_manager_screen_list_item_title)
        val icon: ImageView = item.findViewById(R.id.file_manager_screen_list_icon)

        val lastModified: TextView = item.findViewById(R.id.file_manager_screen_list_item_last_modified)

        val background: LinearLayout = item.findViewById(R.id.file_manager_screen_item_background)

        val menuItem: ImageButton = item.findViewById(R.id.file_manager_screen_list_item_menu)
        menuItem.background = null

        if (model.listItemMode == ListItemMode.Directory) {
            background.setOnClickListener {
                if (App.mainActivity.fileManagerScreen.selectedFileItems.size > 0 || App.mainActivity.fileManagerScreen.selectedDirectoryItems.size > 0) {
                    if (App.mainActivity.fileManagerScreen.selectedDirectoryItems.contains(model.directoryModel)) {
                        deselect()
                    } else {
                        select()
                    }
                } else {
                    App.mainActivity.fileManagerScreen.currentDirectoryId = model.directoryModel.directoryId!!
                    App.mainActivity.fileManagerScreen.reload()
                    App.mainActivity.fileManagerScreen.stopSearching()
                }
            }

            background.setOnLongClickListener {
                if (App.mainActivity.fileManagerScreen.selectedDirectoryItems.contains(model.directoryModel)) {
                    deselect()
                } else {
                    select()
                }
                true
            }

            menuItem.setOnClickListener {
                val fragment = BottomSheetDirectoryOptions(model.directoryModel)
                fragment.show(App.mainActivity.supportFragmentManager, "bottom_sheet")
            }

            itemTitle.text = model.directoryModel.name
            icon.setImageResource(R.drawable.ic_folder)

            val formatter = SimpleDateFormat("dd-MM-yyyy");
            val dateString = formatter.format(Date(model.directoryModel.lastChangedDate))

            lastModified.text = App.mainActivity.getString(R.string.modified) + " $dateString"
        } else {
            background.setOnClickListener {
                if (App.mainActivity.fileManagerScreen.selectedFileItems.size > 0 || App.mainActivity.fileManagerScreen.selectedDirectoryItems.size > 0) {
                    if (App.mainActivity.fileManagerScreen.selectedFileItems.contains(model.fileModel)) {
                        deselect()
                    } else {
                        select()
                    }
                } else {
                    App.mainActivity.editorScreen.loadFile(model.fileModel)
                    App.mainActivity.load(App.mainActivity.editorScreen)
                    App.mainActivity.fileManagerScreen.stopSearching()
                }
            }

            background.setOnLongClickListener {
                if (App.mainActivity.fileManagerScreen.selectedFileItems.contains(model.fileModel)) {
                    deselect()
                } else {
                    select()
                }
                true
            }

            menuItem.setOnClickListener {
                val fragment = BottomSheetFileOptions(model.fileModel)
                fragment.show(App.mainActivity.supportFragmentManager, "bottom_sheet")
            }

            itemTitle.text = model.fileModel.name
            icon.setImageResource(R.drawable.ic_file)

            val formatter = SimpleDateFormat("dd-MM-yyyy");
            val dateString = formatter.format(Date(model.fileModel.lastChangedDate))

            lastModified.text = App.mainActivity.getString(R.string.modified) + " $dateString"
        }

        setHighLightedText(itemTitle, App.mainActivity.fileManagerScreen.currentSearchString)
    }
}