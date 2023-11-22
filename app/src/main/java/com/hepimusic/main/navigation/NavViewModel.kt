package com.hepimusic.main.navigation

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
class NavViewModel @Inject constructor(private val preferences: SharedPreferences) : ViewModel() {
    private var navRepository: NavRepository? = null
    var navItems: LiveData<List<NavItem>>? = null
        private set

    val _navItemsAdded = MutableLiveData<Boolean>().apply { value = false }
    val navItemsAdded: LiveData<Boolean> = _navItemsAdded

    fun init(origin: Int?) {
        if (this.navItems != null) {
            return
        }

        Amplify.Auth.getCurrentUser(
            {
                viewModelScope.launch {
                    navRepository = NavRepository(origin, preferences, true)
                    navItems = navRepository?.liveItems
                    _navItemsAdded.postValue(true)
                }
            },
            {
                viewModelScope.launch {
                    navRepository = NavRepository(origin, preferences, false)
                    navItems = navRepository?.liveItems
                    _navItemsAdded.postValue(true)
                }
            }
        )

        navRepository = NavRepository(origin, preferences, false)
        navItems = navRepository?.liveItems
    }

    fun swap(list: List<NavItem>) = navRepository?.swap(list)
}