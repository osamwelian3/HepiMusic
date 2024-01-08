package com.hepimusic.models.mappers

import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.amplifyframework.datastore.generated.model.Category
import com.hepimusic.main.genres.Genre

fun Category.toGenre() : Genre {
    return Genre(
        id = "[category]"+this.name,
        name = this.name,
        key = this.key
    )
}

fun Genre.toMediaItem() : MediaItem {
    val metaData = MediaMetadata.Builder()
        .setTitle(this.name)
        .setSubtitle(this.name)
        .setDescription(this.key)
        .setIsBrowsable(true)
        .setDisplayTitle(this.name)
        .build()

    return MediaItem.Builder()
        .setMediaMetadata(metaData)
        .setMediaId(this.id)
        .build()
}

fun MediaItem.toGenre(): Genre {
    return Genre(
        this
    )
}