package com.vjti.campusdisasterresponse.network

sealed interface NetworkStatus {
    object Online : NetworkStatus
    object Offline : NetworkStatus
    object Synchronizing : NetworkStatus
}
