package de.vincentscode.AINotes.Fragments

import android.app.Dialog
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import de.vincentscode.AINotes.Controls.DirectoryRenameDialog
import de.vincentscode.AINotes.Helpers.database.FileHelper
import de.vincentscode.AINotes.Models.DirectoryModel
import de.vincentscode.AINotes.R
import kotlinx.android.synthetic.main.bottom_sheet_directory_options.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch


class BottomSheetDirectoryOptions(private var directoryModel: DirectoryModel) :
    BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val displayMetrics = DisplayMetrics()
        activity!!.windowManager.defaultDisplay.getMetrics(displayMetrics)



        return inflater.inflate(R.layout.bottom_sheet_directory_options, container, false)
    }

    override fun getTheme(): Int = R.style.BottomSheetDialogTheme

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        BottomSheetDialog(requireContext(), theme)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        text_view_directory_title.text = directoryModel.name

        text_view_remove_directory.setOnClickListener { delete() }
        text_view_share_directory.setOnClickListener { share() }

        text_view_move_directory.setOnClickListener { move() }
        text_view_copy_directory.setOnClickListener { copy() }

        text_view_rename_directory.setOnClickListener { rename() }
    }

    private fun rename() {
        dismiss()

        val dialog = context?.let { DirectoryRenameDialog(it, directoryModel) }
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
            FileHelper.deleteDirectory(directoryModel)
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