package com.hepimusic.main.admin.songs

import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.hepimusic.BR
import com.hepimusic.R
import com.hepimusic.common.Constants
import com.hepimusic.main.admin.common.BaseAdminFragment
import com.hepimusic.main.common.view.BasePlayerFragment
import com.hepimusic.main.songs.SongsFragmentDirections
import com.hepimusic.models.mappers.toMediaItem
import com.hepimusic.models.mappers.toSong

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AdminSongsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AdminSongsFragment() : BaseAdminFragment<Song>() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    lateinit var preferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferences = requireActivity().getSharedPreferences("main", MODE_PRIVATE)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel = ViewModelProvider(requireActivity())[AdminSongsViewModel::class.java]
        super.onViewCreated(view, savedInstanceState)
    }

    @Suppress("UNCHECKED_CAST")
    override fun onItemClick(position: Int, sharableView: View?) {
        preferences.edit().putString(Constants.LAST_PARENT_ID, "[allSongsID]").apply()
        val songList = (items as List<com.hepimusic.main.songs.Song>).map { it.toMediaItem() }
        playbackViewModel.playAll((items as List<com.hepimusic.main.songs.Song>)[position].id, songList)
    }

    @Suppress("UNCHECKED_CAST")
    override fun onOverflowMenuClick(position: Int) {
        Log.e("MEDIA URI", (items as List<com.hepimusic.main.songs.Song>)[position].path)
        val action =
            AdminSongsFragmentDirections.actionAdminSongsFragmentToAdminSongsMenuBottomSheetDialogFragment(mediaId = (items as List<com.hepimusic.main.songs.Song>)[position].id, song = (items as List<com.hepimusic.main.songs.Song>)[position])
        findNavController().navigate(action)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment AdminSongsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AdminSongsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override var navigationFragmentId: Int = R.id.action_adminSongsFragment_to_adminDashboardFragment
    override var itemLayoutId: Int = R.layout.item_admin_song
    override var clazz: Class<Song> = Song::class.java
    override var parentId: String = ""
    override var viewModelVariableId: Int = BR.song
    override var numberOfDataRes: Int = R.plurals.numberOfSongs
    override var titleRes: Int = R.string.admin_songs
}