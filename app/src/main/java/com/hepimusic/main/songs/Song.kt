package com.hepimusic.main.songs

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import androidx.media3.common.MediaItem
import com.hepimusic.main.albums.Album
import com.hepimusic.main.common.data.Model
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
class Song(
    override val id: String,
    val title: String,
    val titleKey: String,
    val artist: String,
    @Contextual
    val album: Any?,
    @Contextual
    val artWork: Uri?,
    val path: String,
    val duration: Long,
    val number: String?,
    val artPath: String,
    var isCurrent: Boolean = false,
    var selected: Boolean = false,
    var audioId: Long? = null
): Model(), Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readParcelable(Album::class.java.classLoader),
        parcel.readParcelable(Uri::class.java.classLoader),
        parcel.readString()!!,
        parcel.readLong(),
        parcel.readString(),
        parcel.readString()!!,
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte(),
        parcel.readValue(Long::class.java.classLoader) as? Long
    ) {
    }

    constructor(data: MediaItem) : this(
        id = data.mediaId,
        title = data.mediaMetadata.title.toString(),
        titleKey = data.mediaMetadata.subtitle.toString(),
        artist = if (data.mediaMetadata.artist.toString() != "" || data.mediaMetadata.artist != null) data.mediaMetadata.artist.toString() else data.mediaMetadata.subtitle.toString(),
        album = data.mediaMetadata.albumTitle.toString(),
        artWork = data.mediaMetadata.artworkUri,
        path = data.requestMetadata.mediaUri.toString(),
        duration = data.mediaMetadata.extras?.getLong("duration") ?: 0L,
        number = null,
        artPath = data.mediaMetadata.artworkUri.toString(),
        isCurrent = false,
        selected = false,
        audioId = null
    )

    constructor(data: MediaItem, audioId: Long?) : this(
        id = data.mediaId,
        title = data.mediaMetadata.title.toString(),
        titleKey = data.mediaMetadata.subtitle.toString(),
        artist = data.mediaMetadata.artist.toString(),
        album = data.mediaMetadata.albumTitle.toString(),
        artWork = data.mediaMetadata.artworkUri,
        path = data.requestMetadata.mediaUri.toString(),
        duration = data.mediaMetadata.extras?.getLong("duration") ?: 0L,
        artPath = data.mediaMetadata.artworkUri.toString(),
        number = null,
        audioId = audioId
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(title)
        parcel.writeString(titleKey)
        parcel.writeString(artist)
        parcel.writeParcelable(artWork, flags)
        parcel.writeString(path)
        parcel.writeLong(duration)
        parcel.writeString(number)
        parcel.writeString(artPath)
        parcel.writeByte(if (isCurrent) 1 else 0)
        parcel.writeByte(if (selected) 1 else 0)
        parcel.writeValue(audioId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Song> {
        override fun createFromParcel(parcel: Parcel): Song {
            return Song(parcel)
        }

        override fun newArray(size: Int): Array<Song?> {
            return arrayOfNulls(size)
        }
    }
}