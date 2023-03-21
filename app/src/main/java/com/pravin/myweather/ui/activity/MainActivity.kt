package com.pravin.myweather.ui.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.gson.Gson
import com.pravin.myweather.R
import com.pravin.myweather.api.NetworkResult
import com.pravin.myweather.databinding.ActivityMainBinding
import com.pravin.myweather.ui.viewmodel.DashboardViewModel
import com.pravin.myweather.utils.isLocationEnabled
import com.pravin.myweather.utils.positiveButtonClick
import com.pravin.myweather.utils.showAlertDialog
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    lateinit var mFusedLocationProviderClient: FusedLocationProviderClient
    private val dashboardViewModel: DashboardViewModel by viewModels()
    var PERMISSION_ALL = 1
    var PERMISSIONS_ARRAY = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        requestLocationPermission()
//        dashboardViewModel.getWeatherData("18.5204", "73.8567")
        collects()
    }


    private fun requestLocationPermission() {
        locationPermissionRequest.launch(
            PERMISSIONS_ARRAY
        )
    }

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                checkGPS()
            }
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                checkGPS()
            }
            else -> {
                Log.e("TAGG", "Permission denied")
            }
        }
    }

    private fun checkGPS() {
        if (isLocationEnabled()) {
            getLocation()
        } else {
            showAlertDialog {
                setTitle(context.resources.getString(R.string.important))
                setMessage(context.resources.getString(R.string.enable_gps_message))
                positiveButtonClick(context.resources.getString(R.string.ok)) {
                    it.dismiss()
                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(intent)
                }
            }
        }
    }

    fun getLocation() {
        mFusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(this)
        mFusedLocationProviderClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            CancellationTokenSource().token
        ).addOnSuccessListener { location ->
            if (location != null) {
                val lat: Double = location.latitude
                val longt: Double = location.longitude
                Log.e("TAG", "Lat: " + lat)
                Log.e("TAG", "Long: " + longt)
            } else {
                //don't be confused with AlertDialoBox because
                //this our alerbox written by me you can write own alerbox to show the //message
//                        Common.AlertDialogBox(
//                            getContext(), "Fetching location ",
//                            getString(R.string.fetchingerror)
//                        )
            }
        }.addOnFailureListener { e ->
            val message = e.message
            val title = "Location fetching exception"
//                    Common.AlertDialogBox(this, title, message)
        }
    }

    private fun collects() {
        lifecycleScope.launchWhenCreated {
            dashboardViewModel.weatherResponseLiveData.observe(this@MainActivity) { responseData ->
//                progressDialog.hide()
                when (responseData) {
                    is NetworkResult.Loading -> {
//                        progressDialog.show()
                    }
                    is NetworkResult.Error -> {
                        showAlertDialog {
                            setTitle(context.resources.getString(R.string.error))
                            setMessage(responseData.errorMessage?.message)
                            positiveButtonClick(context.resources.getString(R.string.ok)) { }
                        }
                    }
                    is NetworkResult.Success -> {
                        responseData.data?.let {
                            val apiResponse = responseData.data
                            if (null != apiResponse) {

                                Log.e("TAGG", Gson().toJson(apiResponse))
//                                if (apiResponse.data.isNotEmpty()) {
//                                    val preparedList =
//                                        daySalesReconViewModel.getPreparedItemList(apiResponse.data)
//                                    dayReconRecyclerViewAdapter.items = preparedList
//                                } else {
//                                    binding.withDataLayout.visibility = View.GONE
//                                    binding.noDataLayout.visibility = View.VISIBLE
//                                }
                            } else {
                                showAlertDialog {
                                    setTitle(context.resources.getString(R.string.error))
                                    setMessage(resources.getString(R.string.generic_error_message))
                                    positiveButtonClick(context.resources.getString(R.string.ok)) { }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}