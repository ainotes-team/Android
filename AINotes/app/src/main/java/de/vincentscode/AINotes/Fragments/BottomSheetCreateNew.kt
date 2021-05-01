package de.vincentscode.AINotes.Fragments

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import de.vincentscode.AINotes.Controls.DirectoryCreationDialog
import de.vincentscode.AINotes.Controls.FileCreationDialog
import de.vincentscode.AINotes.R
import kotlinx.android.synthetic.main.bottom_sheet_filemanager.*


class BottomSheetCreateNew : BottomSheetDialogFragment() {
    lateinit var content: View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        content = inflater.inflate(R.layout.bottom_sheet_filemanager, container, false)
        return content
    }

    override fun getTheme(): Int = R.style.BottomSheetDialogTheme

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        BottomSheetDialog(requireContext(), theme)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        text_view_add_file.setOnClickListener { createFile() }

        text_view_add_directory.setOnClickListener { createDirectory() }
    }

    private fun createFile() {
        close()

        val dialog = context?.let { FileCreationDialog(it) }
        dialog?.show()
    }

    private fun createDirectory() {
        close()

        val dialog = context?.let { DirectoryCreationDialog(it) }
        dialog?.show()
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