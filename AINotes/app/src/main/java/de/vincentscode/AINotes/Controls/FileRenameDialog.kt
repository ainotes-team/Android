package de.vincentscode.AINotes.Controls

import android.content.Context
import android.os.Bundle
import de.vincentscode.AINotes.Helpers.database.FileHelper
import de.vincentscode.AINotes.Models.FileModel
import de.vincentscode.AINotes.R
import kotlinx.android.synthetic.main.file_rename_dialog.*

class FileRenameDialog(context: Context, private val fileModel: FileModel) : CustomDialog(context) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.file_rename_dialog)

        directory_name_text_field.setText(fileModel.name)
        directory_name_text_field.setSelection(fileModel.name.length)

        directory_rename_cancel_button.setOnClickListener {
            dismiss()
        }

        directory_rename_rename_button.setOnClickListener {
            dismiss()

            Thread {
                fileModel.name = directory_name_text_field.text.toString()
                FileHelper.updateFile(fileModel)
            }.start()
        }
    }
}