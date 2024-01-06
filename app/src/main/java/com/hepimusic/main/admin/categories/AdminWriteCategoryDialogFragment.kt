package com.hepimusic.main.admin.categories

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.hepimusic.R
import com.hepimusic.databinding.FragmentAdminWriteAlbumDialogBinding
import com.hepimusic.databinding.FragmentAdminWriteCategoryDialogBinding
import com.hepimusic.main.admin.albums.AdminAlbumsViewModel
import com.hepimusic.main.admin.albums.Album
import com.hepimusic.main.admin.creators.Creator
import com.hepimusic.main.common.utils.Utils
import com.hepimusic.main.common.view.BaseFullscreenDialogFragment

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AdminWriteCategoryDialogFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AdminWriteCategoryDialogFragment : BaseFullscreenDialogFragment(), View.OnClickListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    lateinit var binding: FragmentAdminWriteCategoryDialogBinding
    lateinit var viewModel: AdminCategoriesViewModel

    var categoriesList = emptyList<Category>()
    private var category: Category? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentAdminWriteCategoryDialogBinding.inflate(LayoutInflater.from(requireContext()))
        viewModel = ViewModelProvider(requireActivity())[AdminCategoriesViewModel::class.java]
        category = arguments?.getParcelable("category")
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
        binding.let {
            it.viewModel = viewModel.getObservable()
            return it.root
        }// return inflater.inflate(R.layout.fragment_admin_write_category_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        observeViewModel()

    }

    override fun onResume() {
        super.onResume()
        setupViews()
        observeViewModel()
    }

    fun setupViews() {
        if (category != null) {
            viewModel.getObservable()._categoryToEdit.postValue(category!!.originalCategory)
            viewModel.getObservable().name.observe(viewLifecycleOwner) {
                if (it != binding.nameField.text.toString()) {
                    binding.nameField.setText(it)
                }
            }
        }
        binding.closeButton.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.closeButton.setOnClickListener(this)
        binding.writeCategory.setOnClickListener(this)
        binding.nameField.addTextChangedListener(textWatcher)
        binding.nameField.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                writeCategory()
                binding.writeCategory.visibility = View.INVISIBLE
                binding.progressBar.visibility = View.VISIBLE
                binding.progressCount.visibility = View.VISIBLE
                return@setOnEditorActionListener true
            }

            return@setOnEditorActionListener false
        }

        if (category != null) {
            viewModel.getObservable()._categoryToEdit.postValue(category!!.originalCategory)
            binding.writeCategory.setText(R.string.save_changes)

            if (viewModel.getObservable().catname.value == null) {
                viewModel.getObservable().catname.postValue(category!!.originalCategory.name)
                binding.nameField.setText(category!!.originalCategory.name)
            } else {
                viewModel.getObservable().catname.postValue(viewModel.getObservable().catname.value)
                binding.nameField.setText(viewModel.getObservable().catname.value)
            }
        }
    }

    fun observeViewModel() {
        viewModel.getObservable().data.observe(viewLifecycleOwner, Observer {
            if (it.success || it.message == null) {
                Utils.vibrateAfterAction(activity)
                viewModel.getObservable().clearResult(R.string.saved)
                dialog?.dismiss()
            } else {
                Toast.makeText(activity, it.message, Toast.LENGTH_SHORT).show()
                binding.writeCategory.visibility = View.VISIBLE
                binding.progressBar.visibility = View.GONE
                binding.progressCount.visibility = View.GONE
            }
        })
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment AdminWriteCategoryDialogFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AdminWriteCategoryDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.closeButton.id -> findNavController().popBackStack()
            binding.writeCategory.id -> {
                writeCategory()
                binding.writeCategory.visibility = View.INVISIBLE
                binding.progressBar.visibility = View.VISIBLE
                binding.progressCount.visibility = View.VISIBLE
            }
        }
    }

    private fun writeCategory() {
        if (!validateData()) {
            return
        }
        if (category == null) {
            viewModel.getObservable().createCategory()
        } else {
            viewModel.getObservable()._categoryToEdit.postValue(category!!.originalCategory)
            viewModel.getObservable().editCategory()
        }
    }

    private fun validateData(showToast: Boolean = true): Boolean {
        val categoryName = binding.nameField.text.toString()
        @StringRes var res: Int? = null
        if (category == null && (viewModel.getObservable().catname.value.isNullOrEmpty())) {
            res = R.string.category_empty_message
        } else if (category != null) {
            if (categoryName.trim() == category!!.originalCategory.name.trim()) {
                res = R.string.category_empty_thumb_message
            }
        }

        if (res == null) return true

        if (showToast) Toast.makeText(activity, res, Toast.LENGTH_SHORT).show()

        return false
    }

    private fun enableWriteButton() {
        binding.writeCategory.isEnabled = validateData(false)
    }

    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            Log.e("TextWatcher","Before Text Change")
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            enableWriteButton()
        }

        override fun afterTextChanged(s: Editable?) {
            enableWriteButton()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        val vm = viewModel
        vm.getObservable()._categoryToEdit.postValue(null)
        vm.getObservable().catname.postValue(null)
    }
}