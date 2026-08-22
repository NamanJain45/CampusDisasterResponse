package com.vjti.campusdisasterresponse.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource

class LocationHandler(
    private val activity: ComponentActivity,
    private val onLocationResult: (
        latitude: Double?,
        longitude: Double?,
        error: String?
    ) -> Unit
) {

    private val fusedLocationClient =
        LocationServices.getFusedLocationProviderClient(
            activity
        )

    private val locationPermissionRequest =
        activity.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->

            when {
                permissions.getOrDefault(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    false
                ) -> {
                    fetchLocation()
                }

                permissions.getOrDefault(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    false
                ) -> {
                    fetchLocation()
                }

                else -> {
                    onLocationResult(
                        null,
                        null,
                        "Permission denied by user"
                    )
                }
            }
        }

    fun requestLocation() {

        if (hasPermissions()) {
            fetchLocation()
        } else {

            locationPermissionRequest.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun hasPermissions(): Boolean {

        val fineGranted =
            ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        val coarseGranted =
            ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        return fineGranted || coarseGranted
    }

    private fun isLocationEnabled(): Boolean {

        val locationManager =
            activity.getSystemService(
                Context.LOCATION_SERVICE
            ) as LocationManager

        return locationManager.isProviderEnabled(
            LocationManager.GPS_PROVIDER
        ) ||
            locationManager.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER
            )
    }

    private fun fetchLocation() {

        if (!isLocationEnabled()) {

            onLocationResult(
                null,
                null,
                "Device location services (GPS/Network) are disabled"
            )

            return
        }

        try {

            val cancellationTokenSource =
                CancellationTokenSource()

            fusedLocationClient
                .getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    cancellationTokenSource.token
                )
                .addOnSuccessListener { location ->

                    if (location != null) {

                        onLocationResult(
                            location.latitude,
                            location.longitude,
                            null
                        )

                    } else {

                        onLocationResult(
                            null,
                            null,
                            "Location unavailable at this moment"
                        )
                    }
                }
                .addOnFailureListener { exception ->

                    onLocationResult(
                        null,
                        null,
                        exception.localizedMessage
                    )
                }

        } catch (e: SecurityException) {

            onLocationResult(
                null,
                null,
                "Security exception: ${e.message}"
            )
        }
    }
}
