package com.custom.amplify.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation
import androidx.room.TypeConverters
import com.amplifyframework.core.model.temporal.Temporal
import com.custom.amplify.converters.Converters

@Entity(tableName = "RequestPlayerTable")
@TypeConverters(Converters::class)
data class RequestPlayerEntity(
    @PrimaryKey val key: String,
    val name: String?,
    val desc: String?,
    val longitude: String?,
    val latitude: String?,
    val followers: List<String>?,
    val following: List<String>?,
    val owners: List<String>?,
    val createdAt: Temporal.DateTime?,
    val updatedAt: Temporal.DateTime?
)