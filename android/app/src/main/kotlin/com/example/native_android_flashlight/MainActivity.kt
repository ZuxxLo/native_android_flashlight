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

import io.flutter.plugins.GeneratedPluginRegistrant
import android.content.Intent
import android.content.IntentFilter 

 class MainActivity: FlutterActivity() {

  private val CHANNEL_PROXIMITY_SENSOR = "samples.flutter.dev/proximitysensor"
    private val CHANNEL = "example_service"

    private lateinit var proximitySensor: Sensor
    private lateinit var sensorManager: SensorManager
    private lateinit var cameraManager: CameraManager

    // To keep track of flashlight state
    private var isFlashlightOn = false


    override fun configureFlutterEngine(@NonNull flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL_PROXIMITY_SENSOR).setMethodCallHandler { call, result ->

            if (call.method == "getProximityData") {

                // val proximityData = getProximityData()
                result.success(proximityValue < 5f)
            } else {
                result.notImplemented()
            }
        }
 MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL).setMethodCallHandler {
            // Note: this method is invoked on the main thread.
            call, result ->
            when (call.method) {
                "startExampleService" -> {
                            // Initialize your sensor when the activity is created
        initializeProximitySensor()

        // Initialize CameraManager
        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
                    startService(Intent(this, ExampleService::class.java))
                    result.success("Started!")
                }
                "stopExampleService" -> {
                    stopService(Intent(this, ExampleService::class.java))
                                     stopProximitySensor()
        turnOffFlashlight()

                    result.success("Stopped!")
                }
                else -> {
                    result.notImplemented()
                }
            }
        }
        
    }
        
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
      // Register broadcast receiver
        val filter = IntentFilter("STOP_SENSOR_AND_FLASHLIGHT")
        registerReceiver(stopReceiver, filter)
    }
    private val stopReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {

            println("im clicked stooop")
            // Stop proximity sensor and flashlight
            // You can call the respective methods or set flags to stop them
             
            stopProximitySensor()
        
            // Turn off flashlight
            turnOffFlashlight()

 
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Unregister the receiver

        println("on destroy main activity") 
        unregisterReceiver(stopReceiver)
    }
   

    private fun initializeProximitySensor() {
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)
        if (proximitySensor == null) {
            Toast.makeText(this, "No proximity sensor found in device..", Toast.LENGTH_SHORT).show()
        } else {
            sensorManager.registerListener(proximitySensorEventListener, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL)
            println("Proximity sensor initialized")
                        Toast.makeText(this, "Proximity sensor initialized", Toast.LENGTH_SHORT).show()

        }
    }

    private fun getProximityData(): Boolean {
        // Return true if an object is near the sensor, false otherwise
        // Here, we'll consider if the distance is less than 5cm, it's near, otherwise, it's far.
        return proximityValue < 5f
    }

    private var proximityValue: Float = 0f

    private val proximitySensorEventListener = object : SensorEventListener {
        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}

        override fun onSensorChanged(event: SensorEvent) {
            println("onSensorChanged")

            proximityValue = event.values[0]
            println("sensor value $proximityValue")

            if (proximityValue < 5f) {
                // Turn on flashlight
                if (!isFlashlightOn) {
                    turnOnFlashlight()
                    isFlashlightOn = true
                }
            } else {
                // Turn off flashlight
                if (isFlashlightOn) {
                    turnOffFlashlight()
                    isFlashlightOn = false
                }
            }

            // Toast.makeText(this@MainActivity, "sensor value $proximityValue", Toast.LENGTH_SHORT).show()

        }
    }



private fun stopProximitySensor() {
    // Unregister the sensor listener
    sensorManager.unregisterListener(proximitySensorEventListener)
}


     private fun turnOnFlashlight() {
        try {
            // Check for camera permission if running on Android 6.0 or higher
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    // Permission not granted, request it
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), PERMISSION_REQUEST_CAMERA)
                    return
                }
            }

            // Turn on the flashlight
            cameraManager.setTorchMode(cameraManager.cameraIdList[0], true)
                                isFlashlightOn = true
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private fun turnOffFlashlight() {
        try {
            // Turn off the flashlight
            cameraManager.setTorchMode(cameraManager.cameraIdList[0], false)
                    isFlashlightOn = false

        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    companion object {
        private const val PERMISSION_REQUEST_CAMERA = 1
    }




//  

}
