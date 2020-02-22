package com.example.nearbyplace.model

import com.google.gson.annotations.SerializedName

data class ResultsItem(

	@field:SerializedName("types")
	val types: List<String?>? = null,

	@field:SerializedName("icon")
	val icon: String? = null,

	@field:SerializedName("rating")
	val rating: Double? = null,

	@field:SerializedName("photos")
	val photos: List<PhotosItem?>? = null,

	@field:SerializedName("reference")
	val reference: String? = null,

	@field:SerializedName("user_ratings_total")
	val userRatingsTotal: Int? = null,

	@field:SerializedName("price_level")
	val priceLevel: Int? = null,

	@field:SerializedName("scope")
	val scope: String? = null,

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("opening_hours")
	val openingHours: OpeningHours? = null,

	@field:SerializedName("geometry")
	val geometry: Geometry? = null,

	@field:SerializedName("vicinity")
	val vicinity: String? = null,

	@field:SerializedName("id")
	val id: String? = null,

	@field:SerializedName("plus_code")
	val plusCode: PlusCode? = null,

	@field:SerializedName("place_id")
	val placeId: String? = null
)