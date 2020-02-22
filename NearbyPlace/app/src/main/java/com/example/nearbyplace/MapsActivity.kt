package com.example.nearbyplace

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.annotation.IntegerRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.nearbyplace.common.Common
import com.example.nearbyplace.model.MyPlaces
import com.example.nearbyplace.remote.IGoogleAPIService
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import kotlinx.android.synthetic.main.activity_maps.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    private var latitude:Double=0.toDouble()
    private  var  longitude:Double=0.toDouble()

    private lateinit var mLastLocation:Location
    private  var mMarker:Marker ?= null

    // location
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    lateinit var locationRequest: LocationRequest
    lateinit var locationCallback: LocationCallback

    companion object {
        private const val MY_PERMISSION_CODE : Int = 1000
    }

    lateinit var mService: IGoogleAPIService

    internal lateinit var currentPlace: MyPlaces

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // init service
        mService = Common.googleAPIService
        // request runtime permission
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkLocationPermission()) {
                buildLocationRequest()
                buildLocationCallback()

                fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
                fusedLocationProviderClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.myLooper()
                )

                mapsBottomNavigationView.setOnNavigationItemSelectedListener { item ->
                    when(item.itemId) {
                        R.id.action_market -> nearByPlace("market")
                        R.id.action_school -> nearByPlace("school")
                        R.id.action_restaurant -> nearByPlace("restaurant")
                        R.id.action_haspital -> nearByPlace("hospital")

                    }

                    true
                }
            }
        } else {
            buildLocationRequest()
            buildLocationCallback()

            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
            fusedLocationProviderClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.myLooper()
            )
        }

    }

    private fun checkLocationPermission() : Boolean {
        if(ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            if(ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {

                ActivityCompat.requestPermissions(this,
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), MY_PERMISSION_CODE)

            } else {
                ActivityCompat.requestPermissions(this,
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), MY_PERMISSION_CODE)
            }

            return false

        } else {

            return true
        }
    }

    private fun nearByPlace(typePlace: String) {

        // clear all maarker on map
        mMap.clear()
        // build URL request base on location
        val url = getUrl(latitude, longitude, typePlace)

        mService.retrieveNearbyPlaces(url)
            .enqueue(object: Callback<MyPlaces> {
                override fun onResponse(call: Call<MyPlaces>, response: Response<MyPlaces>) {
                    currentPlace = response.body()!!

                    if(response.isSuccessful) {

                        for (i in 0 until response.body()!!.results!!.size) {
                            val markerOptions = MarkerOptions()
                            val googlePlace = response.body()!!.results!![i]
                            val lat = googlePlace.geometry!!.location!!.lat
                            val lng = googlePlace.geometry!!.location!!.lng
                            val placeName = googlePlace.name
                            val latLng = LatLng(lat!!, lng!!)

                            markerOptions.position(latLng)
                            markerOptions.title(placeName)
                            if(typePlace.equals("market"))
                                //markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_market))
                                markerOptions.icon(bitmapDescriptorFromVector(this@MapsActivity,
                                    R.drawable.ic_shopping_cart_black_24dp))
                            else if(typePlace.equals("school"))
                                //markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_school))
                                markerOptions.icon(bitmapDescriptorFromVector(this@MapsActivity,
                                    R.drawable.ic_school_black_24dp))
                            else if(typePlace.equals("restaurant"))
                                //markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_restaurant))
                                markerOptions.icon(bitmapDescriptorFromVector(this@MapsActivity,
                                    R.drawable.ic_restaurant_black_24dp))
                            else if(typePlace.equals("hospital"))
                                //markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_hospital_white))
                                markerOptions.icon(bitmapDescriptorFromVector(this@MapsActivity,
                                    R.drawable.ic_local_hospital_black_24dp))
                            else
                                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))

                            markerOptions.snippet(i.toString())  // assign index for market

                            // add mareker to map
                            mMap.addMarker(markerOptions)
                            // move camera
                            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
                            mMap.animateCamera(CameraUpdateFactory.zoomTo(11f))

                        }

                    }

                }

                override fun onFailure(call: Call<MyPlaces>, t: Throwable) {
                    Toast.makeText(baseContext, "" + t.message, Toast.LENGTH_SHORT).show()
                }

            })
    }

    private fun getUrl(latitude: Double, longitude: Double, typePlace: String): String {
        val googlePlaceUrl = StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json")
        googlePlaceUrl.append("?location=$latitude,$longitude")
        googlePlaceUrl.append("&radius=10000")  // 10 km
        googlePlaceUrl.append("&type=$typePlace")
        googlePlaceUrl.append("&key=AIzaSyAFULOiGuwOKw3_CdlwubrcH9XNES-sJYw")

        Log.d("URL DEBUG",googlePlaceUrl.toString())
        return googlePlaceUrl.toString()
    }

    private fun buildLocationRequest()  {
        locationRequest = LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 5000
        locationRequest.fastestInterval = 3000
        locationRequest.smallestDisplacement = 10f

    }

    private fun buildLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult?) {
                // retrieve location
                mLastLocation = p0!!.locations.get(p0.locations.size - 1)

                if(mMarker != null) {
                    mMarker!!.remove()
                }

                latitude = mLastLocation.latitude
                longitude = mLastLocation.longitude

                val latLng = LatLng(latitude, longitude)
                val markerOptions = MarkerOptions()
                    .position(latLng)
                    .title("Your position")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                mMarker = mMap.addMarker(markerOptions)

                // move camera
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
                mMap.animateCamera(CameraUpdateFactory.zoomTo(11f))
            }
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // init google play services
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {

                mMap.isMyLocationEnabled = true
            } else {
                mMap.isMyLocationEnabled = false
            }

            // enable zoom control
            mMap.uiSettings.isZoomControlsEnabled = true

            // make event klik on marker
            mMap.setOnMarkerClickListener { marker ->
                // when user select marker, retrieve place result into static variable
                Common.currentResult = currentPlace!!.results!![Integer.parseInt(marker.snippet)]
                startActivity(Intent(this@MapsActivity, ViewPlaceActivity::class.java))
                true
            }
        }
    }

    override fun onStop() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        super.onStop()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
            when(requestCode) {
                MY_PERMISSION_CODE -> {
                    if(grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                        if(ContextCompat.checkSelfPermission(this,
                                android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                            if(checkLocationPermission()) {
                                buildLocationRequest()
                                buildLocationCallback()

                                fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
                                fusedLocationProviderClient.requestLocationUpdates(
                                    locationRequest,
                                    locationCallback,
                                    Looper.myLooper()
                                )
    
                                mMap.isMyLocationEnabled = true
                                }
                        } else {
                            Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
    }

    private fun bitmapDescriptorFromVector(context: Context, @DrawableRes  vectorDrawableResourceId: Int): BitmapDescriptor {
        val background = ContextCompat.getDrawable(context, vectorDrawableResourceId)
        background!!.setBounds(0, 0, background.intrinsicWidth, background.intrinsicHeight)

        val vectorDrawable = ContextCompat.getDrawable(context, vectorDrawableResourceId)
        vectorDrawable!!.setBounds(
            40,
            20,
            vectorDrawable.intrinsicWidth + 40,
            vectorDrawable.intrinsicHeight + 20
        )
        val bitmap = Bitmap.createBitmap(
            background.intrinsicWidth,
            background.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        background.draw(canvas)
        vectorDrawable.draw(canvas)

        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }
}
