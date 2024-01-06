package com.custom.amplify.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.amplifyframework.core.model.temporal.Temporal
import com.custom.amplify.converters.Converters

@Entity(tableName = "SongTable")
@TypeConverters(Converters::class) // Add TemporalConverter for Temporal.DateTime
data class SongEntity(
    @PrimaryKey val key: String,
    val fileUrl: String,
    val fileKey: String,
    val listens: List<String>,
    val trendingListens: List<String>,
    val listOfUidDownVotes: List<String>,
    val listOfUidUpVotes: List<String>,
    val name: String,
    val partOf: String,
    val selectedCategory: String,
    val selectedCreator: String,
    val thumbnail: String,
    val thumbnailKey: String,
    val owner: String,
    val createdAt: Temporal.DateTime,
    val updatedAt: Temporal.DateTime
    // Add other fields as required
)