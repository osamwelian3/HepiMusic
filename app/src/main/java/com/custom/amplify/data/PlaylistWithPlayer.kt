package com.custom.amplify.data

import androidx.room.Embedded
import androidx.room.Relation

data class PlaylistWithPlayer(
    @Embedded val playlist: RequestPlaylistEntity,
    @Relation(
        parentColumn = "requestPlayerPlaylistsKey",
        entityColumn = "key"
    )
    val player: RequestPlayerEntity
)