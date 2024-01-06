package com.hepimusic.main.profile

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
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
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.hepimusic.R
import com.hepimusic.common.crossFadeWidth
import com.hepimusic.common.px
import com.hepimusic.databinding.FragmentWriteProfieDialogBinding
import com.hepimusic.main.common.dataBinding.DataBindingAdapters
import com.hepimusic.main.common.utils.Utils
import com.hepimusic.main.common.view.BaseFullscreenDialogFragment
import java.io.File

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [WriteProfieDialogFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class WriteProfieDialogFragment : BaseFullscreenDialogFragment(), View.OnClickListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private val permissionRequestExternalStorage = 0
    private val imageRequestCode = 1
    private var tempThumbUri: Uri? = null
    private var profile: Profile? = null
    lateinit var viewModel: WriteProfileViewModel
    lateinit var profileViewModel: ProfileViewModel
    var deleteImageFile = false

    lateinit var binding: FragmentWriteProfieDialogBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        profile = arguments?.getParcelable("profile")
        viewModel = ViewModelProvider(requireActivity())[WriteProfileViewModel::class.java]
        profileViewModel = ViewModelProvider(requireActivity())[ProfileViewModel::class.java]
        viewModel.originalProfile.postValue(profile?.originalProfile)
        binding = FragmentWriteProfieDialogBinding.inflate(LayoutInflater.from(requireContext()))
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        Log.e("PROFILE", profile?.originalProfile?.name.toString())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return binding.root // inflater.inflate(R.layout.fragment_write_profie_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.originalProfile.observe(viewLifecycleOwner) {
            profile = Profile(it)
            binding.displayNameField.setText(profile!!.originalProfile.name)
            binding.emailField.setText(profile!!.originalProfile.email)
            binding.phoneField.setText(profile!!.originalProfile.phoneNumber)
        }

        viewModel.uploadProgress.observe(viewLifecycleOwner) {
            binding.progressCount.text = it
        }

        viewModel.data.observe(viewLifecycleOwner, Observer {
            if (it.success || it.message == null) {
                Utils.vibrateAfterAction(activity)
                viewModel.clearResult(R.string.saved)
                viewModel.uploadProgress.postValue("")
                findNavController().popBackStack()
            } else {
                Toast.makeText(activity, it.message, Toast.LENGTH_SHORT).show()
                binding.writeProfile.visibility = View.VISIBLE
                binding.progressBar.visibility = View.GONE
                binding.progressCount.visibility = View.GONE
                viewModel.uploadProgress.postValue("")
            }
        })
    }

    private fun setupViews() {
        binding.closeButton.setOnClickListener(this)
        binding.writeProfile.setOnClickListener(this)
        binding.clickableThumbBackground.setOnClickListener(this)
        binding.removePicture.setOnClickListener(this)
        binding.displayNameField.addTextChangedListener(textWatcher)
        binding.emailField.addTextChangedListener(textWatcher)
        binding.phoneField.addTextChangedListener(textWatcher)
        binding.phoneField.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                writeProfile()
                binding.writeProfile.visibility = View.INVISIBLE
                binding.progressBar.visibility = View.VISIBLE
                binding.progressCount.visibility = View.VISIBLE
                return@setOnEditorActionListener true
            }

            return@setOnEditorActionListener false
        }

        if (profile != null) {
            binding.writeProfile.setText(R.string.save_changes)
            binding.displayNameField.setText(profile!!.originalProfile.name)
            binding.emailField.setText(profile!!.originalProfile.email)
            binding.phoneField.setText(profile!!.originalProfile.phoneNumber)
            displayImage(Profile(profile!!.originalProfile)/*.modForViewWidth(getPlaylistArtWidth())*/)
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment WriteProfieDialogFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            WriteProfieDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.closeButton -> findNavController().popBackStack()
            R.id.writeProfile -> {
                writeProfile()
                binding.writeProfile.visibility = View.INVISIBLE
                binding.progressBar.visibility = View.VISIBLE
                binding.progressCount.visibility = View.VISIBLE
            }
            R.id.clickableThumbBackground -> checkForReadStoragePermission()
            R.id.removePicture -> removeSelectedImage()
        }
    }

    private fun removeSelectedImage() {
        tempThumbUri = null
        deleteImageFile = true
        enableWriteButton()
        displayImage(null)
    }

    private fun checkForReadStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasStoragePermission = isPermissionGranted(android.Manifest.permission.READ_MEDIA_IMAGES) && isPermissionGranted(android.Manifest.permission.READ_MEDIA_AUDIO);
            if (hasStoragePermission) {
                launchImagePickerIntent()
            } else {
                requestPermissions(
                    arrayOf(android.Manifest.permission.READ_MEDIA_IMAGES),
                    permissionRequestExternalStorage
                )
            }
        } else {
            val hasStoragePermission =
                isPermissionGranted(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (hasStoragePermission) {
                launchImagePickerIntent()
            } else {
                requestPermissions(
                    arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    permissionRequestExternalStorage
                )
            }
        }
    }

    private fun launchImagePickerIntent() {
        val pickImageIntent = Intent(Intent.ACTION_GET_CONTENT)
        pickImageIntent.type = "image/*"

        startActivityForResult(pickImageIntent, imageRequestCode)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == permissionRequestExternalStorage) {
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission request was granted
                launchImagePickerIntent()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == imageRequestCode && resultCode == Activity.RESULT_OK && data != null) {
            val uri = data.data
            if (uri != null) {
                tempThumbUri = uri
                if (profile != null && !profile!!.originalProfile.imageKey.isNullOrEmpty()) {
                    deleteImageFile = true
                }
                displayImage(tempThumbUri!!)
            }
            enableWriteButton()
        }
    }

    private fun writeProfile() {
        if (!validateData()) {
            return
        }
        val profileName = binding.displayNameField.text.toString().trim()
        val profileEmail = binding.emailField.text.toString().trim()
        val profilePhone = binding.phoneField.text.toString().trim()
        if (profile == null) {
            viewModel.createProfile(profileName, profileEmail, profilePhone, tempThumbUri, profileViewModel.userId.value!!)
        } else {
            viewModel.editProfile(profileName, profileEmail, profilePhone,
                profile!!.originalProfile, tempThumbUri, deleteImageFile)
        }
    }

    private val thumbLoadListener = object : RequestListener<Drawable> {
        override fun onLoadFailed(
            e: GlideException?,
            model: Any?,
            target: Target<Drawable>?,
            isFirstResource: Boolean
        ): Boolean {
            binding.unselectedImageGroup.crossFadeWidth(binding.profilePictureArt)
            if (binding.removePicture.visibility == View.VISIBLE || binding.uploadPicture.visibility != View.VISIBLE) {
                binding.uploadPicture.crossFadeWidth(binding.removePicture)
            }
            return true
        }

        override fun onResourceReady(
            resource: Drawable?,
            model: Any?,
            target: Target<Drawable>?,
            dataSource: DataSource?,
            isFirstResource: Boolean
        ): Boolean {
            binding.profilePictureArt.crossFadeWidth(binding.unselectedImageGroup, visibility = View.INVISIBLE)
            // Check if tt was loaded from a selected image
            if (model is Uri
                || (profile != null
                        && !profile!!.originalProfile.imageKey.isNullOrEmpty())
            ) {
                binding.removePicture.crossFadeWidth(binding.uploadPicture, visibility = View.INVISIBLE)
            }
            return false
        }

    }

    private fun displayImage(any: Any?) {
        if (any !is Profile) {
            Glide.with(this)
                .load(any)
                .transform(
                    MultiTransformation(DataBindingAdapters.centerCrop, RoundedCorners(7.px))
                ).listener(thumbLoadListener)
                .into(binding.profilePictureArt)
        } else {
            val uri = FileProvider.getUriForFile(requireContext().applicationContext, "${requireContext().applicationContext.packageName}.provider", File(
                requireContext().applicationContext.filesDir,
                any.originalProfile.name ?: any.originalProfile.key
            )
            )
            Glide.with(requireContext())
                .setDefaultRequestOptions(
                    RequestOptions()
                        .placeholder(R.drawable.profile_person)
                        .error(R.drawable.profile_person)
                        .diskCacheStrategy(DiskCacheStrategy.DATA)
                )
                .load(/*playlist.modForViewWidth(view.measuredWidth)*/Uri.parse("https://dn1i8z7909ivj.cloudfront.net/"+any.originalProfile.imageKey))
                .transform(
                    MultiTransformation(
                        DataBindingAdapters.centerCrop,
                        DataBindingAdapters.circleCrop
                    )
                )
                .placeholder(R.drawable.profile_person)
                .listener(thumbLoadListener)
                .into(binding.profilePictureArt)
        }
    }

    private fun validateData(showToast: Boolean = true): Boolean {
        val profileName = binding.displayNameField.text.toString()
        val profileEmail = binding.emailField.text.toString()
        val profilePhone = binding.phoneField.text.toString()
        @StringRes var res: Int? = null
        if (profileName.trim().isEmpty() && profileEmail.trim().isEmpty() && profilePhone.trim().isEmpty()) {
            res = R.string.profile_empty_message
        } else if (profile != null) {
            if (tempThumbUri == null && profileName.trim() == profile!!.originalProfile.name && profileEmail.trim() == profile!!.originalProfile.email && profilePhone.trim() == profile!!.originalProfile.phoneNumber && !deleteImageFile) {
                res = R.string.profile_empty_thumb_message
            }
        }

        if (res == null) return true

        if (showToast) Toast.makeText(activity, res, Toast.LENGTH_SHORT).show()

        return false
    }

    private fun enableWriteButton() {
        binding.writeProfile.isEnabled = validateData(false)
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
}