package de.vincentscode.AINotes.Fragments

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import de.vincentscode.AINotes.Controls.FileRenameDialog
import de.vincentscode.AINotes.Helpers.database.FileHelper
import de.vincentscode.AINotes.Models.FileModel
import de.vincentscode.AINotes.R
import kotlinx.android.synthetic.main.bottom_sheet_file_options.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class BottomSheetFileOptions(private var fileModel: FileModel) : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.bottom_sheet_file_options, container, false)
    }

    override fun getTheme(): Int = R.style.BottomSheetDialogTheme

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        BottomSheetDialog(requireContext(), theme)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        text_view_file_title.text = fileModel.name

        text_view_share_file.setOnClickListener { share() }
        text_view_remove_directory.setOnClickListener { delete() }

        text_view_move_file.setOnClickListener { move() }
        text_view_copy_file.setOnClickListener { copy() }

        text_view_rename_file.setOnClickListener { rename() }
    }

    private fun rename() {
        dismiss()

        val dialog = context?.let { FileRenameDialog(it, fileModel) }
        dialog?.show()
    }

    private fun move() {
        dismiss()

        //TODO
    }

    private fun copy() {
        dismiss()

        // TODO
    }

    private fun share() {
        dismiss()

        // TODO
    }

    private fun delete() {
        dismiss()

        CoroutineScope(IO).launch {
            FileHelper.deleteFile(fileModel)
        }
    }

    private fun close() {
        this.dismiss()
    }

    override fun onStart() {
        super.onStart()

        val bottomSheet: View? = dialog?.findViewById(R.id.design_bottom_sheet)
        BottomSheetBehavior.from(bottomSheet!!).state = BottomSheetBehavior.STATE_EXPANDED
    }
}