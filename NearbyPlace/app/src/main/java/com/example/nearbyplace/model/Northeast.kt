package com.example.nearbyplace.model

import com.google.gson.annotations.SerializedName

data class Northeast(

	@field:SerializedName("lng")
	val lng: Double? = null,

	@field:SerializedName("lat")
	val lat: Double? = null
)