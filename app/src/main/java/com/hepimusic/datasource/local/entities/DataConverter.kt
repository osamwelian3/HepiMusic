package com.hepimusic.datasource.local.entities

import androidx.room.TypeConverter
import com.amplifyframework.core.model.temporal.Temporal
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.Serializable
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.util.Locale

class DataConverter: Serializable {

    @TypeConverter
    fun fromStringList(string: List<String>?): String? {
        if (string == null) {
            return (null)
        }
        val gson = Gson()
        val type: Type = object : TypeToken<List<String>>() {}.type
        return gson.toJson(string, type)
    }

    @TypeConverter
    fun toStringList(string: String?): List<String>? {
        if (string != null) {
            if (string.isEmpty()) {
                return (null)
            }
        }
        val gson = Gson()
        val type: Type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(string, type)
    }

    @TypeConverter
    fun fromTemporalDate(date: Temporal.DateTime?): String? {
        if (date == null) {
            return (null)
        }
        return date.toDate().toString()
    }

    @TypeConverter
    fun toTemporalDate(date: String?): Temporal.DateTime? {
        if (date == null) {
            return null
        }
        if (date.isEmpty()){
            return (null)
        }
        val inputFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US)
//        inputFormat.timeZone = TimeZone.getTimeZone("GMT")

        return Temporal.DateTime(inputFormat.parse(date)!!, 0) // yyyy-MM-dd'T'HH:mm:ss.SSS'Z'
    }

}