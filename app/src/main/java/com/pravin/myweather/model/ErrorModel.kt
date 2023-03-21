package com.pravin.myweather.model

import java.io.Serializable

data class ErrorModel(
    val message: String,
    val statusCode: Int
) : Serializable