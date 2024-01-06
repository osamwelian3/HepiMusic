package com.custom.amplify.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.amplifyframework.core.model.temporal.Temporal
import com.custom.amplify.converters.Converters

@Entity(tableName = "CategoryTable")
@TypeConverters(Converters::class)
data class CategoryEntity(
    @PrimaryKey val key: String,
    val name: String,
    val owner: String?,
    val createdAt: Temporal.DateTime?,
    val updatedAt: Temporal.DateTime?
)