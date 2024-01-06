package com.hepimusic.main.admin.albums

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.hepimusic.BR
import com.hepimusic.R
import com.hepimusic.common.safeNavigate
import com.hepimusic.main.admin.common.BaseAdminAdapter
import com.hepimusic.main.admin.common.BaseAdminFragment
import com.hepimusic.main.admin.creators.AdminCreatorsFragmentDirections
import com.hepimusic.main.admin.creators.AdminCreatorsViewModel
import com.hepimusic.main.admin.creators.Creator

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AdminAlbumsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AdminAlbumsFragment : BaseAdminFragment<Album>() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    lateinit var preferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferences = requireActivity().getSharedPreferences("main", Context.MODE_PRIVATE)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel = ViewModelProvider(requireActivity())[AdminAlbumsViewModel::class.java]
        super.onViewCreated(view, savedInstanceState)
        (viewModel as AdminAlbumsViewModel).getObservable().editClicked.observe(viewLifecycleOwner) {
            if (it) {
                val albumToEdit = (viewModel as AdminAlbumsViewModel).getObservable().albumToEdit.value
                if (albumToEdit != null) {
                    findNavController().safeNavigate(
                        AdminAlbumsFragmentDirections.actionAdminAlbumsFragmentToAdminWriteAlbumDialogFragment(
                            Album(albumToEdit.key, albumToEdit)
                        )
                    )
                }
            }
        }

        binding.searchText.addTextChangedListener(textWatcher)
    }

    @Suppress("UNCHECKED_CAST")
    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            val inputText = s.toString()
            val albums = viewModel.albums.value
            val filteredAlbums = albums?.filter { it.originalAlbum.name?.contains(inputText, true) == true || it.originalAlbum.thumbnailKey?.contains(inputText, true) == true }
            if (!filteredAlbums.isNullOrEmpty()) {
                (binding.dataRV.adapter as BaseAdminAdapter<Album>).updateItems(filteredAlbums)
                binding.dataNum.text = resources.getQuantityString(numberOfDataRes, filteredAlbums.count(), filteredAlbums.count())
            } else {
                (binding.dataRV.adapter as BaseAdminAdapter<Album>).updateItems(emptyList())
                binding.dataNum.text = resources.getQuantityString(numberOfDataRes, 0, 0)
            }
            if (inputText.isEmpty()) {
                (binding.dataRV.adapter as BaseAdminAdapter<Album>).updateItems(albums ?: emptyList())
                binding.dataNum.text = resources.getQuantityString(numberOfDataRes, albums?.count() ?: 0, albums?.count() ?: 0)
            }
        }

        override fun afterTextChanged(s: Editable?) {

        }
    }

    override fun onResume() {
        super.onResume()
        val vm = viewModel as AdminAlbumsViewModel
        vm.getObservable()._albumToEdit.postValue(null)
        vm.getObservable().alname.postValue(null)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.addNewButton -> {
                (viewModel as AdminAlbumsViewModel).getObservable()._albumToEdit.postValue(null)
                findNavController().safeNavigate(
                    AdminAlbumsFragmentDirections.actionAdminAlbumsFragmentToAdminWriteAlbumDialogFragment()
                )
            }
        }
    }

    override fun onItemClick(position: Int, sharableView: View?) {
        Log.e("MEDIA URI", items[position].originalAlbum.thumbnailKey)
        val action =
            AdminAlbumsFragmentDirections.actionAdminAlbumsFragmentToAdminAlbumsMenuBottomSheetDialogFragment(
                mediaId = "[item]"+items[position].originalAlbum.key!!, album = items[position])
        findNavController().safeNavigate(action)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment AdminAlbumsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AdminAlbumsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override var navigationFragmentId: Int = R.id.action_adminAlbumsFragment_to_adminDashboardFragment
    override var currentNavigationFragmentId: Int = R.id.adminAlbumsFragment
    override var itemLayoutId: Int = R.layout.item_admin_album
    override var clazz: Class<Album> = Album::class.java
    override var parentId: String = ""
    override var viewModelVariableId: Int = BR.adminAlbum
    override var numberOfDataRes: Int = R.plurals.numberOfAlbums
    override var titleRes: Int = R.string.admin_albums
}