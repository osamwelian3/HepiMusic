package com.hepimusic.playback

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.hepimusic.BR
import com.hepimusic.R
import com.hepimusic.databinding.FragmentCurrentSongsBinding
import com.hepimusic.main.common.callbacks.MediaItemDataDiffCallback
import com.hepimusic.main.common.callbacks.OnItemClickListener
import com.hepimusic.main.common.view.BaseAdapter
import com.hepimusic.main.common.view.BaseFragment
import com.hepimusic.models.mappers.toMediaItem
import com.hepimusic.models.mappers.toMediaItemData

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [CurrentSongsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CurrentSongsFragment : BaseFragment(), OnItemClickListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var viewModel: PlaybackViewModel // by sharedViewModel()
    lateinit var binding: FragmentCurrentSongsBinding
    private var items = emptyList<MediaItemData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentCurrentSongsBinding.inflate(LayoutInflater.from(requireContext()))
        viewModel = ViewModelProvider(requireActivity())[PlaybackViewModel::class.java]
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
        return binding.root // inflater.inflate(R.layout.fragment_current_songs, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewViews()
        observeViewModel()
    }

    private fun setupViewViews() {
        binding.currentRV.layoutManager = LinearLayoutManager(activity)
        val adapter = BaseAdapter(items, requireActivity(),R.layout.item_current, BR.mediaItem, this, null)
        binding.currentRV.adapter = adapter
    }

    @Suppress("UNCHECKED_CAST")
    private fun observeViewModel() {
        viewModel.mediaItems.observe(viewLifecycleOwner, Observer {
            Log.e("MediaItem Count", it.size.toString())
            val mediaItemData = it.map { mediaItem -> mediaItem.toMediaItemData(mediaItem.mediaId == (viewModel.currentItem.value?.mediaId ?: ""), viewModel.browser.isLoading) }
            Log.e("MediaItemData Count", mediaItemData.size.toString())
            (binding.currentRV.adapter as BaseAdapter<MediaItemData>).updateItems(mediaItemData, MediaItemDataDiffCallback(items, mediaItemData))
            items = mediaItemData
        })

        viewModel.currentItem.observe(viewLifecycleOwner, Observer {
            val index = items.indexOf(it?.toMediaItemData(it.mediaId == (viewModel.currentItem.value?.mediaId ?: ""), viewModel.browser.isLoading))
            if (index < 0) return@Observer
            binding.currentRV.scrollToPosition(index)
        })
    }


    override fun onItemClick(position: Int, sharableView: View?) = viewModel.playMediaId(items[position].toMediaItem())

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment CurrentSongsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CurrentSongsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}