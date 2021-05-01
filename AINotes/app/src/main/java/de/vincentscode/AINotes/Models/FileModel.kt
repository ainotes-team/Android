package de.vincentscode.AINotes.Models

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import de.vincentscode.AINotes.Helpers.database.FileHelper

@Entity(indices = [Index(
    value = ["fileId"],
    unique = true
)],
    tableName = "fileModel"
)
class FileModel {
    @PrimaryKey(autoGenerate = true)
    var fileId: Int? = null

    var parentDirectoryId: Int = -1

    var name: String = ""

    var creationDate: Long = -1

    var lastChangedDate: Long = -1

    var lineMode: String = ""

    var strokeContent: String = ""

    var zoom: Float = -1f

    var scrollX: Double = -1.0

    var scrollY: Double = -1.0

    var remoteId: String = ""

    var lastSynced: Long = -1

    var internalPluginModels: String = ""

    var deleted: Boolean = false

    fun getPluginModels() : List<PluginModel> {
        return FileHelper.getFilePlugins(fileId!!)
    }

    override fun toString(): String {
        return name
    }
}