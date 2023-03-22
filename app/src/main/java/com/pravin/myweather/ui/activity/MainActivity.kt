package com.pravin.myweather.ui.activity

import android.Manifest
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.pravin.myweather.R
import com.pravin.myweather.api.NetworkResult
import com.pravin.myweather.databinding.ActivityMainBinding
import com.pravin.myweather.ui.viewmodel.DashboardViewModel
import com.pravin.myweather.utils.*
import com.pravin.myweather.utils.AppConstant.LOCATION_REQUEST_CODE
import dagger.hilt.android.AndroidEntryPoint
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions


@AndroidEntryPoint
class MainActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks {
    private lateinit var binding: ActivityMainBinding
    private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient
    private val dashboardViewModel: DashboardViewModel by viewModels()
    lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.apply {
            lifecycleOwner = this@MainActivity
            viewModel = dashboardViewModel
        }
        initView()
        initObserver()
    }

    fun initView() {
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle(getString(R.string.app_name))
        progressDialog.setMessage(getString(R.string.please_wait))
    }

    override fun onResume() {
        super.onResume()
        checkGPSPermission()
    }

    private fun checkGPSPermission() {
        if (isLocationEnabled()) {
            getLocationData()
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

    fun getLocationData() {
        if (hasLocationPermissions()) {
            getWeatherDataLocation()
        } else {
            EasyPermissions.requestPermissions(
                this,
                getString(R.string.rationale_location),
                LOCATION_REQUEST_CODE,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        getWeatherDataLocation()
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this).build().show()
        } else {
            checkGPSPermission()
        }
    }

    fun getWeatherDataLocation() {
        mFusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(this)
        mFusedLocationProviderClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            CancellationTokenSource().token
        ).addOnSuccessListener { location ->
            if (location != null) {
                if (isNetworkAvailable(this)) {
                    dashboardViewModel.getWeatherData(
                        latitude = location.latitude.toString(),
                        longitude = location.longitude.toString()
                    )
                } else {
                    showInternetAlertDialog(this)
                }
            } else {
                showAlertDialog {
                    setTitle(context.resources.getString(R.string.important))
                    setMessage(context.resources.getString(R.string.unable_fetch_location))
                    positiveButtonClick(context.resources.getString(R.string.ok)) {}
                }
            }
        }.addOnFailureListener { e ->
            showAlertDialog {
                setTitle(context.resources.getString(R.string.important))
                setMessage(e.message)
                positiveButtonClick(context.resources.getString(R.string.ok)) {}
            }
        }
    }

    private fun initObserver() {
        lifecycleScope.launchWhenCreated {
            dashboardViewModel.weatherResponseLiveData.observe(this@MainActivity) { responseData ->
                progressDialog.dismiss()
                when (responseData) {
                    is NetworkResult.Loading -> {
                        progressDialog.show()
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
                                dashboardViewModel.setData(apiResponse)
                                setGlideImage(
                                    binding.imageViewWeatherIcon,
                                    AppConstant.WEATHER_API_IMAGE_ENDPOINT + "${apiResponse.weather[0].icon}@4x.png"
                                )
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