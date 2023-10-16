package com.hepimusic.main.explore

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.ViewCompat
import androidx.databinding.BindingAdapter
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.navigation.Navigation
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.hepimusic.BR
import com.hepimusic.R
import com.hepimusic.common.crossFadeWidth
import com.hepimusic.databinding.FragmentExploreBinding
import com.hepimusic.main.albums.Album
import com.hepimusic.main.common.callbacks.OnItemClickListener
import com.hepimusic.main.common.view.BaseAdapter
import com.hepimusic.main.songs.SongsViewModel
import com.hepimusic.models.mappers.toAlbum
import com.hepimusic.ui.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.coroutines.coroutineContext

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ExploreFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
@AndroidEntryPoint
class ExploreFragment : Fragment(), OnItemClickListener {

    @Inject lateinit var glide: RequestManager

    private var albums: List<Album> = emptyList()
    private var playedList: List<RecentlyPlayed> = emptyList()
    private val viewModel  by activityViewModels<ExploreViewModel>() //

    lateinit var binding: FragmentExploreBinding

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /*requireActivity().also { fragmentActivity ->
            val actvty = fragmentActivity as MainActivity
            val sngsVM: SongsViewModel = actvty.songsViewModel
            actvty.observeViewModel()
            Log.e("VIEWMODEL FROM ACTIVITY", "VIEWMODEL FROM ACTIVITY: "+actvty.songsViewModel.items.value?.size.toString())
            sngsVM.items.observe(fragmentActivity){
                if (it.isNotEmpty()){
                    Log.e("VIEWMODEL FROM ACTIVITY2", "VIEWMODEL FROM ACTIVITY2: "+actvty.songsViewModel.items.value?.size.toString())
                }
            }

        }*/

        Log.d("EXPLORE FRAGMENT EV", viewModel.toString())

//        viewModel = ViewModelProvider(this).get(ExploreViewModel::class.java)
        binding = FragmentExploreBinding.inflate(LayoutInflater.from(requireContext()))
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setupViews()
        observeViewModel()
        // Inflate the layout for this fragment
        return binding.root // inflater.inflate(R.layout.fragment_explore, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        /*setupViews()
        observeViewModel()*/
        binding.navigationIcon.setOnClickListener(
            Navigation.createNavigateOnClickListener(
                R.id.action_exploreFragment_to_navigationDialogFragment
            )
        )
    }

    override fun onStart() {
        super.onStart()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ExploreFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ExploreFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

    }

    @Suppress("UNCHECKED_CAST")
    private fun observeViewModel() {
        // We want to load the random albums before the recently played

        fun observePlayed() {
            viewModel.recentlyPlayed.observe(viewLifecycleOwner, Observer {
                if (playedList.isEmpty()) {
                    // We only want to play the animation the first time a non empty result is returned
                    if (it.isEmpty()) {
                        binding.emptyPlaylist.crossFadeWidth(binding.progressBar)
                        return@Observer
                    } else {
                        val otherView = if (binding.emptyPlaylist.visibility == View.VISIBLE) binding.emptyPlaylist else binding.progressBar
                        binding.playedRV.crossFadeWidth(otherView)
                    }
                }
                playedList = it
                playedList.forEachIndexed { index, played -> played.isPlaying = index == 0 }
                (binding.playedRV.adapter as BaseAdapter<RecentlyPlayed>).updateItems(playedList)
            })
        }

        if (albums.isEmpty()) {
//            viewModel.init()
            /*if ((viewModel.items.value?.size ?: 0) == 0) {
                suspend fun load() {
                    if (viewModel.isBrowserInitialized()) {
                        lifecycleScope.launch(Dispatchers.IO) {
                            viewModel.loadData()
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
                }
            }*/
            /*viewModel.items.observe(requireActivity()) { list ->
                if (list.isEmpty()) {
                    viewModel.isBrowserConnected.observe(requireActivity()) { connected ->
                        if (connected) {
                            viewModel.loadData("[albumID]")
                        }
                    }
                }
            }*/
            viewModel.items.observe(viewLifecycleOwner, Observer {
                albums = it
                (binding.randomAlbumsRV.adapter as BaseAdapter<Album>).updateItems(albums)
                observePlayed()
            })
            /*viewModel.isBrowserConnected.observe(viewLifecycleOwner, Observer { connected ->
                if (connected) {
                    viewModel.items.observe(viewLifecycleOwner, Observe/*viewModel.isBrowserConnected.observe(viewLifecycleOwner, Observer { connected ->
                if (connected) {
                    viewModel.items.observe(viewLifecycleOwner, Observer {
                        albums = it
                        (binding.randomAlbumsRV.adapter as BaseAdapter<Album>).updateItems(albums)
                        observePlayed()
                    })
                }
            })*/r {
                        albums = it
                        (binding.randomAlbumsRV.adapter as BaseAdapter<Album>).updateItems(albums)
                        observePlayed()
                    })
                }
            })*/
            /*viewModel.items.observe(viewLifecycleOwner, Observer {
                albums = it
                (binding.randomAlbumsRV.adapter as BaseAdapter<Album>).updateItems(albums)
                observePlayed()
            })*/
        } else {
            viewModel.overrideCurrentItems(albums)
        }
    }

    private fun setupViews() {
        val albumAdapter = BaseAdapter(
            albums, requireActivity(), R.layout.item_album, BR.album, this,
            setOf(R.anim.fast_fade_in), true
        )
        binding.randomAlbumsRV.adapter = albumAdapter
        val layoutManager = LinearLayoutManager(activity, RecyclerView.HORIZONTAL, false)
        binding.randomAlbumsRV.layoutManager = layoutManager

        val playedAdapter =
            BaseAdapter(playedList,
                requireActivity(), R.layout.item_recently, BR.recentlyPlayed, animSet = null)
        binding.playedRV.adapter = playedAdapter
        binding.playedRV.layoutManager = LinearLayoutManager(activity)
        binding.scrollView.isNestedScrollingEnabled = true
    }

    override fun onItemClick(position: Int, sharableView: View?) {
        Log.e("CLICKED", albums[position].name.toString())
        val transitionName = ViewCompat.getTransitionName(sharableView!!)!!
        val extras = FragmentNavigator.Extras.Builder()
            .addSharedElement(sharableView, transitionName)
            .build()
        val action =
            ExploreFragmentDirections.actionExploreFragmentToAlbumSongsFragment(albums[position], transitionName)
        findNavController().navigate(action, extras)
    }

    override fun onItemLongClick(position: Int) {
        val action =
            ExploreFragmentDirections.actionExploreFragmentToAlbumsMenuBottomSheetDialogFragment(album = albums[position])
        findNavController().navigate(action)
    }


    override fun onDestroyView() {
        binding.randomAlbumsRV.adapter = null
        binding.playedRV.adapter = null
        super.onDestroyView()
    }
}