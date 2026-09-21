package com.lastasylum.automation.modules

import com.lastasylum.automation.Module
import com.lastasylum.automation.TouchAction
import com.lastasylum.automation.UiState

/**
 * Auto-Scavenge module:
 *  1. Open map → select wasteland → choose biome → start scavenge
 *  2. When timer completes → collect reward → repeat
 *
 * Reference: https://automationmacro.com/blog/last-asylum-plague-automation-bot
 */
class AutoScavengeModule : Module {
    override val id: String = "auto_scavenge"
    override val name: String = "Auto Scavenge"

    private enum class State {
        IDLE,
        OPEN_MAP,
        SELECT_WASTELAND,
        SELECT_BIOME,
        START_SCAVENGE,
        WAIT_FOR_TIMER,
        COLLECT_REWARD
    }

    private var state: State = State.IDLE
    private var scavengeStartedAt: Long = 0L
    private val scavengeDurationMs = 300_000L // 5 minutes typical

    override fun decideNextAction(uiState: UiState): TouchAction? {
        return when (state) {
            State.IDLE -> {
                state = State.OPEN_MAP
                TouchAction(100f, 200f, description = "tap map icon")
            }
            State.OPEN_MAP -> {
                state = State.SELECT_WASTELAND
                TouchAction(540f, 800f, description = "tap wasteland button")
            }
            State.SELECT_WASTELAND -> {
                state = State.SELECT_BIOME
                TouchAction(540f, 1000f, description = "tap forest biome")
            }
            State.SELECT_BIOME -> {
                state = State.START_SCAVENGE
                TouchAction(540f, 1600f, description = "tap start scavenge")
            }
            State.START_SCAVENGE -> {
                state = State.WAIT_FOR_TIMER
                scavengeStartedAt = System.currentTimeMillis()
                TouchAction(540f, 1600f, description = "confirm scavenge start", durationMs = 500L)
            }
            State.WAIT_FOR_TIMER -> {
                if (System.currentTimeMillis() - scavengeStartedAt > scavengeDurationMs) {
                    state = State.COLLECT_REWARD
                    TouchAction(540f, 1600f, description = "tap collect reward")
                } else {
                    null // Wait
                }
            }
            State.COLLECT_REWARD -> {
                state = State.IDLE
                TouchAction(540f, 1600f, description = "tap ok", durationMs = 500L)
            }
        }
    }
}
