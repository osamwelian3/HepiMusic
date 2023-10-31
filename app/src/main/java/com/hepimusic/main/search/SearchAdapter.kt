package com.hepimusic.main.search

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.PagerAdapter
import com.hepimusic.main.albums.Album
import com.hepimusic.main.artists.Artist
import com.hepimusic.main.genres.Genre
import com.hepimusic.main.playlist.Playlist
import com.hepimusic.main.songs.Song

class SearchAdapter(fm: FragmentManager, private val context: Context?, private var results: List<Result>) :
    FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(position: Int): Fragment {
        val result = results.withResult[position]
        return when (result.type) {
            Type.Songs -> ResultsFragment.newInstance<Song>(result)
            Type.Albums -> ResultsFragment.newInstance<Album>(result)
            Type.Artists -> ResultsFragment.newInstance<Artist>(result)
            Type.Genres -> ResultsFragment.newInstance<Genre>(result)
            Type.Playlists -> ResultsFragment.newInstance<Playlist>(result)
            else -> ResultsFragment.newInstance<Song>(result)
        }
    }

    override fun getCount(): Int = results.withResult.size

    override fun getPageTitle(position: Int): CharSequence? = context?.getString(results.withResult[position].title)

    override fun getItemPosition(`object`: Any): Int {
        return PagerAdapter.POSITION_NONE
    }

    fun updateResults(results: List<Result>) {
        this.results = results
        notifyDataSetChanged()
    }

}

val List<Result>.withResult: List<Result> get() = this.filter { it.hasResults }
