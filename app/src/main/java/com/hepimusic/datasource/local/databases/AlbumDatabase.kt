package com.hepimusic.datasource.local.databases

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.hepimusic.datasource.local.dao.AlbumDao
import com.hepimusic.datasource.local.entities.AlbumEntity
import com.hepimusic.datasource.local.entities.DataConverter

@Database(
    entities = [AlbumEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(DataConverter::class)
abstract class AlbumDatabase: RoomDatabase() {

    abstract val dao: AlbumDao

    companion object
    {
        @Volatile private var instance: AlbumDatabase? = null

        operator fun invoke(context: Context) = instance?: synchronized(Any())
        {
            instance?: createDB(context).also {
                it
            }
        }

        private fun createDB(context: Context) = Room.databaseBuilder(
            context.applicationContext,
            AlbumDatabase::class.java,
            "albumentity"
        ).build()
    }
}