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

import android.util.Log


class MainActivity : FlutterActivity() {

    private val CHANNEL_PROXIMITY_SENSOR = "samples.flutter.dev/proximitysensor"
    private val CHANNEL = "example_service"

    private lateinit var proximitySensor: Sensor
    private lateinit var sensorManager: SensorManager
    private lateinit var cameraManager: CameraManager


    private var proximityValue: Float = 0f

    private var isFlashlightOn = false

    override fun configureFlutterEngine(@NonNull flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL_PROXIMITY_SENSOR)
            .setMethodCallHandler { call, result ->
                if (call.method == "getProximityData") {
                    result.success(proximityValue < 5f)
                } else {
                    result.notImplemented()
                }
            }

        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL)
            .setMethodCallHandler { call, result ->
                when (call.method) {
                    "startExampleService" -> {
                        initializeProximitySensor()
                        startService(Intent(this, ExampleService::class.java))
                        result.success("Started!")
                    }
                    "stopExampleService" -> {
                        stopService(Intent(this, ExampleService::class.java))
                        stopProximitySensor()
                        turnOffFlashlight()
                        result.success("Stopped!")
                    }
                    else -> result.notImplemented()
                }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        instance = this

        // registerReceiver(stopReceiver, IntentFilter("STOP_SENSOR_AND_FLASHLIGHT"))
    }

    override fun onDestroy() {
        super.onDestroy()
        // unregisterReceiver(stopReceiver)
                   

  


    }

      inner class StopReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            println("s***************")
            Log.d("FlashlightService", "stopReceiver received")
            turnOffFlashlight()
            stopProximitySensor()
        }
    }

    //  var StopReceiver = object : BroadcastReceiver() {
    //     override fun onReceive(context: Context?, intent: Intent?) {
    
    //         println("s***************")
    //         Log.d("FlashlightService", "stopReceiver received")
    //         turnOffFlashlight()
    //         stopProximitySensor()

    //     }
    // }

     fun initializeProximitySensor() {
        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
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
            proximityValue = event.values[0]
            if (proximityValue < 5f) {
                if (!isFlashlightOn) {
                    turnOnFlashlight()
                    isFlashlightOn = true
                }
            } else {
                if (isFlashlightOn) {
                    turnOffFlashlight()
                    isFlashlightOn = false
                }
            }
        }
    }

     fun stopProximitySensor() {

        println("stopProximitySensor........................")
        sensorManager.unregisterListener(proximitySensorEventListener)
    }

    private fun turnOnFlashlight() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), PERMISSION_REQUEST_CAMERA)
                    return
                }
            }
            cameraManager.setTorchMode(cameraManager.cameraIdList[0], true)
            isFlashlightOn = true
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

     fun turnOffFlashlight() {

                Log.d("FlashlightService", "turnOffFlashlight called")

        try {
                        isFlashlightOn = false


            
            cameraManager.setTorchMode(cameraManager.cameraIdList[0], false)
        } catch (e: CameraAccessException) {

                        Log.d("CameraAccessException", "************************")

            e.printStackTrace()
        }
    }

    companion object {
        private const val PERMISSION_REQUEST_CAMERA = 1
        private var instance: MainActivity? = null

          fun getInstance(): MainActivity? {
            return instance
        }
    }
}
