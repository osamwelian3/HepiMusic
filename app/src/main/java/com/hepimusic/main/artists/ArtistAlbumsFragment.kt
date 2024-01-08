package com.hepimusic.main.artists

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.fragment.findNavController
import androidx.transition.TransitionInflater
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.hepimusic.BR
import com.hepimusic.R
import com.hepimusic.common.px
import com.hepimusic.common.safeNavigate
import com.hepimusic.common.safeNavigationOnClickListener
import com.hepimusic.databinding.FragmentArtistAlbumsBinding
import com.hepimusic.main.albums.Album
import com.hepimusic.main.common.callbacks.OnItemClickListener
import com.hepimusic.main.common.view.BaseAdapter
import com.hepimusic.main.common.view.BaseFragment
import com.hepimusic.main.songs.Song

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ArtistAlbumsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ArtistAlbumsFragment : BaseFragment(), OnItemClickListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    lateinit var artist: Artist
    lateinit var viewModel: ArtistAlbumsViewModel
    private var albums = emptyList<Album>()
    private var songs = emptyList<Song>()
    lateinit var binding: FragmentArtistAlbumsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentArtistAlbumsBinding.inflate(LayoutInflater.from(requireContext()))
        sharedElementEnterTransition = TransitionInflater.from(requireContext()).inflateTransition(android.R.transition.move)
        artist = requireArguments().getParcelable("artist")!!
        viewModel = ViewModelProvider(requireActivity())[ArtistAlbumsViewModel::class.java]
        viewModel.init(artist.id, artist)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding.artist = artist
        // Inflate the layout for this fragment
        return binding.root // inflater.inflate(R.layout.fragment_artist_albums, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ViewCompat.setTransitionName(binding.sectionTitle, requireArguments().getString("transitionName"))
        setupRecyclerView()
        observeViewModel()
        binding.sectionBackButton.setOnClickListener { findNavController().popBackStack() }
        binding.navigationIcon.setOnClickListener(
            Navigation.safeNavigationOnClickListener(
                R.id.artistAlbumsFragment,
                R.id.action_artistAlbumsFragment_to_navigationDialogFragment
            )
        )
    }

    private fun observeViewModel() {
        viewModel.items.observe(viewLifecycleOwner, Observer(this::updateViews))
    }

    @Suppress("UNCHECKED_CAST")
    private fun updateViews(albums: List<Album>) {
        if (albums.isEmpty()) {
            findNavController().popBackStack()
            return
        }
        this.albums = albums.filter { it.artist.trim() == artist.name.trim() }
        (binding.artistAlbumsRV.adapter as BaseAdapter<Album>).updateItems(this.albums)
    }

    @SuppressLint("WrongConstant")
    private fun setupRecyclerView() {
        val adapter = BaseAdapter(
            albums, requireActivity(), R.layout.item_album, BR.album, itemClickListener = this,
            longClick = true
        )
        if (artist.albumsCount == 1) {
            binding.artistAlbumsRV.setPadding(20.px, 0, 0, 0)
        }
        binding.artistAlbumsRV.adapter = adapter
        binding.artistAlbumsRV.layoutManager = FlexboxLayoutManager(activity).apply {
            flexDirection = FlexDirection.ROW
            justifyContent = if (artist.albumsCount != null && artist.albumsCount!! > 1) JustifyContent.SPACE_EVENLY else JustifyContent.FLEX_START
        }


    }

    override fun onItemClick(position: Int, sharableView: View?) {
        val transitionName = ViewCompat.getTransitionName(sharableView!!)!!
        val extras = FragmentNavigator.Extras.Builder()
            .addSharedElement(sharableView, transitionName)
            .build()
        val action =
            ArtistAlbumsFragmentDirections.actionArtistAlbumsFragmentToAlbumSongsFragment(
                albums[position],
                transitionName
            )
        findNavController().safeNavigate(action, extras)
    }

    override fun onItemLongClick(position: Int) {
        val action =
            ArtistAlbumsFragmentDirections.actionArtistAlbumsFragmentToAlbumsMenuBottomSheetDialogFragment(album = albums[position])
        findNavController().safeNavigate(action)

    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ArtistAlbumsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ArtistAlbumsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}