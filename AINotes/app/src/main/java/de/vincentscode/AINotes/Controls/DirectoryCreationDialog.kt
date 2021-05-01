package de.vincentscode.AINotes.Controls

import android.content.Context
import android.os.Bundle
import de.vincentscode.AINotes.App
import de.vincentscode.AINotes.Helpers.database.FileHelper
import de.vincentscode.AINotes.Models.DirectoryModel
import de.vincentscode.AINotes.R
import kotlinx.android.synthetic.main.directory_creation_dialog.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import java.lang.System.currentTimeMillis

class DirectoryCreationDialog(context: Context) : CustomDialog(context) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.directory_creation_dialog)

        directory_creation_cancel_button.setOnClickListener {
            dismiss()
        }

        directory_creation_create_button.setOnClickListener {
            dismiss()

            CoroutineScope(IO).launch {
                FileHelper.createDirectory(DirectoryModel().also { directoryModel ->
                    directoryModel.name = directory_name_text_field.text.toString()
                    directoryModel.parentDirectoryId =
                        App.mainActivity.fileManagerScreen.currentDirectoryId
                    directoryModel.creationDate = currentTimeMillis()
                    directoryModel.lastChangedDate = currentTimeMillis()
                })
            }
        }
    }
}