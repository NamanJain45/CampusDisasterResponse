package com.vjti.campusdisasterresponse.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class NetworkMonitor(
    context: Context
) {

    private val connectivityManager =
        context.getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager

    private val _networkStatus =
        MutableStateFlow<NetworkStatus>(
            NetworkStatus.Offline
        )

    val networkStatus: StateFlow<NetworkStatus> =
        _networkStatus.asStateFlow()

    private val networkCallback =
        object : ConnectivityManager.NetworkCallback() {

            override fun onAvailable(
                network: Network
            ) {
                _networkStatus.value =
                    NetworkStatus.Synchronizing
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                val hasInternet =
                    networkCapabilities.hasCapability(
                        NetworkCapabilities.NET_CAPABILITY_INTERNET
                    ) &&
                    networkCapabilities.hasCapability(
                        NetworkCapabilities.NET_CAPABILITY_VALIDATED
                    )

                _networkStatus.value =
                    if (hasInternet) {
                        NetworkStatus.Online
                    } else {
                        NetworkStatus.Offline
                    }
            }

            override fun onLost(
                network: Network
            ) {
                _networkStatus.value =
                    NetworkStatus.Offline
            }
        }

    fun startMonitoring() {

        val activeNetwork =
            connectivityManager.activeNetwork

        val capabilities =
            connectivityManager.getNetworkCapabilities(
                activeNetwork
            )

        val isConnected =
            capabilities?.hasCapability(
                NetworkCapabilities.NET_CAPABILITY_INTERNET
            ) == true &&
            capabilities.hasCapability(
                NetworkCapabilities.NET_CAPABILITY_VALIDATED
            )

        _networkStatus.value =
            if (isConnected) {
                NetworkStatus.Online
            } else {
                NetworkStatus.Offline
            }

        val request =
            NetworkRequest.Builder()
                .addCapability(
                    NetworkCapabilities.NET_CAPABILITY_INTERNET
                )
                .build()

        connectivityManager.registerNetworkCallback(
            request,
            networkCallback
        )
    }

    fun setSynchronizing() {
        _networkStatus.value =
            NetworkStatus.Synchronizing
    }

    fun stopMonitoring() {
        try {
            connectivityManager.unregisterNetworkCallback(
                networkCallback
            )
        } catch (_: IllegalArgumentException) {
            // Callback already unregistered
        }
    }
}
