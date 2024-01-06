package com.hepimusic.main.requests

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.hepimusic.R
import com.hepimusic.common.BaseFragment
import com.hepimusic.common.crossFadeWidth
import com.hepimusic.common.fadeIn
import com.hepimusic.common.safeNavigate
import com.hepimusic.common.safeNavigationOnClickListener
import com.hepimusic.databinding.FragmentRequestsOnBoardingBinding
import com.hepimusic.ui.MainActivity

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [RequestsOnBoardingFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RequestsOnBoardingFragment : BaseFragment(), View.OnClickListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    lateinit var binding: FragmentRequestsOnBoardingBinding

    val coarseLocationRequestCode = 0
    val fineLocationRequestCode = 1
    val coarseLocationPermission = android.Manifest.permission.ACCESS_COARSE_LOCATION
    val fineLocationPermission = android.Manifest.permission.ACCESS_FINE_LOCATION

    private val requestCoarseLocationPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted){
            navigateToNext()
        } else {
            binding.infoText.text = "Permission Denied by user. Go to Settings > Apps > HepiMusic to allow Permissions manually to proceed."
            if (binding.infoText.visibility != View.VISIBLE) {
                binding.infoText.fadeIn(duration = 500)
            }
            binding.goToAppInfo.crossFadeWidth(binding.getStarted, 500)
        }
    }

    private val requestFineLocationPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted){
            requestCoarseLocationPermission.launch(coarseLocationPermission)
        } else {
            binding.infoText.text = "Permission Denied by user. Go to Settings > Apps > HepiMusic to allow Permissions manually to proceed."
            if (binding.infoText.visibility != View.VISIBLE) {
                binding.infoText.fadeIn(duration = 500)
            }
            binding.goToAppInfo.crossFadeWidth(binding.getStarted, 500)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentRequestsOnBoardingBinding.inflate(LayoutInflater.from(requireContext()))
        if (isPermissionGranted(fineLocationPermission) && isPermissionGranted(coarseLocationPermission)) {
            navigateToNext()
        }
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestFineLocationPermission.launch(coarseLocationPermission)
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(), arrayOf(coarseLocationPermission),
                0
            )
        }*/

        binding.getStarted.setOnClickListener(this)
        binding.goToAppInfo.setOnClickListener(this)
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
        return binding.root // return inflater.inflate(R.layout.fragment_requests_on_boarding, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            val navController = activity?.findNavController(R.id.mainNavHostFragment)
            navController?.setGraph(R.navigation.navigation_graph)
            navController?.popBackStack()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            if (requestCode == fineLocationRequestCode) {
                if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission request was granted
                    ActivityCompat.requestPermissions(
                        requireActivity(), arrayOf(coarseLocationPermission),
                        coarseLocationRequestCode
                    )
                } else {
                    // Permission request was denied.
                    if (ActivityCompat.shouldShowRequestPermissionRationale(
                            requireActivity(),
                            fineLocationPermission
                        )
                    ) {
                        binding.infoText.setText(R.string.location_permission_info)
                    } else {
                        // The user has denied the permission and selected the "Don't ask again"
                        // option in the permission request dialog
                        binding.infoText.text = getString(
                            R.string.location_permission_settings,
                            getString(R.string.app_name)
                        )
                        binding.goToAppInfo.crossFadeWidth(binding.getStarted, 500)
                    }
                    if (binding.infoText.visibility != View.VISIBLE) {
                        binding.infoText.fadeIn(duration = 500)
                    }
                }
            } else if (requestCode == coarseLocationRequestCode) {
                if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission request was granted
                    navigateToNext()
                } else {
                    // Permission request was denied.
                    if (ActivityCompat.shouldShowRequestPermissionRationale(
                            requireActivity(),
                            coarseLocationPermission
                        )
                    ) {
                        binding.infoText.setText(R.string.location_permission_info)
                    } else {
                        // The user has denied the permission and selected the "Don't ask again"
                        // option in the permission request dialog
                        binding.infoText.text = getString(
                            R.string.location_permission_settings,
                            getString(R.string.app_name)
                        )
                        binding.goToAppInfo.crossFadeWidth(binding.getStarted, 500)
                    }
                    if (binding.infoText.visibility != View.VISIBLE) {
                        binding.infoText.fadeIn(duration = 500)
                    }
                }
            }
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.getStarted.id -> getStarted()
            binding.goToAppInfo.id -> goToAppInfo()
        }
    }


    private fun getStarted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (isPermissionGranted(fineLocationPermission) && isPermissionGranted(coarseLocationPermission)) {
                navigateToNext()
            } else {
                try {
                    requestFineLocationPermission.launch(fineLocationPermission)
//                requestStoragePermission.launch(imagesPermission)
                } catch (e: Exception) {
                    ActivityCompat.requestPermissions(
                        requireActivity(), arrayOf(fineLocationPermission),
                        fineLocationRequestCode
                    )
                }
            }
        } else {
            if (isPermissionGranted(fineLocationPermission)) {
                navigateToNext()
            } else {
                try {
                    requestFineLocationPermission.launch(fineLocationPermission)
                } catch (e: Exception) {
                    ActivityCompat.requestPermissions(
                        requireActivity(), arrayOf(fineLocationPermission),
                        fineLocationRequestCode
                    )
                }
            }
        }

    }

    private fun goToAppInfo() {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        val uri = Uri.fromParts("package", requireActivity().packageName, null)
        intent.data = uri
        startActivity(intent)
    }

    private fun navigateToNext() {
        findNavController().safeNavigate(RequestsOnBoardingFragmentDirections.actionRequestsOnBoardingFragmentToRequestsMainFragment())
    }

    override fun onResume() {
        if (isPermissionGranted(fineLocationPermission)) {
            navigateToNext()
        }
        super.onResume()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment RequestsOnBoardingFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            RequestsOnBoardingFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}