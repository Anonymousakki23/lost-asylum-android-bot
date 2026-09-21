package com.lastasylum.automation

/**
 * Base interface for every game-automation module.
 *
 * Each module knows how to decide the next action given the current
 * screen state (UiState). This keeps the automation loop clean and lets
 * us hot-swap strategies at runtime.
 */
interface Module {
    val id: String
    val name: String

    /** Returns the next TouchAction to perform, or null to wait. */
    fun decideNextAction(uiState: UiState): TouchAction?
}
