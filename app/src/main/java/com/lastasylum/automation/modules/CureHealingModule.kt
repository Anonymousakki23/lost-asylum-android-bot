package com.lastasylum.automation.modules

import com.lastasylum.automation.Module
import com.lastasylum.automation.TouchAction
import com.lastasylum.automation.UiState

/**
 * Cure & Healing module:
 *  1. Check plague/infection level
 *  2. If above threshold → use cure item
 *  3. Check health → use medkit if low
 *  4. Visit hospital for auto-heal
 */
class CureHealingModule : Module {
    override val id: String = "cure_healing"
    override val name: String = "Cure & Healing"

    private enum class State {
        CHECK_STATUS,
        USE_CURE,
        USE_MEDKIT,
        VISIT_HOSPITAL,
        DONE
    }

    private var state: State = State.CHECK_STATUS

    override fun decideNextAction(uiState: UiState): TouchAction? {
        val plagueText = uiState.findElementByText("Plague", 0.5f)?.text ?: ""
        val plagueLevel = extractNumber(plagueText)

        val healthText = uiState.findElementByText("Health", 0.5f)?.text ?: ""
        val healthLevel = extractNumber(healthText)

        return when (state) {
            State.CHECK_STATUS -> {
                if (plagueLevel > 75) {
                    state = State.USE_CURE
                    TouchAction(540f, 1200f, description = "tap cure button")
                } else if (healthLevel < 30) {
                    state = State.USE_MEDKIT
                    TouchAction(540f, 1300f, description = "tap medkit")
                } else {
                    state = State.DONE
                    null
                }
            }
            State.USE_CURE -> {
                state = State.CHECK_STATUS
                TouchAction(300f, 1000f, description = "select cure item")
            }
            State.USE_MEDKIT -> {
                state = State.CHECK_STATUS
                TouchAction(300f, 1100f, description = "select medkit")
            }
            State.VISIT_HOSPITAL -> {
                state = State.CHECK_STATUS
                TouchAction(700f, 1600f, description = "go to hospital")
            }
            State.DONE -> null
        }
    }

    private fun extractNumber(text: String): Int {
        val num = text.filter { it.isDigit() }
        return if (num.isNotEmpty()) num.toInt() else 0
    }
}
