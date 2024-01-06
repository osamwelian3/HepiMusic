package com.hepimusic.main.admin.creators

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
import com.hepimusic.main.admin.songs.AdminSongsFragmentDirections
import com.hepimusic.main.admin.songs.AdminSongsViewModel
import com.hepimusic.main.admin.songs.Song

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AdminCreatorsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AdminCreatorsFragment : BaseAdminFragment<Creator>() {
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
        viewModel = ViewModelProvider(requireActivity())[AdminCreatorsViewModel::class.java]
        super.onViewCreated(view, savedInstanceState)
        (viewModel as AdminCreatorsViewModel).getObservable().editClicked.observe(viewLifecycleOwner) {
            if (it) {
                val creatorToEdit = (viewModel as AdminCreatorsViewModel).getObservable().creatorToEdit.value
                if (creatorToEdit != null) {
                    findNavController().safeNavigate(
                        AdminCreatorsFragmentDirections.actionAdminCreatorsFragmentToAdminWriteCreatorDialogFragment(
                            Creator(creatorToEdit.key, creatorToEdit)
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
            val creators = viewModel.creators.value
            val filteredCreators = creators?.filter { it.originalCreator.name?.contains(inputText, true) == true || it.originalCreator.desc?.contains(inputText, true) == true || it.originalCreator.instagram?.contains(inputText, true) == true || it.originalCreator.facebook?.contains(inputText, true) == true || it.originalCreator.twitter?.contains(inputText, true) == true || it.originalCreator.youtube?.contains(inputText, true) == true }
            if (!filteredCreators.isNullOrEmpty()) {
                (binding.dataRV.adapter as BaseAdminAdapter<Creator>).updateItems(filteredCreators)
                binding.dataNum.text = resources.getQuantityString(numberOfDataRes, filteredCreators.count(), filteredCreators.count())
            } else {
                (binding.dataRV.adapter as BaseAdminAdapter<Creator>).updateItems(emptyList())
                binding.dataNum.text = resources.getQuantityString(numberOfDataRes, 0, 0)
            }
            if (inputText.isEmpty()) {
                (binding.dataRV.adapter as BaseAdminAdapter<Creator>).updateItems(creators!!)
                binding.dataNum.text = resources.getQuantityString(numberOfDataRes, creators.count(), creators.count())
            }
        }

        override fun afterTextChanged(s: Editable?) {

        }
    }

    override fun onResume() {
        super.onResume()
        val vm = viewModel as AdminCreatorsViewModel
        vm.getObservable()._creatorToEdit.postValue(null)
        vm.getObservable().crname.postValue(null)
        vm.getObservable().ddesc.postValue(null)
        vm.getObservable().iinstagram.postValue(null)
        vm.getObservable().ffacebook.postValue(null)
        vm.getObservable().ttwitter.postValue(null)
        vm.getObservable().yyoutube.postValue(null)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.addNewButton -> {
                (viewModel as AdminCreatorsViewModel).getObservable()._creatorToEdit.postValue(null)
                findNavController().safeNavigate(
                    AdminCreatorsFragmentDirections.actionAdminCreatorsFragmentToAdminWriteCreatorDialogFragment()
                )
            }
        }
    }

    override fun onItemClick(position: Int, sharableView: View?) {
        Log.e("MEDIA URI", items[position].originalCreator.thumbnailKey)
        val action =
            AdminCreatorsFragmentDirections.actionAdminCreatorsFragmentToAdminCreatorsMenuBottomSheetDialogFragment(
                mediaId = "[item]"+items[position].originalCreator.key!!, creator = items[position])
        findNavController().safeNavigate(action)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment AdminCreatorsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AdminCreatorsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override var navigationFragmentId: Int = R.id.action_adminCreatorsFragment_to_adminDashboardFragment
    override var currentNavigationFragmentId: Int = R.id.adminCreatorsFragment
    override var itemLayoutId: Int = R.layout.item_admin_artist
    override var clazz: Class<Creator> = Creator::class.java
    override var parentId: String = ""
    override var viewModelVariableId: Int = BR.adminArtist
    override var numberOfDataRes: Int = R.plurals.numberOfCreators
    override var titleRes: Int = R.string.admin_creators
}