package com.pravin.myweather.ui.activity

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.pravin.myweather.R
import com.pravin.myweather.databinding.ActivityMainBinding
import com.pravin.myweather.ui.fragment.DashboardFragment
import com.pravin.myweather.utils.isLocationEnabled
import com.pravin.myweather.utils.showToast
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    lateinit var mFusedLocationProviderClient: FusedLocationProviderClient

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportFragmentManager
            .beginTransaction()
            .addToBackStack("ItemFragment")
            .add(R.id.frameLayout, DashboardFragment())
            .commit()

        requestLocationPermission()
        showToast("Please turn on" + " your location...")
    }

    private fun requestLocationPermission() {
        locationPermissionRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                Log.e("TAG", "ACCESS_FINE_LOCATION")
                checkGPS()
            }
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                Log.e("TAG", "ACCESS_COARSE_LOCATION")
                checkGPS()
            }
            else -> {
                Log.e("TAG", "No location access granted")
            }
        }
    }


    private fun checkGPS() {
        if (this.isLocationEnabled()) {
            getLocation()
        } else {
            showToast("Please turn on" + " your location...")
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        checkGPS()
    }

    fun getLocation() {
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
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
}