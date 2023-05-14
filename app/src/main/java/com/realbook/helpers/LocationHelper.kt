package com.realbook

import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.core.app.ActivityCompat

class LocationHelper(private val context: Context) : LocationListener {
    private var locationManager: LocationManager? = null
    var currentLocation: Location? = null
    private var isGpsEnabled = false
    private var isNetworkEnabled = false

    init {
        locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager?
        isGpsEnabled = locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) ?: false
        isNetworkEnabled = locationManager?.isProviderEnabled(LocationManager.NETWORK_PROVIDER) ?: false
    }

    fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        if (isGpsEnabled) {
            locationManager?.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 15f, this)
        } else if (isNetworkEnabled) {
            locationManager?.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 15f, this)
        }
    }

    fun stopLocationUpdates() {
        locationManager?.removeUpdates(this)
    }

    override fun onLocationChanged(location: Location) {
        currentLocation = location

    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}

    override fun onProviderEnabled(provider: String) {}

    override fun onProviderDisabled(provider: String) {}

}
