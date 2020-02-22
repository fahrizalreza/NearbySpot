package com.example.nearbyplace.model

import com.google.gson.annotations.SerializedName

class MyPlaces {

    var html_attributions: Array<String>? = null
    var status: String ?= null
    var next_page_token: String? = null
    var results: Array<Results>? = null

}