package com.hepimusic.main.explore

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.hepimusic.common.Constants

@Dao
interface RecentlyPlayedDao {

    @Query("SELECT * FROM recently_played ORDER BY entryDate DESC")
    fun fetchAll(): LiveData<List<RecentlyPlayed>>

    @Query("SELECT * FROM recently_played ORDER BY entryDate DESC LIMIT 1")
    fun fetchFirst(): RecentlyPlayed?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(recentlyPlayed: RecentlyPlayed)

    /**
     * We want to keep a maximum of [Constants.MAX_RECENTLY_PLAYED] items in this database
     *
     * This will delete the rows whose id is greater than [Constants.MAX_RECENTLY_PLAYED]
     */
    @Query("DELETE FROM recently_played where id NOT IN (SELECT id from recently_played ORDER BY entryDate DESC LIMIT ${Constants.MAX_RECENTLY_PLAYED})")
    fun trim()
}