package com.hepimusic.getStarted

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.hepimusic.R
import com.hepimusic.common.BaseActivity
import com.hepimusic.common.crossFadeWidth
import com.hepimusic.common.fadeIn
import com.hepimusic.databinding.ActivityGetStartedBinding
import com.hepimusic.ui.MainActivity

class GetStartedActivity : BaseActivity(), View.OnClickListener {

    private val permissionRequestExternalStorage = 0
    private val storagePermission = android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private val audioPermission = android.Manifest.permission.READ_MEDIA_AUDIO
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private val imagesPermission = android.Manifest.permission.READ_MEDIA_IMAGES

    lateinit var binding: ActivityGetStartedBinding

    private lateinit var getStarted: Button
    private lateinit var goToAppInfo: Button
    private lateinit var infoText: TextView
    private val requestStoragePermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted){
            startMainActivity()
        } else {
            infoText.text = "Permission Denied by user. Please Allow Permissions manually to proceed."
            if (infoText.visibility != View.VISIBLE) {
                infoText.fadeIn(duration = 500)
            }
            goToAppInfo.crossFadeWidth(getStarted, 500)
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private val requestStoragePermissionTwo = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted){
            requestStoragePermission.launch(imagesPermission)
        } else {
            infoText.text = "Permission Denied by user. Please Allow Permissions manually to proceed."
            if (infoText.visibility != View.VISIBLE) {
                infoText.fadeIn(duration = 500)
            }
            goToAppInfo.crossFadeWidth(getStarted, 500)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGetStartedBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)
        getStarted = binding.getStarted // findViewById(R.id.getStarted)
        goToAppInfo = binding.goToAppInfo // findViewById(R.id.goToAppInfo)
        infoText = binding.infoText // findViewById(R.id.infoText)
        getStarted.setOnClickListener(this)
        goToAppInfo.setOnClickListener(this)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            if (requestCode == permissionRequestExternalStorage) {
                if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission request was granted
                    startMainActivity()
                } else {
                    // Permission request was denied.
                    if (ActivityCompat.shouldShowRequestPermissionRationale(
                            this,
                            storagePermission
                        )
                    ) {
                        infoText.setText(R.string.read_storage_permission_info)
                    } else {
                        // The user has denied the permission and selected the "Don't ask again"
                        // option in the permission request dialog
                        infoText.text = getString(
                            R.string.read_storage_permission_settings,
                            getString(R.string.app_name)
                        )
                        goToAppInfo.crossFadeWidth(getStarted, 500)
                    }
                    if (infoText.visibility != View.VISIBLE) {
                        infoText.fadeIn(duration = 500)
                    }
                }
            }
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            getStarted.id -> getStarted()
            goToAppInfo.id -> goToAppInfo()
        }
    }


    private fun getStarted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (isPermissionGranted(audioPermission) && isPermissionGranted(imagesPermission)) {
                startMainActivity()
            } else {
                try {
                    requestStoragePermissionTwo.launch(audioPermission)
//                requestStoragePermission.launch(imagesPermission)
                } catch (e: Exception) {
                    ActivityCompat.requestPermissions(
                        this, arrayOf(storagePermission),
                        permissionRequestExternalStorage
                    )
                }
            }
        } else {
            if (isPermissionGranted(storagePermission)) {
                startMainActivity()
            } else {
                try {
                    requestStoragePermission.launch(storagePermission)
                } catch (e: Exception) {
                    ActivityCompat.requestPermissions(
                        this, arrayOf(storagePermission),
                        permissionRequestExternalStorage
                    )
                }
            }
        }

    }

    private fun goToAppInfo() {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        val uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivity(intent)
    }

    private fun startMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        finish()
    }

    public override fun onRestart() {
        if (isPermissionGranted(storagePermission)) {
            startMainActivity()
        }
        super.onRestart()
    }


}