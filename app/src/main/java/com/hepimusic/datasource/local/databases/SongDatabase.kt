package com.hepimusic.datasource.local.databases

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.hepimusic.datasource.local.dao.SongDao
import com.hepimusic.datasource.local.entities.DataConverter
import com.hepimusic.datasource.local.entities.SongEntity

@Database(
    entities = [SongEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(DataConverter::class)
abstract class SongDatabase: RoomDatabase() {

    abstract val dao: SongDao

    companion object
    {
        @Volatile private var instance: SongDatabase? = null

        operator fun invoke(context: Context) = instance?: synchronized(Any())
        {
            instance?: createDB(context).also {
                it
            }
        }

        private fun createDB(context: Context) = Room.databaseBuilder(
            context.applicationContext,
            SongDatabase::class.java,
            "songentity"
        ).build()
    }
}