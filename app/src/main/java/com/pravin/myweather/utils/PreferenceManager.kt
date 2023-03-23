package com.pravin.myweather.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.pravin.myweather.model.CurrentWeatherResponse
import com.pravin.myweather.utils.AppConstant.PREFERENCE_FILE
import com.pravin.myweather.utils.AppConstant.SAVED_CITY_DATA_KEY
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class PreferenceManager @Inject constructor(@ApplicationContext context: Context) {
    private var preferences: SharedPreferences =
        context.getSharedPreferences(PREFERENCE_FILE, Context.MODE_PRIVATE)

    fun saveCityWeatherObject(weatherObject: CurrentWeatherResponse?) {
        val editor = preferences.edit()
        editor.putString(SAVED_CITY_DATA_KEY, Gson().toJson(weatherObject))
        editor.apply()
    }

    fun getSelectedStoreObject(): CurrentWeatherResponse? {
        val stringObject = preferences.getString(SAVED_CITY_DATA_KEY, null)
        return Gson().fromJson(stringObject, CurrentWeatherResponse::class.java)
    }

    fun clearAllData() {
        val editor = preferences.edit()
        editor.clear().apply()
    }
}