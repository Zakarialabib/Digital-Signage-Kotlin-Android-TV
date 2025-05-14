package com.signagepro.app.background

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.signagepro.app.MainActivity
import com.signagepro.app.core.utils.Constants
import com.signagepro.app.core.utils.Logger
import com.signagepro.app.core.workers.ContentSyncWorker
import com.signagepro.app.core.workers.HeartbeatWorker
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Broadcast receiver that starts the app automatically when the device boots.
 * 
 * This is essential for digital signage applications to ensure content
 * display resumes automatically after power cycles or reboots.
 */
class BootReceiver : BroadcastReceiver() {
    // @Inject lateinit var workManager: WorkManager // This won't work directly in onReceive without further Hilt setup for BroadcastReceivers

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Logger.i("BootReceiver: Boot completed, starting app and scheduling workers.")

            // Start the main activity
            val launchIntent = Intent(context, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(launchIntent)

            // Schedule periodic workers
            val workManager = WorkManager.getInstance(context.applicationContext)

            // Content Sync Worker - e.g., every 1 hour
            val contentSyncRequest = PeriodicWorkRequestBuilder<ContentSyncWorker>(
                1, TimeUnit.HOURS
            )
            // .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()) // Optional constraints
            .build()
            workManager.enqueueUniquePeriodicWork(
                ContentSyncWorker.WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP, // Or REPLACE if new params/logic
                contentSyncRequest
            )

            // Heartbeat Worker - e.g., every 15 minutes (as per Constants)
            val heartbeatRequest = PeriodicWorkRequestBuilder<HeartbeatWorker>(
                Constants.HEARTBEAT_INTERVAL_MINUTES, TimeUnit.MINUTES
            )
            // .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .build()
            workManager.enqueueUniquePeriodicWork(
                HeartbeatWorker.WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                heartbeatRequest
            )
            Logger.i("BootReceiver: ContentSyncWorker and HeartbeatWorker enqueued.")
        }
    }
}