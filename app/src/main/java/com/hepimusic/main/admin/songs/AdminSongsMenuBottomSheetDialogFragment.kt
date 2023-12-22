package com.hepimusic.main.admin.songs

import android.app.AlertDialog
import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.IdRes
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.amplifyframework.core.Amplify
import com.hepimusic.R
import com.hepimusic.databinding.FragmentAdminSongsMenuBottomSheetDialogBinding
import com.hepimusic.databinding.FragmentSongsMenuBottomSheetDialogBinding
import com.hepimusic.main.common.utils.Utils
import com.hepimusic.main.common.view.BaseMenuBottomSheet
import com.hepimusic.main.playlist.WritePlaylistViewModel
import com.hepimusic.main.songs.SongsMenuBottomSheetDialogFragment
import com.hepimusic.main.songs.SongsMenuBottomSheetDialogFragmentDirections
import com.hepimusic.playback.PlaybackViewModel

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AdminSongsMenuBottomSheetDialogFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AdminSongsMenuBottomSheetDialogFragment : BaseMenuBottomSheet() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var song: Song? = null
    private var mediaId: String = ""
    @IdRes
    private var popUpTo: Int = 0

    lateinit var binding: FragmentAdminSongsMenuBottomSheetDialogBinding
    lateinit var playbackViewModel: PlaybackViewModel
    lateinit var adminSongsViewModel: AdminSongsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        playbackViewModel = ViewModelProvider(requireActivity())[PlaybackViewModel::class.java]
        adminSongsViewModel = ViewModelProvider(requireActivity())[AdminSongsViewModel::class.java]
        binding = FragmentAdminSongsMenuBottomSheetDialogBinding.inflate(LayoutInflater.from(requireContext()))
        mediaId = requireArguments().getString("mediaId").toString()
        popUpTo = requireArguments().getInt("popUpTo")
        song = requireArguments().getParcelable("song")!!
        adminSongsViewModel.getObservable()._songToEdit.postValue(song!!.originalSong)
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

    override fun onDestroyView() {
        super.onDestroyView()
        val vm = adminSongsViewModel
        vm.getObservable()._songToEdit.postValue(null)
        vm.getObservable().nname.postValue(null)
        vm.getObservable().albumName = ""
        vm.getObservable().creatorName = ""
        vm.getObservable().categoryName = ""
        vm.getObservable().sselectedCategory.postValue(null)
        vm.getObservable()._selectedCreator.postValue(null)
        vm.getObservable()._partOf.postValue(null)
        vm.getObservable()._imageUri.postValue(null)
        vm.getObservable()._fileUri.postValue(null)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.editSong -> editTrack()
            R.id.delete -> deleteTrack()
        }
    }

    private fun showDeleteConfirmation() {
        fun deletePlaylist(dialog: DialogInterface?) {
            val viewModel = ViewModelProvider(requireActivity())[AdminSongsViewModel::class.java]

            viewModel.getObservable().data.observe(viewLifecycleOwner, Observer {
                val activity = activity
                if (activity != null && !isDetached) {
                    val message = if (it.success) {
                        Utils.vibrateAfterAction(activity)
                        getString(R.string.sth_deleted, song!!.originalSong.name)
                    } else {
                        getString(it.message!!)
                    }
                    Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
                }

                dialog?.dismiss()
                findNavController().popBackStack()
            })

            adminSongsViewModel.getObservable().deleteSong()
            binding.delete.setText("Deleting... Please Wait!! ")
            binding.delete.isEnabled = false
            binding.delete.setTextColor(Color.RED)
        }

        val builder = AlertDialog.Builder(requireActivity())
            .setMessage(requireActivity().getString(R.string.song_delete_message, song!!.originalSong.name))
            .setNegativeButton(R.string.no_thanks) { dialog, _ -> dialog.dismiss() }
            .setPositiveButton(R.string.ok) { dialog, _ -> deletePlaylist(dialog) }

        val dialog = builder.create()
        dialog.window?.attributes?.windowAnimations = R.style.Theme_HepiMusic_DialogAnimation
        dialog.show()
    }

    private fun deleteTrack() {
        showDeleteConfirmation()
    }

    private fun editTrack() {
        /*findNavController().navigate(
            AdminSongsMenuBottomSheetDialogFragmentDirections.actionAdminSongsMenuBottomSheetDialogFragmentToAdminWriteSongDialogFragment(song)
        )*/
        adminSongsViewModel.getObservable()._editClicked.postValue(true)
        findNavController().popBackStack()
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
            AdminSongsMenuBottomSheetDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}