package com.hepimusic.main.admin.common

import com.hepimusic.main.common.view.BaseAdapter
import com.hepimusic.main.common.view.BaseFragment
import com.hepimusic.main.common.view.BaseMediaStoreViewModel

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.media3.ui.PlayerView
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hepimusic.R
import com.hepimusic.databinding.FragmentBaseAdminBinding
import com.hepimusic.databinding.FragmentBasePlayerBinding
import com.hepimusic.main.admin.albums.Album
import com.hepimusic.main.admin.categories.Category
import com.hepimusic.main.common.callbacks.OnItemClickListener
import com.hepimusic.main.common.data.Model
import com.hepimusic.main.profile.Profile
import com.hepimusic.main.songs.Song
import com.hepimusic.main.songs.SongsViewModel
import com.hepimusic.models.mappers.toMediaItem
import com.hepimusic.models.mappers.toSong
import com.hepimusic.playback.PlaybackViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.reflect.typeOf

abstract class BaseAdminFragment<T : Model> : BaseFragment(), View.OnClickListener,
    OnItemClickListener {
    lateinit var binding: FragmentBaseAdminBinding
    lateinit var playbackViewModel: PlaybackViewModel // by activityViewModels<PlaybackViewModel>() // viewModels<PlaybackViewModel>()
    var songItems = emptyList<com.hepimusic.main.admin.songs.Song>()
    var albumItems = emptyList<Album>()
    var categoryItems = emptyList<Category>()
    var profileItems = emptyList<Profile>()
    var items = emptyList<T>()
    lateinit var viewModel: BaseAdminViewModel
    @get: IdRes
    abstract var navigationFragmentId: Int
    @get: PluralsRes
    open var numberOfDataRes: Int = -1
    @get: StringRes
    open var titleRes: Int = -1
    @get: LayoutRes
    abstract var itemLayoutId: Int
    abstract var clazz: Class<T>
    abstract var parentId: String
    abstract var viewModelVariableId: Int
    open var adapterItemAnimSet = setOf(R.anim.up_from_bottom, R.anim.down_from_top)
    open var longClickItems = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentBaseAdminBinding.inflate(LayoutInflater.from(requireContext()))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
//        playbackViewModel = ViewModelProvider(requireActivity()).get(PlaybackViewModel::class.java)
        /*playbackViewModel.also {
            it.viewModelScope.launch {
                playbackViewModel.initializeBrowser(requireContext().applicationContext)
                playbackViewModel.initializeController(requireContext().applicationContext)
            }
        }*/
        /*playbackViewModel.isBrowserConnected.observe(viewLifecycleOwner){ connected ->
            if (connected){
                playbackViewModel.mediaItems.observe(viewLifecycleOwner){ mediaList ->
                    mediaList.map {
                        Log.e("PLAYBACK VIEWMODEL: ", it.mediaMetadata.title.toString())
                    }
                }
            }
        }*/
//        Log.e("BPF VM", viewModel.items.value?.size.toString())
        return binding.root // inflater.inflate(R.layout.fragment_base_player, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        playbackViewModel = ViewModelProvider(requireActivity())[PlaybackViewModel::class.java]
        setupView()
        observeViewModel()
        /*playbackViewModel.isBrowserConnected.observe(viewLifecycleOwner) { connected ->
            if (connected) {
                playbackViewModel.currentItem.observe(viewLifecycleOwner) {

                }
            }
        }*/
        binding.navigationIcon.setOnClickListener(
            Navigation.createNavigateOnClickListener(
                navigationFragmentId
            )
        )
        binding.addNewButton.setOnClickListener(this)
    }

    @Suppress("UNCHECKED_CAST")
    private fun observeViewModel() {
        viewModel.songs.observe(viewLifecycleOwner) {
            songItems = it
            if (clazz == com.hepimusic.main.admin.songs.Song::class.java) {
                items = songItems as List<T> // .map { it.originalSong.toSong() } as List<T>
                updateViews(items)
            }
        }
        viewModel.albums.observe(viewLifecycleOwner) {
            albumItems = it
        }
        viewModel.categories.observe(viewLifecycleOwner) {
            categoryItems = it
        }
        viewModel.profiles.observe(viewLifecycleOwner) {
            profileItems = it
        }

    }


    @Suppress("UNCHECKED_CAST")
    private fun updateViews(items: List<T>) {
        this.items = items
        (binding.dataRV.adapter as BaseAdminAdapter<T>).updateItems(items)
        binding.dataNum.text = resources.getQuantityString(numberOfDataRes, items.count(), items.count())
    }

    @SuppressLint("ResourceType")
    private fun setupView() {
        if (titleRes > -1) {
            binding.sectionTitle.setText(titleRes)
        }
        val adapter =
            BaseAdminAdapter(
                items, requireActivity(), itemLayoutId, viewModelVariableId, this, null,
                adapterItemAnimSet, longClickItems
            )
        binding.dataRV.adapter = adapter
        binding.dataRV.layoutManager = layoutManager()
    }

    open fun layoutManager(): RecyclerView.LayoutManager {
        return LinearLayoutManager(activity)
    }


    abstract override fun onClick(v: View?)

    // Derived classed will be forced to implemebt this
    abstract override fun onItemClick(position: Int, sharableView: View?)

}