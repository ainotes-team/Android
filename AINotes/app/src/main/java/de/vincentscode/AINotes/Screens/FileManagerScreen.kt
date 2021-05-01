package de.vincentscode.AINotes.Screens

import android.annotation.SuppressLint
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import de.vincentscode.AINotes.App
import de.vincentscode.AINotes.Controls.PageContent
import de.vincentscode.AINotes.Fragments.BottomSheetCreateNew
import de.vincentscode.AINotes.Fragments.BottomSheetSortOptions
import de.vincentscode.AINotes.Helpers.Adapters.FileManagerItem
import de.vincentscode.AINotes.Helpers.Adapters.FileManagerScreenListAdapter
import de.vincentscode.AINotes.Helpers.Animations.RecyclerViewAnimator
import de.vincentscode.AINotes.Helpers.CustomLinearLayoutManager
import de.vincentscode.AINotes.Helpers.FileManagerScreenListItem
import de.vincentscode.AINotes.Helpers.ListItemMode
import de.vincentscode.AINotes.Helpers.Logger
import de.vincentscode.AINotes.Helpers.database.FileHelper
import de.vincentscode.AINotes.Icon
import de.vincentscode.AINotes.Models.DirectoryModel
import de.vincentscode.AINotes.Models.FileModel
import de.vincentscode.AINotes.R
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.file_manager_screen.view.*
import kotlinx.android.synthetic.main.search_toolbar_filemanager.view.*
import kotlinx.android.synthetic.main.selection_toolbar_filemanager.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlin.collections.ArrayList


class FileManagerScreen(context: Context) : PageContent(context) {

    enum class SortOption {
        LastCreated,
        LastModified,
        Alphabetical,
        Label
    }

    enum class SortDirection {
        Ascending,
        Descending
    }

    private lateinit var adapter: FileManagerScreenListAdapter

    var searching: Boolean = false

    private var toolbarContent: View =
        View.inflate(context, R.layout.search_toolbar_filemanager, null)

    private var directories: ArrayList<DirectoryModel> = arrayListOf()
    private var files: ArrayList<FileModel> = arrayListOf()

    private var allItemsList: ArrayList<FileManagerScreenListItem> = ArrayList()

    private var itemsList: RecyclerView

    val currentListViewHolders: ArrayList<FileManagerItem> = arrayListOf()

    var currentDirectoryId: Int = -1

    val selectedDirectoryItems: ArrayList<DirectoryModel> = arrayListOf()
    val selectedFileItems: ArrayList<FileModel> = arrayListOf()

    var currentSortOption: SortOption = SortOption.LastCreated
    var currentSortDirection: SortDirection = SortDirection.Ascending

    var currentSearchString: String = ""

