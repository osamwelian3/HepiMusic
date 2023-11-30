package com.hepimusic.main.playlist

import android.os.Parcel
import android.os.Parcelable
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.hepimusic.common.Constants
import com.hepimusic.main.common.data.Model
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "playlist")
data class Playlist(
    @PrimaryKey
    override val id: String,
    var name: String,
    var modified: Long?,
    var songsCount: Int = 0,
    @Ignore
    var selected: Boolean = false,
    var playlistImage: String? = ""
) : Model(),
    Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readLong(),
        parcel.readInt(),
        parcel.readByte() != 0.toByte(),
        parcel.readString()
    ) {
    }

    constructor(mediaItem: MediaItem) : this(
        id = mediaItem.mediaId,
        name = mediaItem.mediaMetadata.title.toString(),
        modified = mediaItem.mediaMetadata.releaseYear?.toLong() ?: 0L,
        playlistImage = mediaItem.mediaMetadata.artworkUri.toString()
    )

    constructor(p: Playlist) : this(id = p.id, name = p.name, modified = p.modified, songsCount = p.songsCount, playlistImage = p.playlistImage)

    constructor(id: String) : this(id = id, name = "", modified = 0L)

    /**
     *  When [width] is more than [Constants.MAX_MODEL_IMAGE_THUMB_WIDTH], we'll change the value of any of this
     *  playlist's fields to force Glide to use a cache key different from an unmodified playlist. The reason for this
     *  is that we are using a different loading algorithm for ImageViews with with more
     *  than [Constants.MAX_MODEL_IMAGE_THUMB_WIDTH]. See [PlaylistModelLoader] for the algorithm
     *
     *  @param width the width of the ImageViw in pixels
     *  @return this playlist
     */
    fun modForViewWidth(width: Int): Playlist {
        if (width.dp > Constants.MAX_MODEL_IMAGE_THUMB_WIDTH.dp) {
            name = "$name$id$songsCount"
        }
        return this
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(name)
        modified?.let { parcel.writeLong(it) }
        parcel.writeInt(songsCount)
        parcel.writeByte(if (selected) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Playlist> {
        override fun createFromParcel(parcel: Parcel): Playlist {
            return Playlist(parcel)
        }

        override fun newArray(size: Int): Array<Playlist?> {
            return arrayOfNulls(size)
        }
    }

}