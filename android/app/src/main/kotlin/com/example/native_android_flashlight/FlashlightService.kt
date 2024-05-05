package com.example.native_android_flashlight

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import android.app.Service
 
class FlashlightService : Service() {

    companion object {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "FlashlightServiceChannel"
    }

      fun onCreate(intent: Intent?, flags: Int, startId: Int): Int {
 
        val input = intent?.getStringExtra("inputExtra")
        startForeground(NOTIFICATION_ID, createNotification(input ?: "Foreground Service Running"))
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

   private fun createNotification(message: String): Notification {
    val notificationIntent = Intent(this, MainActivity::class.java)
    val stopIntent = Intent(this, FlashlightService::class.java).apply {
        action = "STOP_SERVICE"
    }
    val pendingIntent = PendingIntent.getActivity(
        this,
        0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT
    )
    val stopPendingIntent = PendingIntent.getService(
        this,
        0, stopIntent, PendingIntent.FLAG_CANCEL_CURRENT
    )

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        createNotificationChannel()
    }

    val actionStop = NotificationCompat.Action.Builder(
        android.R.drawable.ic_media_pause, // Icon
        "Stop Service", // Title
        stopPendingIntent // Intent
    ).build()

    return NotificationCompat.Builder(this, CHANNEL_ID)
        .setContentTitle("Flashlight Service")
        .setContentText(message)
        .setContentIntent(pendingIntent)
        .addAction(actionStop)
        .build()
}
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val serviceChannel = NotificationChannel(
            CHANNEL_ID,
            "Flashlight Service Channel",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(serviceChannel)
    }
}
