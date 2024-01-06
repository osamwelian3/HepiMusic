package com.custom.amplify.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.amplifyframework.core.model.temporal.Temporal
import com.custom.amplify.converters.Converters

@Entity(tableName = "CreatorTable")
@TypeConverters(Converters::class)
data class CreatorEntity(
    @PrimaryKey val key: String,
    val desc: String?,
    val facebook: String?,
    val instagram: String?,
    val name: String,
    val thumbnail: String?,
    val thumbnailKey: String?,
    val twitter: String?,
    val youtube: String?,
    val owner: String?,
    val createdAt: Temporal.DateTime?,
    val updatedAt: Temporal.DateTime?
)