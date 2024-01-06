package com.hepimusic.main.admin.creators

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
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
import com.google.gson.Gson
import com.hepimusic.R
import com.hepimusic.common.crossFadeWidth
import com.hepimusic.common.px
import com.hepimusic.databinding.FragmentAdminWriteCreatorDialogBinding
import com.hepimusic.databinding.FragmentAdminWriteSongDialogBinding
import com.hepimusic.main.admin.albums.Album
import com.hepimusic.main.admin.categories.Category
import com.hepimusic.main.admin.songs.AdminSongsViewModel
import com.hepimusic.main.admin.songs.Song
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
 * Use the [AdminWriteCreatorDialogFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AdminWriteCreatorDialogFragment : BaseFullscreenDialogFragment(), View.OnClickListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private val permissionRequestExternalStorage = 0
    private val imageRequestCode = 1
    private var tempThumbUri: Uri? = null
    var deleteImageFile = false

    var fetchingFile = false

    lateinit var binding: FragmentAdminWriteCreatorDialogBinding
    lateinit var viewModel: AdminCreatorsViewModel

    var creatorList = emptyList<Creator>()
    private var creator: Creator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentAdminWriteCreatorDialogBinding.inflate(LayoutInflater.from(requireContext()))
        viewModel = ViewModelProvider(requireActivity())[AdminCreatorsViewModel::class.java]
        creator = arguments?.getParcelable("creator")
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
        }// return inflater.inflate(R.layout.fragment_admin_write_creator_dialog, container, false)
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
        if (creator != null) {
            viewModel.getObservable()._creatorToEdit.postValue(creator!!.originalCreator)
            viewModel.getObservable().name.observe(viewLifecycleOwner) {
                if (it != binding.nameField.text.toString()) {
                    binding.nameField.setText(it)
                }
            }
            viewModel.getObservable().desc.observe(viewLifecycleOwner) {
                if (it != binding.descField.text.toString()) {
                    binding.descField.setText(it)
                }
            }
            viewModel.getObservable().instagram.observe(viewLifecycleOwner) {
                if (it != binding.instagramField.text.toString()) {
                    binding.instagramField.setText(it)
                }
            }
            viewModel.getObservable().facebook.observe(viewLifecycleOwner) {
                if (it != binding.facebookField.text.toString()) {
                    binding.facebookField.setText(it)
                }
            }
            viewModel.getObservable().twitter.observe(viewLifecycleOwner) {
                if (it != binding.twitterField.text.toString()) {
                    binding.twitterField.setText(it)
                }
            }
            viewModel.getObservable().youtube.observe(viewLifecycleOwner) {
                if (it != binding.youtubeField.text.toString()) {
                    binding.youtubeField.setText(it)
                }
            }
        }
        binding.closeButton.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.closeButton.setOnClickListener(this)
        binding.writeCreator.setOnClickListener(this)
        binding.clickableThumbBackground.setOnClickListener(this)
        binding.removePicture.setOnClickListener(this)
        binding.nameField.addTextChangedListener(textWatcher)
        binding.youtubeField.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                writeCreator()
                binding.writeCreator.visibility = View.INVISIBLE
                binding.progressBar.visibility = View.VISIBLE
                binding.progressCount.visibility = View.VISIBLE
                return@setOnEditorActionListener true
            }

            return@setOnEditorActionListener false
        }

        if (creator != null) {
            viewModel.getObservable()._creatorToEdit.postValue(creator!!.originalCreator)
            binding.writeCreator.setText(R.string.save_changes)

            if (viewModel.getObservable().crname.value == null) {
                viewModel.getObservable().crname.postValue(creator!!.originalCreator.name)
                binding.nameField.setText(creator!!.originalCreator.name)
            } else {
                viewModel.getObservable().crname.postValue(viewModel.getObservable().crname.value)
                binding.nameField.setText(viewModel.getObservable().crname.value)
            }

            if (viewModel.getObservable().ddesc.value == null) {
                viewModel.getObservable().ddesc.postValue(creator!!.originalCreator.desc)
                binding.descField.setText(creator!!.originalCreator.desc)
            } else {
                viewModel.getObservable().ddesc.postValue(viewModel.getObservable().ddesc.value)
                binding.descField.setText(viewModel.getObservable().ddesc.value)
            }

            if (viewModel.getObservable().iinstagram.value == null) {
                viewModel.getObservable().iinstagram.postValue(creator!!.originalCreator.instagram)
                binding.instagramField.setText(creator!!.originalCreator.instagram)
            } else {
                viewModel.getObservable().iinstagram.postValue(viewModel.getObservable().iinstagram.value)
                binding.instagramField.setText(viewModel.getObservable().iinstagram.value)
            }

            if (viewModel.getObservable().ffacebook.value == null) {
                viewModel.getObservable().ffacebook.postValue(creator!!.originalCreator.facebook)
                binding.facebookField.setText(creator!!.originalCreator.facebook)
            } else {
                viewModel.getObservable().ffacebook.postValue(viewModel.getObservable().ffacebook.value)
                binding.facebookField.setText(viewModel.getObservable().ffacebook.value)
            }

            if (viewModel.getObservable().ttwitter.value == null) {
                viewModel.getObservable().ttwitter.postValue(creator!!.originalCreator.twitter)
                binding.twitterField.setText(creator!!.originalCreator.twitter)
            } else {
                viewModel.getObservable().ttwitter.postValue(viewModel.getObservable().ttwitter.value)
                binding.twitterField.setText(viewModel.getObservable().ttwitter.value)
            }

            if (viewModel.getObservable().yyoutube.value == null) {
                viewModel.getObservable().yyoutube.postValue(creator!!.originalCreator.youtube)
                binding.youtubeField.setText(creator!!.originalCreator.youtube)
            } else {
                viewModel.getObservable().yyoutube.postValue(viewModel.getObservable().yyoutube.value)
                binding.youtubeField.setText(viewModel.getObservable().yyoutube.value)
            }

            if (tempThumbUri == null) {
                displayImage(
                    Creator(
                        creator!!.originalCreator.key,
                        creator!!.originalCreator
                    )/*.modForViewWidth(getPlaylistArtWidth())*/
                )
            }
        }
    }

    fun observeViewModel() {

        viewModel.getObservable().imageUploadProgress.observe(viewLifecycleOwner) {
            binding.progressCount.text = it
        }

        viewModel.getObservable().data.observe(viewLifecycleOwner, Observer {
            if (it.success || it.message == null) {
                Utils.vibrateAfterAction(activity)
                viewModel.getObservable().clearResult(R.string.saved)
                viewModel.getObservable().imageUploadProgress.postValue("")
                dialog?.dismiss()
            } else {
                Toast.makeText(activity, it.message, Toast.LENGTH_SHORT).show()
                binding.writeCreator.visibility = View.VISIBLE
                binding.progressBar.visibility = View.GONE
                binding.progressCount.visibility = View.GONE
                viewModel.getObservable().imageUploadProgress.postValue("")
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
         * @return A new instance of fragment AdminWriteCreatorDialogFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AdminWriteCreatorDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.closeButton.id -> findNavController().popBackStack()
            binding.writeCreator.id -> {
                writeCreator()
                binding.writeCreator.visibility = View.INVISIBLE
                binding.progressBar.visibility = View.VISIBLE
                binding.progressCount.visibility = View.VISIBLE
            }
            R.id.clickableThumbBackground -> checkForReadStoragePermission {
                launchImagePickerIntent()
            }
            R.id.removePicture -> removeSelectedImage()
        }
    }

    private fun removeSelectedImage() {
        tempThumbUri = null
        deleteImageFile = true
        enableWriteButton()
        displayImage(null)
        viewModel.getObservable()._imageUri.postValue(null)
    }

    private fun checkForReadStoragePermission(filePickerIntent: () -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasStoragePermission = isPermissionGranted(android.Manifest.permission.READ_MEDIA_IMAGES) && isPermissionGranted(android.Manifest.permission.READ_MEDIA_AUDIO);
            if (hasStoragePermission) {
                filePickerIntent()
            } else {
                requestPermissions(
                    arrayOf(android.Manifest.permission.READ_MEDIA_IMAGES, android.Manifest.permission.READ_MEDIA_AUDIO),
                    permissionRequestExternalStorage
                )
            }
        } else {
            val hasStoragePermission =
                isPermissionGranted(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (hasStoragePermission) {
                filePickerIntent()
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
        fetchingFile =true

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
                viewModel.getObservable()._imageUri.postValue(uri)
                if (creator != null && !creator!!.originalCreator.thumbnailKey.isNullOrEmpty()) {
                    deleteImageFile = true
                }
                displayImage(tempThumbUri!!)
            }
            enableWriteButton()
            fetchingFile = false
        }
    }

    private fun writeCreator() {
        if (!validateData()) {
            return
        }
        if (creator == null) {
            viewModel.getObservable().createCreator()
        } else {
            viewModel.getObservable()._creatorToEdit.postValue(creator!!.originalCreator)
            viewModel.getObservable()._deleteOldImage.postValue(deleteImageFile)
            viewModel.getObservable().editCreator()
        }
    }

    private val thumbLoadListener = object : RequestListener<Drawable> {
        override fun onLoadFailed(
            e: GlideException?,
            model: Any?,
            target: Target<Drawable>?,
            isFirstResource: Boolean
        ): Boolean {
            binding.unselectedImageGroup.crossFadeWidth(binding.songArtWork)
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
            binding.songArtWork.crossFadeWidth(binding.unselectedImageGroup, visibility = View.INVISIBLE)
            // Check if tt was loaded from a selected image
            if (model is Uri
                || (creator != null
                        && !creator!!.originalCreator.thumbnailKey.isNullOrEmpty())
            ) {
                binding.removePicture.crossFadeWidth(binding.uploadPicture, visibility = View.INVISIBLE)
            }
            return false
        }

    }

    private fun displayImage(any: Any?) {
        if (any !is Creator) {
            Glide.with(this)
                .load(any)
                .transform(
                    MultiTransformation(DataBindingAdapters.centerCrop, RoundedCorners(7.px))
                ).listener(thumbLoadListener)
                .into(binding.songArtWork)
        } else {
            val uri = FileProvider.getUriForFile(requireContext().applicationContext, "${requireContext().applicationContext.packageName}.provider", File(
                requireContext().applicationContext.filesDir,
                any.originalCreator.name ?: any.originalCreator.key
            )
            )
            Glide.with(requireContext())
                .setDefaultRequestOptions(
                    RequestOptions()
                        .placeholder(R.drawable.profile_person)
                        .error(R.drawable.profile_person)
                        .diskCacheStrategy(DiskCacheStrategy.DATA)
                )
                .load(/*playlist.modForViewWidth(view.measuredWidth)*/Uri.parse("https://dn1i8z7909ivj.cloudfront.net/public/"+any.originalCreator.thumbnailKey))
                .transform(
                    MultiTransformation(
                        DataBindingAdapters.centerCrop,
                        DataBindingAdapters.circleCrop
                    )
                )
                .placeholder(R.drawable.profile_person)
                .listener(thumbLoadListener)
                .into(binding.songArtWork)
        }
    }

    private fun validateData(showToast: Boolean = true): Boolean {
        val artistName = binding.nameField.text.toString()
        val artistDesc = binding.descField.text.toString()
        val artistIG = binding.instagramField.text.toString()
        val artistFB = binding.facebookField.text.toString()
        val artistTwitter = binding.twitterField.text.toString()
        val artistYT = binding.youtubeField.text.toString()
        @StringRes var res: Int? = null
        if (creator == null && (viewModel.getObservable().crname.value.isNullOrEmpty() || viewModel.getObservable().ddesc.value.isNullOrEmpty() || viewModel.getObservable().iinstagram.value.isNullOrEmpty() || viewModel.getObservable().ffacebook.value.isNullOrEmpty() || viewModel.getObservable().ttwitter.value.isNullOrEmpty() || viewModel.getObservable().yyoutube.value.isNullOrEmpty() || tempThumbUri == null)) {
            res = R.string.artist_empty_message
        } else if (creator != null) {
            if (tempThumbUri == null && artistName.trim() == creator!!.originalCreator.name && artistDesc.trim() == creator!!.originalCreator.desc && artistIG.trim() == creator!!.originalCreator.instagram && artistFB.trim() == creator!!.originalCreator.facebook && artistTwitter.trim() == creator!!.originalCreator.twitter && artistYT.trim() == creator!!.originalCreator.youtube) {
                res = R.string.creator_empty_thumb_message
            }
        }

        if (res == null) return true

        if (showToast) Toast.makeText(activity, res, Toast.LENGTH_SHORT).show()

        return false
    }

    private fun enableWriteButton() {
        binding.writeCreator.isEnabled = validateData(false)
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
        if (!fetchingFile) {
            val vm = viewModel
            vm.getObservable()._creatorToEdit.postValue(null)
            vm.getObservable().crname.postValue(null)
            vm.getObservable().ddesc.postValue(null)
            vm.getObservable().iinstagram.postValue(null)
            vm.getObservable().ffacebook.postValue(null)
            vm.getObservable().ttwitter.postValue(null)
            vm.getObservable().yyoutube.postValue(null)
            vm.getObservable()._imageUri.postValue(null)
            vm.getObservable()._deleteOldImage.postValue(null)
        }
    }
}