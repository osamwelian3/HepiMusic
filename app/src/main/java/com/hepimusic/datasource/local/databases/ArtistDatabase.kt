package com.hepimusic.datasource.local.databases

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.hepimusic.datasource.local.dao.ArtistDao
import com.hepimusic.datasource.local.entities.ArtistEntity
import com.hepimusic.datasource.local.entities.DataConverter

@Database(
    entities = [ArtistEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(DataConverter::class)
abstract class ArtistDatabase: RoomDatabase() {

    abstract val dao: ArtistDao

    companion object
    {
        @Volatile private var instance: ArtistDatabase? = null

        operator fun invoke(context: Context) = instance?: synchronized(Any())
        {
            instance?: createDB(context).also {
                it
            }
        }

        private fun createDB(context: Context) = Room.databaseBuilder(
            context.applicationContext,
            ArtistDatabase::class.java,
            "artistentity"
        ).build()
    }
}