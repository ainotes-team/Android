package de.vincentscode.AINotes.Helpers

import de.vincentscode.AINotes.Models.DirectoryModel
import de.vincentscode.AINotes.Models.FileModel

class FileManagerScreenListItem {
    public lateinit var listItemMode: ListItemMode
    public lateinit var directoryModel: DirectoryModel
    public lateinit var fileModel: FileModel
}

enum class ListItemMode {
    Directory,
    File
}