package com.hepimusic.main.admin.categories

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
import com.hepimusic.main.admin.albums.AdminAlbumsFragmentDirections
import com.hepimusic.main.admin.albums.AdminAlbumsViewModel
import com.hepimusic.main.admin.albums.Album
import com.hepimusic.main.admin.common.BaseAdminAdapter
import com.hepimusic.main.admin.common.BaseAdminFragment
import com.hepimusic.main.admin.creators.Creator

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AdminCategoriesFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AdminCategoriesFragment : BaseAdminFragment<Category>() {
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
        viewModel = ViewModelProvider(requireActivity())[AdminCategoriesViewModel::class.java]
        super.onViewCreated(view, savedInstanceState)
        (viewModel as AdminCategoriesViewModel).getObservable().editClicked.observe(viewLifecycleOwner) {
            if (it) {
                val categoryToEdit = (viewModel as AdminCategoriesViewModel).getObservable().categoryToEdit.value
                if (categoryToEdit != null) {
                    findNavController().safeNavigate(
                        AdminCategoriesFragmentDirections.actionAdminCategoriesFragmentToAdminWriteCategoryDialogFragment(
                            Category(categoryToEdit.key, categoryToEdit)
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
            val categories = viewModel.categories.value
            val filteredCategories = categories?.filter { it.originalCategory.name?.contains(inputText, true) == true }
            if (!filteredCategories.isNullOrEmpty()) {
                (binding.dataRV.adapter as BaseAdminAdapter<Category>).updateItems(filteredCategories)
                binding.dataNum.text = resources.getQuantityString(numberOfDataRes, filteredCategories.count(), filteredCategories.count())
            } else {
                (binding.dataRV.adapter as BaseAdminAdapter<Category>).updateItems(emptyList())
                binding.dataNum.text = resources.getQuantityString(numberOfDataRes, 0, 0)
            }
            if (inputText.isEmpty()) {
                (binding.dataRV.adapter as BaseAdminAdapter<Category>).updateItems(categories ?: emptyList())
                binding.dataNum.text = resources.getQuantityString(numberOfDataRes, categories?.count() ?: 0, categories?.count() ?: 0)
            }
        }

        override fun afterTextChanged(s: Editable?) {

        }
    }

    override fun onResume() {
        super.onResume()
        val vm = viewModel as AdminCategoriesViewModel
        vm.getObservable()._categoryToEdit.postValue(null)
        vm.getObservable().catname.postValue(null)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.addNewButton -> {
                (viewModel as AdminCategoriesViewModel).getObservable()._categoryToEdit.postValue(null)
                findNavController().safeNavigate(
                    AdminCategoriesFragmentDirections.actionAdminCategoriesFragmentToAdminWriteCategoryDialogFragment()
                )
            }
        }
    }

    override fun onItemClick(position: Int, sharableView: View?) {
        Log.e("MEDIA URI", items[position].originalCategory.name)
        val action =
            AdminCategoriesFragmentDirections.actionAdminCategoriesFragmentToAdminCategoriesMenuBottomSheetDialogFragment(
                mediaId = items[position].id!!, category = items[position])
        findNavController().safeNavigate(action)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment AdminCategoriesFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AdminCategoriesFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override var navigationFragmentId: Int = R.id.action_adminCategoriesFragment_to_adminDashboardFragment
    override var currentNavigationFragmentId: Int = R.id.adminCategoriesFragment
    override var itemLayoutId: Int = R.layout.item_admin_category
    override var clazz: Class<Category> = Category::class.java
    override var parentId: String = ""
    override var viewModelVariableId: Int = BR.adminCategory
    override var numberOfDataRes: Int = R.plurals.numberOfCategories
    override var titleRes: Int = R.string.admin_categories
}