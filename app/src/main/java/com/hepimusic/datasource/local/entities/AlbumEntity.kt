package com.hepimusic.datasource.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.amplifyframework.core.model.temporal.Temporal

@Entity(tableName = "albumentity")
data class AlbumEntity(
    @PrimaryKey
    val key: String,
    val name: String,
    val thumbnail: String? = null,
    val thumbnailKey: String? = null,
    @TypeConverters(DataConverter::class)
    val createdAt: Temporal.DateTime? = null,
    @TypeConverters(DataConverter::class)
    val updatedAt: Temporal.DateTime? = null
)