package com.hepimusic.main.playlist

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.collection.SparseArrayCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.hepimusic.BR
import com.hepimusic.R
import com.hepimusic.common.crossFadeWidth
import com.hepimusic.databinding.FragmentPlaylistSongsEditorDialogBinding
import com.hepimusic.main.common.callbacks.OnItemClickListener
import com.hepimusic.main.common.utils.Utils
import com.hepimusic.main.common.view.BaseAdapter
import com.hepimusic.main.common.view.BaseFullscreenDialogFragment
import com.hepimusic.main.songs.Song

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [PlaylistSongsEditorDialogFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PlaylistSongsEditorDialogFragment : BaseFullscreenDialogFragment(), OnItemClickListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    var items = emptyList<Song>()
    lateinit var viewModel: PlaylistSongsEditorViewModel
    private lateinit var playlist: Playlist
    lateinit var binding: FragmentPlaylistSongsEditorDialogBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentPlaylistSongsEditorDialogBinding.inflate(LayoutInflater.from(requireContext()))
        viewModel = ViewModelProvider(requireActivity())[PlaylistSongsEditorViewModel::class.java]
        playlist = requireArguments().getParcelable("playlist")!!
//        viewModel.init(playlist.id)
        viewModel.loadData()
        viewModel.playlist = playlist
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return binding.root // inflater.inflate(R.layout.fragment_playlist_songs_editor_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        observeViewModel()
        binding.doneButton.setOnClickListener { editPlaylist() }
    }

    private fun editPlaylist() {
        val animatorSet = binding.progressBar.crossFadeWidth(binding.doneButton, 500)
        viewModel.playlistValue.observe(viewLifecycleOwner, Observer { event ->

            event.getContentIfNotHandled()?.let {
                if (it) {
                    // Updating playlist was successful
                    Utils.vibrateAfterAction(activity)
                    findNavController().popBackStack()
                } else {
                    // Updating playlist wasn't successful;
                    if (animatorSet.isRunning) animatorSet.cancel()
                    binding.doneButton.crossFadeWidth(binding.progressBar, 500, visibility = View.INVISIBLE)
                    Toast.makeText(activity, R.string.sth_went_wrong, Toast.LENGTH_SHORT).show()
                }
            }

        })
        viewModel.updatePlaylist()

    }

    private fun observeViewModel() {
        viewModel.items.observe(viewLifecycleOwner) { songList ->
            songList.forEach {
                Log.e("RECEIVED", it.title)
            }
        }
        viewModel.items.observe(viewLifecycleOwner, Observer(::updateViews))
    }

    @Suppress("UNCHECKED_CAST")
    private fun updateViews(items: List<Song>) {
        this.items = items
        (binding.songsRV.adapter as BaseAdapter<Song>).updateItems(items)
        updateSelectedCount()
        binding.content.crossFadeWidth(binding.largeProgressBar)
    }

    private fun updateSelectedCount() {
        val selectedSongs = items.filter { it.selected }
        binding.dataNum.setText(
            resources.getQuantityString(
                R.plurals.numberOfSongsSelected,
                selectedSongs.count(),
                selectedSongs.count()
            )
        )
    }

    private fun setupView() {
        val variables = SparseArrayCompat<Any>(1)
        variables.put(BR.selectable, true)
        val adapter =
            BaseAdapter(items, requireActivity(), R.layout.item_song, BR.song, this, variables = variables)
        binding.songsRV.adapter = adapter
        binding.songsRV.layoutManager = LinearLayoutManager(activity)
    }

    /**
     * DiffUtil should handle the changes but there's an issue in [PlaylistSongsEditorViewModel.reverseSelection]
     * function that's preventing DiffUtil from detecting changes.
     *
     * The issue is that the data value in the [PlaylistSongsEditorViewModel] and [items] point to
     * the same object in memory so any item change in the values are reflected across. So by the
     * time observers are notified, the current [items] and incoming items are already the same
     * hence DiffUtil can't detect changes
     */
    @Suppress("UNCHECKED_CAST")
    override fun onItemClick(position: Int, sharableView: View?) {
        if (viewModel.reverseSelection(position)) {
            (binding.songsRV.adapter as BaseAdapter<Song>).notifyItemChanged(position, 0)
            updateSelectedCount()
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment PlaylistSongsEditorDialogFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            PlaylistSongsEditorDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}