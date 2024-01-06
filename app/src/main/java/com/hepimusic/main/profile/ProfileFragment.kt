package com.hepimusic.main.profile

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.PopupWindow
import androidx.annotation.DimenRes
import androidx.compose.ui.graphics.Color
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amplifyframework.core.Amplify
import com.amplifyframework.datastore.generated.model.Song
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.hepimusic.BR
import com.hepimusic.R
import com.hepimusic.common.safeNavigate
import com.hepimusic.common.safeNavigationOnClickListener
import com.hepimusic.databinding.FragmentProfileBinding
import com.hepimusic.databinding.ItemOuterRvBinding
import com.hepimusic.main.common.callbacks.OnItemClickListener
import com.hepimusic.main.common.dataBinding.DataBindingAdapters
import com.hepimusic.main.common.view.BaseAdapter
import com.hepimusic.main.common.view.OuterAdapter
import com.hepimusic.models.mappers.toSong

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProfileFragment : Fragment(), OnItemClickListener, View.OnClickListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var favouritesList = emptyList<com.hepimusic.main.songs.Song>()
    private var favouritesListDS = emptyList<Song>()

    lateinit var binding: FragmentProfileBinding
    lateinit var profileViewModel: ProfileViewModel
    lateinit var displayIn: ItemOuterRvBinding
    var chunkPosition = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        profileViewModel = ViewModelProvider(requireActivity())[ProfileViewModel::class.java]
        binding = FragmentProfileBinding.inflate(LayoutInflater.from(requireContext()))
        displayIn = ItemOuterRvBinding.inflate(LayoutInflater.from(requireContext()))
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

        binding.backButton.setOnClickListener(this)
        binding.navigationIcon.setOnClickListener(
            Navigation.safeNavigationOnClickListener(
                R.id.profileFragement,
                R.id.action_profileFragment_to_navigationDialogFragment
            )
        )

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding.let {
            it.viewModel = profileViewModel
            return it.root
        } // inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        observeViewModel()
    }

    @Suppress("UNCHECKED_CAST")
    private fun observeViewModel() {
        if (favouritesList.isEmpty()) {
            profileViewModel.favourites.observe(viewLifecycleOwner) { favList ->
                favouritesList = favList.map { it.toSong() }
                favouritesListDS = favList
                (binding.favouritesRV.adapter as OuterAdapter<com.hepimusic.main.songs.Song>).updateItems(
                    favouritesList
                )
            }
        } else {
            profileViewModel.overrideCurrentItems(favouritesListDS)
        }

        profileViewModel.profile.observe(viewLifecycleOwner) {
            Glide.with(requireContext())
                .setDefaultRequestOptions(
                    RequestOptions()
                        .placeholder(R.drawable.profile_person)
                        .error(R.drawable.profile_person)
                        .diskCacheStrategy(DiskCacheStrategy.DATA)
                )
                .load(Uri.parse("https://dn1i8z7909ivj.cloudfront.net/"+it.originalProfile.imageKey))
                .transform(
                    MultiTransformation(DataBindingAdapters.centerCrop, DataBindingAdapters.circleCrop)
                )
                .placeholder(R.drawable.profile_person)
                .into(binding.profilePicture)

            if (!it.originalProfile.name.isNullOrEmpty()) binding.userNameDisplay.text = it.originalProfile.name
        }
    }

    private fun setupViews() {
        val adapter = BaseAdapter(emptyList(), requireActivity(), R.layout.item_favourite, BR.song, itemClickListener = this, mediaitemClicked = ::trendingitemClicked, animSet = null, longClick = true)
        displayIn.outerRV.adapter = adapter
        displayIn.outerRV.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)

        val favouritesAdapter =
            OuterAdapter(favouritesList,
                requireActivity(), R.layout.item_favourite, BR.song, itemClickListener = this, mediaitemClicked = ::trendingitemClicked, animSet = null, longClick = true, displayIn = displayIn.outerRV, outerLayoutId = R.layout.item_outer_rv, itemBoundBinding = displayIn, chunkSize = 3)
        binding.favouritesRV.adapter = favouritesAdapter
        binding.favouritesRV.layoutManager = LinearLayoutManager(activity, RecyclerView.HORIZONTAL, false)
        binding.scrollView.isNestedScrollingEnabled = true

        binding.editProfile.setOnClickListener(this)
        binding.viewMore.setOnClickListener(this)
        if (profileViewModel.profile.value != null) {
            binding.userNameDisplay.text = profileViewModel.profile.value!!.originalProfile.name ?: binding.userNameDisplay.text

            Glide.with(requireContext())
                .setDefaultRequestOptions(
                    RequestOptions()
                        .placeholder(R.drawable.profile_person)
                        .error(R.drawable.profile_person)
                        .diskCacheStrategy(DiskCacheStrategy.DATA)
                )
                .load(Uri.parse("https://dn1i8z7909ivj.cloudfront.net/"+profileViewModel.profile.value!!.originalProfile.imageKey))
                .transform(
                    MultiTransformation(DataBindingAdapters.centerCrop, DataBindingAdapters.circleCrop)
                )
                .placeholder(R.drawable.profile_person)
                .into(binding.profilePicture)

        }
    }

    fun trendingitemClicked(i: Int, view: View?) {
        Log.e("CHUNK POSITION", chunkPosition.toString())
        Log.e("ITEM POSITION", i.toString())
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ProfileFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ProfileFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.backButton.id -> findNavController().popBackStack()
            binding.editProfile.id -> findNavController().safeNavigate(
                ProfileFragmentDirections.actionProfileFragmentToWriteProfieDialogFragment(profileViewModel.profile.value)
            )
            binding.viewMore.id -> findNavController().safeNavigate(
                ProfileFragmentDirections.actionProfileFragmentToProfileMenuBottomSheetDialogFragment(profileViewModel.profile.value!!)
            )
        }
    }

    override fun onItemClick(position: Int, sharableView: View?) {
        chunkPosition = position
    }

    override fun onReactionButtonLongClick(position: Int, rawX: Float, rawY: Float) {
        super.onReactionButtonLongClick(position, rawX, rawY)
        showReactionButtons(rawX, rawY)
    }

    private fun showReactionButtons(rawX: Float, rawY: Float) {
        Log.e("rawX and rawY", "rawX - $rawX and rawY - $rawY")
        val inflater = requireActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.reactions_popup, null)
        val popUpWindow = PopupWindow(
            view,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )

        popUpWindow.isOutsideTouchable = true
        popUpWindow.isFocusable = true

        popUpWindow.setBackgroundDrawable(ColorDrawable(Color.White.hashCode()))

        val xOffset = (rawX - popUpWindow.width-100).toInt()
        val yOffset = (rawY - popUpWindow.height-100).toInt()

        popUpWindow.showAtLocation(view, Gravity.NO_GRAVITY, xOffset, yOffset)

        view.findViewById<ImageButton>(R.id.upVoteReaction).setOnClickListener{
            Log.e("UPVOTE POPUP", "Upvote reaction clicked")
            popUpWindow.dismiss()
        }
        view.findViewById<ImageButton>(R.id.downVoteReaction).setOnClickListener{
            Log.e("DOWNVOTE POPUP", "Downvote reaction clicked")
            popUpWindow.dismiss()
        }
    }
}
