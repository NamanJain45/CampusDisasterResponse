package com.vjti.campusdisasterresponse.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.vjti.campusdisasterresponse.R

object NotificationHelper {
    const val CHANNEL_EMERGENCY = "campus_emergency"
    private const val CHANNEL_NAME = "Campus Emergencies"

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(NotificationChannel(CHANNEL_EMERGENCY, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Verified campus emergencies and urgent safety alerts"
                enableVibration(true)
            })
        }
    }

    fun showEmergency(context: Context, title: String, message: String, id: Int = (System.currentTimeMillis() % Int.MAX_VALUE).toInt()) {
        if (Build.VERSION.SDK_INT >= 33 && context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return
        createChannels(context)
        NotificationManagerCompat.from(context).notify(id, NotificationCompat.Builder(context, CHANNEL_EMERGENCY)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setAutoCancel(true)
            .build())
    }
}
