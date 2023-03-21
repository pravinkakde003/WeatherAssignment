package com.pravin.myweather.di

import com.pravin.myweather.BuildConfig
import com.pravin.myweather.api.ApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class NetworkModule {

    @Singleton
    @Provides
    fun providesRetrofit(): Retrofit.Builder {
        return Retrofit.Builder().baseUrl(BuildConfig.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
    }
//
//    @Singleton
//    @Provides
//    fun providesWeatherDataAPI(retrofitBuilder: Retrofit.Builder): ApiService {
//        return retrofitBuilder.build().create(ApiService::class.java)
//    }

    @Singleton
    @Provides
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder().addInterceptor(
            HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }).build()
    }

    @Singleton
    @Provides
    fun providesWeatherDataAPI(
        retrofitBuilder: Retrofit.Builder,
        okHttpClient: OkHttpClient
    ): ApiService {
        return retrofitBuilder.client(okHttpClient).build().create(ApiService::class.java)
    }


}