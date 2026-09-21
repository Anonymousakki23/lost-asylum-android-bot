package com.lastasylum.automation

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import kotlin.math.max
import kotlin.math.min

/**
 * Accessibility service that injects humanized touch gestures into the game.
 *
 * This service is the "hands" of the automation: every tap, swipe and long-press
 * emitted by an automation module flows through this class, where we add
 * humanization (variable duration, jitter, multi-finger probability) before the
 * event reaches the system.
 */
class LostAsylumAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "AccessibilityService"
        const val ACTION_INJECT_TOUCH = "com.lastasylum.automation.action.INJECT_TOUCH"
        const val EXTRA_ACTION = "touch_action"

        private var _instance: LostAsylumAccessibilityService? = null
        
        /** Public accessor for the service instance */
        fun getInstance(): LostAsylumAccessibilityService? = _instance

        /** Check if service is ready */
        val isReady: Boolean
            get() = _instance != null

        @Volatile
        private var instance: LostAsylumAccessibilityService? = null

        fun injectTouch(
            x: Float,
            y: Float,
            actionType: TouchAction.ActionType = TouchAction.ActionType.TAP,
            durationMs: Long = 150L,
            description: String = "tap"
        ): Boolean {
            return instance?.dispatchAction(
                TouchAction(x, y, actionType, durationMs, description = description)
            ) ?: false
        }

        fun injectAction(action: TouchAction): Boolean = instance?.dispatchAction(action) ?: false
    }

    private val handler = Handler(Looper.getMainLooper())
    private val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.SupervisorJob() + kotlinx.coroutines.Dispatchers.Default)

    override fun onServiceConnected() {
        super.onServiceConnected()
        _instance = this
        instance = this
        Log.d(TAG, "Accessibility service connected")

        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED or
                    AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_REQUEST_TOUCH_EXPLORATION_MODE or
                    AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
            notificationTimeout = 100
        }
        serviceInfo = info
    }

    override fun onInterrupt() {
        Log.w(TAG, "Accessibility service interrupted")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event ?: return
        if (event.packageName != null && event.packageName.toString() != "com.lastasylum.plague") {
            // Ignore events from other apps to avoid accidental input.
            return
        }
        // Modules poll window content on demand; we keep the stream for
        // state-change detection (e.g. a new screen opened).
    }

    override fun onDestroy() {
        super.onDestroy()
        if (instance === this) {
            instance = null
            _instance = null
        }
    }

    /**
     * Dispatches a single humanized gesture. Returns true when the gesture was
     * handed to the system.
     */
    fun dispatchAction(action: TouchAction): Boolean {
        if (action.x < 0 || action.y < 0) {
            Log.w(TAG, "Invalid coordinates: ${action.x},${action.y}")
            return false
        }

        val gesture = buildGesture(action) ?: return false

        return dispatchGesture(gesture, null, null)
    }

    private fun buildGesture(action: TouchAction): GestureDescription? {
        val path = Path()
        val (startX, startY) = applyJitter(action.x, action.y)

        when (action.actionType) {
            TouchAction.ActionType.TAP, TouchAction.ActionType.LONG_PRESS -> {
                path.moveTo(startX, startY)
                val stroke = GestureDescription.StrokeDescription(
                    path,
                    0,
                    if (action.actionType == TouchAction.ActionType.LONG_PRESS)
                        maxOf(action.durationMs, 600L) else action.durationMs
                )
                return GestureDescription.Builder().addStroke(stroke).build()
            }
            TouchAction.ActionType.SWIPE, TouchAction.ActionType.DRAG -> {
                val (endX, endY) = applyJitter(action.x + 120f, action.y + 120f)
                path.moveTo(startX, startY)
                path.quadTo(
                    (startX + endX) / 2f,
                    (startY + endY) / 2f,
                    endX,
                    endY
                )
                val stroke = GestureDescription.StrokeDescription(
                    path,
                    0,
                    maxOf(action.durationMs, 300L)
                )
                return GestureDescription.Builder().addStroke(stroke).build()
            }
        }
    }

    private fun applyJitter(x: Float, y: Float): Pair<Float, Float> {
        // +/-2.5px jitter prevents mechanical, grid-perfect input patterns.
        val jitterX = (Math.random() * 5.0f - 2.5f).toFloat()
        val jitterY = (Math.random() * 5.0f - 2.5f).toFloat()
        return Pair(x + jitterX, y + jitterY)
    }

    override fun onUnbind(intent: android.content.Intent?): Boolean {
        if (instance === this) {
            instance = null
            _instance = null
        }
        return super.onUnbind(intent)
    }
}
