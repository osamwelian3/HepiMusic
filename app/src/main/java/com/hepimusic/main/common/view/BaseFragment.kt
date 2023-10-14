package com.hepimusic.main.common.view

import androidx.fragment.app.Fragment
import com.hepimusic.R
import com.hepimusic.main.common.utils.TimeUtils
import com.hepimusic.main.common.utils.Utils
import com.hepimusic.main.songs.Song
import java.util.concurrent.TimeUnit

open class BaseFragment: Fragment() {
    fun isPermissionGranted(permission: String): Boolean = Utils.isPermissionGranted(permission, activity)

    fun getSongsTotalTime(songs: List<Song>): CharSequence? {
        val secs = TimeUnit.MILLISECONDS.toSeconds(TimeUtils.getTotalSongsDuration(songs))
        return getString(
            R.string.two_comma_separated_values,
            resources.getQuantityString(R.plurals.numberOfSongs, songs.size, songs.size),
            TimeUtils.formatElapsedTime(secs, activity)
        )
    }
}