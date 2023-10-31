package com.hepimusic.main.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.FragmentNavigator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.hepimusic.BR
import com.hepimusic.R
import com.hepimusic.databinding.FragmentResultsBinding
import com.hepimusic.main.albums.Album
import com.hepimusic.main.albums.albumItemAnimSet
import com.hepimusic.main.artists.Artist
import com.hepimusic.main.common.callbacks.OnItemClickListener
import com.hepimusic.main.common.data.Model
import com.hepimusic.main.common.view.BaseAdapter
import com.hepimusic.main.genres.Genre
import com.hepimusic.main.playlist.Playlist
import com.hepimusic.main.songs.Song
import com.hepimusic.models.mappers.toMediaItem
import com.hepimusic.playback.PlaybackViewModel

private const val RESULT = "RESULT"


class ResultsFragment<T : Model> : Fragment(), OnItemClickListener {

    lateinit var result: Result
    var items = emptyList<Model>()
    private lateinit var viewModel: SearchViewModel // by sharedViewModel()
    private lateinit var playbackViewModel: PlaybackViewModel
    lateinit var binding: FragmentResultsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentResultsBinding.inflate(LayoutInflater.from(requireContext()))
        viewModel = ViewModelProvider(requireActivity())[SearchViewModel::class.java]
        playbackViewModel = ViewModelProvider(requireActivity())[PlaybackViewModel::class.java]
        result = requireArguments().getParcelable(RESULT)!!
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return binding.root // inflater.inflate(R.layout.fragment_results, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        observeViewModel()
    }

    private fun observeViewModel() {
        when (result.type) {
            Type.Songs -> viewModel.songsResults.observe(viewLifecycleOwner, Observer {
                items = it
                updateData()
            })
            Type.Albums -> viewModel.albumsResults.observe(viewLifecycleOwner, Observer {
                items = it
                updateData()
            })
            Type.Artists -> viewModel.artistsResults.observe(viewLifecycleOwner, Observer {
                items = it
                updateData()
            })
            Type.Genres -> viewModel.genresResults.observe(viewLifecycleOwner, Observer {
                items = it
                updateData()
            })
            Type.Playlists -> viewModel.playlistResults.observe(viewLifecycleOwner, Observer {
                items = it
                updateData()
            })

            else -> {}
        }
    }

    private fun setupViews() {
        var layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(requireActivity())
        val adapter = when (result.type) {
            Type.Songs -> BaseAdapter(items, requireActivity(), R.layout.item_song, BR.song, this)
            Type.Albums -> {
                layoutManager = FlexboxLayoutManager(activity).apply {
                    flexDirection = FlexDirection.ROW
                    justifyContent = JustifyContent.SPACE_EVENLY
                }
                BaseAdapter(items, requireActivity(), R.layout.item_album, BR.album, this, animSet = albumItemAnimSet, longClick = true)
            }
            Type.Artists -> BaseAdapter(items, requireActivity(), R.layout.item_artist, BR.artist, this)
            Type.Genres -> BaseAdapter(items, requireActivity(), R.layout.item_genre, BR.genre, this, longClick = true)
            Type.Playlists -> BaseAdapter(
                items, requireActivity(), R.layout.item_playlist, BR.playlist, this, longClick = true
            )

            else -> null
        }
        binding.resultsRV.adapter = adapter
        binding.resultsRV.layoutManager = layoutManager
    }

    @Suppress("UNCHECKED_CAST")
    private fun updateData() = (binding.resultsRV.adapter as BaseAdapter<T>).updateItems(items as List<T>)

    override fun onItemClick(position: Int, sharableView: View?) {
        when (result.type) {
            Type.Songs -> onSongItemClick(position)
            Type.Albums -> onAlbumItemClick(position, sharableView)
            Type.Artists -> onArtistItemClick(position, sharableView)
            Type.Genres -> onGenreItemClick(position, sharableView)
            Type.Playlists -> onPlaylistItemClick(position, sharableView)
            else -> {}
        }
    }

    override fun onItemLongClick(position: Int) {
        when (result.type) {
            Type.Albums -> onAlbumItemLongClick(position)
            Type.Genres -> onGenreItemLongClick(position)
            Type.Playlists -> onPlaylistItemLongClick(position)
            else -> Unit
        }
    }

    override fun onOverflowMenuClick(position: Int) {
        when (result.type) {
            Type.Songs -> onSongItemOverflowMenuClick(position)
            else -> Unit
        }
    }

    private fun onPlaylistItemLongClick(position: Int) {
        val directions =
            SearchFragmentDirections
                .actionSearchFragmentToPlaylistMenuBottomSheetDialogFragment(playlist = items[position] as Playlist)
        viewModel.navigateFrmSearchFragment(SearchNavigation(directions))
    }

    private fun onAlbumItemLongClick(position: Int) {
        val directions =
            SearchFragmentDirections
                .actionSearchFragmentToAlbumsMenuBottomSheetDialogFragment(album = items[position] as Album)
        viewModel.navigateFrmSearchFragment(SearchNavigation(directions))
    }

    private fun onGenreItemLongClick(position: Int) {
        val directions =
            SearchFragmentDirections
                .actionSearchFragmentToGenresMenuBottomSheetDialogFragment(genre = items[position] as Genre)
        viewModel.navigateFrmSearchFragment(SearchNavigation(directions))
    }

    private fun onSongItemOverflowMenuClick(position: Int) {
        val directions =
            SearchFragmentDirections
                .actionSearchFragmentToSongsMenuBottomSheetDialogFragment(mediaId = items[position].id as String, song = items[position] as Song)
        viewModel.navigateFrmSearchFragment(SearchNavigation(directions))
    }


    private fun onPlaylistItemClick(position: Int, sharableView: View?) {
        val transitionName = ViewCompat.getTransitionName(sharableView!!)!!

        val extras = FragmentNavigator.Extras.Builder()
            .addSharedElement(sharableView, transitionName)
            .build()

        val directions = SearchFragmentDirections.actionSearchFragmentToPlaylistSongsFragment(
            transitionName,
            items[position] as Playlist
        )
        viewModel.navigateFrmSearchFragment(SearchNavigation(directions, extras))
    }

    private fun onGenreItemClick(position: Int, sharableView: View?) {
        val transitionName = ViewCompat.getTransitionName(sharableView!!)!!

        val directions =
            SearchFragmentDirections.actionSearchFragmentToGenreSongsFragment(
                items[position] as Genre,
                transitionName
            )

        val extras = FragmentNavigator.Extras.Builder()
            .addSharedElement(sharableView, transitionName)
            .build()

        viewModel.navigateFrmSearchFragment(SearchNavigation(directions, extras))
    }

    private fun onArtistItemClick(position: Int, sharableView: View?) {
        val transitionName = ViewCompat.getTransitionName(sharableView!!)!!

        val directions = SearchFragmentDirections
            .actionSearchFragmentToArtistAlbumsFragment(items[position] as Artist, transitionName)

        val extras = FragmentNavigator.Extras.Builder()
            .addSharedElement(sharableView, transitionName)
            .build()

        viewModel.navigateFrmSearchFragment(SearchNavigation(directions, extras))
    }

    private fun onAlbumItemClick(position: Int, sharableView: View?) {
        val transitionName = ViewCompat.getTransitionName(sharableView!!)!!

        val directions = SearchFragmentDirections
            .actionSearchFragmentToAlbumSongsFragment(items[position] as Album, transitionName)

        val extras = FragmentNavigator.Extras.Builder()
            .addSharedElement(sharableView, transitionName)
            .build()

        viewModel.navigateFrmSearchFragment(SearchNavigation(directions, extras))
    }

    private fun onSongItemClick(position: Int) {
        playbackViewModel.playAll(items[position].id as String, items.map { (it as Song).toMediaItem() })
        //To change body of created functions use File | Settings | File Templates.
    }


    companion object {
        @JvmStatic fun <T : Model> newInstance(result: Result) =
            ResultsFragment<T>().apply {
                arguments = Bundle().apply {
                    putParcelable(RESULT, result)
                }
            }
    }
}