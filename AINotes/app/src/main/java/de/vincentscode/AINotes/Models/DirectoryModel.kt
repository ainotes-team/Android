package de.vincentscode.AINotes.Models

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(indices = [Index(
    value = ["directoryId"],
    unique = true
)],
    tableName = "directoryModel"
)
class DirectoryModel {
    @PrimaryKey(autoGenerate = true)
    var directoryId: Int? = null

    var parentDirectoryId: Int = -1

    var name: String = ""

    var subject: String = ""

    var hexColor: String = ""

    var remoteId: String = ""

    var deleted: Boolean = false

    var creationDate: Long = -1

    var lastChangedDate: Long = -1

    override fun toString(): String {
        return name
    }
}