package com.hepimusic.main.explore

import androidx.lifecycle.LiveData

class RecentlyPlayedRepository(private val playedDao: RecentlyPlayedDao) {

    val recentlyPlayed: LiveData<List<RecentlyPlayed>> = playedDao.fetchAll()

    suspend fun insert(recentlyPlayed: RecentlyPlayed) = playedDao.insert(recentlyPlayed)

    suspend fun trim() = playedDao.trim()

    suspend fun fetchFirst(): RecentlyPlayed? = playedDao.fetchFirst()
}