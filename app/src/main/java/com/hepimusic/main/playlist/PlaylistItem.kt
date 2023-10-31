package com.hepimusic.main.playlist

import androidx.media3.common.MediaItem
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.hepimusic.main.common.data.Model

@Entity(tableName = "playlist_items_table")
class PlaylistItem(
    @PrimaryKey
    override var id: String, // media id
    var playlistId: String,
    val mediaUri: String,
    val artWorkUri: String,
    val artist: String,
    val album: String,
    val title: String,
    val duration: Long,
    val entryDate: Long,
    @Ignore
    var selected: Boolean = false,
    @Ignore
    var isPlaying: Boolean = false
): Model() {


    constructor(
        id: String,
        playlistId: String,
        mediaUri: String,
        artWorkUri: String,
        artist: String,
        album: String,
        title: String,
        duration: Long,
        entryDate: Long,
    ) : this(id, playlistId, mediaUri, artWorkUri, artist, album, title, duration, entryDate, false, false)

    constructor(meta: MediaItem) : this(
        id = meta.mediaId,
        playlistId = meta.mediaMetadata.extras?.getString("playlistId") ?: "Unknown Playlist",
        mediaUri = meta.requestMetadata.mediaUri.toString(),
        artWorkUri = meta.mediaMetadata.artworkUri.toString(),
        artist = meta.mediaMetadata.artist?.toString() ?: "",
        title = meta.mediaMetadata.title?.toString() ?: "",
        album = meta.mediaMetadata.albumTitle?.toString() ?: "Unknown Album",
        duration = meta.mediaMetadata.extras?.getLong("duration") ?: 0L,
        entryDate = System.currentTimeMillis(),
        isPlaying = false
    )

    constructor(meta: MediaItem, playlistId: String) : this(
        id = meta.mediaId,
        playlistId = meta.mediaMetadata.extras?.getString("playlistId") ?: playlistId,
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