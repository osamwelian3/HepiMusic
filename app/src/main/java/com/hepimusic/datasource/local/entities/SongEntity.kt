package com.hepimusic.datasource.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.amplifyframework.core.model.temporal.Temporal

@Entity(tableName = "songentity")
data class SongEntity(
    @PrimaryKey
    val key: String,
    val fileUrl: String,
    val fileKey: String,
    @TypeConverters(DataConverter::class)
    val listens: List<String>?,
    @TypeConverters(DataConverter::class)
    val trendingListens: List<String>?,
    @TypeConverters(DataConverter::class)
    val listOfUidDownVotes: List<String>?,
    @TypeConverters(DataConverter::class)
    val listOfUidUpVotes: List<String>?,
    val name: String,
    val partOf: String?,
    val selectedCategory: String,
    val selectedCreator: String?,
    val thumbnail: String,
    val thumbnailKey: String,
    @TypeConverters(DataConverter::class)
    val createdAt: Temporal.DateTime?,
    @TypeConverters(DataConverter::class)
    val updatedAt: Temporal.DateTime?
)