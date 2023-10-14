package com.hepimusic.main.navigation

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NavViewModel @Inject constructor(private val preferences: SharedPreferences) : ViewModel() {
    private var navRepository: NavRepository? = null
    var navItems: LiveData<List<NavItem>>? = null
        private set

    fun init(origin: Int?) {
        if (this.navItems != null) {
            return
        }

        navRepository = NavRepository(origin, preferences)
        navItems = navRepository?.liveItems
    }

    fun swap(list: List<NavItem>) = navRepository?.swap(list)
}