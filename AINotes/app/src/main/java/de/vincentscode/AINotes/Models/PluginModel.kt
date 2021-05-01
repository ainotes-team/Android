package de.vincentscode.AINotes.Models

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import de.vincentscode.AINotes.Plugins.ImagePlugin
import de.vincentscode.AINotes.Plugins.TextPlugin
import de.vincentscode.AINotes.Plugins.base.Plugin

@Entity(indices = [Index(
    value = ["pluginId"],
    unique = true
)]
)
class PluginModel {
    @PrimaryKey(autoGenerate = true)
    var pluginId: Int? = null

    var fileId: Int = -1

    var type: String = ""

    var content: String = ""

    var posX: Double = -1.0

    var posY: Double = -1.0

    var sizeX: Double = -1.0

    var sizeY: Double = -1.0

    fun toPlugin() : Plugin {
        val newPlugin = pluginTypes[type]!!.newInstance()
        newPlugin.pluginId = pluginId!!

        return newPlugin
    }

    companion object {
        val pluginTypes = hashMapOf(
            "text" to TextPlugin::class.java,
            "image" to ImagePlugin::class.java
        )
    }
}