package com.custom.amplify.converters

import androidx.room.TypeConverter
import com.amplifyframework.core.model.temporal.Temporal
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Date

class Converters {
    @TypeConverter
    fun fromStringList(list: List<String>): String {
        // Convert list to a single string, e.g., using Gson
        return Gson().toJson(list)
    }

    @TypeConverter
    fun toStringList(data: String): List<String> {
        // Convert string back to a list, e.g., using Gson
        val listType = object : TypeToken<List<String>>() {}.type
        return Gson().fromJson(data, listType)
    }

    @TypeConverter
    fun fromTemporalDate(date: Temporal.DateTime?): Long? {
        return date?.toDate()?.time?.toLong()
    }

    @TypeConverter
    fun toTemporalDate(date: Long?): Temporal.DateTime? {
        return date?.let {
            Temporal.DateTime(Date(it), 0)
        }
    }
}