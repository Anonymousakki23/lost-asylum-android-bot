package com.lastasylum.automation

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/** Receives system broadcasts that keep the automation alive across device reboots. */
class AutomationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        when (intent?.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_LOCKED_BOOT_COMPLETED -> {
                // Reschedule any configured recurring automation jobs.
                // The actual scheduling is handled by WorkManager.
            }
        }
    }
}
