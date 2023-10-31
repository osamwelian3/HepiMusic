package com.hepimusic.main.playlist

import com.hepimusic.main.explore.RecentlyPlayed
import com.hepimusic.main.explore.RecentlyPlayedDao
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [PlaylistItem::class],
    version = 1,
    exportSchema = false
)
abstract class PlaylistItemsDatabase: RoomDatabase() {
    abstract val dao: PlaylistItemsDao

    companion object
    {
        @Volatile private var instance: PlaylistItemsDatabase? = null

        operator fun invoke(context: Context) = instance?: synchronized(Any())
        {
            instance?: createDB(context).also {
                it
            }
        }

        private fun createDB(context: Context) = Room.databaseBuilder(
            context.applicationContext,
            PlaylistItemsDatabase::class.java,
            "playlist_items_database"
        ).build()
    }
}