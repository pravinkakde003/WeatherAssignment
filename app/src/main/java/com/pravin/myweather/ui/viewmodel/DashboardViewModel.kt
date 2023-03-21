package com.pravin.myweather.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pravin.myweather.api.NetworkResult
import com.pravin.myweather.api.repository.WeatherRepository
import com.pravin.myweather.model.CurrentWeatherResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class DashboardViewModel @Inject constructor(private val weatherRepository: WeatherRepository) :
    ViewModel() {

    val weatherResponseLiveData: LiveData<NetworkResult<CurrentWeatherResponse>>
        get() = weatherRepository.weatherResponseLiveData

    fun getWeatherData(
        latitude: String,
        longitude: String
    ) = viewModelScope.launch {
        weatherRepository.getCurrentWeatherData(latitude = latitude, longitude = longitude)
    }
}