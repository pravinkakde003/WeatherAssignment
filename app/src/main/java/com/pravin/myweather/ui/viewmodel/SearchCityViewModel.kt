package com.pravin.myweather.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pravin.myweather.api.NetworkResult
import com.pravin.myweather.api.repository.WeatherByCityRepository
import com.pravin.myweather.model.CurrentWeatherResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class SearchCityViewModel @Inject constructor(private val weatherRepository: WeatherByCityRepository) :
    ViewModel() {
    lateinit var apiResponse: CurrentWeatherResponse
    val cityName = MutableLiveData<String>()
    val countryName = MutableLiveData<String>()

    val weatherResponseLiveData: LiveData<NetworkResult<CurrentWeatherResponse>>
        get() = weatherRepository.weatherResponseLiveData

    fun getWeatherDataForCity(
        cityName: String
    ) = viewModelScope.launch {
        weatherRepository.getCurrentWeatherForCityData(cityName = cityName)
    }

    fun setData(apiResponse: CurrentWeatherResponse) {
        this.apiResponse = apiResponse
        if (apiResponse.sys.country.isNotBlank()) {
            countryName.value = apiResponse.sys.country.toString()
        }
        if (apiResponse.name.isNotBlank()) {
            cityName.value = apiResponse.name
        }
    }

    fun getApiResponseObject(): CurrentWeatherResponse? {
        return if (this::apiResponse.isInitialized) {
            apiResponse
        } else null
    }
}