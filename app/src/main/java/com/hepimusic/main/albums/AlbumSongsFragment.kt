package com.hepimusic.main.albums

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.TransitionInflater
import com.hepimusic.BR
import com.hepimusic.R
import com.hepimusic.common.Constants
import com.hepimusic.common.safeNavigate
import com.hepimusic.databinding.FragmentAlbumSongsBinding
import com.hepimusic.main.common.callbacks.OnItemClickListener
import com.hepimusic.main.common.view.BaseAdapter
import com.hepimusic.main.common.view.BaseFragment
import com.hepimusic.main.songs.Song
import com.hepimusic.models.mappers.toMediaItem
import com.hepimusic.playback.PlaybackViewModel

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AlbumSongsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AlbumSongsFragment : BaseFragment(), OnItemClickListener, View.OnClickListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    lateinit var binding: FragmentAlbumSongsBinding

    private lateinit var viewModel: AlbumSongsViewModel
    lateinit var playbackViewModel: PlaybackViewModel // by sharedViewModel()
    private lateinit var album: Album
    private var items = emptyList<Song>()
    private val filteredItems = mutableListOf<Song>()
    lateinit var preferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferences = requireActivity().getSharedPreferences("main", Context.MODE_PRIVATE)
        sharedElementEnterTransition = TransitionInflater.from(requireContext()).inflateTransition(android.R.transition.move)
        album = requireArguments().getParcelable("album")!!
        preferences.edit().putString(Constants.CURRENT_ALBUM_ID, album.id).apply()
        playbackViewModel = ViewModelProvider(requireActivity())[PlaybackViewModel::class.java]
        viewModel = ViewModelProvider(requireActivity())[AlbumSongsViewModel::class.java]
        viewModel.init(album.id)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_album_songs, container, false)
        binding.album = album
        // Inflate the layout for this fragment
        return binding.root // inflater.inflate(R.layout.fragment_album_songs, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ViewCompat.setTransitionName(binding.albumArt, requireArguments().getString("transitionName"))
        setupViews()
        observeViewModel()
        binding.sectionBackButton.setOnClickListener { findNavController().popBackStack() }
    }

    private fun observeViewModel() {
        viewModel.albumId = album.id
        viewModel.getAlbumSongs()
        viewModel.filteredItems.observe(viewLifecycleOwner, Observer(this::updateViews))
    }

    @Suppress("UNCHECKED_CAST")
    private fun updateViews(items: List<Song>) {
        if (items.isEmpty()) {
            findNavController().popBackStack()
            return
        }
        this.items = items
        Log.e("ALBUM ITEM COUNT", items.size.toString())
        items.map {
            if (it.album as String == album.name) {
                filteredItems.add(it)
            }
        }
        binding.albumSongsDuration.text = getSongsTotalTime(filteredItems)
        (binding.albumSongsRV.adapter as BaseAdapter<Song>).updateItems(filteredItems)
    }

    private fun setupViews() {
        val adapter = BaseAdapter(items, requireActivity(), R.layout.item_album_song, BR.song, itemClickListener = this)
        binding.albumSongsRV.adapter = adapter
        binding.albumSongsRV.layoutManager = LinearLayoutManager(requireActivity())
        binding.moreOptions.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.moreOptions -> findNavController().safeNavigate(
                AlbumSongsFragmentDirections
                    .actionAlbumSongsFragmentToAlbumsMenuBottomSheetDialogFragment(album = album)
            )
        }
    }

    override fun onItemClick(position: Int, sharableView: View?) {
//        playbackViewModel.playAlbum(album, items[position].id.toString())
        preferences.edit().putString(Constants.LAST_PARENT_ID, album.id).apply()
        playbackViewModel.playAll(filteredItems[position].toMediaItem().mediaId, filteredItems.map { it.toMediaItem() })
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment AlbumSongsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AlbumSongsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onResume() {
        super.onResume()
        viewModel.startAlbumSongsJob()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.stopAlbumSongsJob()
    }
}