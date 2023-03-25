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
    val weatherCondition = MutableLiveData<CurrentWeatherResponse>()
    val currentDate = MutableLiveData<String>()
    val isWithData = MutableLiveData<Boolean>()
    val isWithNoData = MutableLiveData<Boolean>()
    val isShowLoading = MutableLiveData<Boolean>()

    val weatherResponseLiveData: LiveData<NetworkResult<CurrentWeatherResponse>>
        get() = weatherRepository.weatherResponseLiveData

    /**
     * Call API and get weather data
     * @param latitude
     * @param longitude
     */
    fun getWeatherData(
        latitude: String,
        longitude: String
    ) = viewModelScope.launch {
        weatherRepository.getCurrentWeatherData(latitude = latitude, longitude = longitude)
    }

    /**
     * Set data to livedata Variables
     * @param apiResponse CurrentWeatherResponse response object
     */
    fun setData(apiResponse: CurrentWeatherResponse) {
        weatherCondition.value = apiResponse
        currentDate.value = getCurrentDateTime(AppConstant.DATE_FORMAT)
    }
}