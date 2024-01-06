package com.custom.amplify.data

import androidx.room.Embedded
import androidx.room.Relation

data class PlayerWithPlaylistsWithPlaylistSongs (
    @Embedded val player: RequestPlayerEntity,
    @Relation(
        parentColumn = "key",
        entityColumn = "requestPlayerPlaylistsKey",
        entity = RequestPlaylistEntity::class
    )
    val playlists: List<PlaylistWithSongs>
)