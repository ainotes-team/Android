package de.vincentscode.AINotes.Helpers.database

import androidx.room.Database
import androidx.room.RoomDatabase
import de.vincentscode.AINotes.Helpers.database.DaoAccess
import de.vincentscode.AINotes.Models.DirectoryModel
import de.vincentscode.AINotes.Models.FileModel
import de.vincentscode.AINotes.Models.PluginModel

@Database(entities = [(FileModel::class), (DirectoryModel::class), (PluginModel::class)], version = 1)
abstract class FileDatabase : RoomDatabase() {
    abstract fun daoAccess(): DaoAccess
}