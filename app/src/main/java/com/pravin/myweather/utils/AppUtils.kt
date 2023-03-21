package com.pravin.myweather.utils

import android.app.Activity
import android.location.LocationManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

fun Activity.isLocationEnabled(): Boolean {
    val locationManager = getSystemService(AppCompatActivity.LOCATION_SERVICE) as LocationManager
    return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
        LocationManager.NETWORK_PROVIDER
    )
}

fun Activity.showToast(textToDisplay: String) {
    if (textToDisplay.isBlank()) return
    Toast.makeText(this, textToDisplay, Toast.LENGTH_SHORT).show()
}