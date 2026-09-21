package com.lastasylum.automation.modules

import com.lastasylum.automation.Module
import com.lastasylum.automation.TouchAction
import com.lastasylum.automation.UiState

/**
 * Recon & Intel module:
 *  1. Open intel tab
 *  2. Scout nearby locations
 *  3. Complete available missions
 */
class ReconModule : Module {
    override val id: String = "recon"
    override val name: String = "Recon & Intel"

    private enum class State {
        OPEN_INTEL,
        SCOUT_LOCATION,
        ACCEPT_MISSION,
        COMPLETE_MISSION,
        DONE
    }

    private var state: State = State.OPEN_INTEL

    override fun decideNextAction(uiState: UiState): TouchAction? {
        return when (state) {
            State.OPEN_INTEL -> {
                state = State.SCOUT_LOCATION
                TouchAction(100f, 1600f, description = "tap intel tab")
            }
            State.SCOUT_LOCATION -> {
                val scoutBtn = uiState.findElementByText("Scout", 0.6f)
                if (scoutBtn != null) {
                    state = State.ACCEPT_MISSION
                    TouchAction(scoutBtn.x + scoutBtn.width / 2f, scoutBtn.y + scoutBtn.height / 2f,
                            description = "tap scout")
                } else {
                    state = State.DONE
                    null
                }
            }
            State.ACCEPT_MISSION -> {
                val acceptBtn = uiState.findElementByText("Accept", 0.6f)
                if (acceptBtn != null) {
                    state = State.COMPLETE_MISSION
                    TouchAction(acceptBtn.x + acceptBtn.width / 2f, acceptBtn.y + acceptBtn.height / 2f,
                            description = "tap accept")
                } else {
                    state = State.OPEN_INTEL // Reset to find new missions
                    null
                }
            }
            State.COMPLETE_MISSION -> {
                val completeBtn = uiState.findElementByText("Complete", 0.6f)
                if (completeBtn != null) {
                    state = State.OPEN_INTEL
                    TouchAction(completeBtn.x + completeBtn.width / 2f, completeBtn.y + completeBtn.height / 2f,
                            description = "tap complete", durationMs = 800L)
                } else {
                    state = State.OPEN_INTEL
                    null
                }
            }
            State.DONE -> null
        }
    }
}
