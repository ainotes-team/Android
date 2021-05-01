package de.vincentscode.AINotes.Helpers.Canvas

import android.os.Parcel
import android.os.Parcelable
import android.view.View
import de.vincentscode.AINotes.App
import java.util.*

internal class CustomParcelable : View.BaseSavedState {
    var paths = LinkedHashMap<CustomPath, DrawingAttributes>()

    constructor(superState: Parcelable) : super(superState)

    constructor(parcel: Parcel) : super(parcel) {
        val size = parcel.readInt()
        for (i in 0 until size) {
            val key = parcel.readSerializable() as CustomPath
            val paintOptions = DrawingAttributes(parcel.readInt(), parcel.readFloat())
            paths[key] = paintOptions
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        super.writeToParcel(parcel, flags)
        parcel.writeInt(paths.size)
        for ((path, paintOptions) in paths) {
            parcel.writeSerializable(path)
            parcel.writeInt(paintOptions.color)
            parcel.writeFloat(paintOptions.strokeWidth)
        }
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<CustomParcelable> = object : Parcelable.Creator<CustomParcelable> {
            override fun createFromParcel(source: Parcel) =
                CustomParcelable(source)

            override fun newArray(size: Int) = arrayOf<CustomParcelable>()
        }
    }
}