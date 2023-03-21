package com.pravin.myweather.utils

import android.app.Activity
import android.content.pm.PackageManager
import android.location.LocationManager
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

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

fun Activity.requestPermissions(request: ActivityResultLauncher<Array<String>>, permissions: Array<String>) = request.launch(permissions)

fun Activity.isAllPermissionsGranted(permissions: Array<String>) = permissions.all {
    ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
}