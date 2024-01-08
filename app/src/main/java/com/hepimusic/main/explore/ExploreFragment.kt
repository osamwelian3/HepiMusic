package com.hepimusic.main.explore

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.RequestManager
import com.hepimusic.BR
import com.hepimusic.R
import com.hepimusic.common.Constants
import com.hepimusic.common.crossFadeWidth
import com.hepimusic.common.safeNavigate
import com.hepimusic.common.safeNavigationOnClickListener
import com.hepimusic.databinding.FragmentExploreBinding
import com.hepimusic.main.albums.Album
import com.hepimusic.main.common.callbacks.OnItemClickListener
import com.hepimusic.main.common.view.BaseAdapter
import com.hepimusic.models.mappers.toMediaItem
import com.hepimusic.models.mappers.toSong
import com.hepimusic.playback.PlaybackViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

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
class ExploreFragment : Fragment(), OnItemClickListener, SwipeRefreshLayout.OnRefreshListener {

    @Inject lateinit var glide: RequestManager

    private var albums: List<Album> = emptyList()
    private var playedList: List<RecentlyPlayed> = emptyList()
    private var trendingList: List<com.hepimusic.main.songs.Song> = emptyList()
    private val viewModel  by activityViewModels<ExploreViewModel>() //
    lateinit var playbackViewModel: PlaybackViewModel

    lateinit var binding: FragmentExploreBinding

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    lateinit var preferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferences = requireActivity().getSharedPreferences("main", Context.MODE_PRIVATE)
        playbackViewModel = ViewModelProvider(requireActivity())[PlaybackViewModel::class.java]
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
        /*setupViews()
        observeViewModel()*/
        // Inflate the layout for this fragment
        return binding.root // inflater.inflate(R.layout.fragment_explore, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        observeViewModel()
        binding.navigationIcon.setOnClickListener(
            Navigation.safeNavigationOnClickListener(
                R.id.exploreFragment,
                R.id.action_exploreFragment_to_navigationDialogFragment
            )
        )
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
        viewModel.loadData("[albumID]")
        viewModel.trendingSongs.observe(viewLifecycleOwner, Observer { trendingSongs ->
            if (trendingSongs.isNotEmpty()) {
                binding.trending.visibility = View.VISIBLE
            }
            trendingList = trendingSongs.map { it.toSong() }
            (binding.trendingRV.adapter as BaseAdapter<com.hepimusic.main.songs.Song>).updateItems(trendingList)

        })
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
                binding.playedRV.scrollToPosition(0)
                binding.swipeContainer.isRefreshing = false
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
            /*viewModel.items.observe(viewLifecycleOwner) { list ->
                if (list.isEmpty()) {
                    viewModel.isBrowserConnected.observe(viewLifecycleOwner) { connected ->
                        if (connected) {
                            viewModel.loadData("[albumID]")
                        }
                    }
                }
            }*/
            if (!viewModel.items.value.isNullOrEmpty()) {
                albums = viewModel.items.value!!
                (binding.randomAlbumsRV.adapter as BaseAdapter<Album>).updateItems(albums)
                observePlayed()
            }

            viewModel.items.observe(viewLifecycleOwner, Observer {
                if (viewModel.exploreAlbums.value != null && it.size >= viewModel.exploreAlbums.value!!.size) {
                    albums = it
                    (binding.randomAlbumsRV.adapter as BaseAdapter<Album>).updateItems(albums)
                    observePlayed()
                }
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
            binding.swipeContainer.isRefreshing = false
        }
    }

    private fun setupViews() {
        binding.swipeContainer.setOnRefreshListener(this)
        val albumAdapter = BaseAdapter(
            albums, requireActivity(), R.layout.item_album, BR.album, this, null,
            setOf(R.anim.fast_fade_in), true
        )
        binding.randomAlbumsRV.adapter = albumAdapter
        val layoutManager = LinearLayoutManager(activity, RecyclerView.HORIZONTAL, false)
        binding.randomAlbumsRV.layoutManager = layoutManager

        val playedAdapter =
            BaseAdapter(playedList,
                requireActivity(), R.layout.item_recently, BR.recentlyPlayed, mediaitemClicked = ::mediaitemClicked, animSet = null)
        binding.playedRV.adapter = playedAdapter
        binding.playedRV.layoutManager = LinearLayoutManager(activity, RecyclerView.HORIZONTAL, false)
        binding.scrollView.isNestedScrollingEnabled = true

        val trendingAdapter =
            BaseAdapter(trendingList,
                requireActivity(), R.layout.item_trending, BR.trending, mediaitemClicked = ::trendingitemClicked, animSet = null)
        binding.trendingRV.adapter = trendingAdapter
        binding.trendingRV.layoutManager = LinearLayoutManager(activity, RecyclerView.HORIZONTAL, false)
        binding.scrollView.isNestedScrollingEnabled = true
    }

    fun mediaitemClicked(position: Int, sharableView: View?) {
        Log.e("RECENT PLAY CLICKED", "POSITION: "+position.toString()+" ITEM: "+playedList[position].title)
        preferences.edit().putString(Constants.LAST_PARENT_ID, "[recentlyPlayedId]").apply()
        playbackViewModel.playAll(playedList[position].id, playedList.map { it.toMediaItem() })
    }

    fun trendingitemClicked(position: Int, sharableView: View?) {
        Log.e("TRENDING ITEM CLICKED", "POSITION: "+position.toString()+" ITEM: "+trendingList[position].artWork.toString())
        preferences.edit().putString(Constants.LAST_PARENT_ID, "[trending]").apply()
        playbackViewModel.playAll(trendingList[position].id, trendingList.map { it.toMediaItem() })
    }

    override fun onItemClick(position: Int, sharableView: View?) {
        Log.e("CLICKED", albums[position].name.toString())
        val transitionName = ViewCompat.getTransitionName(sharableView!!)!!
        val extras = FragmentNavigator.Extras.Builder()
            .addSharedElement(sharableView, transitionName)
            .build()
        val action =
            ExploreFragmentDirections.actionExploreFragmentToAlbumSongsFragment(albums[position], transitionName)
        findNavController().safeNavigate(action, extras)
    }

    override fun onItemLongClick(position: Int) {
        val action =
            ExploreFragmentDirections.actionExploreFragmentToAlbumsMenuBottomSheetDialogFragment(album = albums[position])
        findNavController().safeNavigate(action)
    }

    override fun onResume() {
        super.onResume()
        viewModel.startExploreJob()
    }

    override fun onDestroyView() {
        binding.randomAlbumsRV.adapter = null
        binding.playedRV.adapter = null
        viewModel.stopExploreJob()
        super.onDestroyView()
    }

    override fun onRefresh() {
        binding.swipeContainer.isRefreshing = true
        observeViewModel()
        viewModel.startExploreJob()
    }
}