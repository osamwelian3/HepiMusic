package com.hepimusic.playback

import android.animation.AnimatorSet
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.Player
import androidx.navigation.findNavController
import androidx.navigation.fragment.FragmentNavigator
import com.hepimusic.R
import com.hepimusic.common.crossFadeWidth
import com.hepimusic.databinding.FragmentBottomPlaybackBinding
import com.hepimusic.main.common.view.BaseFragment
import com.hepimusic.ui.MainActivity
import com.hepimusic.ui.MainFragmentDirections
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
        viewModel = ViewModelProvider(requireActivity()).get(PlaybackViewModel::class.java)
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
        binding.let {
            viewModel.isBrowserConnected.observe(requireActivity()) { connected ->
                if (connected) {
                    it.viewModel = viewModel
                    it.lifecycleOwner = requireActivity()
                }
            }
            return it.root
        } // inflater.inflate(R.layout.fragment_bottom_playback, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        observeViewModel()
    }

    private fun observeViewModel() {
        var animatorSet: AnimatorSet? = null
        viewModel.playbackState.observe(viewLifecycleOwner, Observer {
            /*animatorSet?.cancel()
            animatorSet = if (it == Player.STATE_BUFFERING) {
                binding.progressBar.crossFadeWidth(binding.playButton, 1, visibility = View.INVISIBLE)
            } else {
                binding.playButton.crossFadeWidth(binding.progressBar, 1, visibility = View.INVISIBLE)
            }*/
            if (it == Player.STATE_BUFFERING) {
                binding.progressBar.visibility = View.VISIBLE
                binding.playButton.visibility = View.INVISIBLE
            } else {
                binding.playButton.visibility = View.VISIBLE
                binding.progressBar.visibility = View.INVISIBLE
            }
        })
        viewModel.isPlaying.observe(viewLifecycleOwner) {
            if (it) {
                binding.playButton.visibility = View.VISIBLE
                binding.progressBar.visibility = View.INVISIBLE
            }
        }
    }

    private fun setupViews() {
        binding.playbackSeekBar.setOnTouchListener { _, _ ->
            return@setOnTouchListener true
        }

        binding.clickableView.setOnClickListener {
            viewModel.currentItem.value?.let {
                val transitionName = ViewCompat.getTransitionName(binding.sharableView)!!
                val extras = FragmentNavigator.Extras.Builder().addSharedElement(binding.sharableView, transitionName).build()
                val action = MainFragmentDirections.actionMainFragmentToPlaybackFragment(transitionName)
                activity?.findNavController(R.id.mainNavHostFragment)?.navigate(action, extras)
                /*(requireActivity() as MainActivity).navController.navigate(action, extras)*/
            }
        }
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