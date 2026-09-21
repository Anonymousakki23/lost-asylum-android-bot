package com.lastasylum.automation

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {

    private lateinit var statusText: TextView
    private lateinit var startButton: Button
    private lateinit var stopButton: Button
    private lateinit var moduleContainer: LinearLayout
    private lateinit var permissionStatus: TextView

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    companion object {
        private const val REQUEST_MEDIA_PROJECTION = 1001
        private const val REQUEST_OVERLAY = 1002

        var isRunning = false
        var currentModule: String = "auto_scavenge"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupUI()
        checkPermissions()
    }

    private fun setupUI() {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
            setBackgroundColor(resources.getColor(com.lastasylum.automation.R.color.background, null))
        }

        val title = TextView(this).apply {
            text = "Last Asylum: Plague Automation"
            textSize = 22f
            setTextColor(resources.getColor(com.lastasylum.automation.R.color.white, null))
            gravity = android.view.Gravity.CENTER_HORIZONTAL
            setPadding(0, 0, 0, 16)
        }

        statusText = TextView(this).apply {
            text = "Status: Stopped"
            textSize = 16f
            setTextColor(resources.getColor(com.lastasylum.automation.R.color.danger, null))
            gravity = android.view.Gravity.CENTER_HORIZONTAL
            setPadding(0, 0, 0, 24)
        }

        permissionStatus = TextView(this).apply {
            textSize = 14f
            setTextColor(resources.getColor(com.lastasylum.automation.R.color.secondary, null))
            gravity = android.view.Gravity.CENTER_HORIZONTAL
            setPadding(0, 0, 0, 24)
        }

        moduleContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 0, 0, 24)
        }

        createModuleButtons()

        startButton = Button(this).apply {
            text = "Start"
            setTextColor(resources.getColor(com.lastasylum.automation.R.color.white, null))
            setBackgroundColor(resources.getColor(com.lastasylum.automation.R.color.primary, null))
            setOnClickListener { startAutomation() }
        }

        stopButton = Button(this).apply {
            text = "Stop"
            setTextColor(resources.getColor(com.lastasylum.automation.R.color.white, null))
            setBackgroundColor(resources.getColor(com.lastasylum.automation.R.color.danger, null))
            setOnClickListener { stopAutomation() }
        }

        val settingsButton = Button(this).apply {
            text = "Settings"
            setTextColor(resources.getColor(com.lastasylum.automation.R.color.white, null))
            setBackgroundColor(resources.getColor(com.lastasylum.automation.R.color.secondary, null))
            setOnClickListener {
                startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
            }
        }

        layout.addView(title)
        layout.addView(statusText)
        layout.addView(permissionStatus)
        layout.addView(moduleContainer)
        layout.addView(startButton)
        layout.addView(stopButton)
        layout.addView(settingsButton)

        setContentView(layout)
    }

    private fun createModuleButtons() {
        val modules = listOf(
            "auto_scavenge" to "Auto Scavenge",
            "resource_farm" to "Resource Farm",
            "cure_healing" to "Cure & Healing",
            "recon" to "Recon & Intel",
            "raids_rallies" to "Raids & Rallies",
            "daily_quests" to "Daily Quests"
        )

        for ((id, name) in modules) {
            val btn = Button(this).apply {
                text = name
                setTextColor(resources.getColor(com.lastasylum.automation.R.color.white, null))
                setBackgroundColor(resources.getColor(com.lastasylum.automation.R.color.purple_700, null))
                setOnClickListener {
                    currentModule = id
                    val prefs = getSharedPreferences(SettingsActivity.PREFS_NAME, MODE_PRIVATE)
                    prefs.edit().putString(SettingsActivity.KEY_MODULE, id).apply()
                    statusText.text = "Selected: $name"
                    refreshModuleButtons()
                }
            }
            moduleContainer.addView(btn)
        }
        refreshModuleButtons()
    }

    private fun refreshModuleButtons() {
        for (i in 0 until moduleContainer.childCount) {
            val btn = moduleContainer.getChildAt(i) as Button
            btn.setBackgroundColor(
                if (btn.text.toString() == getModuleName(currentModule))
                    resources.getColor(com.lastasylum.automation.R.color.primary_variant, null)
                else
                    resources.getColor(com.lastasylum.automation.R.color.purple_700, null)
            )
        }
    }

    private fun getModuleName(id: String): String = when (id) {
        "auto_scavenge" -> "Auto Scavenge"
        "resource_farm" -> "Resource Farm"
        "cure_healing" -> "Cure & Healing"
        "recon" -> "Recon & Intel"
        "raids_rallies" -> "Raids & Rallies"
        "daily_quests" -> "Daily Quests"
        else -> "Auto Scavenge"
    }

    private fun checkPermissions() {
        val overlayGranted = Settings.canDrawOverlays(this)
        val accessibilityEnabled = isAccessibilityEnabled()
        val mediaProjectionGranted = MediaProjectionHolder.isAvailable

        val status = buildString {
            append("Overlay: ${if (overlayGranted) "OK" else "NEEDS GRANT"}\n")
            append("Accessibility: ${if (accessibilityEnabled) "OK" else "NEEDS ENABLE"}\n")
            append("Screen Capture: ${if (mediaProjectionGranted) "OK" else "NEEDS GRANT"}")
        }
        permissionStatus.text = status
    }

    private fun isAccessibilityEnabled(): Boolean {
        val service = "${packageName}/.LostAsylumAccessibilityService"
        val enabled = try {
            val setting = Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
            setting != null && setting.split(':').contains(service)
        } catch (e: Exception) {
            false
        }
        return enabled
    }

    private fun startAutomation() {
        if (!Settings.canDrawOverlays(this)) {
            statusText.text = "Grant overlay permission first!"
            requestOverlayPermission()
            return
        }

        if (!isAccessibilityEnabled()) {
            statusText.text = "Enable accessibility service first!"
            openAccessibilitySettings()
            return
        }

        if (!MediaProjectionHolder.isAvailable) {
            statusText.text = "Grant screen capture first!"
            requestMediaProjection()
            return
        }

        val intent = Intent(this, AutomationService::class.java)
        intent.action = AutomationService.ACTION_START
        ContextCompat.startForegroundService(this, intent)
        isRunning = true
        statusText.text = "Status: Running (${getModuleName(currentModule)})"
        statusText.setTextColor(resources.getColor(com.lastasylum.automation.R.color.primary, null))
    }

    private fun stopAutomation() {
        val intent = Intent(this, AutomationService::class.java)
        intent.action = AutomationService.ACTION_STOP
        startService(intent)
        isRunning = false
        statusText.text = "Status: Stopped"
        statusText.setTextColor(resources.getColor(com.lastasylum.automation.R.color.danger, null))
    }

    private fun requestOverlayPermission() {
        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
            startActivityForResult(intent, REQUEST_OVERLAY)
        }
    }

    private fun openAccessibilitySettings() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        startActivity(intent)
    }

    private fun requestMediaProjection() {
        val manager = getSystemService(MediaProjectionManager::class.java)
        val intent = manager.createScreenCaptureIntent()
        startActivityForResult(intent, REQUEST_MEDIA_PROJECTION)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_MEDIA_PROJECTION -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val manager = getSystemService(MediaProjectionManager::class.java)
                    val projection = manager.getMediaProjection(resultCode, data)
                    projection?.let { MediaProjectionHolder.set(it) }
                    statusText.text = "Screen capture ready!"
                }
            }
            REQUEST_OVERLAY -> {
                checkPermissions()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        checkPermissions()
        if (isRunning) {
            statusText.text = "Status: Running (${getModuleName(currentModule)})"
            statusText.setTextColor(resources.getColor(com.lastasylum.automation.R.color.primary, null))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}

/** Holds the MediaProjection instance across configuration changes. */
object MediaProjectionHolder {
    @Volatile
    var projection: MediaProjection? = null

    val isAvailable: Boolean
        get() = projection != null

    fun set(p: MediaProjection) {
        projection = p
    }

    fun get(): MediaProjection? = projection
}
