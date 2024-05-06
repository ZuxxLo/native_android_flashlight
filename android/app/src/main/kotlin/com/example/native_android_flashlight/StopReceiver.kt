package com.example.native_android_flashlight
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class StopReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        println("/*/*///////////////////")
        Log.d("FlashlightService", "stopReceiver received")
        // Call methods from MainActivity to turn off flashlight and stop proximity sensor

           MainActivity.getInstance()?.turnOffFlashlight();
           MainActivity.getInstance()?.stopProximitySensor();

    //    context?.let { ctx ->
    //         val mainActivity = MainActivity()
 
    //         mainActivity.stopProximitySensor()
    //         mainActivity.turnOffFlashlight()
    //     }
    }
}