package com.example.nearbyplace.common

import com.example.nearbyplace.model.Results
import com.example.nearbyplace.remote.IGoogleAPIService
import com.example.nearbyplace.remote.RetrofitClient

object Common {

    private val GOOGLE_API_URL = "https://maps.googleapis.com/maps/"

    var currentResult:Results? = null
    val googleAPIService: IGoogleAPIService
        get() = RetrofitClient.getClient(GOOGLE_API_URL).create(IGoogleAPIService::class.java)
}