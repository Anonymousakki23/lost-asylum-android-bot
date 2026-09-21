package com.lastasylum.automation.analytics

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.lastasylum.automation.Module
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Tracks automated runs statistics:
 * - Total runs completed
 * - Actions performed
 * - Success rate
 * - Session duration
 * - Error count
 *
 * Thread-safe singleton accessible from any automation module.
 */
object StatsTracker {
    private const val PREFS_NAME = "stats_tracker"

    // Keys
    private const val KEY_TOTAL_RUNS = "total_runs"
    private const val KEY_TOTAL_ACTIONS = "total_actions"
    private const val KEY_TOTAL_ERRORS = "total_errors"
    private const val KEY_SESSION_START = "session_start"
    private const val KEY_SESSION_ACTIONS = "session_actions"

    private lateinit var prefs: SharedPreferences
    private val mutex = Mutex()

    var sessionStart: Long = 0L
        private set

    private var totalActionsThisSession = 0

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sessionStart = System.currentTimeMillis()
    }

    suspend fun recordRun() = mutex.withLock {
        prefs.edit { putInt(KEY_TOTAL_RUNS, (prefs.getInt(KEY_TOTAL_RUNS, 0) + 1)) }
    }

    suspend fun recordAction(count: Int = 1) = mutex.withLock {
        totalActionsThisSession += count
        prefs.edit { putInt(KEY_TOTAL_ACTIONS, (prefs.getInt(KEY_TOTAL_ACTIONS, 0) + count)) }
    }

    suspend fun recordError() = mutex.withLock {
        prefs.edit { putInt(KEY_TOTAL_ERRORS, (prefs.getInt(KEY_TOTAL_ERRORS, 0) + 1)) }
    }

    suspend fun startSession() {
        sessionStart = System.currentTimeMillis()
        totalActionsThisSession = 0
        prefs.edit { putLong(KEY_SESSION_START, sessionStart) }
    }

    suspend fun stopSession() {
        val sessionDuration = System.currentTimeMillis() - sessionStart
        prefs.edit {
            putLong("session_duration_ms", sessionDuration)
            putInt("session_actions", totalActionsThisSession)
        }
    }

    fun isInitialized(): Boolean = ::prefs.isInitialized

    fun getTotalRuns(): Int = prefs.getInt(KEY_TOTAL_RUNS, 0)
    fun getTotalActions(): Int = prefs.getInt(KEY_TOTAL_ACTIONS, 0)
    fun getTotalErrors(): Int = prefs.getInt(KEY_TOTAL_ERRORS, 0)
    fun getSessionActions(): Int = totalActionsThisSession

    fun getSessionDurationMs(): Long = System.currentTimeMillis() - sessionStart

    fun getSuccessRate(): Float {
        val total = getTotalActions()
        val errors = getTotalErrors()
        return if (total > 0) (total - errors).toFloat() / total.toFloat() else 0f
    }

    fun getActionsPerMinute(): Float {
        val durationSec = getSessionDurationMs() / 1000f
        return if (durationSec > 0) totalActionsThisSession * 60f / durationSec else 0f
    }

    fun getStatsForDisplay(): RunStats {
        return RunStats(
            totalRuns = getTotalRuns(),
            totalActions = getTotalActions(),
            totalErrors = getTotalErrors(),
            sessionActions = totalActionsThisSession,
            sessionDurationMs = getSessionDurationMs(),
            successRate = getSuccessRate(),
            actionsPerMinute = getActionsPerMinute()
        )
    }

    data class RunStats(
        val totalRuns: Int,
        val totalActions: Int,
        val totalErrors: Int,
        val sessionActions: Int,
        val sessionDurationMs: Long,
        val successRate: Float,
        val actionsPerMinute: Float
    )
}
