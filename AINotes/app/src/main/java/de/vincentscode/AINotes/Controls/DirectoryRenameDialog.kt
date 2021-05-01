package de.vincentscode.AINotes.Controls

import android.content.Context
import android.os.Bundle
import de.vincentscode.AINotes.Helpers.database.FileHelper
import de.vincentscode.AINotes.Models.DirectoryModel
import de.vincentscode.AINotes.R
import kotlinx.android.synthetic.main.file_rename_dialog.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class DirectoryRenameDialog(context: Context, private val directoryModel: DirectoryModel) :
    CustomDialog(context) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.directory_rename_dialog)

        directory_name_text_field.setText(directoryModel.name)
        directory_name_text_field.setSelection(directoryModel.name.length)

        directory_rename_cancel_button.setOnClickListener {
            dismiss()
        }

        directory_rename_rename_button.setOnClickListener {
            dismiss()

            CoroutineScope(IO).launch {
                directoryModel.name = directory_name_text_field.text.toString()
                FileHelper.updateDirectory(directoryModel)
            }
        }
    }
}