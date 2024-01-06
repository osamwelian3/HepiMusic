package com.custom.amplify.data

import androidx.room.Embedded
import androidx.room.Relation

data class SongWithPlaylist(
    @Embedded val song: RequestSongEntity,
    @Relation(
        parentColumn = "requestPlaylistSongsKey",
        entityColumn = "key"
    )
    val playlist: RequestPlaylistEntity
)