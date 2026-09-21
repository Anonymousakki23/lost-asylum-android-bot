package com.lastasylum.automation

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object NotificationChannel {
    const val CHANNEL_ID = "lost_asylum_channel"
    const val CHANNEL_NAME = "Last Asylum Automation"

    fun create(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(NotificationManager::class.java)
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notifications for the active automation module"
                setShowBadge(false)
            }
            manager.createNotificationChannel(channel)
        }
    }
}
