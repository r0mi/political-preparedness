package com.example.android.politicalpreparedness.representative

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationRequest
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.android.politicalpreparedness.BuildConfig
import com.example.android.politicalpreparedness.MyApp
import com.example.android.politicalpreparedness.R
import com.example.android.politicalpreparedness.databinding.FragmentRepresentativeBinding
import com.example.android.politicalpreparedness.ifNotNull
import com.example.android.politicalpreparedness.network.models.Address
import com.example.android.politicalpreparedness.representative.adapter.RepresentativeListAdapter
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.material.snackbar.Snackbar
import timber.log.Timber
import java.util.*

class DetailFragment : Fragment() {

    private lateinit var binding: FragmentRepresentativeBinding

    private val viewModel by viewModels<RepresentativeViewModel> {
        RepresentativeViewModelFactory(
            (requireContext().applicationContext as MyApp).electionsRepository
        )
    }

    private val startIntentSenderForResult =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
            checkDeviceLocationSettingsAndRequestLocation(false)
        }

    private val locationPermissionRequest =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val locationPermissionGranted = permissions.entries.any { it.value == true }
            when {
                permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                    Timber.d("Precise location access granted")
                }
                permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                    Timber.d("Only approximate location access granted")
                }
                else -> {
                    Timber.e("No location access granted")
                }
            }
            if (locationPermissionGranted) {
                checkDeviceLocationSettingsAndRequestLocation()
            } else {
                showPermissionDeniedRationale()
            }
        }

    private fun showPermissionDeniedRationale() {
        showSnackbar(
            R.string.location_permission_rationale,
            duration = Snackbar.LENGTH_INDEFINITE,
            actionStrResId = R.string.settings,
        ) {
            startActivity(Intent().apply {
                action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            })
        }
    }

    private val fusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(requireContext())
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRepresentativeBinding.inflate(inflater)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        viewModel.requestLocation.observe(viewLifecycleOwner) {
            if (checkLocationPermissions()) {
                checkDeviceLocationSettingsAndRequestLocation()
            }
        }

        viewModel.showErrorMessage.observe(viewLifecycleOwner) { (msg, resId) ->
            if (resId != null && msg != null) {
                Snackbar.make(this.requireView(), getString(resId, msg), Snackbar.LENGTH_LONG)
                    .show()
            } else if (resId != null) {
                Snackbar.make(this.requireView(), resId, Snackbar.LENGTH_LONG).show()
            } else if (msg != null) {
                Snackbar.make(this.requireView(), msg, Snackbar.LENGTH_LONG).show()
            }
        }

        viewModel.findRepresentatives.observe(viewLifecycleOwner) {
            hideKeyboard()
        }

        binding.representativesRecyclerView.adapter = RepresentativeListAdapter()

        return binding.root
    }

    /*
     *  Uses the Location Client to check the current state of location settings, and gives the user
     *  the opportunity to turn on location services within our app.
     */
    private fun checkDeviceLocationSettingsAndRequestLocation(resolve: Boolean = true) {
        val locationRequest = com.google.android.gms.location.LocationRequest.create().apply {
            priority = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                LocationRequest.QUALITY_LOW_POWER
            } else {
                com.google.android.gms.location.LocationRequest.PRIORITY_LOW_POWER
            }
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val settingsClient = LocationServices.getSettingsClient(requireActivity())
        val locationSettingsResponseTask =
            settingsClient.checkLocationSettings(builder.build())
        locationSettingsResponseTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException && resolve) {
                try {
                    val intentSenderRequest =
                        IntentSenderRequest.Builder(exception.resolution).build()
                    startIntentSenderForResult.launch(intentSenderRequest)
                } catch (sendEx: IntentSender.SendIntentException) {
                    Timber.d("Error getting location settings resolution: ${sendEx.message}")
                }
            } else {
                showSnackbar(
                    R.string.location_service_rationale,
                    Snackbar.LENGTH_INDEFINITE,
                    R.string.enable
                ) {
                    checkDeviceLocationSettingsAndRequestLocation()
                }
            }
        }.addOnCompleteListener {
            if (it.isSuccessful) {
                getLocation()
            }
        }
    }

    private fun checkLocationPermissions(): Boolean {
        return if (isPermissionGranted()) {
            true
        } else {
            if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION) || shouldShowRequestPermissionRationale(
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                Timber.w("Show permission rationale, as it was previously denied")
                showPermissionDeniedRationale()
            } else {
                locationPermissionRequest.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
            false
        }
    }

    private fun isPermissionGranted(): Boolean {
        val permissions = arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        return permissions.map { permission ->
            ContextCompat.checkSelfPermission(
                requireContext(),
                permission
            ) == PackageManager.PERMISSION_GRANTED
        }.any { it }
    }

    @SuppressLint("MissingPermission")
    private fun getLocation() {
        val priority = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            LocationRequest.QUALITY_HIGH_ACCURACY
        } else {
            com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        if (isPermissionGranted()) {
            fusedLocationProviderClient.getCurrentLocation(
                priority,
                CancellationTokenSource().token
            ).addOnSuccessListener {
                it?.let {
                    val address = geoCodeLocation(it)
                    Timber.d("location is $it, address $address")
                    viewModel.setAddress(address)
                } ?: showSnackbar(R.string.location_failed)
            }.addOnFailureListener {
                showSnackbar(R.string.location_failed)
                Timber.d("Oops location failed with exception: $it")
            }.addOnCanceledListener {
                Timber.d("Location cancelled")
            }
        }
    }

    private fun geoCodeLocation(location: Location): Address {
        val geocoder = Geocoder(context, Locale.getDefault())
        return geocoder.getFromLocation(location.latitude, location.longitude, 1)
            .map { address ->
                Address(
                    address.thoroughfare,
                    address.subThoroughfare,
                    address.locality,
                    address.adminArea,
                    address.postalCode
                )
            }
            .first()
    }

    private fun hideKeyboard() {
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(requireView().windowToken, 0)
    }

    private fun showSnackbar(
        @StringRes strResId: Int,
        duration: Int = Snackbar.LENGTH_LONG,
        @StringRes actionStrResId: Int? = null,
        listener: View.OnClickListener? = null,
    ) {
        val sb = Snackbar.make(
            requireView(),
            strResId,
            duration
        ).apply {
            (view.findViewById<View?>(R.id.snackbar_text) as? TextView?)?.isSingleLine = false
        }
        ifNotNull(actionStrResId, listener) { _actionStrResId, _listener ->
            sb.setAction(_actionStrResId, _listener)
        }
        sb.show()
    }
}