package com.hepimusic.main.playlist

import android.animation.AnimatorSet
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.collection.SparseArrayCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.hepimusic.BR
import com.hepimusic.R
import com.hepimusic.common.crossFadeWidth
import com.hepimusic.common.safeNavigate
import com.hepimusic.databinding.FragmentAddSongsToPlaylistsBinding
import com.hepimusic.main.common.callbacks.OnItemClickListener
import com.hepimusic.main.common.event.Event
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
 * Use the [AddSongsToPlaylistsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AddSongsToPlaylistsFragment : BaseFullscreenDialogFragment(), OnItemClickListener, View.OnClickListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var playlists = emptyList<Playlist>()
    private lateinit var viewModel: AddSongsToPlaylistsViewModel
    private var crossFadeAnimatorSet: AnimatorSet? = null

    lateinit var binding: FragmentAddSongsToPlaylistsBinding
    var song: Song? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentAddSongsToPlaylistsBinding.inflate(LayoutInflater.from(requireContext()))
        viewModel = ViewModelProvider(requireActivity())[AddSongsToPlaylistsViewModel::class.java]
        val strUri = requireArguments().getString("songsUri")
        var uri: Uri? = null; if (strUri != null) uri = Uri.parse(strUri)
        val selection = requireArguments().getString("songsSelection")
        val selectionArgs = requireArguments().getStringArray("songsSelectionArgs")
        song = requireArguments().getParcelable("song")!!
        viewModel.init(uri, selection, selectionArgs)
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
        return binding.root // inflater.inflate(R.layout.fragment_add_songs_to_playlists, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        observeViewModel()
        binding.doneButton.setOnClickListener { addToPlayList() }
    }

    private fun observeViewModel() {
        viewModel.mediatorItems.observe(viewLifecycleOwner, Observer(::updateViews))
    }

    @Suppress("UNCHECKED_CAST")
    private fun updateViews(data: Any) {
        crossFadeAnimatorSet?.cancel()
        if (data is Event<*>) {
            data.getContentIfNotHandled()?.let {
                it as InsertionResult
                crossFadeAnimatorSet = binding.doneButton.crossFadeWidth(binding.progressBar, 600, visibility = View.INVISIBLE)
                if (it.message != null) Toast.makeText(activity, it.message, Toast.LENGTH_SHORT).show()
                if (it.success == null || it.success) {
                    if (it.success == true) Utils.vibrateAfterAction(activity)
                    findNavController().popBackStack()
                }
            }
        } else {
            this.playlists = data as List<Playlist>
            (binding.playlistRV.adapter as BaseAdapter<Playlist>).updateItems(playlists)
            updateSelectedCount()
            binding.content.crossFadeWidth(binding.largeProgressBar)
        }
    }

    private fun updateSelectedCount() {
        val selectedPlaylists = playlists.filter { it.selected }
        val count = selectedPlaylists.count()
        if (count == 0) {
            crossFadeAnimatorSet?.cancel()
            crossFadeAnimatorSet = binding.unselectedButtons.crossFadeWidth(binding.doneButton, 600, visibility = View.INVISIBLE)
        } else {
            if (crossFadeAnimatorSet == null) {
                crossFadeAnimatorSet = binding.doneButton.crossFadeWidth(binding.unselectedButtons)
            } else if ((crossFadeAnimatorSet!!.isRunning) || binding.doneButton.visibility != View.VISIBLE) {
                crossFadeAnimatorSet!!.cancel()
                crossFadeAnimatorSet = binding.doneButton.crossFadeWidth(binding.unselectedButtons, 600)
            }
        }
        binding.dataNum.setText(
            resources.getQuantityString(
                R.plurals.numberOfPlaylistsSelected,
                count,
                count
            )
        )
    }

    private fun setupView() {
        val variables = SparseArrayCompat<Any>(1)
        variables.put(BR.selectable, true)
        val adapter = BaseAdapter(
            playlists, requireActivity(), R.layout.item_playlist, BR.playlist, this, variables = variables)
        binding.playlistRV.adapter = adapter
        binding.playlistRV.layoutManager = LinearLayoutManager(activity)
        binding.closeButton.setOnClickListener(this)
        binding.addPlayListIcon.setOnClickListener(this)
    }

    private fun addToPlayList() {
        crossFadeAnimatorSet?.cancel()
        binding.progressBar.crossFadeWidth(binding.doneButton, 600, visibility = View.VISIBLE)
        viewModel.addToPlaylist(song)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.closeButton -> dismissAllowingStateLoss()
            R.id.addPlayListIcon -> findNavController().safeNavigate(AddSongsToPlaylistsFragmentDirections.actionAddSongsToPlaylistsFragmentToWritePlaylistDialogFragment())
        }
    }

    /**
     * DiffUtil should handle the changes but there's an issue in [AddSongsToPlaylistsViewModel.reverseSelection]
     * function that's preventing DiffUtil from detecting changes.
     *
     * The issue is that the data value in the [AddSongsToPlaylistsViewModel] and [playlists] point to
     * the same object in memory so any item change in the values are reflected across. So by the
     * time observers are notified, the current [playlists] and incoming items are already the same
     * hence DiffUtil can't detect changes
     */
    @Suppress("UNCHECKED_CAST")
    override fun onItemClick(position: Int, sharableView: View?) {
        if (viewModel.reverseSelection(position)) {
            (binding.playlistRV.adapter as BaseAdapter<Playlist>).notifyItemChanged(position, 0)
            updateSelectedCount()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.removeSource()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment AddSongsToPlaylistsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AddSongsToPlaylistsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}