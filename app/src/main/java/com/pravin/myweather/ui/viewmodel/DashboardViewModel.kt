package com.pravin.myweather.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pravin.myweather.api.NetworkResult
import com.pravin.myweather.api.repository.WeatherRepository
import com.pravin.myweather.model.CurrentWeatherResponse
import com.pravin.myweather.utils.AppConstant
import com.pravin.myweather.utils.getCurrentDateTime
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class DashboardViewModel @Inject constructor(private val weatherRepository: WeatherRepository) :
    ViewModel() {
    val currentTemp = MutableLiveData<String>()
    val cityName = MutableLiveData<String>()
    val currentDate = MutableLiveData<String>()
    val weatherDescription = MutableLiveData<String>()
    val weatherMinMax = MutableLiveData<String>()
    val feelsLike = MutableLiveData<String>()
    val pressure = MutableLiveData<String>()
    val humidity = MutableLiveData<String>()
    val visibility = MutableLiveData<String>()

    val weatherResponseLiveData: LiveData<NetworkResult<CurrentWeatherResponse>>
        get() = weatherRepository.weatherResponseLiveData

    fun getWeatherData(
        latitude: String,
        longitude: String
    ) = viewModelScope.launch {
        weatherRepository.getCurrentWeatherData(latitude = latitude, longitude = longitude)
    }

    fun setData(apiResponse: CurrentWeatherResponse) {
        if (apiResponse.main.temp.toString().isNotBlank()) {
            currentTemp.value = apiResponse.main.temp.toString()
        }
        if (apiResponse.name.isNotBlank()) {
            cityName.value = apiResponse.name
        }

        currentDate.value = getCurrentDateTime(AppConstant.DATE_FORMAT)

        if (apiResponse.weather[0].description.isNotBlank()) {
            weatherDescription.value = apiResponse.weather[0].description
        }

        if (apiResponse.main.temp_min.toString()
                .isNotBlank() && apiResponse.main.temp_max.toString().isNotBlank()
        ) {
            weatherMinMax.value =
                "Min: " + apiResponse.main.temp_min.toString() + " | Max: " + apiResponse.main.temp_max.toString()
        }

        if (apiResponse.main.feels_like.toString().isNotBlank()) {
            feelsLike.value = apiResponse.main.feels_like.toString()
        }

        if (apiResponse.main.pressure.toString().isNotBlank()) {
            pressure.value = apiResponse.main.pressure.toString()
        }

        if (apiResponse.main.humidity.toString().isNotBlank()) {
            humidity.value = apiResponse.main.humidity.toString()
        }

        if (apiResponse.visibility.toString().isNotBlank()) {
            val visibilityInKm = apiResponse.visibility / 1000
            visibility.value = visibilityInKm.toString()
        }
    }
}