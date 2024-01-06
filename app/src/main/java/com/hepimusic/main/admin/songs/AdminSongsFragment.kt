package com.hepimusic.main.admin.songs

import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.hepimusic.BR
import com.hepimusic.R
import com.hepimusic.common.Constants
import com.hepimusic.common.safeNavigate
import com.hepimusic.main.admin.common.BaseAdminAdapter
import com.hepimusic.main.admin.common.BaseAdminFragment
import com.hepimusic.models.mappers.toMediaItem

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AdminSongsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AdminSongsFragment() : BaseAdminFragment<Song>() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    lateinit var preferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferences = requireActivity().getSharedPreferences("main", MODE_PRIVATE)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel = ViewModelProvider(requireActivity())[AdminSongsViewModel::class.java]
        super.onViewCreated(view, savedInstanceState)
        (viewModel as AdminSongsViewModel).getObservable().editClicked.observe(viewLifecycleOwner) {
            if (it) {
                val songToEdit = (viewModel as AdminSongsViewModel).getObservable().songToEdit.value
                if (songToEdit != null) {
                    findNavController().safeNavigate(
                        AdminSongsFragmentDirections.actionAdminSongsFragmentToAdminWriteSongDialogFragment(
                            Song(songToEdit.key, songToEdit)
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
            val songs = viewModel.songs.value
            val filteredSongs = songs?.filter { it.originalSong.name?.contains(inputText, true) == true || it.originalSong.selectedCreator?.contains(inputText, true) == true || it.originalSong.selectedCategory?.contains(inputText, true) == true }
            if (!filteredSongs.isNullOrEmpty()) {
                (binding.dataRV.adapter as BaseAdminAdapter<Song>).updateItems(filteredSongs)
                binding.dataNum.text = resources.getQuantityString(numberOfDataRes, filteredSongs.count(), filteredSongs.count())
            }
            if (inputText.isEmpty()) {
                (binding.dataRV.adapter as BaseAdminAdapter<Song>).updateItems(songs!!)
                binding.dataNum.text = resources.getQuantityString(numberOfDataRes, songs.count(), songs.count())
            }
        }

        override fun afterTextChanged(s: Editable?) {

        }
    }

    override fun onResume() {
        super.onResume()
        val vm = viewModel as AdminSongsViewModel
        vm.getObservable()._songToEdit.postValue(null)
        vm.getObservable().nname.postValue(null)
        vm.getObservable().albumName = ""
        vm.getObservable().creatorName = ""
        vm.getObservable().categoryName = ""
        vm.getObservable().sselectedCategory.postValue(null)
        vm.getObservable()._selectedCreator.postValue(null)
        vm.getObservable()._partOf.postValue(null)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.addNewButton -> {
                (viewModel as AdminSongsViewModel).getObservable()._songToEdit.postValue(null)
                findNavController().safeNavigate(
                    AdminSongsFragmentDirections.actionAdminSongsFragmentToAdminWriteSongDialogFragment()
                )
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun onItemClick(position: Int, sharableView: View?) {
        preferences.edit().putString(Constants.LAST_PARENT_ID, "[allSongsID]").apply()
        val songList = items.map { it.originalSong.toMediaItem() }
        playbackViewModel.playAll("[item]"+items[position].originalSong.key!!, songList)
    }

    @Suppress("UNCHECKED_CAST")
    override fun onOverflowMenuClick(position: Int) {
        Log.e("MEDIA URI", items[position].originalSong.fileKey)
        val action =
            AdminSongsFragmentDirections.actionAdminSongsFragmentToAdminSongsMenuBottomSheetDialogFragment(
                mediaId = "[item]"+items[position].originalSong.key!!, song = items[position])
        findNavController().safeNavigate(action)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment AdminSongsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AdminSongsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override var navigationFragmentId: Int = R.id.action_adminSongsFragment_to_adminDashboardFragment
    override var currentNavigationFragmentId: Int = R.id.adminSongsFragment
    override var itemLayoutId: Int = R.layout.item_admin_song
    override var clazz: Class<Song> = Song::class.java
    override var parentId: String = ""
    override var viewModelVariableId: Int = BR.song
    override var numberOfDataRes: Int = R.plurals.numberOfSongs
    override var titleRes: Int = R.string.admin_songs
}