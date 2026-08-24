package com.vjti.campusdisasterresponse.network

/**
 * Development backend address used by the physical-device MVP build.
 * Keep the Android phone and development laptop on the same network.
 * Change this single value when the laptop's LAN IP changes.
 */
object BackendConfig {
    const val BASE_URL = "http://192.168.1.163:3000"
}
