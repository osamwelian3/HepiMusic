package com.custom.amplify.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.amplifyframework.core.model.temporal.Temporal
import com.custom.amplify.converters.Converters


@Entity(tableName = "ProfileTable")
@TypeConverters(Converters::class)
data class ProfileEntity(
    @PrimaryKey val key: String,
    val name: String?,
    val email: String?,
    val phoneNumber: String?,
    val imageKey: String?,
    val followers: List<String>?,
    val follows: List<String>?,
    val activeStatus: String?, // Change this to appropriate type if needed
    val lastActive: Temporal.DateTime?, // Change this to appropriate type if needed
    val owner: String?
)