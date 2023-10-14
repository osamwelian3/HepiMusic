package com.hepimusic.main.artists

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import androidx.media3.common.MediaItem
import com.hepimusic.main.common.data.Model
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class Artist(
    override val id: String?,
    val name: String,
    val artist: String,
    @Contextual
    val albumArt: Uri?,
    val tracks: Int? = 0,
    val year: String? = "",
    val key: String
): Model(), Parcelable {

    constructor(data: MediaItem) : this(
        id = data.mediaId,
        name = data.mediaMetadata.title.toString(),
        artist = data.mediaMetadata.artist.toString(),
        albumArt = data.mediaMetadata.artworkUri,
        year = data.mediaMetadata.releaseYear.toString(),
        tracks = data.mediaMetadata.totalTrackCount,
        key = data.mediaId
    )

    constructor(data: MediaItem, id: Any?) : this(
        name = data.mediaMetadata.title.toString(),
        artist = data.mediaMetadata.artist.toString(),
        albumArt = data.mediaMetadata.artworkUri,
        id = id.toString(),
        key = data.mediaId
    )

    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readParcelable(Uri::class.java.classLoader),
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readString(),
        parcel.readString()!!
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(name)
        parcel.writeString(artist)
        parcel.writeParcelable(albumArt, flags)
        parcel.writeValue(tracks)
        parcel.writeString(year)
        parcel.writeString(key)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Artist> {
        override fun createFromParcel(parcel: Parcel): Artist {
            return Artist(parcel)
        }

        override fun newArray(size: Int): Array<Artist?> {
            return arrayOfNulls(size)
        }
    }

}
