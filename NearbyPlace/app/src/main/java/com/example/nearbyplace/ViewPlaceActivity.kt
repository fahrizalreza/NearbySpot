package com.example.nearbyplace

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.nearbyplace.remote.IGoogleAPIService

class ViewPlaceActivity : AppCompatActivity() {

    internal lateinit var mService: IGoogleAPIService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_place)
    }
}
