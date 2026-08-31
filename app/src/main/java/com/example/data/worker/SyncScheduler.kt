package com.example.data.worker

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object SyncScheduler {
    private const val TAG = "SyncScheduler"
    private const val PERIODIC_WORK_NAME = "gagarin_order_periodic_sync"
    private const val IMMEDIATE_WORK_NAME = "gagarin_order_immediate_sync"

    fun schedulePeriodicSync(context: Context) {
        try {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val periodicRequest = PeriodicWorkRequestBuilder<OrderSyncWorker>(
                15, TimeUnit.MINUTES,
                5, TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                PERIODIC_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                periodicRequest
            )
            Log.d(TAG, "Scheduled periodic order sync work")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to schedule periodic sync: ${e.message}")
        }
    }

    fun enqueueImmediateSync(context: Context) {
        try {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val immediateRequest = OneTimeWorkRequestBuilder<OrderSyncWorker>()
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                IMMEDIATE_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                immediateRequest
            )
            Log.d(TAG, "Enqueued immediate order sync work")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to enqueue immediate sync: ${e.message}")
        }
    }
}
