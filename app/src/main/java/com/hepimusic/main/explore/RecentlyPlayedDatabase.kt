package com.hepimusic.main.explore

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [RecentlyPlayed::class],
    version = 1,
    exportSchema = false
)
abstract class RecentlyPlayedDatabase: RoomDatabase() {
    abstract val dao: RecentlyPlayedDao

    companion object
    {
        @Volatile private var instance: RecentlyPlayedDatabase? = null

        operator fun invoke(context: Context) = instance?: synchronized(Any())
        {
            instance?: createDB(context).also {
                it
            }
        }

        private fun createDB(context: Context) = Room.databaseBuilder(
            context.applicationContext,
            RecentlyPlayedDatabase::class.java,
            "recently_played_database"
        ).build()
    }
}