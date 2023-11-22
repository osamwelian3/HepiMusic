package com.hepimusic.main.songs

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.hepimusic.BR
import com.hepimusic.R
import com.hepimusic.common.Constants
import com.hepimusic.main.common.view.BasePlayerFragment
import com.hepimusic.models.mappers.toMediaItem
import dagger.hilt.android.AndroidEntryPoint

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SongsFragment.newInstance] factory method to
 * create an instance of this fragment.long
 */
@AndroidEntryPoint
class SongsFragment : BasePlayerFragment<Song>() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    lateinit var preferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferences = requireActivity().getSharedPreferences("main", Context.MODE_PRIVATE)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        /*val vm: SongsViewModel = ViewModelProvider(requireActivity()).get(SongsViewModel::class.java)
        playbackViewModel = ViewModelProvider(requireActivity()).get(PlaybackViewModel::class.java)
        vm.items.observeForever { list ->
            if (list.isNotEmpty()){
                viewModel = vm
            }
            Log.e("SF VM", "SF VM: "+list.size.toString())
        }
        viewModel = vm*/ // ViewModelProvider(this).get(SongsViewModel::class.java)

        /*viewModel.isBrowserConnected.observe(requireActivity()){ connected ->
                if (connected) {
                    viewModel.loadData("[allSongsID]")
                    viewModel.items.observe(requireActivity()){ songList ->
                        viewModel.overrideCurrentItems(songList)
                    }
                }
            }*/
        /*lifecycleScope.launch(Dispatchers.IO) {
            suspend fun load() {
                if (playbackViewModel.isBrowserInitialized()) {
                    *//*playbackViewModel.mediaItems.observe(requireActivity()){ mediaList ->
                        mediaList.map {
                            Log.e("MEDIA ITEM: ", it.mediaMetadata.title.toString())
                        }
                    }*//*
                    viewModel.loadData()
                } else {
                    delay(2000)
                    Log.e("RETRY", "RETRY")
                    load()
                }
            }
            playbackViewModel.also {
                it.viewModelScope.launch {
                    load()
                }
            }
        }*/

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel = ViewModelProvider(requireActivity())[SongsViewModel::class.java]
        super.onViewCreated(view, savedInstanceState)

        Log.d("SONGS FRAGMENT SV", viewModel.toString())
        Log.d("SONGS FRAGMENT PV", playbackViewModel.toString())

        // viewModel.loadData()
//        Log.e("SF VM", "SF VM: "+viewModel.items.value?.size.toString())
    }

    /*override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_songs, container, false)
    }*/

    override fun onItemClick(position: Int, sharableView: View?) {
        Log.e("CLICKED", items[position].title +" "+items[position].path)
        preferences.edit().putString(Constants.LAST_PARENT_ID, "[allSongsID]").apply()
        playbackViewModel.playAll(items[position].id, items.map { it.toMediaItem() })
    }

    override fun onOverflowMenuClick(position: Int) {
        Log.e("MEDIA URI", items[position].path)
        val action =
            SongsFragmentDirections.actionSongsFragmentToSongsMenuBottomSheetDialogFragment(mediaId = items[position].id, song = items[position])
        findNavController().navigate(action)
    }

    override var itemLayoutId: Int = R.layout.item_song
    override var viewModelVariableId: Int = BR.song
    override var navigationFragmentId: Int = R.id.action_songsFragment_to_navigationDialogFragment
    override var numberOfDataRes: Int = R.plurals.numberOfSongs
    override var titleRes: Int = R.string.songs
    override var parentId: String = "[allSongsID]"

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment SongsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SongsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}