package com.hepimusic.main.playlist

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.TransitionInflater
import com.hepimusic.BR
import com.hepimusic.R
import com.hepimusic.common.Constants
import com.hepimusic.databinding.FragmentPlaylistSongsBinding
import com.hepimusic.main.common.callbacks.OnItemClickListener
import com.hepimusic.main.common.view.BaseAdapter
import com.hepimusic.main.common.view.BaseFragment
import com.hepimusic.main.songs.Song
import com.hepimusic.models.mappers.toMediaItem
import com.hepimusic.models.mappers.toSong
import com.hepimusic.playback.PlaybackViewModel

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [PlaylistSongsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PlaylistSongsFragment : BaseFragment(), OnItemClickListener, View.OnClickListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var binding: FragmentPlaylistSongsBinding
    private lateinit var songsViewModel: PlaylistSongsViewModel
    private lateinit var playlistViewModel: PlaylistViewModel
    private lateinit var playbackViewModel: PlaybackViewModel
    private lateinit var playlist: Playlist
    lateinit var preferences: SharedPreferences
    private var items = emptyList<Song>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferences = requireActivity().getSharedPreferences("main", Context.MODE_PRIVATE)
        binding = FragmentPlaylistSongsBinding.inflate(LayoutInflater.from(requireContext()))
        sharedElementEnterTransition = TransitionInflater.from(requireContext()).inflateTransition(android.R.transition.move)
        playlist = requireArguments().getParcelable("playlist")!!
        songsViewModel = ViewModelProvider(requireActivity())[PlaylistSongsViewModel::class.java]
        playlistViewModel = ViewModelProvider(requireActivity())[PlaylistViewModel::class.java]
        playbackViewModel = ViewModelProvider(requireActivity())[PlaybackViewModel::class.java]
        songsViewModel.init(playlist.id)
        playlistViewModel.init(playlist.id)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding.let {
            it.playlist = playlist
            it.lifecycleOwner = requireActivity()
        }
        // Inflate the layout for this fragment
        return binding.root // inflater.inflate(R.layout.fragment_playlist_songs, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ViewCompat.setTransitionName(binding.playlistArt, requireArguments().getString("transitionName"))
        setupViews()
        observeViewModel()
    }

    private fun observeViewModel() {
        songsViewModel.playlistItems.observe(viewLifecycleOwner, Observer { playlistItems ->
            updateViews(playlistItems.map { it.toSong() })
        })
        playlistViewModel.playlistItems.observe(viewLifecycleOwner, Observer {
            if (it.isEmpty()) {
                Toast.makeText(requireContext(), "Playlist is empty!!!", Toast.LENGTH_LONG).show()
                findNavController().popBackStack()
            } else {
                binding.playlist = Playlist(playlist)
            }
        })
    }

    @Suppress("UNCHECKED_CAST")
    private fun updateViews(items: List<Song>) {
        this.items = items
        binding.playlistSongsDuration.text = getSongsTotalTime(items)
        (binding.playlistSongsRV.adapter as BaseAdapter<Song>).updateItems(items)
    }

    private fun setupViews() {
        val adapter = BaseAdapter(items, requireActivity(), R.layout.item_model_song, BR.song, itemClickListener = this)
        binding.playlistSongsRV.adapter = adapter
        binding.playlistSongsRV.layoutManager = LinearLayoutManager(requireActivity())
        binding.sectionBackButton.setOnClickListener(this)
        binding.moreOptions.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.sectionBackButton -> findNavController().popBackStack()
            R.id.moreOptions -> findNavController().navigate(
                PlaylistSongsFragmentDirections
                    .actionPlaylistSongsFragmentToPlaylistMenuBottomSheetDialogFragment(playlist = playlist)
            )
        }
    }


    override fun onItemClick(position: Int, sharableView: View?) {
        preferences.edit().putString(Constants.LAST_PARENT_ID, playlist.id).apply()
        playbackViewModel.playAll(items[position].id, items.map { it.toMediaItem() })
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment PlaylistSongsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            PlaylistSongsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}