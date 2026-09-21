package com.lastasylum.automation.modules

import com.lastasylum.automation.Module
import com.lastasylum.automation.TouchAction
import com.lastasylum.automation.UiState

/**
 * Raids & Rallies module:
 *  1. Open raid/rally tab
 *  2. Find active raid with troops needed
 *  3. Join raid → wait → claim rewards
 */
class RaidsRalliesModule : Module {
    override val id: String = "raids_rallies"
    override val name: String = "Raids & Rallies"

    private enum class State {
        OPEN_RAIDS,
        FIND_ACTIVE_RAID,
        JOIN_RAID,
        WAIT_FOR_COMPLETION,
        CLAIM_REWARDS
    }

    private var state: State = State.OPEN_RAIDS
    private var raidJoinedAt: Long = 0L

    override fun decideNextAction(uiState: UiState): TouchAction? {
        return when (state) {
            State.OPEN_RAIDS -> {
                state = State.FIND_ACTIVE_RAID
                TouchAction(200f, 1600f, description = "tap raids tab")
            }
            State.FIND_ACTIVE_RAID -> {
                val joinBtn = uiState.findElementByText("Join", 0.6f)
                if (joinBtn != null) {
                    state = State.JOIN_RAID
                    TouchAction(joinBtn.x + joinBtn.width / 2f, joinBtn.y + joinBtn.height / 2f,
                            description = "tap join raid")
                } else {
                    null // Wait for raid to appear
                }
            }
            State.JOIN_RAID -> {
                state = State.WAIT_FOR_COMPLETION
                raidJoinedAt = System.currentTimeMillis()
                TouchAction(540f, 1000f, description = "confirm join")
            }
            State.WAIT_FOR_COMPLETION -> {
                // Typical raid duration: 2-5 minutes
                if (System.currentTimeMillis() - raidJoinedAt > 180_000L) { // 3 min
                    state = State.CLAIM_REWARDS
                    TouchAction(540f, 1600f, description = "tap claim")
                } else {
                    null // Still waiting
                }
            }
            State.CLAIM_REWARDS -> {
                state = State.OPEN_RAIDS
                TouchAction(540f, 1600f, description = "tap ok", durationMs = 500L)
            }
        }
    }
}
