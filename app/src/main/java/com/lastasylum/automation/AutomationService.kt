package com.lastasylum.automation

import android.hardware.display.DisplayManager
import android.view.accessibility.AccessibilityManager
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.lastasylum.automation.modules.AutoScavengeModule
import kotlinx.coroutines.*

/**
 * Orchestrates the full automation loop.
 */
class AutomationService : Service() {

    companion object {
        const val ACTION_START = "action_start"
        const val ACTION_STOP = "action_stop"
        const val TAG = "AutomationService"
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var automationJob: Job? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var prefs: SharedPreferences? = null
    private var isPaused = false
    private var currentModuleId: String = AutoScavengeModule().id
    private var lastActivityTime: Long = 0L

    private lateinit var screenAnalyzer: ScreenAnalyzer
    private lateinit var accessibilityManager: AccessibilityManager

    override fun onCreate() {
        super.onCreate()
        prefs = getSharedPreferences(SettingsActivity.PREFS_NAME, MODE_PRIVATE)
        accessibilityManager = getSystemService(AccessibilityManager::class.java)
        screenAnalyzer = ScreenAnalyzer(this)

        val projection = MediaProjectionHolder.get()
        if (projection != null) {
            screenAnalyzer.attachMediaProjection(projection)
        }

        val savedModule = prefs?.getString(SettingsActivity.KEY_MODULE, "auto_scavenge")
            ?: "auto_scavenge"
        currentModuleId = savedModule

        acquireWakeLock()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startAutomation()
            ACTION_STOP -> stopAutomation()
        }
        return START_STICKY
    }

    private fun startAutomation() {
        Log.d(TAG, "Starting automation")
        com.lastasylum.automation.NotificationChannel.create(this)

        val module = ModuleRepository.getById(currentModuleId)
            ?: ModuleRepository.getDefaultId().let(ModuleRepository::getById) ?: run {
                Log.e(TAG, "Unknown module: ${currentModuleId}")
                stopAutomation()
                return
            }

        val pacing = prefs?.getInt(SettingsActivity.KEY_PACING, 300) ?: 300

        val startIntent = Intent(this, MainActivity::class.java)
        startIntent.action = AutomationService.ACTION_STOP
        val stopPending = PendingIntent.getActivity(this, 0, startIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(this, com.lastasylum.automation.NotificationChannel.CHANNEL_ID)
            .setContentTitle("Last Asylum Automation")
            .setContentText("Module: ${module.name} -- running")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Stop",
                stopPending
            )
            .build()

        startForeground(1, notification)

        automationJob = scope.launch {
            runAutomationLoop(module, pacing)
        }
    }

    private suspend fun runAutomationLoop(module: Module, pacing: Int) {
        updateStatus("Running: ${module.name}")
        MainActivity.isRunning = true
        MainActivity.currentModule = currentModuleId

        var consecutiveFailures = 0

        while (!Thread.currentThread().isInterrupted) {
            if (!isPaused) {
                try {
                    val screenSnapshot = screenAnalyzer.captureScreen()
                    if (screenSnapshot != null) {
                        val uiState = screenAnalyzer.analyzeUI(screenSnapshot)
                        if (uiState != null) {
                            val action = module.decideNextAction(uiState)
                            if (action != null) {
                                injectTouch(action.x, action.y, action.actionType, action.durationMs, action.description)
                                lastActivityTime = System.currentTimeMillis()
                                consecutiveFailures = 0
                            } else {
                                consecutiveFailures++
                            }
                        } else {
                            Log.w(TAG, "UI analysis returned null")
                            consecutiveFailures++
                        }
                    } else {
                        consecutiveFailures++
                    }

                    val baseDelay = pacing.toLong()
                    val jitter = (Math.random() * pacing * 0.3).toLong()
                    val delay = baseDelay + jitter
                    delay(delay.toLong())

                } catch (e: Exception) {
                    Log.e(TAG, "Automation step failed: ${e.message}", e)
                    consecutiveFailures++
                    val backoff = Math.min(500L * (1L shl Math.min(consecutiveFailures, 5)), 30_000L)
                    delay(backoff.toLong())
                }
            } else {
                delay(500L)
            }
        }
    }

    private fun injectTouch(
        x: Float,
        y: Float,
        actionType: TouchAction.ActionType = TouchAction.ActionType.TAP,
        durationMs: Long = 150L,
        description: String = "tap"
    ): Boolean {
        val service = LostAsylumAccessibilityService.getInstance()
            ?: run {
                Log.w(TAG, "Accessibility service not connected")
                return false
            }
        return service.dispatchAction(
            TouchAction(x, y, actionType, durationMs, description = description)
        )
    }

    private fun stopAutomation() {
        Log.d(TAG, "Stopping automation")
        automationJob?.cancel()
        releaseWakeLock()
        stopForeground(true)
        stopSelf()
        MainActivity.isRunning = false
        updateStatus("Stopped")
    }

    private fun acquireWakeLock() {
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "lostasylum:automation"
        ).apply {
            acquire(10 * 60 * 1000L)
        }
    }

    private fun releaseWakeLock() {
        if (wakeLock != null && wakeLock!!.isHeld) {
            wakeLock!!.release()
        }
    }

    private fun updateStatus(status: String) {
        Log.d(TAG, status)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
