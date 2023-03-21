package com.pravin.myweather.api

import com.pravin.myweather.BuildConfig
import com.pravin.myweather.model.CurrentWeatherResponse
import com.pravin.myweather.utils.AppConstant
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET(AppConstant.WEATHER + BuildConfig.API_KEY)
    suspend fun getCurrentWeatherData(
        @Query("lat") latitude: String,
        @Query("lon") longitude: String
    ): Response<CurrentWeatherResponse>
}
