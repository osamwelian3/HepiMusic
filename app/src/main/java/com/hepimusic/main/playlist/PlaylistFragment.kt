package com.hepimusic.main.playlist

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.hepimusic.BR
import com.hepimusic.R
import com.hepimusic.common.safeNavigate
import com.hepimusic.databinding.FragmentPlaylistBinding
import com.hepimusic.main.common.callbacks.OnItemClickListener
import com.hepimusic.main.common.view.BaseAdapter
import com.hepimusic.main.common.view.BaseFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [PlaylistFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PlaylistFragment : BaseFragment(), OnItemClickListener, View.OnClickListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var items: List<Playlist> = emptyList()
    private lateinit var viewModel: PlaylistViewModel
    lateinit var binding: FragmentPlaylistBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentPlaylistBinding.inflate(LayoutInflater.from(requireContext()))
        viewModel = ViewModelProvider(requireActivity())[PlaylistViewModel::class.java]
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
        return binding.root // inflater.inflate(R.layout.fragment_playlist, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        observeViewModel()
    }

    @Suppress("UNCHECKED_CAST")
    private fun observeViewModel() {
//        viewModel.init()
        viewModel.playlists.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                this.items = it
                viewModel.playlistItems.observe(viewLifecycleOwner, Observer { pItems ->
                    for (playlist in items) {
                        if (playlist.songsCount != pItems.count { pItem -> pItem.playlistId == playlist.id }) {
                            playlist.songsCount =
                                pItems.count { pItem -> pItem.playlistId == playlist.id }
                            viewModel.also { pvm ->
                                pvm.viewModelScope.launch(Dispatchers.IO) {
                                    viewModel.playlistRepository.insert(playlist)
                                    Log.e(playlist.name, playlist.songsCount.toString())
                                }
                            }
                        }
                        (binding.playlistRV.adapter as BaseAdapter<Playlist>).updateItems(it)
                    }
                })
                /*(binding.playlistRV.adapter as BaseAdapter<Playlist>).updateItems(it)*/
            }
            updateViews()
        })
    }

    private fun updateViews() {
        if (items.isEmpty()) {
            binding.playlistsGroup.visibility = View.GONE
            binding.noPlaylistGroup.visibility = View.VISIBLE
        } else {
            binding.noPlaylistGroup.visibility = View.GONE
            binding.playlistsGroup.visibility = View.VISIBLE
            binding.playlistsNum.text = resources.getQuantityString(R.plurals.numberOfPlaylists, items.count(), items.count())
        }
    }

    private fun setupViews() {
        binding.playlistRV.adapter = BaseAdapter(
            items, requireActivity(), R.layout.item_playlist, BR.playlist, this, longClick = true
        )
        val layoutManager = LinearLayoutManager(activity)
        binding.playlistRV.layoutManager = layoutManager

        binding.navigationIcon.setOnClickListener(this)
        binding.addPlayListIcon.setOnClickListener(this)
        binding.addPlayList.setOnClickListener(this)
    }

    override fun onItemClick(position: Int, sharableView: View?) {
        val transitionName = ViewCompat.getTransitionName(sharableView!!)!!
        val extras = FragmentNavigator.Extras.Builder()
            .addSharedElement(sharableView, transitionName)
            .build()
        val action =
            PlaylistFragmentDirections.actionPlaylistFragmentToPlaylistSongsFragment(transitionName, items[position])
        findNavController().safeNavigate(action, extras)
    }

    override fun onItemLongClick(position: Int) {
        super.onItemLongClick(position)
        val action =
            PlaylistFragmentDirections.actionPlaylistFragmentToPlaylistMenuBottomSheetDialogFragment(playlist = items[position])
        findNavController().safeNavigate(action)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.navigationIcon -> findNavController().safeNavigate(R.id.playlistFragment, R.id.action_playlistFragment_to_navigationDialogFragment)
            R.id.addPlayList, R.id.addPlayListIcon -> findNavController().safeNavigate(
                R.id.playlistFragment,
                R.id.action_playlistFragment_to_writePlaylistDialogFragment
            )
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment PlaylistFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            PlaylistFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}