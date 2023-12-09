package com.hepimusic.main.profile

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context.MODE_PRIVATE
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.compose.material3.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.amplifyframework.core.Amplify
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.hepimusic.R
import com.hepimusic.auth.LoginActivity
import com.hepimusic.common.Constants
import com.hepimusic.databinding.FragmentPlaylistMenuBottomSheetDialogBinding
import com.hepimusic.databinding.FragmentProfileMenuBottomSheetDialogBinding
import com.hepimusic.main.common.dataBinding.DataBindingAdapters
import com.hepimusic.main.common.view.BaseMenuBottomSheet
import com.hepimusic.main.playlist.Playlist
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ProfileMenuBottomSheetDialogFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProfileMenuBottomSheetDialogFragment : BaseMenuBottomSheet() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    lateinit var profile: Profile
    @IdRes
    var popUpTo: Int = 0
    lateinit var binding: FragmentProfileMenuBottomSheetDialogBinding
    lateinit var preferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentProfileMenuBottomSheetDialogBinding.inflate(LayoutInflater.from(requireContext()))
        preferences = requireActivity().getSharedPreferences("main", MODE_PRIVATE)
        profile = requireArguments().getParcelable("profile")!!
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
        /*return inflater.inflate(
            R.layout.fragment_profile_menu_bottom_sheet_dialog,
            container,
            false
        )*/
        setupViews()
        return binding.root
    }

    fun setupViews() {
        Glide.with(requireContext())
            .setDefaultRequestOptions(
                RequestOptions()
                    .placeholder(R.drawable.profile_person)
                    .error(R.drawable.profile_person)
                    .diskCacheStrategy(DiskCacheStrategy.DATA)
            )
            .load(Uri.parse("https://dn1i8z7909ivj.cloudfront.net/"+profile.originalProfile.imageKey))
            .transform(
                MultiTransformation(DataBindingAdapters.centerCrop, DataBindingAdapters.circleCrop)
            )
            .placeholder(R.drawable.profile_person)
            .into(binding.userAvator)

        binding.userName.text = profile.originalProfile.name ?: profile.originalProfile.email
        binding.userEmail.text = if (profile.originalProfile.name == null) "" else profile.originalProfile.email
        binding.userEmail.isSelected = true
        binding.myFollowsCount.text = if (profile.originalProfile.follows != null) profile.originalProfile.follows?.size.toString() else "0"
        binding.myFollowersCount.text = if (profile.originalProfile.followers != null) profile.originalProfile.followers?.size.toString() else "0"
        // TO DO
        binding.mySongsCount.text = "0"
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.editProfile -> findNavController().navigate(
                ProfileMenuBottomSheetDialogFragmentDirections.actionProfileMenuBottomSheetDialogFragmentToWriteProfieDialogFragment(profile)
            )
            R.id.aboutHepi -> {
                val builder = AlertDialog.Builder(requireContext())
                builder.setTitle("ABOUT HEPI MUSIC")
                builder.setMessage(R.string.about_hepi)
                builder.setIcon(R.drawable.thumb_circular_default)


                val alertDialog = builder.create()
                alertDialog.setCancelable(true)
                alertDialog.show()
            }
            R.id.logout -> {
                Amplify.Auth.signOut {
                    lifecycleScope.launch(Dispatchers.Main) {
                        try {
                            findNavController().popBackStack()
                        } catch (e: Exception) {
                            e.printStackTrace()

                        }
                        preferences.edit().putBoolean(Constants.AUTH_TYPE_SOCIAL, false).apply()
                        startActivity(Intent(requireActivity(), LoginActivity::class.java))
                        requireActivity().finish()
                    }
                }
            }
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ProfileMenuBottomSheetDialogFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ProfileMenuBottomSheetDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}