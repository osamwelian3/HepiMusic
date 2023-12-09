package com.hepimusic.main.admin.dashboard

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amplifyframework.core.Amplify
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminNavViewModel @Inject constructor(private val preferences: SharedPreferences) : ViewModel() {
    private var navRepository: AdminNavRepository? = null
    var navItems: LiveData<List<AdminNavItem>>? = null
        private set

    val _navItemsAdded = MutableLiveData<Boolean>().apply { postValue(false) }
    val navItemsAdded: LiveData<Boolean> = _navItemsAdded

    init {

    }

    fun init(origin: Int?) {
        if (this.navItems != null) {
            return
        }

        Amplify.Auth.getCurrentUser(
            {
                viewModelScope.launch {
                    navRepository = AdminNavRepository(origin, preferences, true)
                    navItems = navRepository?.liveItems
                    _navItemsAdded.postValue(true)
                }
            },
            {
                viewModelScope.launch {
                    navRepository = AdminNavRepository(origin, preferences, false)
                    navItems = navRepository?.liveItems
                    _navItemsAdded.postValue(true)
                }
            }
        )

        navRepository = AdminNavRepository(origin, preferences, false)
        navItems = navRepository?.liveItems
    }

    fun swap(list: List<AdminNavItem>) = navRepository?.swap(list)
}