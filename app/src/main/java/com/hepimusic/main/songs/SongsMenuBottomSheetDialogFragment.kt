package com.hepimusic.main.songs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.hepimusic.R
import com.hepimusic.databinding.FragmentSongsMenuBottomSheetDialogBinding
import com.hepimusic.main.common.view.BaseMenuBottomSheet
import com.hepimusic.playback.PlaybackViewModel

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SongsMenuBottomSheetDialogFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SongsMenuBottomSheetDialogFragment : BaseMenuBottomSheet() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var song: Song? = null
    private var mediaId: String = ""
    @IdRes
    private var popUpTo: Int = 0

    lateinit var binding: FragmentSongsMenuBottomSheetDialogBinding
    lateinit var playbackViewModel: PlaybackViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        playbackViewModel = ViewModelProvider(requireActivity())[PlaybackViewModel::class.java]
        binding = FragmentSongsMenuBottomSheetDialogBinding.inflate(LayoutInflater.from(requireContext()))
        mediaId = requireArguments().getString("mediaId").toString()
        popUpTo = requireArguments().getInt("popUpTo")
        song = requireArguments().getParcelable("song")!!
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
        return binding.root // inflater.inflate(R.layout.fragment_songs_menu_bottom_sheet_dialog, container, false)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.share -> shareTrack()
            R.id.playNext -> playNextTrack()
            R.id.favourite -> favouriteTrack()
            R.id.addToPlayList -> addTrackToPlayList()
            R.id.delete -> deleteTrack()
        }
    }

    private fun deleteTrack() {
        // TODO: Implement
    }

    private fun addTrackToPlayList() {
        /*val selection = "$basicSongsSelection AND ${MediaStore.Audio.Media._ID} = ?"
        val selectionArgs = arrayOf(basicSongsSelectionArg, mediaId.toString())*/
        val action = SongsMenuBottomSheetDialogFragmentDirections
            .actionSongsMenuBottomSheetDialogFragmentToAddSongsToPlaylistsFragment(/*selectionArgs*/null, /*selection*/null, song = song)
        val navOptions = NavOptions.Builder().setPopUpTo(popUpTo, false).build()
        findNavController().navigate(action, navOptions)
    }

    private fun favouriteTrack() {
        // TODO: Implement
    }

    private fun shareTrack() {
        // TODO: Implement
    }

    private fun playNextTrack() {
        // TODO: Implement
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment SongsMenuBottomSheetDialogFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SongsMenuBottomSheetDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}