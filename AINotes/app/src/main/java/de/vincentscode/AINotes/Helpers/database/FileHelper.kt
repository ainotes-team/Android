package de.vincentscode.AINotes.Helpers.database

import de.vincentscode.AINotes.App
import de.vincentscode.AINotes.Models.DirectoryModel
import de.vincentscode.AINotes.Models.FileModel
import de.vincentscode.AINotes.Models.PluginModel

class FileHelper {
    companion object {
        private fun onDatabaseUpdated() {
            App.mainActivity.fileManagerScreen.reload()
        }

        fun listFiles(parentDirectoryId: Int, includeDeleted: Boolean = false) : ArrayList<FileModel> {
            val list = App.mainActivity.fileDatabase.daoAccess().fetchAllFiles()
            val s: ArrayList<FileModel> = ArrayList()
            list.forEach { fileModel: FileModel ->
                if (fileModel.parentDirectoryId == parentDirectoryId) {
                    if (!includeDeleted) {
                        if (!fileModel.deleted) s.add(fileModel)
                    } else {
                        s.add(fileModel)
                    }
                }
            }
            return s
        }

        fun listDirectories(parentDirectoryId: Int, includeDeleted: Boolean = false) : ArrayList<DirectoryModel> {
            val list = App.mainActivity.fileDatabase.daoAccess().fetchAllDirectories()
            val s: ArrayList<DirectoryModel> = ArrayList()
            list.forEach { directoryModel: DirectoryModel ->
                if (directoryModel.parentDirectoryId == parentDirectoryId) {
                    if (!includeDeleted) {
                        if (!directoryModel.deleted) s.add(directoryModel)
                    } else {
                        s.add(directoryModel)
                    }
                }
            }
            return s
        }

        fun listFiles(search: String = "", includeDeleted: Boolean = false) : ArrayList<FileModel> {
            val q = if (search != "") "%$search%" else ""
            val list = if (q == "") App.mainActivity.fileDatabase.daoAccess().fetchAllFiles() else App.mainActivity.fileDatabase.daoAccess().searchFiles(q)
            val s: ArrayList<FileModel> = ArrayList()
            list.forEach { fileModel: FileModel ->
                if (!includeDeleted) {
                    if (!fileModel.deleted) s.add(fileModel)
                } else {
                    s.add(fileModel)
                }
            }
            return s
        }

        fun listDirectories(search: String = "", includeDeleted: Boolean = false) : ArrayList<DirectoryModel> {
            val q = if (search != "") "%$search%" else ""
            val list = if (q == "") App.mainActivity.fileDatabase.daoAccess().fetchAllDirectories() else App.mainActivity.fileDatabase.daoAccess().searchDirectories(q)
            val s: ArrayList<DirectoryModel> = ArrayList()
            list.forEach { directoryModel: DirectoryModel ->
                if (!includeDeleted) {
                    if (!directoryModel.deleted) s.add(directoryModel)
                } else {
                    s.add(directoryModel)
                }
            }
            return s
        }

        fun createFile(fileModel: FileModel) : Int {
            App.mainActivity.fileDatabase.daoAccess().insertOrReplaceFile(fileModel)
            onDatabaseUpdated()
            if (fileModel.fileId == null) return -1
            return fileModel.fileId!!
        }

        fun insertOrReplaceFile(fileModel: FileModel) {
            App.mainActivity.fileDatabase.daoAccess().insertOrReplaceFile(fileModel)
            onDatabaseUpdated()
        }

        fun updateFile(fileModel: FileModel) {
            App.mainActivity.fileDatabase.daoAccess().updateFile(fileModel)
            onDatabaseUpdated()
        }

        fun getFile(fileId: Int) : FileModel? {
            return App.mainActivity.fileDatabase.daoAccess().getFile(fileId)[0]
        }

        fun deleteFile(fileId: Int) {
            deleteFile(
                getFile(
                    fileId
                )!!
            )
        }

        fun deleteFile(fileModel: FileModel, markOnly: Boolean = true) {
            if (markOnly) {
                fileModel.deleted = true
                App.mainActivity.fileDatabase.daoAccess().insertOrReplaceFile(fileModel)
            } else {
                App.mainActivity.fileDatabase.daoAccess().deleteFile(fileModel.fileId)
            }
            onDatabaseUpdated()
        }

        fun insertOrReplacePlugin(pluginModel: PluginModel) : Int {
            App.mainActivity.fileDatabase.daoAccess().insertOrReplacePlugin(pluginModel)
            onDatabaseUpdated()
            if (pluginModel.pluginId == null) return -1
            return pluginModel.pluginId!!
        }

        fun updatePlugin(pluginModel: PluginModel) {
            App.mainActivity.fileDatabase.daoAccess().updatePlugin(pluginModel)
            onDatabaseUpdated()
        }

        fun getPluginByRemoteId(remotePluginId: String) : PluginModel? {
            // TODO
            return null
        }

        fun deletePlugin(pluginId: Int) {
            App.mainActivity.fileDatabase.daoAccess().deletePlugin(pluginId)
        }

        fun deletePlugin(pluginModel: PluginModel) {
            App.mainActivity.fileDatabase.daoAccess().deletePlugin(pluginModel.pluginId)
        }

        fun getFilePlugins(fileId: Int) : List<PluginModel> {
            return App.mainActivity.fileDatabase.daoAccess().fetchAllPluginsFromFile(fileId)
        }

        fun deleteFilePlugins(fileId: Int) {
            App.mainActivity.fileDatabase.daoAccess().deleteFilePlugins(fileId)
        }

        fun createDirectory(directoryModel: DirectoryModel) : Int {
            App.mainActivity.fileDatabase.daoAccess().insertOrReplaceDirectory(directoryModel)
            onDatabaseUpdated()
            if (directoryModel.directoryId == null) return -1
            return directoryModel.directoryId!!
        }

        fun getDirectory(directoryId: Int) : DirectoryModel? {
            return App.mainActivity.fileDatabase.daoAccess().getDirectory(directoryId)[0]
        }

        fun updateDirectory(directoryModel: DirectoryModel) {
            App.mainActivity.fileDatabase.daoAccess().updateDirectory(directoryModel)
            onDatabaseUpdated()
        }

        fun deleteDirectory(directoryModel: DirectoryModel, markOnly: Boolean = true) {
            if (markOnly) {
                directoryModel.deleted = true
                App.mainActivity.fileDatabase.daoAccess().insertOrReplaceDirectory(directoryModel)

                listDirectories(
                    directoryModel.directoryId!!
                ).forEach {
                    deleteDirectory(
                        it
                    )
                }

                listFiles(
                    directoryModel.directoryId!!
                ).forEach {
                    deleteFile(
                        it.fileId!!
                    )
                }
            } else {
                App.mainActivity.fileDatabase.daoAccess().deleteDirectory(directoryModel.directoryId)
            }
            onDatabaseUpdated()
        }
    }
}