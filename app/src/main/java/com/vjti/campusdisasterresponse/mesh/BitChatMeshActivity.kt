package com.vjti.campusdisasterresponse.mesh

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.vjti.campusdisasterresponse.data.local.AppDatabase
import com.vjti.campusdisasterresponse.data.queue.EmergencyEvent
import com.vjti.campusdisasterresponse.data.queue.EmergencyQueueRepository
import com.vjti.campusdisasterresponse.worker.SyncScheduler
import kotlinx.coroutines.launch
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.AdvertisingOptions
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import com.google.android.gms.nearby.connection.DiscoveryOptions
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.PayloadTransferUpdate
import com.google.android.gms.nearby.connection.Strategy
import com.vjti.campusdisasterresponse.R

class BitChatMeshActivity : ComponentActivity() {

    private val serviceId =
        "com.vjti.campusdisasterresponse.bitchat"

    private lateinit var tvLogs: TextView

    private val connectedEndpoints =
        mutableSetOf<String>()

    private val seenMessageIds =
        mutableSetOf<String>()

    private val queueRepository by lazy {
        EmergencyQueueRepository(
            dao = AppDatabase
                .getDatabase(this)
                .emergencyEventDao(),
            scheduleSync = {
                SyncScheduler.scheduleSync(this)
            }
        )
    }

    private var meshStarted = false

    private val payloadCallback =
        object : PayloadCallback() {

            override fun onPayloadReceived(
                endpointId: String,
                payload: Payload
            ) {
                payload.asBytes()?.let { bytes ->

                    val packet =
                        MeshPacketCodec.decode(bytes)

                    if (packet == null) {
                        logMessage(
                            "Ignored invalid mesh packet from $endpointId"
                        )

                        return@let
                    }

                    if (!seenMessageIds.add(packet.messageId)) {
                        logMessage(
                            "Ignored duplicate mesh packet ${packet.messageId}"
                        )

                        return@let
                    }

                    lifecycleScope.launch {
                        try {
                            queueRepository.enqueue(
                                EmergencyEvent(
                                    id = packet.messageId,
                                    timestamp = packet.timestamp,
                                    eventType = packet.type.name,
                                    payload =
                                        """{"messageId":"${packet.messageId}","type":"${packet.type.name}","status":"${packet.status}","timestamp":${packet.timestamp},"ttl":${packet.ttl}}"""
                                )
                            )

                            logMessage(
                                "Persisted mesh packet ${packet.messageId}"
                            )
                        } catch (error: Exception) {
                            logMessage(
                                "Mesh persistence failed: ${error.message}"
                            )
                        }
                    }

                    logMessage(
                        "🚨 ${packet.type.name} from $endpointId: ${packet.status} " +
                            "[${packet.messageId}] TTL=${packet.ttl}"
                    )

                    if (packet.ttl <= 0) {
                        return@let
                    }

                    val relayTargets =
                        connectedEndpoints.filter { it != endpointId }

                    if (relayTargets.isEmpty()) {
                        return@let
                    }

                    val relayedPacket =
                        packet.copy(
                            ttl = packet.ttl - 1
                        )

                    val relayPayload =
                        Payload.fromBytes(
                            MeshPacketCodec.encode(relayedPacket)
                        )

                    Nearby
                        .getConnectionsClient(
                            this@BitChatMeshActivity
                        )
                        .sendPayload(
                            relayTargets,
                            relayPayload
                        )
                        .addOnSuccessListener {
                            logMessage(
                                "Relayed ${packet.messageId} to ${relayTargets.size} peers " +
                                    "with TTL=${relayedPacket.ttl}"
                            )
                        }
                        .addOnFailureListener { error ->
                            logMessage(
                                "Mesh relay failed: ${error.message}"
                            )
                        }
                }
            }

            override fun onPayloadTransferUpdate(
                endpointId: String,
                update: PayloadTransferUpdate
            ) {
                // Prototype does not display transfer progress.
            }
        }

    private val connectionLifecycleCallback =
        object : ConnectionLifecycleCallback() {

            override fun onConnectionInitiated(
                endpointId: String,
                info: ConnectionInfo
            ) {
                logMessage(
                    "Connection initiated by ${info.endpointName}"
                )

                /*
                 * Prototype behavior:
                 * automatically accept nearby peers so the
                 * disaster mesh forms with minimal interaction.
                 */

                Nearby
                    .getConnectionsClient(
                        this@BitChatMeshActivity
                    )
                    .acceptConnection(
                        endpointId,
                        payloadCallback
                    )
            }

            override fun onConnectionResult(
                endpointId: String,
                result: ConnectionResolution
            ) {
                if (
                    result.status.isSuccess
                ) {
                    connectedEndpoints.add(
                        endpointId
                    )

                    logMessage(
                        "✅ Connected to $endpointId"
                    )
                } else {
                    logMessage(
                        "Connection failed for $endpointId: ${result.status.statusCode}"
                    )
                }
            }

            override fun onDisconnected(
                endpointId: String
            ) {
                connectedEndpoints.remove(
                    endpointId
                )

                logMessage(
                    "❌ Disconnected from $endpointId"
                )
            }
        }

    private val endpointDiscoveryCallback =
        object : EndpointDiscoveryCallback() {

