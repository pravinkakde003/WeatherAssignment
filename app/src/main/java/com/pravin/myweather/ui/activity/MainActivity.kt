package com.pravin.myweather.ui.activity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.pravin.myweather.BuildConfig
import com.pravin.myweather.R
import com.pravin.myweather.api.NetworkResult
import com.pravin.myweather.databinding.ActivityMainBinding
import com.pravin.myweather.model.CurrentWeatherResponse
import com.pravin.myweather.ui.viewmodel.DashboardViewModel
import com.pravin.myweather.utils.*
import com.pravin.myweather.utils.AppConstant.LOCATION_REQUEST_CODE
import com.pravin.myweather.utils.AppConstant.RESPONSE_OBJECT_KEY
import dagger.hilt.android.AndroidEntryPoint
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks {
    private lateinit var binding: ActivityMainBinding
    private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient
    private val dashboardViewModel: DashboardViewModel by viewModels()

    @Inject
    lateinit var preferenceManager: PreferenceManager

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
        checkStoredCityData()
    }

    /**
     * Initialize the initial state of view
     */
    private fun initView() {
        dashboardViewModel.isWithData.value = false
        dashboardViewModel.isWithNoData.value = false
        binding.includedDataLayout.imageViewRefreshIcon.setOnClickListener {
            dashboardViewModel.isWithData.value = false
            dashboardViewModel.isShowLoading.value = true
            checkGPSPermission()
        }
    }

    /**
     * Check for stored city data
     */
    private fun checkStoredCityData() {
        val storedCityObject = preferenceManager.getSelectedStoreObject()
        if (null != storedCityObject) {
            updateUI(storedCityObject)
        } else {
            checkGPSPermission()
        }
    }

    /**
     * Check for GPS enable or not
     * if not navigate user to setting to enable GPS
     */
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

    override fun onResume() {
        super.onResume()
        if (!isLocationEnabled()) {
            showRetryLayout()
        }
    }

    /**
     * Retry view
     */
    private fun showRetryLayout() {
        dashboardViewModel.isWithNoData.value = true
        binding.includedNoDataLayout.buttonRetry.setOnClickListener {
            dashboardViewModel.isWithNoData.value = false
            dashboardViewModel.isShowLoading.value = true
            checkGPSPermission()
        }
    }

    /**
     * Get location if user have permission else ask for location permission
     */
    private fun getLocationData() {
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

    /**
     * Get weather data by using LocationServices and call API to get data
     */
    private fun getWeatherDataLocation() {
        mFusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(this)
        mFusedLocationProviderClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            CancellationTokenSource().token
        ).addOnSuccessListener { location ->
            if (location != null) {
                if (isNetworkAvailable(this)) {
                    dashboardViewModel.isShowLoading.value = true
                    dashboardViewModel.getWeatherData(
                        latitude = location.latitude.toString(),
                        longitude = location.longitude.toString()
                    )
                } else {
                    showRetryLayout()
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

    /**
     * Init observer for api response
     */
    private fun initObserver() {
        lifecycleScope.launchWhenCreated {
            dashboardViewModel.weatherResponseLiveData.observe(this@MainActivity) { responseData ->
                dashboardViewModel.isShowLoading.value = false
                when (responseData) {
                    is NetworkResult.Loading -> {
                        dashboardViewModel.isShowLoading.value = true
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
                                updateUI(apiResponse)
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

    /**
     * Update UI
     * @param apiResponse CurrentWeatherResponse response object
     */
    private fun updateUI(apiResponse: CurrentWeatherResponse) {
        dashboardViewModel.isWithNoData.value = false
        dashboardViewModel.isWithData.value = true
        dashboardViewModel.setData(apiResponse)
        setGlideImage(
            binding.includedDataLayout.imageViewWeatherIcon,
            BuildConfig.WEATHER_API_IMAGE_ENDPOINT + "${apiResponse.weather[0].icon}@4x.png"
        )
    }

    private val startForResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            data?.let {
                val dataObject: CurrentWeatherResponse =
                    data.getSerializableExtra(RESPONSE_OBJECT_KEY) as CurrentWeatherResponse
                updateUI(dataObject)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.navigation_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.search -> {
                startForResult.launch(Intent(this, SearchActivity::class.java))
                return true
            }
            R.id.delete -> {
                preferenceManager.clearAllData()
                checkGPSPermission()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}