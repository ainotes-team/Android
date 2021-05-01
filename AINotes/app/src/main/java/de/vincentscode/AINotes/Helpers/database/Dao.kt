package de.vincentscode.AINotes.Helpers.database

import androidx.room.*
import de.vincentscode.AINotes.Models.DirectoryModel
import de.vincentscode.AINotes.Models.FileModel
import de.vincentscode.AINotes.Models.PluginModel


@Dao
interface DaoAccess {
    // files
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrReplaceFile(fileModel: FileModel)

    @Query("SELECT * FROM FileModel ORDER BY fileId desc")
    fun fetchAllFiles(): List<FileModel>

    @Query("SELECT * FROM FileModel WHERE fileId =:fileId")
    fun getFile(fileId: Int): List<FileModel>

    @Update
    fun updateFile(fileModel: FileModel)

    @Query("DELETE FROM FileModel WHERE fileId =:fileId")
    fun deleteFile(fileId: Int?)

    @Query("SELECT * FROM FileModel WHERE LOWER(name) LIKE LOWER(:search)")
    fun searchFiles(search: String): List<FileModel>


    // directories
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrReplaceDirectory(directoryModel: DirectoryModel)

    @Query("SELECT * FROM DirectoryModel ORDER BY directoryId desc")
    fun fetchAllDirectories(): List<DirectoryModel>

    @Query("SELECT * FROM DirectoryModel WHERE directoryId =:directoryId")
    fun getDirectory(directoryId: Int): List<DirectoryModel>

    @Update
    fun updateDirectory(directoryModel: DirectoryModel)

    @Query("DELETE FROM DirectoryModel WHERE directoryId =:directoryId")
    fun deleteDirectory(directoryId: Int?)

    @Query("SELECT * FROM DirectoryModel WHERE LOWER(name) LIKE LOWER(:search)")
    fun searchDirectories(search: String): List<DirectoryModel>


    // plugins
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrReplacePlugin(pluginModel: PluginModel)

    @Query("SELECT * FROM PluginModel ORDER BY pluginId desc")
    fun fetchAllPlugins(): List<PluginModel>

    @Query("SELECT * FROM PluginModel WHERE fileId =:fileId")
    fun fetchAllPluginsFromFile(fileId: Int): List<PluginModel>

    @Query("SELECT * FROM PluginModel WHERE pluginId =:pluginId")
    fun getPlugin(pluginId: Int): List<PluginModel>

    @Update
    fun updatePlugin(pluginModel: PluginModel)

    @Query("DELETE FROM PluginModel WHERE fileId =:fileId")
    fun deleteFilePlugins(fileId: Int?)

    @Query("DELETE FROM PluginModel WHERE pluginId =:pluginId")
    fun deletePlugin(pluginId: Int?)
}