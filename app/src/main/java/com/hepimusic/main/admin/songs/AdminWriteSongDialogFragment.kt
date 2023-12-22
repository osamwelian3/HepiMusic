package com.hepimusic.main.admin.songs

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
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
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.Filter.FilterListener
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.content.FileProvider
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.google.gson.Gson
import com.hepimusic.R
import com.hepimusic.common.crossFadeWidth
import com.hepimusic.common.px
import com.hepimusic.databinding.DialogSearchableSpinnerBinding
import com.hepimusic.databinding.FragmentAdminWriteSongDialogBinding
import com.hepimusic.main.admin.albums.Album
import com.hepimusic.main.admin.categories.Category
import com.hepimusic.main.admin.creators.Creator
import com.hepimusic.main.common.dataBinding.DataBindingAdapters
import com.hepimusic.main.common.utils.Utils
import com.hepimusic.main.common.view.BaseFullscreenDialogFragment
import com.hepimusic.main.profile.Profile
import com.hepimusic.ui.MainActivity
import java.io.File

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AdminWriteSongDialogFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AdminWriteSongDialogFragment : BaseFullscreenDialogFragment(), View.OnClickListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private val permissionRequestExternalStorage = 0
    private val imageRequestCode = 1
    private var tempThumbUri: Uri? = null
    var deleteImageFile = false

    private val permissionRequestExternalStorage2 = 1
    private val fileRequestCode = 2
    private var tempFileUri: Uri? = null
    var deleteSongFile = false

    var imageRequest = false
    var fetchingFile = false

    lateinit var binding: FragmentAdminWriteSongDialogBinding
    lateinit var viewModel: AdminSongsViewModel

    var albumList = emptyList<Album>()
    var categoryList = emptyList<Category>()
    var creatorList = emptyList<Creator>()
    val albumSuggestions = mutableListOf<String>()
    val categoriesSuggestions = mutableListOf<String>()
    val creatorsSuggestions = mutableListOf<String>()
    private var song: com.hepimusic.main.admin.songs.Song? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentAdminWriteSongDialogBinding.inflate(LayoutInflater.from(requireContext()))
        viewModel = ViewModelProvider(requireActivity())[AdminSongsViewModel::class.java]
        song = arguments?.getParcelable("song")
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
        } // inflater.inflate(R.layout.fragment_admin_write_song_dialog, container, false)
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
        if (song != null) {
            viewModel.getObservable()._songToEdit.postValue(song!!.originalSong)
            viewModel.getObservable().name.observe(viewLifecycleOwner) {
                if (it != binding.nameField.text.toString()) {
                    binding.nameField.setText(it)
                }
            }
            viewModel.getObservable().partOf.observe(viewLifecycleOwner) { partOf ->
                val album = viewModel.albums.value?.find { it.originalAlbum.key == partOf }
                if (album != null && album.originalAlbum.name != binding.autoCompleteAlbums.text.toString()) {
                    binding.autoCompleteAlbums.setText(
                        viewModel.albums.value?.find { it.originalAlbum.key == song!!.originalSong.partOf }?.originalAlbum?.name
                            ?: "Select an album"
                    )
                }
            }
            viewModel.getObservable().selectedCategory.observe(viewLifecycleOwner) { category ->
                if (category != null && category != binding.autoCompleteCategories.text.toString()) {
                    binding.autoCompleteAlbums.setText(category)
                }
            }
            viewModel.getObservable().selectedCreator.observe(viewLifecycleOwner) { creator ->
                val artist = viewModel.creators.value?.find { it.originalCreator.key == (Gson().fromJson(
                    song!!.originalSong.selectedCreator,
                    com.amplifyframework.datastore.generated.model.Creator::class.java
                )?.key) }
                if (artist != null && artist.originalCreator.name != binding.autoCompleteCreators.text.toString()) {
                    binding.autoCompleteCreators.setText(artist.originalCreator.name)
                }
            }
        }
        binding.closeButton.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.autoCompleteCreators.setOnClickListener(this)
        binding.autoCompleteCategories.setOnClickListener(this)
        binding.autoCompleteAlbums.setOnClickListener(this)
        binding.closeButton.setOnClickListener(this)
        binding.writeSong.setOnClickListener(this)
        binding.clickableThumbBackground.setOnClickListener(this)
        binding.removePicture.setOnClickListener(this)
        binding.clickableFileBackground.setOnClickListener(this)
        binding.removeSong.setOnClickListener(this)
        binding.nameField.addTextChangedListener(textWatcher)
        /*binding.creatorField.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                writeProfile()
                binding.writeProfile.visibility = View.INVISIBLE
                binding.progressBar.visibility = View.VISIBLE
                binding.progressCount.visibility = View.VISIBLE
                return@setOnEditorActionListener true
            }

            return@setOnEditorActionListener false
        }*/

        if (song != null) {
            viewModel.getObservable()._songToEdit.postValue(song!!.originalSong)
            binding.writeSong.setText(R.string.save_changes)
            binding.uploadSong.setText("Change ${song!!.originalSong.name} file.")
            if (viewModel.getObservable().nname.value == null) {
                viewModel.getObservable().nname.postValue(song!!.originalSong.name)
                binding.nameField.setText(song!!.originalSong.name)
            } else {
                viewModel.getObservable().nname.postValue(viewModel.getObservable().nname.value)
                binding.nameField.setText(viewModel.getObservable().nname.value)
            }

            if (viewModel.getObservable().sselectedCategory.value == null) {
                viewModel.getObservable().categoryName =
                    viewModel.categories.value?.find { it.originalCategory.name == song!!.originalSong.selectedCategory }?.originalCategory?.name
                        ?: ""
                binding.autoCompleteCategories.setText(
                    viewModel.categories.value?.find { it.originalCategory.name == song!!.originalSong.selectedCategory }?.originalCategory?.name
                        ?: "Select a category"
                )
            } else {
                viewModel.getObservable().categoryName = viewModel.getObservable().sselectedCategory.value!!
                binding.autoCompleteCategories.setText(viewModel.getObservable().sselectedCategory.value!!)
            }

            if (viewModel.getObservable()._partOf.value == null) {
                viewModel.getObservable().albumName =
                    viewModel.albums.value?.find { it.originalAlbum.key == song!!.originalSong.partOf }?.originalAlbum?.name
                        ?: ""
                binding.autoCompleteAlbums.setText(
                    viewModel.albums.value?.find { it.originalAlbum.key == song!!.originalSong.partOf }?.originalAlbum?.name
                        ?: "Select an album"
                )
            } else {
                val cAlbm = viewModel.albums.value?.find { it.originalAlbum.key == viewModel.getObservable()._partOf.value }?.originalAlbum?.name
                    ?: ""
                viewModel.getObservable().albumName = cAlbm
                binding.autoCompleteAlbums.setText(cAlbm)
            }

            if (viewModel.getObservable()._selectedCreator.value == null) {
                viewModel.getObservable().creatorName = viewModel.creators.value?.find {
                    it.originalCreator.name == (Gson().fromJson(
                        song!!.originalSong.selectedCreator,
                        com.amplifyframework.datastore.generated.model.Creator::class.java
                    )?.name ?: "")
                }?.originalCreator?.name
                    ?: ""
                binding.autoCompleteCreators.setText(
                    viewModel.creators.value?.find {
                        it.originalCreator.name == (Gson().fromJson(
                            song!!.originalSong.selectedCreator,
                            com.amplifyframework.datastore.generated.model.Creator::class.java
                        )?.name ?: "")
                    }?.originalCreator?.name
                        ?: "Select an artist"
                )
            } else {
                viewModel.getObservable().creatorName = viewModel.creators.value?.find {
                    it.originalCreator.name == (Gson().fromJson(
                        viewModel.getObservable()._selectedCreator.value,
                        com.amplifyframework.datastore.generated.model.Creator::class.java
                    )?.name ?: "")
                }?.originalCreator?.name
                    ?: ""
                binding.autoCompleteCreators.setText(
                    viewModel.creators.value?.find {
                        it.originalCreator.name == (Gson().fromJson(
                            viewModel.getObservable()._selectedCreator.value,
                            com.amplifyframework.datastore.generated.model.Creator::class.java
                        )?.name ?: "")
                    }?.originalCreator?.name
                        ?: ""
                )
            }

            if (tempThumbUri == null) {
                displayImage(
                    Song(
                        song!!.originalSong.key,
                        song!!.originalSong
                    )/*.modForViewWidth(getPlaylistArtWidth())*/
                )
            }
        }
    }

    fun observeViewModel() {

        viewModel.getObservable().imageUploadProgress.observe(viewLifecycleOwner) {
            binding.progressCount.text = it
        }

        viewModel.getObservable().fileUploadProgress.observe(viewLifecycleOwner) {
            binding.progressCount.text = it
        }

        viewModel.getObservable().data.observe(viewLifecycleOwner, Observer {
            if (it.success || it.message == null) {
                Utils.vibrateAfterAction(activity)
                viewModel.getObservable().clearResult(R.string.saved)
                viewModel.getObservable().imageUploadProgress.postValue("")
                viewModel.getObservable().fileUploadProgress.postValue("")
                dialog?.dismiss()
            } else {
                Toast.makeText(activity, it.message, Toast.LENGTH_SHORT).show()
                binding.writeSong.visibility = View.VISIBLE
                binding.progressBar.visibility = View.GONE
                binding.progressCount.visibility = View.GONE
                viewModel.getObservable().fileUploadProgress.postValue("")
                viewModel.getObservable().imageUploadProgress.postValue("")
            }
        })
        viewModel.getObservable().albumExists.observe(viewLifecycleOwner) {
            if (it) {
                binding.autoCompleteAlbums.setTextColor(Color.BLACK)
                enableWriteButton()
            } else {
                binding.autoCompleteAlbums.setTextColor(Color.RED)
            }
        }
        viewModel.getObservable().categoryExists.observe(viewLifecycleOwner) {
            if (it) {
                binding.autoCompleteCategories.setTextColor(Color.BLACK)
                enableWriteButton()
            } else {
                binding.autoCompleteCategories.setTextColor(Color.RED)
            }
        }
        viewModel.getObservable().creatorExists.observe(viewLifecycleOwner) {
            if (it) {
                binding.autoCompleteCreators.setTextColor(Color.BLACK)
                enableWriteButton()
            } else {
                binding.autoCompleteCreators.setTextColor(Color.RED)
            }
        }
    }

    private fun setupAlbumInput() {
        val dialog = Dialog(requireActivity())
        dialog.setContentView(R.layout.dialog_searchable_spinner)
        dialog.window?.setLayout(650, 800)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.BLACK))
        dialog.show()

        val selectionTitle = dialog.findViewById<TextView>(R.id.selectionTitle)
        val editText = dialog.findViewById<EditText>(R.id.editText)
        val listView = dialog.findViewById<ListView>(R.id.listView)

        selectionTitle.text = "Select or search for an Album"
        selectionTitle.setTextColor(Color.BLACK)
        editText.setTextColor(Color.BLACK)

        albumList = viewModel.albums.value ?: emptyList()
        albumSuggestions.addAll(albumList.map { it.originalAlbum.name })

        val adapter = ArrayAdapter<String>(requireContext(), /*androidx.appcompat.R.layout.support_simple_spinner_dropdown_item*/R.layout.searchable_list_item, R.id.text_view, albumSuggestions)
        adapter.setNotifyOnChange(true)

        viewModel.albums.observe(viewLifecycleOwner) {
            albumList = it
            albumSuggestions.clear()
            albumSuggestions.addAll(albumList.map { it.originalAlbum.name })
            adapter.clear()
            adapter.addAll(albumList.map { it.originalAlbum.name })
            adapter.notifyDataSetChanged()
        }

        listView.adapter = adapter
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filter.filter(s)
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })

        listView.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                binding.autoCompleteAlbums.text = adapter.getItem(position).toString()
                viewModel.getObservable().albumName = adapter.getItem(position).toString()
                enableWriteButton()
                dialog.dismiss()
            }

    }

    private fun setupCategoriesInput() {
        val dialog = Dialog(requireActivity())
        dialog.setContentView(R.layout.dialog_searchable_spinner)
        dialog.window?.setLayout(650, 800)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.BLACK))
        dialog.show()

        val selectionTitle = dialog.findViewById<TextView>(R.id.selectionTitle)
        val editText = dialog.findViewById<EditText>(R.id.editText)
        val listView = dialog.findViewById<ListView>(R.id.listView)

        selectionTitle.text = "Select or search for a Category"
        selectionTitle.setTextColor(Color.BLACK)
        editText.setTextColor(Color.BLACK)

        categoryList = viewModel.categories.value ?: emptyList()
        categoriesSuggestions.addAll(categoryList.map { it.originalCategory.name })

        val adapter = ArrayAdapter<String>(requireContext(), /*androidx.appcompat.R.layout.support_simple_spinner_dropdown_item*/R.layout.searchable_list_item, R.id.text_view, categoriesSuggestions)
        adapter.setNotifyOnChange(true)

        viewModel.categories.observe(viewLifecycleOwner) {
            categoryList = it
            categoriesSuggestions.clear()
            categoriesSuggestions.addAll(categoryList.map { it.originalCategory.name })
            adapter.clear()
            adapter.addAll(categoryList.map { it.originalCategory.name })
            adapter.notifyDataSetChanged()
        }

        listView.adapter = adapter
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filter.filter(s)
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })

        listView.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                binding.autoCompleteCategories.text = adapter.getItem(position).toString()
                viewModel.getObservable().categoryName = adapter.getItem(position).toString()
                enableWriteButton()
                dialog.dismiss()
            }

    }

    private fun setupCreatorsInput() {
        val dialog = Dialog(requireActivity())
        dialog.setContentView(R.layout.dialog_searchable_spinner)
        dialog.window?.setLayout(650, 800)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.BLACK))
        dialog.show()

        val selectionTitle = dialog.findViewById<TextView>(R.id.selectionTitle)
        val editText = dialog.findViewById<EditText>(R.id.editText)
        val listView = dialog.findViewById<ListView>(R.id.listView)

        selectionTitle.text = "Select or search for an Artist"
        selectionTitle.setTextColor(Color.BLACK)
        editText.setTextColor(Color.BLACK)

        creatorList = viewModel.creators.value ?: emptyList()
        creatorsSuggestions.addAll(creatorList.map { it.originalCreator.name })

        val adapter = ArrayAdapter<String>(requireContext(), /*androidx.appcompat.R.layout.support_simple_spinner_dropdown_item*/R.layout.searchable_list_item, R.id.text_view, creatorsSuggestions)
        adapter.setNotifyOnChange(true)

        viewModel.creators.observe(viewLifecycleOwner) {
            creatorList = it
            creatorsSuggestions.clear()
            creatorsSuggestions.addAll(creatorList.map { it.originalCreator.name })
            adapter.clear()
            adapter.addAll(creatorList.map { it.originalCreator.name })
            adapter.notifyDataSetChanged()
        }

        listView.adapter = adapter
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filter.filter(s)
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })

        listView.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                binding.autoCompleteCreators.text = adapter.getItem(position).toString()
                viewModel.getObservable().creatorName = adapter.getItem(position).toString()
                enableWriteButton()
                dialog.dismiss()
            }

    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment AdminWriteSongDialogFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AdminWriteSongDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.closeButton -> findNavController().popBackStack()
            R.id.writeSong -> {
                writeSong()
                binding.writeSong.visibility = View.INVISIBLE
                binding.progressBar.visibility = View.VISIBLE
                binding.progressCount.visibility = View.VISIBLE
            }
            R.id.clickableThumbBackground -> checkForReadStoragePermission {
                imageRequest = true
                launchImagePickerIntent()
            }
            R.id.removePicture -> removeSelectedImage()
            R.id.clickableFileBackground -> checkForReadStoragePermission {
                imageRequest = false
                launchFilePickerIntent()
            }
            R.id.removeSong -> removeSelectedSong()
            binding.autoCompleteCreators.id -> setupCreatorsInput()
            binding.autoCompleteAlbums.id -> setupAlbumInput()
            binding.autoCompleteCategories.id -> setupCategoriesInput()
        }
    }

    private fun removeSelectedImage() {
        tempThumbUri = null
        deleteImageFile = true
        enableWriteButton()
        displayImage(null)
        viewModel.getObservable()._imageUri.postValue(null)
    }

    private fun removeSelectedSong() {
        tempFileUri = null
        deleteSongFile = true
        enableWriteButton()
        binding.removeSong.setText("REMOVE SONG")
        binding.unselectedFileGroup.crossFadeWidth(binding.songPictureArt, visibility = View.INVISIBLE)
        binding.uploadSong.crossFadeWidth(binding.removeSong)
        viewModel.getObservable()._fileUri.postValue(null)
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

    private fun launchFilePickerIntent() {
        val pickImageIntent = Intent(Intent.ACTION_GET_CONTENT)
        pickImageIntent.type = "audio/*"
        fetchingFile = true

        startActivityForResult(pickImageIntent, fileRequestCode)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == permissionRequestExternalStorage) {
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission request was granted
                if (imageRequest) launchImagePickerIntent() else launchFilePickerIntent()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == imageRequestCode && resultCode == Activity.RESULT_OK && data != null) {
            val uri = data.data
            if (uri != null) {
                tempThumbUri = uri
                viewModel.getObservable()._imageUri.postValue(uri)
                if (song != null && !song!!.originalSong.thumbnailKey.isNullOrEmpty()) {
                    deleteImageFile = true
                }
                displayImage(tempThumbUri!!)
            }
            enableWriteButton()
            fetchingFile = false
        }
        if (requestCode == fileRequestCode && resultCode == Activity.RESULT_OK && data != null) {
            val uri = data.data
            if (uri != null) {
                tempFileUri = uri
                viewModel.getObservable()._fileUri.postValue(uri)
                if (song != null && !song!!.originalSong.fileKey.isNullOrEmpty()) {
                    deleteSongFile = true
                }
                Glide.with(requireContext())
                    .load(R.drawable.album_art)
                    .centerCrop()
                    .circleCrop()
                    .into(binding.songPictureArt)
                binding.removeSong.setText("REMOVE ${getFileNameFromUri(uri)}")
                binding.removeSong.crossFadeWidth(binding.uploadSong)
                if (binding.songPictureArt.visibility == View.INVISIBLE && binding.unselectedFileGroup.visibility == View.VISIBLE) {
                    binding.songPictureArt.crossFadeWidth(
                        binding.unselectedFileGroup,
                        visibility = View.INVISIBLE
                    )
                }
            }
            enableWriteButton()
            fetchingFile = false
        }
    }

    private fun getFileNameFromUri(uri: Uri): String? {
        val contentResolver = requireContext().contentResolver
        val cursor = contentResolver.query(uri, null, null, null, null)
        return cursor?.use {
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            cursor.moveToFirst()
            cursor.getString(nameIndex)
        }
    }

    private fun writeSong() {
        if (!validateData()) {
            return
        }
        val songName = binding.nameField.text.toString().trim()
        val songAlbum = binding.autoCompleteAlbums.text.toString().trim()
        val songCategory = binding.autoCompleteCategories.text.toString().trim()
        val songCreator = viewModel.profile.value?.originalProfile?.name
        if (song == null) {
            viewModel.getObservable().createSong()
        } else {
            viewModel.getObservable()._songToEdit.postValue(song!!.originalSong)
            viewModel.getObservable()._deleteOldImage.postValue(deleteImageFile)
            viewModel.getObservable().editSong()
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
                || (song != null
                        && !song!!.originalSong.thumbnailKey.isNullOrEmpty())
            ) {
                binding.removePicture.crossFadeWidth(binding.uploadPicture, visibility = View.INVISIBLE)
            }
            return false
        }

    }

    private fun displayImage(any: Any?) {
        if (any !is Song) {
            Glide.with(this)
                .load(any)
                .transform(
                    MultiTransformation(DataBindingAdapters.centerCrop, RoundedCorners(7.px))
                ).listener(thumbLoadListener)
                .into(binding.songArtWork)
        } else {
            val uri = FileProvider.getUriForFile(requireContext().applicationContext, "${requireContext().applicationContext.packageName}.provider", File(
                requireContext().applicationContext.filesDir,
                any.originalSong.name ?: any.originalSong.key
            )
            )
            Glide.with(requireContext())
                .setDefaultRequestOptions(
                    RequestOptions()
                        .placeholder(R.drawable.album_art)
                        .error(R.drawable.album_art)
                        .diskCacheStrategy(DiskCacheStrategy.DATA)
                )
                .load(/*playlist.modForViewWidth(view.measuredWidth)*/Uri.parse("https://dn1i8z7909ivj.cloudfront.net/public/"+any.originalSong.thumbnailKey))
                .transform(
                    MultiTransformation(
                        DataBindingAdapters.centerCrop,
                        DataBindingAdapters.circleCrop
                    )
                )
                .placeholder(R.drawable.album_art)
                .listener(thumbLoadListener)
                .into(binding.songArtWork)
        }
    }

    private fun validateData(showToast: Boolean = true): Boolean {
        val songName = binding.nameField.text.toString()
        val songAlbum = binding.autoCompleteAlbums.text.toString()
        val songCategory = binding.autoCompleteCategories.text.toString()
        @StringRes var res: Int? = null
        if (song == null && (songName.trim().isEmpty() || viewModel.getObservable().nname.value.isNullOrEmpty() || viewModel.getObservable()._partOf.value.isNullOrEmpty() || viewModel.getObservable().sselectedCategory.value.isNullOrEmpty() || viewModel.getObservable()._selectedCreator.value.isNullOrEmpty() || tempFileUri == null || tempThumbUri == null)) {
            res = R.string.profile_empty_message
        } else if (song != null) {
            if (tempThumbUri == null && songName.trim() == song!!.originalSong.name && songAlbum.trim() == (viewModel.albums.value?.find { it.originalAlbum.key == song!!.originalSong.partOf }?.originalAlbum?.name
                    ?: "") && songCategory.trim() == song!!.originalSong.selectedCategory && !deleteImageFile
            ) {
                res = R.string.profile_empty_thumb_message
            }
        }

        if (res == null) return true

        if (showToast) Toast.makeText(activity, res, Toast.LENGTH_SHORT).show()

        return false
    }

    private fun enableWriteButton() {
        binding.writeSong.isEnabled = validateData(false)
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
            vm.getObservable()._songToEdit.postValue(null)
            vm.getObservable().nname.postValue(null)
            vm.getObservable().albumName = ""
            vm.getObservable().creatorName = ""
            vm.getObservable().categoryName = ""
            vm.getObservable().sselectedCategory.postValue(null)
            vm.getObservable()._selectedCreator.postValue(null)
            vm.getObservable()._partOf.postValue(null)
        }
    }

}