package com.hepimusic.main.admin.albums

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
import androidx.navigation.fragment.findNavController
import com.hepimusic.R
import com.hepimusic.databinding.FragmentAdminAlbumsMenuBottomSheetDialogBinding
import com.hepimusic.databinding.FragmentAdminCreatorsMenuBottomSheetDialogBinding
import com.hepimusic.main.admin.creators.AdminCreatorsViewModel
import com.hepimusic.main.admin.creators.Creator
import com.hepimusic.main.common.utils.Utils
import com.hepimusic.main.common.view.BaseMenuBottomSheet

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AdminAlbumsMenuBottomSheetDialogFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AdminAlbumsMenuBottomSheetDialogFragment : BaseMenuBottomSheet() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var album: Album? = null
    private var mediaId: String = ""
    @IdRes
    private var popUpTo: Int = 0

    lateinit var binding: FragmentAdminAlbumsMenuBottomSheetDialogBinding
    lateinit var viewModel: AdminAlbumsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentAdminAlbumsMenuBottomSheetDialogBinding.inflate(LayoutInflater.from(requireContext()))
        viewModel = ViewModelProvider(requireActivity())[AdminAlbumsViewModel::class.java]
        mediaId = requireArguments().getString("mediaId").toString()
        popUpTo = requireArguments().getInt("popUpTo")
        album = requireArguments().getParcelable("album")!!
        viewModel.getObservable()._albumToEdit.postValue(album!!.originalAlbum)
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
        /*return inflater.inflate(
            R.layout.fragment_admin_albums_menu_bottom_sheet_dialog,
            container,
            false
        )*/
        return binding.root
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.editAlbum.id -> editAlbum()
            binding.delete.id -> deleteAlbum()
        }
    }

    private fun editAlbum() {
        viewModel.getObservable()._editClicked.postValue(true)
        findNavController().popBackStack()
    }

    private fun deleteAlbum() {
        showDeleteConfirmation()
    }

    private fun showDeleteConfirmation() {
        fun deletePlaylist(dialog: DialogInterface?) {

            viewModel.getObservable().data.observe(viewLifecycleOwner, Observer {
                val activity = activity
                if (activity != null && !isDetached) {
                    val message = if (it.success) {
                        Utils.vibrateAfterAction(activity)
                        getString(R.string.sth_deleted, album!!.originalAlbum.name)
                    } else {
                        getString(it.message!!)
                    }
                    Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
                }

                dialog?.dismiss()
                findNavController().popBackStack()
            })

            viewModel.getObservable().deleteAlbum()
            binding.delete.setText("Deleting... Please Wait!! ")
            binding.delete.isEnabled = false
            binding.delete.setTextColor(Color.RED)
        }

        val builder = AlertDialog.Builder(requireActivity())
            .setMessage(requireActivity().getString(R.string.song_delete_message, album!!.originalAlbum.name))
            .setNegativeButton(R.string.no_thanks) { dialog, _ -> dialog.dismiss() }
            .setPositiveButton(R.string.ok) { dialog, _ -> deletePlaylist(dialog) }

        val dialog = builder.create()
        dialog.window?.attributes?.windowAnimations = R.style.Theme_HepiMusic_DialogAnimation
        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        val vm = viewModel
        vm.getObservable()._albumToEdit.postValue(null)
        vm.getObservable().alname.postValue(null)
        vm.getObservable()._imageUri.postValue(null)
        vm.getObservable()._deleteOldImage.postValue(null)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment AdminAlbumsMenuBottomSheetDialogFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AdminAlbumsMenuBottomSheetDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}