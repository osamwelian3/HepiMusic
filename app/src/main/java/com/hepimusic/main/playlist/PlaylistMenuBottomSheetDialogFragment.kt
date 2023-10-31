package com.hepimusic.main.playlist

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.hepimusic.R
import com.hepimusic.databinding.FragmentPlaylistMenuBottomSheetDialogBinding
import com.hepimusic.main.common.utils.Utils
import com.hepimusic.main.common.view.BaseMenuBottomSheet

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [PlaylistMenuBottomSheetDialogFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PlaylistMenuBottomSheetDialogFragment : BaseMenuBottomSheet() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    lateinit var playlist: Playlist
    @IdRes
    var popUpTo: Int = 0
    lateinit var binding: FragmentPlaylistMenuBottomSheetDialogBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentPlaylistMenuBottomSheetDialogBinding.inflate(LayoutInflater.from(requireContext()))
        playlist = requireArguments().getParcelable("playlist")!!
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
        return binding.root /*inflater.inflate(
            R.layout.fragment_playlist_menu_bottom_sheet_dialog,
            container,
            false
        )*/
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.share -> sharePlaylist()
            R.id.playNext -> playPlaylistNext()
            R.id.editSongs -> editSongs()
            R.id.editPlaylist -> editPlaylist()
            R.id.delete -> showDeleteConfirmation()
        }
    }

    private fun showDeleteConfirmation() {
        fun deletePlaylist(dialog: DialogInterface?) {
            val viewModel = ViewModelProvider(requireActivity())[WritePlaylistViewModel::class.java]

            viewModel.data.observe(viewLifecycleOwner, Observer {
                val activity = activity
                if (activity != null && !isDetached) {
                    val message = if (it.success) {
                        Utils.vibrateAfterAction(activity)
                        getString(R.string.sth_deleted, playlist.name)
                    } else {
                        getString(it.message!!)
                    }
                    Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
                }

                dialog?.dismiss()
                findNavController().popBackStack()
            })

            viewModel.deletePlaylist(playlist)
        }

        val builder = AlertDialog.Builder(requireActivity())
            .setMessage(requireActivity().getString(R.string.playlist_delete_message, playlist.name))
            .setNegativeButton(R.string.no_thanks) { dialog, _ -> dialog.dismiss() }
            .setPositiveButton(R.string.ok) { dialog, _ -> deletePlaylist(dialog) }

        val dialog = builder.create()
        dialog.window?.attributes?.windowAnimations = R.style.Theme_HepiMusic_DialogAnimation
        dialog.show()
    }


    private fun editPlaylist() {
        findNavController().navigate(
            PlaylistMenuBottomSheetDialogFragmentDirections
                .actionPlaylistMenuBottomSheetDialogFragmentToWritePlaylistDialogFragment(playlist)
        )
    }

    private fun playPlaylistNext() {
        findNavController().popBackStack()
    }

    private fun sharePlaylist() {
        findNavController().popBackStack()
    }

    private fun editSongs() {
        findNavController().navigate(
            PlaylistMenuBottomSheetDialogFragmentDirections
                .actionPlaylistMenuBottomSheetDialogFragmentToPlaylistSongsEditorDialogFragment(playlist),
            NavOptions.Builder().setPopUpTo(popUpTo, false).build()
        )

    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment PlaylistMenuBottomSheetDialogFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            PlaylistMenuBottomSheetDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}