            override fun onEndpointFound(
                endpointId: String,
                info: DiscoveredEndpointInfo
            ) {
                logMessage(
                    "Found device: ${info.endpointName}. Requesting connection..."
                )

                Nearby
                    .getConnectionsClient(
                        this@BitChatMeshActivity
                    )
                    .requestConnection(
                        Build.MODEL,
                        endpointId,
                        connectionLifecycleCallback
                    )
                    .addOnFailureListener { error ->

                        logMessage(
                            "Connection request failed: ${error.message}"
                        )
                    }
            }

            override fun onEndpointLost(
                endpointId: String
            ) {
                logMessage(
                    "Lost discovered endpoint: $endpointId"
                )
            }
        }

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(
            savedInstanceState
        )

        setContentView(
            R.layout.activity_bitchat_mesh
        )

        tvLogs =
            findViewById(
                R.id.tvLogs
            )

        findViewById<Button>(
            R.id.btnSos
        ).setOnClickListener {

            broadcastSos()
        }

        logMessage(
            "BitChat mesh prototype initialized."
        )

        requestRequiredPermissions()
    }

    private fun startBitChatMesh() {

        if (meshStarted) {
            return
        }

        meshStarted = true

        val strategy =
            Strategy.P2P_CLUSTER

        val advertisingOptions =
            AdvertisingOptions
                .Builder()
                .setStrategy(
                    strategy
                )
                .build()

        Nearby
            .getConnectionsClient(
                this
            )
            .startAdvertising(
                Build.MODEL,
                serviceId,
                connectionLifecycleCallback,
                advertisingOptions
            )
            .addOnSuccessListener {

                logMessage(
                    "Advertising started..."
                )
            }
            .addOnFailureListener { error ->

                meshStarted = false

                logMessage(
                    "Advertising failed: ${error.message}"
                )
            }

        val discoveryOptions =
            DiscoveryOptions
                .Builder()
                .setStrategy(
                    strategy
                )
                .build()

        Nearby
            .getConnectionsClient(
                this
            )
            .startDiscovery(
                serviceId,
                endpointDiscoveryCallback,
                discoveryOptions
            )
            .addOnSuccessListener {

                logMessage(
                    "Discovery started..."
                )
            }
            .addOnFailureListener { error ->

                logMessage(
                    "Discovery failed: ${error.message}"
                )
            }
    }

    private fun broadcastSos() {

        if (
            connectedEndpoints.isEmpty()
        ) {
            logMessage(
                "No peers connected. Cannot send SOS."
            )

            return
        }

        val packet =
            MeshPacket(
                type = MeshMessageType.SOS,
                status = "TRAPPED"
            )

        seenMessageIds.add(packet.messageId)

        val payload =
            Payload.fromBytes(
                MeshPacketCodec.encode(packet)
            )

        Nearby
            .getConnectionsClient(
                this
            )
            .sendPayload(
                connectedEndpoints.toList(),
                payload
            )
            .addOnSuccessListener {

                logMessage(
                    "Broadcasted status to ${connectedEndpoints.size} peers."
                )
            }
            .addOnFailureListener { error ->

                logMessage(
                    "SOS transmission failed: ${error.message}"
                )
            }
    }

    private fun logMessage(
        message: String
    ) {
        runOnUiThread {

            tvLogs.append(
                "\n$message"
            )
        }
    }

    private fun requestRequiredPermissions() {

        val permissions =
            mutableListOf(
                Manifest.permission.ACCESS_FINE_LOCATION
            )

        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.S
        ) {
            permissions.add(
                Manifest.permission.BLUETOOTH_SCAN
            )

            permissions.add(
                Manifest.permission.BLUETOOTH_ADVERTISE
            )

            permissions.add(
                Manifest.permission.BLUETOOTH_CONNECT
            )
        }

        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.TIRAMISU
        ) {
            permissions.add(
                Manifest.permission.NEARBY_WIFI_DEVICES
            )
        }

        val needed =
            permissions.filter { permission ->

                ActivityCompat
                    .checkSelfPermission(
                        this,
                        permission
                    ) !=
                    PackageManager.PERMISSION_GRANTED
            }

        if (
            needed.isNotEmpty()
        ) {
            ActivityCompat.requestPermissions(
                this,
                needed.toTypedArray(),
                REQUEST_PERMISSIONS_CODE
            )
        } else {
            startBitChatMesh()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults
        )

        if (
            requestCode ==
            REQUEST_PERMISSIONS_CODE
        ) {
            if (
                grantResults.isNotEmpty() &&
                grantResults.all {
                    it ==
                    PackageManager.PERMISSION_GRANTED
                }
            ) {
                startBitChatMesh()
            } else {
                logMessage(
                    "Permissions denied. Hardware unavailable for BitChat."
                )
            }
        }
    }

    override fun onDestroy() {

        Nearby
            .getConnectionsClient(
                this
            )
            .apply {
                stopAdvertising()
                stopDiscovery()
                stopAllEndpoints()
            }

        connectedEndpoints.clear()

        meshStarted = false

        super.onDestroy()
    }

    companion object {
        private const val REQUEST_PERMISSIONS_CODE =
            2301
    }
}
