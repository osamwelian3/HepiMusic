package com.hepimusic.playback

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.hepimusic.R
import com.hepimusic.databinding.FragmentBottomPlaybackBinding
import com.hepimusic.main.common.view.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [BottomPlaybackFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
@AndroidEntryPoint
class BottomPlaybackFragment : BaseFragment() {

    lateinit var binding: FragmentBottomPlaybackBinding
    lateinit var viewModel: PlaybackViewModel
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        Snackbar.make(binding.container, viewModel.isPlaying.value.toString(), Snackbar.LENGTH_LONG).show()
//        Log.e("SNACKBAR", viewModel.isPlaying.value.toString())
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_bottom_playback, container, false)
        /*viewModel.isBrowserConnected.observe(requireActivity()){ connected ->
            if (connected){
                viewModel.currentItem.observe(requireActivity()){
                    it?.let {
                        binding.container.visibility = View.VISIBLE
                    }
                }
                viewModel.mediaItems.observe(requireActivity()){ mediaList ->
                    mediaList.map {
                        Log.e("MEDIA ITEM: ", it.mediaMetadata.title.toString())
                    }
                }
            }
        }
        suspend fun load() {
            if (viewModel.isBrowserInitialized()) {
                viewModel.mediaItems.observe(requireActivity()){ mediaList ->
                    mediaList.map {
                        Log.e("MEDIA ITEM: ", it.mediaMetadata.title.toString())
                    }
                }
            } else {
                delay(2000)
                Log.e("RETRY", "RETRY")
                load()
            }
        }
        viewModel.also {
            it.viewModelScope.launch {
                load()
            }
        }*/
        // Inflate the layout for this fragment
        return binding.root // inflater.inflate(R.layout.fragment_bottom_playback, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        viewModel = ViewModelProvider(requireActivity()).get(PlaybackViewModel::class.java)

        viewModel.isControllerInitialized.observe(requireActivity()) { connected ->
            if (connected) {
                binding.viewModel = viewModel
                binding.lifecycleOwner = requireActivity()
            }
        }
        super.onViewCreated(view, savedInstanceState)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment BottomPlaybackFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            BottomPlaybackFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}