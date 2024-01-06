package com.hepimusic.main.requests.players

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.hepimusic.R
import com.hepimusic.databinding.FragmentRequestPlaylistMenuBottomSheetDialogBinding
import com.hepimusic.databinding.FragmentRequestSongsMenuBottomSheetDialogBinding
import com.hepimusic.main.common.view.BaseMenuBottomSheet
import com.hepimusic.main.profile.ProfileViewModel
import com.hepimusic.main.requests.RequestsViewModel

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [RequestPlaylistMenuBottomSheetDialogFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RequestPlaylistMenuBottomSheetDialogFragment : BaseMenuBottomSheet() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var binding: FragmentRequestPlaylistMenuBottomSheetDialogBinding
    private lateinit var viewModel: RequestsViewModel
    private lateinit var profileViewModel: ProfileViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentRequestPlaylistMenuBottomSheetDialogBinding.inflate(LayoutInflater.from(requireContext()))
        viewModel = ViewModelProvider(requireActivity())[RequestsViewModel::class.java]
        profileViewModel = ViewModelProvider(requireActivity())[ProfileViewModel::class.java]
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
            R.layout.fragment_request_playlist_menu_bottom_sheet_dialog,
            container,
            false
        )*/
        return binding.root
    }

    override fun onClick(v: View?) {

    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment RequestPlaylistMenuBottomSheetDialogFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            RequestPlaylistMenuBottomSheetDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}