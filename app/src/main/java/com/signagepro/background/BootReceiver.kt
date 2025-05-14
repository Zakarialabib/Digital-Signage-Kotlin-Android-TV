package com.signagepro.app.background

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.signagepro.app.MainActivity

/**
 * Broadcast receiver that starts the app automatically when the device boots.
 * 
 * This is essential for digital signage applications to ensure content
 * display resumes automatically after power cycles or reboots.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Start the main activity when the device boots
            val launchIntent = Intent(context, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(launchIntent)
            
            // Alternatively, you could start a foreground service instead
            // or schedule WorkManager tasks to handle background operations
        }
    }
}