package com.example.nearbyplace.remote

import com.example.nearbyplace.model.MyPlaces
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Url

interface IGoogleAPIService {
    @GET
    fun retrieveNearbyPlaces(@Url url: String): Call<MyPlaces>
}