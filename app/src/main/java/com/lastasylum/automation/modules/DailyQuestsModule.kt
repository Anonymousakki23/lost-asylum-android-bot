package com.lastasylum.automation.modules

import com.lastasylum.automation.Module
import com.lastasylum.automation.TouchAction
import com.lastasylum.automation.UiState

/**
 * Daily-Quests module:
 *  1. Open quests tab
 *  2. Complete each available quest (tap → wait → claim)
 *  3. Repeat until all dailies are done
 */
class DailyQuestsModule : Module {
    override val id: String = "daily_quests"
    override val name: String = "Daily Quests"

    private enum class State {
        OPEN_QUESTS,
        FIND_QUEST,
        COMPLETE_QUEST,
        CLAIM_REWARD,
        DONE
    }

    private var state: State = State.OPEN_QUESTS
    private var completedToday = 0

    override fun decideNextAction(uiState: UiState): TouchAction? {
        return when (state) {
            State.OPEN_QUESTS -> {
                state = State.FIND_QUEST
                TouchAction(100f, 1400f, description = "tap quests tab")
            }
            State.FIND_QUEST -> {
                val startBtn = uiState.findElementByText("Start", 0.6f)
                        ?: uiState.findElementByText("Go", 0.6f)
                if (startBtn != null) {
                    state = State.COMPLETE_QUEST
                    TouchAction(startBtn.x + startBtn.width / 2f, startBtn.y + startBtn.height / 2f,
                            description = "tap start quest")
                } else {
                    // No more quests available today
                    state = State.DONE
                    null
                }
            }
            State.COMPLETE_QUEST -> {
                // Wait a reasonable time for quest completion (varies by quest)
                state = State.CLAIM_REWARD
                TouchAction(540f, 1000f, description = "wait for completion")
            }
            State.CLAIM_REWARD -> {
                val claimBtn = uiState.findElementByText("Claim", 0.6f)
                        ?: uiState.findElementByText("Collect", 0.6f)
                if (claimBtn != null) {
                    completedToday++
                    state = State.FIND_QUEST
                    TouchAction(claimBtn.x + claimBtn.width / 2f, claimBtn.y + claimBtn.height / 2f,
                            description = "tap claim", durationMs = 500L)
                } else {
                    state = State.FIND_QUEST // Try again
                    null
                }
            }
            State.DONE -> null
        }
    }
}
