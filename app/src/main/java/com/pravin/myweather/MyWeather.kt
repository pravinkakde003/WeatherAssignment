package com.pravin.myweather

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyWeather : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}