package de.vincentscode.AINotes.Fragments

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import de.vincentscode.AINotes.App
import de.vincentscode.AINotes.R
import de.vincentscode.AINotes.Screens.FileManagerScreen
import kotlinx.android.synthetic.main.bottom_sheet_sort_options.*

class BottomSheetSortOptions : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_sort_options, container, false)
    }

    override fun getTheme(): Int = R.style.BottomSheetDialogTheme

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        BottomSheetDialog(requireContext(), theme)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val lastModified: TextView = option_last_modified
        val lastCreated: TextView = option_last_created
        val alphabetical: TextView = option_alphabetical
        val label: TextView = option_label

        val title: TextView = sort_options_title

        if (App.mainActivity.fileManagerScreen.currentSortDirection == FileManagerScreen.SortDirection.Ascending) {
            title.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_sort,
                0,
                R.drawable.ic_arrow_upward,
                0
            )
        } else {
            title.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_sort,
                0,
                R.drawable.ic_arrow_downward,
                0
            )
        }

        when (App.mainActivity.fileManagerScreen.currentSortOption) {
            FileManagerScreen.SortOption.LastModified ->
                lastModified.setBackgroundResource(R.drawable.background_round_padded)
            FileManagerScreen.SortOption.LastCreated ->
                lastCreated.setBackgroundResource(R.drawable.background_round_padded)
            FileManagerScreen.SortOption.Alphabetical ->
                alphabetical.setBackgroundResource(R.drawable.background_round_padded)
            FileManagerScreen.SortOption.Label ->
                label.setBackgroundResource(R.drawable.background_round_padded)
        }

        title.setOnClickListener { v ->
            close()
            if (App.mainActivity.fileManagerScreen.currentSortDirection == FileManagerScreen.SortDirection.Ascending) {
                App.mainActivity.fileManagerScreen.sort(
                    App.mainActivity.fileManagerScreen.currentSortOption,
                    FileManagerScreen.SortDirection.Descending
                )
            } else {
                App.mainActivity.fileManagerScreen.sort(
                    App.mainActivity.fileManagerScreen.currentSortOption,
                    FileManagerScreen.SortDirection.Ascending
                )
            }
        }

        lastModified.setOnClickListener { v ->
            close()
            App.mainActivity.fileManagerScreen.sort(
                FileManagerScreen.SortOption.LastModified,
                App.mainActivity.fileManagerScreen.currentSortDirection
            )
        }

        lastCreated.setOnClickListener { v ->
            close()
            App.mainActivity.fileManagerScreen.sort(
                FileManagerScreen.SortOption.LastCreated,
                App.mainActivity.fileManagerScreen.currentSortDirection
            )
        }

        alphabetical.setOnClickListener { v ->
            close()
            App.mainActivity.fileManagerScreen.sort(
                FileManagerScreen.SortOption.Alphabetical,
                App.mainActivity.fileManagerScreen.currentSortDirection
            )
        }

        label.setOnClickListener { v ->
            close()
            App.mainActivity.fileManagerScreen.sort(
                FileManagerScreen.SortOption.Label,
                App.mainActivity.fileManagerScreen.currentSortDirection
            )
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