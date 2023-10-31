package com.hepimusic.main.albums

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.hepimusic.R
import com.hepimusic.databinding.FragmentAlbumsMenuBottomSheetDialogBinding
import com.hepimusic.main.common.view.BaseBottomSheetDialogFragment
import com.hepimusic.main.common.view.BaseMenuBottomSheet
import com.hepimusic.playback.PlaybackViewModel

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AlbumsMenuBottomSheetDialogFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AlbumsMenuBottomSheetDialogFragment : BaseMenuBottomSheet() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    lateinit var album: Album
    @IdRes
    var popUpTo: Int = 0
    lateinit var viewModel: PlaybackViewModel // by sharedViewModel()
    lateinit var binding: FragmentAlbumsMenuBottomSheetDialogBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[PlaybackViewModel::class.java]
        binding = FragmentAlbumsMenuBottomSheetDialogBinding.inflate(LayoutInflater.from(requireContext()))
        album = requireArguments().getParcelable("album")!!
        popUpTo = requireArguments().getInt("popUpTo")
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
        return binding.root // inflater.inflate(R.layout.fragment_albums_menu_bottom_sheet_dialog, container, false)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.play -> play()
            R.id.playNext -> playNext()
            R.id.addToPlayList -> addToPlayList()
            R.id.share -> share()
        }
    }

    private fun play() {
        viewModel.playAlbum(album)
        findNavController().popBackStack()
    }

    private fun playNext() {
        findNavController().popBackStack()
    }

    private fun addToPlayList() {
        /*val selection = "$basicSongsSelection AND ${MediaStore.Audio.Media.ALBUM_ID} = ?"
        val selectionArgs = arrayOf(basicSongsSelectionArg, album.id.toString())*/
        val action = AlbumsMenuBottomSheetDialogFragmentDirections
            .actionAlbumsMenuBottomSheetDialogFragmentToAddSongsToPlaylistsFragment(/*selectionArgs*/null, /*selection*/null)
        val navOptions = NavOptions.Builder().setPopUpTo(popUpTo, false).build()

        findNavController().navigate(action, navOptions)
    }

    private fun share() {
        findNavController().popBackStack()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment AlbumsMenuBottomSheetDialogFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AlbumsMenuBottomSheetDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}