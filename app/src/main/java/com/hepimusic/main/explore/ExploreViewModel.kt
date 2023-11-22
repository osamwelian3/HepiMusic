package com.hepimusic.main.explore

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.hepimusic.common.Constants
import com.hepimusic.main.albums.AlbumsViewModel
import com.hepimusic.ui.MainActivity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExploreViewModel @Inject constructor(
    application: Application,
    /*browser: MediaBrowser,*/
    recentlyPlayedDatabase: RecentlyPlayedDatabase
): AlbumsViewModel(application /*browser*/) {
    private lateinit var preferences: SharedPreferences

    private val preferencesListener: SharedPreferences.OnSharedPreferenceChangeListener =
        SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
            Log.e("CHANGED KEY", key!!)
            when (key) {
                Constants.INITIALIZATION_COMPLETE -> {
                    Log.e("PREFERENCE CHANGED", sharedPreferences.getBoolean(Constants.INITIALIZATION_COMPLETE, false).toString())
                    loadData()
                }
            }
        }

    private lateinit var recentlyPlayedRepository: RecentlyPlayedRepository
    lateinit var recentlyPlayed: LiveData<List<RecentlyPlayed>>

    override var sortOrder: String? = "RANDOM() LIMIT 5"

    init {
        viewModelScope.launch {
            preferences = application.getSharedPreferences("main", Context.MODE_PRIVATE)
            preferences.registerOnSharedPreferenceChangeListener(preferencesListener)
            val recentDao = recentlyPlayedDatabase.dao
            recentlyPlayedRepository = RecentlyPlayedRepository(recentDao)
            recentlyPlayed = recentlyPlayedRepository.recentlyPlayed
        }
    }
}