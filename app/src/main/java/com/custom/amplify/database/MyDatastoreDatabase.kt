package com.custom.amplify.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.custom.amplify.converters.Converters
import com.custom.amplify.data.AlbumEntity
import com.custom.amplify.data.CategoryEntity
import com.custom.amplify.data.CreatorEntity
import com.custom.amplify.data.ProfileEntity
import com.custom.amplify.data.RequestPlayerEntity
import com.custom.amplify.data.RequestPlaylistEntity
import com.custom.amplify.data.RequestSongEntity
import com.custom.amplify.data.SongEntity

@Database(
    entities = [SongEntity::class, AlbumEntity::class, CategoryEntity::class, CreatorEntity::class, ProfileEntity::class, RequestSongEntity::class, RequestPlaylistEntity::class, RequestPlayerEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class MyDatastoreDatabase: RoomDatabase() {

    abstract fun datastoreDao(): Dao

    companion object {
        @Volatile private var instance: MyDatastoreDatabase? = null
        private val lock = Any()

        operator fun invoke(context: Context) = instance ?: synchronized(lock) {
            instance ?: buildDatabase(context).also {
                instance = it
            }
        }

        private fun buildDatabase(context: Context) = Room.databaseBuilder(
            context,
            MyDatastoreDatabase::class.java,
            "MyCustomDatastoreDB"
        ).build()
    }
}