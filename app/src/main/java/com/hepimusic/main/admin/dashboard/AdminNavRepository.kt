package com.hepimusic.main.admin.dashboard

import android.content.SharedPreferences
import androidx.lifecycle.MediatorLiveData
import com.hepimusic.R
import com.hepimusic.common.Constants

class AdminNavRepository(private var origin: Int?, private val preferences: SharedPreferences, private val userExists: Boolean) {

    val liveItems = MediatorLiveData<List<AdminNavItem>>()

    init {
        val items = arrayOfNulls<AdminNavItem?>(10)
        items[preferences.getInt(Constants.ADMIN_NAV_SONGS, 0)] =
            AdminNavItem(Constants.ADMIN_NAV_SONGS, R.string.songs_admin, R.drawable.ic_song_thin, isFrom(0))
        items[preferences.getInt(Constants.ADMIN_NAV_ALBUMS, 1)] =
            AdminNavItem(Constants.ADMIN_NAV_ALBUMS, R.string.albums_admin, R.drawable.ic_waveforms, isFrom(1))
        items[preferences.getInt(Constants.ADMIN_NAV_CATEGORIES, 2)] =
            AdminNavItem(Constants.ADMIN_NAV_CATEGORIES, R.string.categories_admin, R.drawable.ic_microphone, isFrom(2))
        items[preferences.getInt(Constants.ADMIN_NAV_CREATORS, 3)] =
            AdminNavItem(Constants.ADMIN_NAV_CREATORS, R.string.creators_admin, R.drawable.profile_person, isFrom(3))

        liveItems.value = items.filterNotNull()
    }


    fun swap(list: List<AdminNavItem>) {
        val editor = preferences.edit()
        list.forEachIndexed { i, item ->
            editor.putInt(item.id, i)
        }
        editor.apply()
    }


    /**
     * Check if [NavigationDialogFragment] is launched from this [NavItem]
     * @param originalIndex the index of the item before the user modified it.
     * Corresponds to the default values of the index of the item passed to SharedPreferences
     */
    private fun isFrom(originalIndex: Int): Boolean = originalIndex == origin
}

val keys = arrayListOf(
    Constants.ADMIN_NAV_SONGS,
    Constants.ADMIN_NAV_ALBUMS,
    Constants.ADMIN_NAV_CATEGORIES,
    Constants.ADMIN_NAV_CREATORS
)