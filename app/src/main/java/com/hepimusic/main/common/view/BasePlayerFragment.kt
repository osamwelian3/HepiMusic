package com.hepimusic.main.common.view

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
import com.hepimusic.common.safeNavigationOnClickListener
import com.hepimusic.databinding.FragmentBasePlayerBinding
import com.hepimusic.main.common.callbacks.OnItemClickListener
import com.hepimusic.main.common.data.Model
import com.hepimusic.main.songs.Song
import com.hepimusic.main.songs.SongsViewModel
import com.hepimusic.models.mappers.toMediaItem
import com.hepimusic.playback.PlaybackViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

abstract class BasePlayerFragment<T : Model> : BaseFragment(), View.OnClickListener,
    OnItemClickListener {
    lateinit var binding: FragmentBasePlayerBinding
    lateinit var playbackViewModel: PlaybackViewModel // by activityViewModels<PlaybackViewModel>() // viewModels<PlaybackViewModel>()
    var items = emptyList<T>()
    lateinit var viewModel: BaseMediaStoreViewModel<T>
    @get: IdRes
    abstract var navigationFragmentId: Int
    abstract var currentNavigationFragmentId: Int
    @get: PluralsRes
    open var numberOfDataRes: Int = -1
    @get: StringRes
    open var titleRes: Int = -1
    @get: LayoutRes
    abstract var itemLayoutId: Int
    abstract var parentId: String
    abstract var viewModelVariableId: Int
    open var adapterItemAnimSet = setOf(R.anim.up_from_bottom, R.anim.down_from_top)
    open var longClickItems = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentBasePlayerBinding.inflate(LayoutInflater.from(requireContext()))
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
        playbackViewModel = ViewModelProvider(requireActivity()).get(PlaybackViewModel::class.java)
        setupView()
        observeViewModel()
        /*playbackViewModel.isBrowserConnected.observe(viewLifecycleOwner) { connected ->
            if (connected) {
                playbackViewModel.currentItem.observe(viewLifecycleOwner) {

                }
            }
        }*/
        binding.navigationIcon.setOnClickListener(
            Navigation.safeNavigationOnClickListener(
                currentNavigationFragmentId,
                navigationFragmentId
            )
        )
        binding.playButton.setOnClickListener(this)
    }

    private fun observeViewModel() {
        if (items.isEmpty()) {
            if ((viewModel.items.value?.size ?: 0) == 0) {
                suspend fun load() {
                    if (viewModel.isBrowserInitialized()) {
                        viewModel.loadData(parentId)
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
            }

            viewModel.isBrowserConnected.observe(viewLifecycleOwner){ connected ->
                if (connected) {
//                    playbackViewModel.browser = viewModel.browser
                    viewModel.init()
                    viewModel.items.observe(viewLifecycleOwner) { list ->
                        if (list.isNotEmpty()) {
                            viewModel.items.value?.first()?.id?.let {
                                if (parentId != "[albumID]" && it.toString().contains("[album]")) {
                                    Log.e("ID", it.toString())
                                    viewModel.loadData(parentId)
                                }
                            }
                        }
                    }
                    viewModel.items.observe(viewLifecycleOwner, Observer(::updateViews))
                }
            }

            viewModel.isControllerConnected.observe(viewLifecycleOwner){ connected ->
                if (connected) {
                    playbackViewModel.controller = viewModel.controller
                }
            }

        } else {
            viewModel.overrideCurrentItems(items)
        }
    }


    @Suppress("UNCHECKED_CAST")
    private fun updateViews(items: List<T>) {
        this.items = items
        (binding.dataRV.adapter as BaseAdapter<T>).updateItems(items)
        binding.dataNum.text = resources.getQuantityString(numberOfDataRes, items.count(), items.count())
    }

    @SuppressLint("ResourceType")
    private fun setupView() {
        if (titleRes > -1) {
            binding.sectionTitle.setText(titleRes)
        }
        val adapter =
            BaseAdapter(
                items, requireActivity(), itemLayoutId, viewModelVariableId, this, null,
                adapterItemAnimSet, longClickItems
            )
        binding.dataRV.adapter = adapter
        binding.dataRV.layoutManager = layoutManager()
    }

    open fun layoutManager(): RecyclerView.LayoutManager {
        return LinearLayoutManager(activity)
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.playButton -> playbackViewModel.playAll("", (items as List<Song>).map { it.toMediaItem() })
        }
    }

    // Derived classed will be forced to implemebt this
    abstract override fun onItemClick(position: Int, sharableView: View?)

}