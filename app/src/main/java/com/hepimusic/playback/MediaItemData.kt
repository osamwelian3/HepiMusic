package com.hepimusic.playback

import android.net.Uri
import android.os.Parcelable
import androidx.media3.common.MediaItem
import com.hepimusic.main.common.data.Model

data class MediaItemData(
    override val id: String,
    val title: String,
    val subtitle: String?, // Artist
    val description: String, //Album
    val albumArtUri: Uri?,
    val isBrowsable: Boolean?,
    var isPlaying: Boolean,
    var isBuffering: Boolean,
    var duration: Long = 0L
): Model() {

    val playingOrBuffering get() = isPlaying || isBuffering

    constructor(item: MediaItem, isPlaying: Boolean, isBuffering: Boolean) : this(
        id = item.mediaId,
        title = item.mediaMetadata.title!!.toString(),
        subtitle = item.mediaMetadata.artist!!.toString(),
        albumArtUri = item.mediaMetadata.artworkUri,
        description = item.mediaMetadata.albumTitle.toString(),
        isBrowsable = item.mediaMetadata.isBrowsable,
        isPlaying = isPlaying,
        isBuffering = isBuffering,
        duration = item.mediaMetadata.extras?.getLong("duration") ?: 0L
    )

//    constructor(item: MediaItem, isPlaying: Boolean, isBuffering: Boolean) : this(
//        id = item.mediaId,
//        title = item.mediaMetadata.title!!.toString(),
//        subtitle = item.mediaMetadata.subtitle!!.toString(),
//        albumArtUri = item.mediaMetadata.artworkUri,
//        description = item.mediaMetadata.description.toString(),
//        isBrowsable = false,
//        isPlaying = isPlaying,
//        isBuffering = isBuffering,
//        duration = item.mediaMetadata.extras?.getLong("duration") ?: 0L
//    )

    // We don't want to use all the feeds to check for equality because some of them might change.
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MediaItemData

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    fun areContentsTheSame(other: MediaItemData?): Boolean {
        if (this != other) return false
        return id == other.id
                && title == other.title
                && subtitle == other.subtitle
                && duration == other.duration
                && isPlaying == other.isPlaying
                && albumArtUri == other.albumArtUri
                && description == other.description
                && isBrowsable == other.isBrowsable
                && isBuffering == other.isBuffering
    }


}