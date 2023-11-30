package com.hepimusic.main.playlist

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [Playlist::class],
    version = 1,
    exportSchema = false
)
abstract class PlaylistDatabase: RoomDatabase() {
    abstract val dao: PlaylistDao

    companion object
    {
        @Volatile private var instance: PlaylistDatabase? = null

        operator fun invoke(context: Context) = instance?: synchronized(Any())
        {
            instance?: createDB(context).also {
                it
            }
        }

        private fun createDB(context: Context) = Room.databaseBuilder(
            context.applicationContext,
            PlaylistDatabase::class.java,
            "playlist_database"
        ).build()
    }
}