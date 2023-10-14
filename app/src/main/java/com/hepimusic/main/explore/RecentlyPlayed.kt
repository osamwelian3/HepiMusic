package com.hepimusic.main.explore

import androidx.media3.common.MediaItem
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.hepimusic.main.common.data.Model

@Entity(tableName = "recently_played")
class RecentlyPlayed(
    @PrimaryKey
    override val id: String, // media id
    val mediaUri: String,
    val artWorkUri: String,
    val artist: String,
    val album: String,
    val title: String,
    val duration: Long,
    val entryDate: Long,
    @Ignore
    var isPlaying: Boolean = false
): Model() {


    constructor(
        id: String,
        mediaUri: String,
        artWorkUri: String,
        artist: String,
        album: String,
        title: String,
        duration: Long,
        entryDate: Long
    ) : this(id, mediaUri, artWorkUri, artist, album, title, duration, entryDate, false)

    constructor(meta: MediaItem) : this(
        id = meta.mediaId,
        mediaUri = meta.requestMetadata.mediaUri.toString(),
        artWorkUri = meta.mediaMetadata.artworkUri.toString(),
        artist = meta.mediaMetadata.artist?.toString() ?: "",
        title = meta.mediaMetadata.title?.toString() ?: "",
        album = meta.mediaMetadata.albumTitle?.toString() ?: "Unknown Album",
        duration = meta.mediaMetadata.extras?.getLong("duration") ?: 0L,
        entryDate = System.currentTimeMillis(),
        isPlaying = false
    )
}