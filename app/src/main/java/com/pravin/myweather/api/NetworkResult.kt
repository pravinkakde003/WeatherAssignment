package com.pravin.myweather.api

import com.pravin.myweather.model.ErrorModel

sealed class NetworkResult<T>(val data: T? = null, val errorMessage: ErrorModel? = null) {
    class Loading<T> : NetworkResult<T>()
    class Success<T>(data: T? = null) : NetworkResult<T>(data = data)
    class Error<T>(errorMessage: ErrorModel?) : NetworkResult<T>(errorMessage = errorMessage)
}