    init {
        View.inflate(context, R.layout.file_manager_screen, this)

        itemsList = findViewById(R.id.file_manager_screen_list)
        itemsList.layoutManager = CustomLinearLayoutManager(context)

        // animations => adapter.notify...()
        itemsList.itemAnimator = RecyclerViewAnimator()

        // recyclerView spacing
        itemsList.addItemDecoration(
            DividerItemDecoration(
                context,
                DividerItemDecoration.VERTICAL
            ).also { dividerItemDecoration ->
                dividerItemDecoration.setDrawable(
                    ContextCompat.getDrawable(
                        getContext(),
                        R.drawable.recycler_view_divider
                    )!!
                )
            }
        )

        itemsList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                App.mainActivity.setFabVisibility(dy <= 0)
            }
        })

        // sorting
        sort_item.setOnClickListener {
            val fragment = BottomSheetSortOptions()
            fragment.show(App.mainActivity.supportFragmentManager, "bottom_sheet")
        }

        Logger.log("FileManagerScreen", "Initialization finished")
    }

    fun sort(sortOption: SortOption, sortDirection: SortDirection) {
        currentSortOption = sortOption
        currentSortDirection = sortDirection

        reload()
        Logger.log("FileManagerScreen", "Sorted by $sortOption and $sortDirection")
    }

    fun reload(updateAdapter: Boolean = true) {
        currentListViewHolders.clear()
        directoryChanged()
        CoroutineScope(IO).launch {
            clearLists()

            directories.addAll(FileHelper.listDirectories(currentDirectoryId))
            files.addAll(FileHelper.listFiles(currentDirectoryId))

            if (currentSortDirection == SortDirection.Ascending) {
                when (currentSortOption) {
                    SortOption.LastModified -> {
                        files.sortBy { fileModel ->
                            fileModel.lastChangedDate
                        }
                        directories.sortBy { directoryModel ->
                            directoryModel.lastChangedDate
                        }
                    }
                    SortOption.LastCreated -> {
                        files.sortBy { fileModel ->
                            fileModel.creationDate
                        }
                        directories.sortBy { directoryModel ->
                            directoryModel.creationDate
                        }
                    }
                    SortOption.Alphabetical -> {
                        files.sortBy { fileModel ->
                            fileModel.name
                        }
                        directories.sortBy { directoryModel ->
                            directoryModel.name
                        }
                    }
//                    SortOption.Label ->
//                        files.sortBy { fileModel ->
//
//                        }
                    else -> {
                    }
                }
            } else {
                when (currentSortOption) {
                    SortOption.LastModified -> {
                        files.sortByDescending { fileModel ->
                            fileModel.lastChangedDate
                        }
                        directories.sortByDescending { directoryModel ->
                            directoryModel.lastChangedDate
                        }
                    }
                    SortOption.LastCreated -> {
                        files.sortByDescending { fileModel ->
                            fileModel.creationDate
                        }
                        directories.sortByDescending { directoryModel ->
                            directoryModel.creationDate
                        }
                    }
                    SortOption.Alphabetical -> {
                        files.sortByDescending { fileModel ->
                            fileModel.name
                        }
                        directories.sortByDescending { directoryModel ->
                            directoryModel.name
                        }
                    }
//                    SortOption.Label ->
//                        files.sortByDescending { fileModel ->
//
//                        }
                    else -> {
                    }
                }
            }

            updateList(updateAdapter)
        }
        Logger.log("FileManagerScreen", "Reloaded")
    }

    private fun clearLists() {
        files.clear()
        directories.clear()
        Logger.log("FileManagerScreen", "Lists cleared")
    }

    private fun updateList(updateAdapter: Boolean = true) {
        allItemsList.clear()

        directories.forEach { directoryModel: DirectoryModel ->
            allItemsList.add(FileManagerScreenListItem().also { fileManagerScreenListItem ->
                fileManagerScreenListItem.directoryModel = directoryModel
                fileManagerScreenListItem.listItemMode = ListItemMode.Directory
            })
        }

        files.forEach { fileModel: FileModel ->
            allItemsList.add(FileManagerScreenListItem().also { fileManagerScreenListItem ->
                fileManagerScreenListItem.fileModel = fileModel
                fileManagerScreenListItem.listItemMode = ListItemMode.File
            })
        }

        if (updateAdapter) {
            updateRecyclerView()
        }

        Logger.log("FileManagerScreen", "Lists updated")
    }

    override fun onLoad() {
        Logger.log("FileManagerScreen", "onLoad")

        setSearchToolbar()
        App.mainActivity.setFabVisibility(true)
        App.mainActivity.isToolbarDividerVisible(false)
        App.mainActivity.setFabIcon(Icon.Add)

        App.mainActivity.deselectAllNavigationItems()
    }

    override fun onUnload() {
        Logger.log("FileManagerScreen", "onUnload")

        App.mainActivity.toolbar.removeAllViews()
    }

    private fun directoryChanged() {
        CoroutineScope(Main).launch {
            Logger.log("FileManagerScreen", "Directory changed to $currentDirectoryId")

            if (currentDirectoryId == -1) {
                if (!isSelectionMode) toolbarContent.navigation_icon.setImageResource(R.drawable.ic_hamburger)
            } else {
                if (!isSelectionMode) toolbarContent.navigation_icon.setImageResource(R.drawable.ic_arrow_back)
            }
        }
    }

    private fun startSearching() {
        Logger.log("FileManagerScreen", "Start searching")
        searching = true

        currentListViewHolders.clear()

        toolbarContent.search_text_edit.isCursorVisible = true
        toolbarContent.navigation_icon.setImageResource(R.drawable.ic_arrow_back)
        App.mainActivity.setFabVisibility(false)

        selectedDirectoryItems.clear()
        selectedFileItems.clear()

        sort_item.visibility = View.GONE

        // load all files and directories
        CoroutineScope(IO).launch {
            directories = FileHelper.listDirectories()
            files = FileHelper.listFiles()

            updateList()
        }

        Logger.log("FileManagerScreen", "Started searching")
    }

    fun stopSearching() {
        Logger.log("FileManagerScreen", "Stop searching")
        if (!searching) return
        searching = false

        toolbarContent.search_text_edit.setText("")
        toolbarContent.search_text_edit.isCursorVisible = false

        if (currentDirectoryId == -1) toolbarContent.navigation_icon.setImageResource(R.drawable.ic_hamburger)

        App.mainActivity.hideSoftKeyboard()
        App.mainActivity.setFabVisibility(true)

        sort_item.visibility = View.VISIBLE

        // load files and directories from current dir
        itemsList.adapter = null
        reload()

        Logger.log("FileManagerScreen", "Stopped searching")
    }

    private fun openNavigationDrawer() {
        App.mainActivity.drawer_layout.openDrawer(Gravity.LEFT, true)
        Logger.log("FileManagerScreen", "Left navigation drawer updated")
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setSearchToolbar() {
        toolbarContent = View.inflate(context, R.layout.search_toolbar_filemanager, null)

        App.mainActivity.toolbar.removeAllViews()
        toolbarContent.navigation_icon.setOnClickListener {
            if (searching) {
                stopSearching()
            } else {
                if (currentDirectoryId != -1) {
                    CoroutineScope(IO).launch {
                        val newParentDirectoryId: Int =
                            FileHelper.getDirectory(currentDirectoryId)?.parentDirectoryId!!
                        currentDirectoryId = newParentDirectoryId
                        reload()
                    }
                } else {
                    openNavigationDrawer()
                }
            }
        }

        toolbarContent.search_text_edit.background = null

        toolbarContent.search_text_edit.setOnTouchListener { v, event ->
            startSearching()
            false
        }

        toolbarContent.search_text_edit.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                // ignore
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                // ignore
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                filter(p0.toString())
            }
        })

        App.mainActivity.toolbar.addView(toolbarContent)

        loadProfilePicture()

        Logger.log("FileManagerScreen", "Search bar loaded and set")
    }

    var isSelectionMode: Boolean = false

    @SuppressLint("SetTextI18n")
    fun onSelectionChanged() {
        Logger.log(
            "FileManagerScreen",
            "Selection changed - Dirs $selectedDirectoryItems - Files $selectedFileItems"
        )

        if (selectedDirectoryItems.size == 0 && selectedFileItems.size == 0) {
            if (!isSelectionMode) return
            isSelectionMode = false
            setSearchToolbar()

            sort_item.visibility = View.VISIBLE

            currentListViewHolders.forEach {
                it.setOptionsButtonVisibility(true)
            }

            App.mainActivity.setFabVisibility(true)
        } else {
            if (!isSelectionMode) {
                isSelectionMode = true
                setSelectionToolbar()

                currentListViewHolders.forEach {
                    it.setOptionsButtonVisibility(false)
                }
            }

            sort_item.visibility = View.INVISIBLE

            val selectedItemsCount = selectedDirectoryItems.size + selectedFileItems.size
            toolbarContent.items_count.text = "$selectedItemsCount    " + context.getString(
                R.string.selected
            )

            App.mainActivity.setFabVisibility(false)
        }
    }

    private fun setSelectionToolbar() {
        val searchToolbarContent = toolbarContent
        toolbarContent = View.inflate(context, R.layout.selection_toolbar_filemanager, null)

        App.mainActivity.toolbar.removeAllViews()
        App.mainActivity.toolbar.addView(toolbarContent)

        toolbarContent.button_deselect_selected.setOnClickListener {
            clearSelection()
            App.mainActivity.hideSoftKeyboard()
            toolbarContent = searchToolbarContent
            App.mainActivity.toolbar.removeAllViews()
            App.mainActivity.toolbar.addView(toolbarContent)
        }

        toolbarContent.button_delete_selected.setOnClickListener {
            deleteSelection()
            App.mainActivity.hideSoftKeyboard()
        }

        toolbarContent.button_select_all.setOnClickListener {
            App.mainActivity.hideSoftKeyboard()
            currentListViewHolders.forEach {
                if (!it.selected) {
                    it.select()
                }
            }
        }

        Logger.log("FileManagerScreen", "Selection toolbar set")
    }

    private fun deleteSelection() {
        Logger.log("FileManagerScreen", "deleting selection")
        currentListViewHolders.toList().forEach {
            if (it.selected) {
                it.deselect()
                CoroutineScope(IO).launch {
                    it.delete()
                }
            }
        }

        setSearchToolbar()
        Logger.log("FileManagerScreen", "Selection deleted")
    }

    fun clearSelection() {
        Logger.log("FileManagerScreen", "clearing selection")
        currentListViewHolders.forEach {
            it.deselect()
        }
        Logger.log("FileManagerScreen", "Selection cleared")
    }

    private fun filter(searchString: String) {
        Logger.log("FileManagerScreen", "Filtering by $searchString")

        currentSearchString = searchString
        clearLists()

        CoroutineScope(IO).launch {
            directories.addAll(FileHelper.listDirectories(currentSearchString))
            files.addAll(FileHelper.listFiles(currentSearchString))

            updateList()
        }
    }

    private fun updateRecyclerView() {
        CoroutineScope(Main).launch {
            if (itemsList.adapter == null) {
                adapter =
                    FileManagerScreenListAdapter(
                        App.mainActivity,
                        allItemsList
                    )
                itemsList.adapter = adapter
            }
            (itemsList.adapter as RecyclerView.Adapter).notifyDataSetChanged()
        }
    }

    private fun loadProfilePicture() {
        Logger.log("FileManagerScreen", "Loading profile picture")
        // TODO
    }

    // TODO: handle in MainActivity
    override fun onHideKeyboard() {
        searching = false
        toolbarContent.navigation_icon.setImageResource(R.drawable.ic_hamburger)
        toolbarContent.search_text_edit.isCursorVisible = false
        super.onHideKeyboard()
    }

    override fun onFabClicked() {
        val fragment = BottomSheetCreateNew()
        fragment.show(App.mainActivity.supportFragmentManager, "bottom_sheet")
        Logger.log("FileManagerScreen", "onFabClicked")
    }
}