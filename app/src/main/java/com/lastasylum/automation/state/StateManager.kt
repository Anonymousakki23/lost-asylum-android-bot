package com.lastasylum.automation.state

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

/**
 * Persists automation state across app restarts:
 * - Current module state (for stateful modules)
 * - Last action taken
 * - Module-specific progress
 * - Session resume info
 */
object StateManager {
    private const val PREFS_NAME = "automation_state"

    // Keys
    private const val KEY_MODULE_STATE = "module_state"
    private const val KEY_MODULE_ID = "module_id"
    private const val KEY_LAST_ACTION = "last_action"
    private const val KEY_SESSION_RESUMED = "session_resumed"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveModuleState(moduleId: String, stateName: String) {
        prefs.edit {
            putString(KEY_MODULE_ID, moduleId)
            putString(KEY_MODULE_STATE, stateName)
            putLong("state_timestamp", System.currentTimeMillis())
        }
    }

    fun saveLastAction(description: String) {
        prefs.edit { putString(KEY_LAST_ACTION, description) }
    }

    fun markSessionResumed() {
        prefs.edit { putBoolean(KEY_SESSION_RESUMED, true) }
    }

    fun clearModuleState(moduleId: String) {
        prefs.edit {
            putString(KEY_MODULE_STATE, "reset")
            putString(KEY_MODULE_ID, moduleId)
        }
    }

    fun getModuleState(moduleId: String): String? {
        return if (prefs.getString(KEY_MODULE_ID, "") == moduleId) {
            prefs.getString(KEY_MODULE_STATE, "reset")
        } else null
    }

    fun getLastAction(): String? = prefs.getString(KEY_LAST_ACTION, null)

    fun isSessionResumed(): Boolean = prefs.getBoolean(KEY_SESSION_RESUMED, false)

    fun clearAllState() {
        prefs.edit().clear().apply()
    }

    data class ModuleStateSnapshot(
        val moduleId: String,
        val state: String,
        val timestamp: Long
    )

    fun getModuleStateSnapshot(moduleId: String): ModuleStateSnapshot? {
        if (prefs.getString(KEY_MODULE_ID, "") != moduleId) return null
        return ModuleStateSnapshot(
            moduleId = moduleId,
            state = prefs.getString(KEY_MODULE_STATE, "reset") ?: "reset",
            timestamp = prefs.getLong("state_timestamp", 0L)
        )
    }
}
