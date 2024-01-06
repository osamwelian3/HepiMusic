package com.custom.amplify.data

import androidx.room.Embedded
import androidx.room.Relation

data class PlayerWithPlaylists(
    @Embedded val player: RequestPlayerEntity,
    @Relation(
        parentColumn = "key",
        entityColumn = "requestPlayerPlaylistsKey"
    )
    val playlists: List<PlaylistWithPlayer>
)