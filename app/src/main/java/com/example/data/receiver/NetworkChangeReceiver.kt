package com.example.data.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import com.example.data.worker.SyncScheduler

class NetworkChangeReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "NetworkChangeReceiver"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null) return

        try {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            val isConnected = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val activeNetwork = connectivityManager?.activeNetwork
                val capabilities = connectivityManager?.getNetworkCapabilities(activeNetwork)
                capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true &&
                        capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            } else {
                @Suppress("DEPRECATION")
                connectivityManager?.activeNetworkInfo?.isConnectedOrConnecting == true
            }

            if (isConnected) {
                Log.d(TAG, "Internet connection detected, triggering immediate order sync...")
                SyncScheduler.enqueueImmediateSync(context)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking network in receiver: ${e.message}")
        }
    }
}
