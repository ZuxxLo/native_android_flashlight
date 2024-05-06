package com.example.native_android_flashlight

import androidx.annotation.RequiresApi
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import java.util.Timer
import java.util.TimerTask
import androidx.core.app.NotificationManagerCompat
 

class ExampleServices : Service() {
    val notificationId = 1
    var serviceRunning = false
    lateinit var builder: NotificationCompat.Builder
    lateinit var channel: NotificationChannel
    lateinit var manager: NotificationManager

    override fun onCreate() {
        super.onCreate()
        startForeground()
        serviceRunning = true
        Timer().schedule(object : TimerTask() {
            override fun run() {
                // if (serviceRunning == true) {
                //     updateNotification("I got updated!")
                // }
            }
        }, 1)
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceRunning = false
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(channelId: String, channelName: String): String {
        channel = NotificationChannel(channelId,
            channelName, NotificationManager.IMPORTANCE_NONE)
        channel.lightColor = Color.BLUE
        channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
        return channelId
    }

    private fun startForeground() {
        val channelId =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createNotificationChannel("example_service", "Example Service")
            } else {
                // If earlier version channel ID is not used
                // https://developer.android.com/reference/android/support/v4/app/NotificationCompat.Builder.html#NotificationCompat.Builder(android.content.Context)
                ""
            }
        builder = NotificationCompat.Builder(this, channelId)
        // Action to stop the service
        val stopIntent = Intent(this, ExampleService::class.java)
     stopIntent.action = "STOP_SERVICE"
        val stopPendingIntent = PendingIntent.getBroadcast(this, 0, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)
 
      builder
    .setOngoing(true)
    .setOnlyAlertOnce(true)
    .setSmallIcon(R.mipmap.ic_launcher)
    .setContentTitle("Service is running")
    .setContentText("cover to start/stop the torch")
    .setCategory(Notification.CATEGORY_SERVICE)
    .addAction(android.R.drawable.ic_media_pause, "Stop", stopPendingIntent) // Add stop action button
        startForeground(1, builder.build()) // Add stop action button
     }

    private fun updateNotification(text: String) {
        builder
            .setContentText(text)
        manager.notify(notificationId, builder.build());
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    if (intent?.action == "STOP_SERVICE") {
        println("STOP_SERVICE 111 ")
        stopService()

        // Send broadcast to stop proximity sensor and flashlight
        val stopIntent = Intent("STOP_SENSOR_AND_FLASHLIGHT")
        sendBroadcast(stopIntent)
    }
    return super.onStartCommand(intent, flags, startId)
    }

    private fun stopService() {
        stopForeground(true)
        stopSelf()
        
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }
}


class ExampleService : Service() {
    private val notificationId = 1
    private var serviceRunning = false
    private lateinit var builder: NotificationCompat.Builder
    private lateinit var channel: NotificationChannel
    private lateinit var manager: NotificationManagerCompat

    override fun onCreate() {
        super.onCreate()
        startForeground()
        serviceRunning = true
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceRunning = false
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(channelId: String, channelName: String): String {
        channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_NONE)
        channel.lightColor = Color.BLUE
        channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        manager = NotificationManagerCompat.from(this)
        manager.createNotificationChannel(channel)
        return channelId
    }

    private fun startForeground() {
        val channelId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel("example_service", "Example Service")
        } else {
            ""
        }

           val notificationIntent = Intent(this, MainActivity::class.java)
    val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent,PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)
        builder = NotificationCompat.Builder(this, channelId)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Service is running")
            .setContentText("Tap to start/stop the torch")
            .setCategory(Notification.CATEGORY_SERVICE)
               .setContentIntent(pendingIntent)
            .addAction(android.R.drawable.ic_media_pause, "Stop", stopPendingIntent())
        startForeground(notificationId, builder.build())
    }

    private fun stopPendingIntent(): PendingIntent {
        val stopIntent = Intent(this, ExampleService::class.java)
        stopIntent.action = "STOP_SERVICE"
              stopIntent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);

        return PendingIntent.getService(this, 0, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "STOP_SERVICE") {
            println("onStartCommand")
         val stopIntent = Intent(this, StopReceiver::class.java)
stopIntent.action = "com.example.native_android_flashlight.STOP_SENSOR_AND_FLASHLIGHT"
sendBroadcast(stopIntent)

            stopService()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun stopService() {
        stopForeground(true)
        stopSelf()
    }

    override fun onBind(intent: Intent): IBinder? = null
}