package com.pravin.myweather.api.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.pravin.myweather.BuildConfig
import com.pravin.myweather.api.ApiService
import com.pravin.myweather.api.NetworkResult
import com.pravin.myweather.model.CurrentWeatherResponse
import com.pravin.myweather.model.ErrorModel
import com.pravin.myweather.utils.AppConstant
import retrofit2.Response
import java.net.SocketTimeoutException
import javax.inject.Inject

class WeatherRepository @Inject constructor(private val storeApiServices: ApiService) {

    private val weatherResponseDataMutableLiveData =
        MutableLiveData<NetworkResult<CurrentWeatherResponse>>()

    val weatherResponseLiveData: LiveData<NetworkResult<CurrentWeatherResponse>>
        get() = weatherResponseDataMutableLiveData

    suspend fun getCurrentWeatherData(
        latitude: String,
        longitude: String
    ) {
        try {
            weatherResponseDataMutableLiveData.postValue(NetworkResult.Loading())
            val response = storeApiServices.getCurrentWeatherData(
                latitude = latitude,
                longitude = longitude,
                unit = AppConstant.UNIT_METRIC
            )
            handleResponse(response)
        } catch (exception: Exception) {
            if (exception is SocketTimeoutException) {
                weatherResponseDataMutableLiveData.postValue(
                    NetworkResult.Error(
                        ErrorModel(
                            "SocketTimeoutException",
                            10
                        )
                    )
                )
            } else {
                weatherResponseDataMutableLiveData.postValue(
                    NetworkResult.Error(
                        ErrorModel(
                            "Network Exception",
                            10
                        )
                    )
                )
            }
        }
    }

    private fun handleResponse(response: Response<CurrentWeatherResponse>) {
        if (response.isSuccessful && response.body() != null) {
            weatherResponseDataMutableLiveData.postValue(NetworkResult.Success(response.body()))
        } else if (response.errorBody() != null) {
            val errorModel = Gson().fromJson(response.errorBody()?.string(), ErrorModel::class.java)
            weatherResponseDataMutableLiveData.postValue(NetworkResult.Error(errorModel))
        } else {
            weatherResponseDataMutableLiveData.postValue(
                NetworkResult.Error(
                    ErrorModel(
                        "Exception",
                        10
                    )
                )
            )
        }
    }
}