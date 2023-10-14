package com.hepimusic.main.genres

import android.os.Parcel
import android.os.Parcelable
import androidx.media3.common.MediaItem
import com.hepimusic.main.common.data.Model
import kotlinx.serialization.Serializable

@Serializable
data class Genre(override val id: String, val name: String) : Model(), Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!
    ) {
    }

    constructor(mediaItem: MediaItem) : this(
        id = mediaItem.mediaId,
        name = mediaItem.mediaMetadata.title.toString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(name)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Genre> {
        override fun createFromParcel(parcel: Parcel): Genre {
            return Genre(parcel)
        }

        override fun newArray(size: Int): Array<Genre?> {
            return arrayOfNulls(size)
        }
    }
}