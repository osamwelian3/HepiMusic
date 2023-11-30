package com.hepimusic.main.albums

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.hepimusic.BR
import com.hepimusic.R
import com.hepimusic.main.common.view.BasePlayerFragment

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AlbumsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AlbumsFragment : BasePlayerFragment<Album>() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    lateinit var preferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferences = requireActivity().getSharedPreferences("main", Context.MODE_PRIVATE)
        viewModel = ViewModelProvider(requireActivity())[AlbumsViewModel::class.java]
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun layoutManager(): RecyclerView.LayoutManager {
        return FlexboxLayoutManager(activity).apply {
            flexDirection = FlexDirection.ROW
            justifyContent = JustifyContent.SPACE_EVENLY
        }
    }

    override fun onItemClick(position: Int, sharableView: View?) {
        val transitionName = ViewCompat.getTransitionName(sharableView!!)!!
        val extras = FragmentNavigator.Extras.Builder()
            .addSharedElement(sharableView, transitionName)
            .build()
        val action =
            AlbumsFragmentDirections.actionAlbumsFragmentToAlbumSongsFragment(items[position], transitionName)
        findNavController().navigate(action, extras)

    }

    override fun onItemLongClick(position: Int) {
        val action =
            AlbumsFragmentDirections.actionAlbumsFragmentToAlbumsMenuBottomSheetDialogFragment(album = items[position])
        findNavController().navigate(action)
    }

    override var itemLayoutId: Int = R.layout.item_album
    override var parentId: String = "[albumID]"
    override var viewModelVariableId: Int = BR.album
    override var navigationFragmentId: Int = R.id.action_albumsFragment_to_navigationDialogFragment
    override var numberOfDataRes: Int = R.plurals.numberOfAlbums
    override var titleRes: Int = R.string.albums
    override var adapterItemAnimSet = albumItemAnimSet
    override var longClickItems: Boolean = true

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment AlbumsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AlbumsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}

val albumItemAnimSet = setOf(R.anim.fast_fade_in)