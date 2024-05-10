package com.example.native_android_flashlight

 import androidx.annotation.NonNull
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel


import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import android.Manifest
import android.content.pm.PackageManager
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.os.Build
import androidx.core.app.ActivityCompat
import android.content.BroadcastReceiver
import android.app.Service
import android.os.IBinder

import io.flutter.plugins.GeneratedPluginRegistrant
import android.content.Intent
import android.content.IntentFilter 

import android.util.Log



import androidx.annotation.RequiresApi
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
 import android.graphics.Color
 import androidx.core.app.NotificationCompat
 import androidx.core.app.NotificationManagerCompat
 

class ProxFlashService : Service() {




    private lateinit var sensorManager: SensorManager
    private lateinit var cameraManager: CameraManager
    private lateinit var proximitySensor: Sensor

    private var isFlashlightOn = false


    private fun initializeProximitySensor() {
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)
        proximitySensor?.let {
            sensorManager.registerListener(proximitySensorEventListener, it, SensorManager.SENSOR_DELAY_NORMAL)
            Toast.makeText(this, "Proximity sensor initialized", Toast.LENGTH_SHORT).show()
        } ?: run {
            Toast.makeText(this, "No proximity sensor found in device..", Toast.LENGTH_SHORT).show()
        }
    }

    private val proximitySensorEventListener = object : SensorEventListener {
        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}

        override fun onSensorChanged(event: SensorEvent) {
            val proximityValue = event.values[0]
            val isProximityLessThan5 = proximityValue < 5f

            if (isProximityLessThan5 && isFlashlightOn) {
                turnOffFlashlight()
            } else if (isProximityLessThan5 && !isFlashlightOn) {
                turnOnFlashlight()
            }
        }
    }

    private fun turnOnFlashlight() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    return
                }
            }
            cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
            cameraManager.setTorchMode(cameraManager.cameraIdList[0], true)
            isFlashlightOn = true

            sendBroadcastToMainActivity(isFlashlightOn)
 
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private fun turnOffFlashlight() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    return
                }
            }
            cameraManager.setTorchMode(cameraManager.cameraIdList[0], false)
            isFlashlightOn = false
            sendBroadcastToMainActivity(isFlashlightOn)
 
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

//    var stopReceiver = object : BroadcastReceiver() {
//         override fun onReceive(context: Context?, intent: Intent?) {
    
//             println("s***************")
//             Log.d("FlashlightService", "stopReceiver received")
//             stopSelf()
//             turnOffFlashlight()
//             stopProximitySensor()

//         }
//     }

 fun stopProximitySensor() {

        println("stopProximitySensor........................")
        sensorManager.unregisterListener(proximitySensorEventListener)
    }


//////to update ui

    private val broadcastAction = "updateUI"

    private fun sendBroadcastToMainActivity(value: Boolean) {
        val intent = Intent(broadcastAction)
        intent.putExtra("resultValue", value)

        println("sendBroadcastToMainActivity is done ******...")

        sendBroadcast(intent)
    }



/////



  override   fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        startForeground()
        serviceRunning = true

        // val filter = IntentFilter("STOP_ACTION_v1")
        // registerReceiver(stopReceiver, filter)
        initializeProximitySensor()
    }
 override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        println("onStartCommand**********************")

        if (intent?.action == "STOP_SERVICE") {
            // val stopIntent = Intent(this, ProxFlashService::class.java)
            // stopIntent.action = "STOP_ACTION_v1"
            // sendBroadcast(stopIntent)
            stopService()
        }
        return super.onStartCommand(intent, flags, startId)
    }
    override fun onDestroy() {
        super.onDestroy()

        stopService()

    }







////// notification
    private val notificationId = 1
    private var serviceRunning = false
    private lateinit var builder: NotificationCompat.Builder
    private lateinit var channel: NotificationChannel
    private lateinit var manager: NotificationManagerCompat


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
        val stopIntent = Intent(this, ProxFlashService::class.java)
        stopIntent.action = "STOP_SERVICE"
              stopIntent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);

        return PendingIntent.getService(this, 0, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)
    }

   private fun stopService() {

            serviceRunning = false

        turnOffFlashlight()
        stopProximitySensor()
         stopForeground(true)
        stopSelf()
    }

}
