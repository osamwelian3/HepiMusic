package com.custom.amplify.data

import androidx.room.Embedded
import androidx.room.Relation

class PlaylistWithSongs(
    @Embedded val playlist: RequestPlaylistEntity,
    @Relation(
        parentColumn = "key",
        entityColumn = "requestPlaylistSongsKey"
    )
    val songs: List<RequestSongEntity>
)