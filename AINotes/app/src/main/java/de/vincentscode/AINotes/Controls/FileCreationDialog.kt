package de.vincentscode.AINotes.Controls

import android.content.Context
import android.os.Bundle
import de.vincentscode.AINotes.App
import de.vincentscode.AINotes.Helpers.database.FileHelper
import de.vincentscode.AINotes.Models.FileModel
import de.vincentscode.AINotes.R
import kotlinx.android.synthetic.main.file_creation_dialog.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import java.lang.System.currentTimeMillis

class FileCreationDialog(context: Context) : CustomDialog(context) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.file_creation_dialog)

        directory_rename_cancel_button.setOnClickListener {
            dismiss()
        }

        directory_rename_rename_button.setOnClickListener {
            dismiss()

            CoroutineScope(IO).launch {
                FileHelper.createFile(FileModel().also {
                        fileModel ->
                    fileModel.name = directory_name_text_field.text.toString()
                    fileModel.parentDirectoryId = App.mainActivity.fileManagerScreen.currentDirectoryId
                    fileModel.creationDate = currentTimeMillis()
                    fileModel.lastChangedDate = currentTimeMillis()
                })
            }
        }
    }
}