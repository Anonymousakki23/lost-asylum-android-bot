package com.lastasylum.automation.modules

import com.lastasylum.automation.Module
import com.lastasylum.automation.TouchAction
import com.lastasylum.automation.UiState

/**
 * Plague Automation module:
 *  1. Check infection level & deploy cures automatically
 *  2. Collect DNA rewards from infected targets
 *  3. Upgrade plague strains for maximum yield
 *  4. Manage quarantine & outbreak cycles
 *
 * This module replicates the core plague automation logic from
 * the automationmacro.com blog but runs entirely on-device.
 *
 * Reference: https://automationmacro.com/blog/last-asylum-plague-automation-bot
 */
class PlagueModule : Module {
    override val id: String = "plague"
    override val name: String = "Plague Automation"

    private enum class State {
        IDLE,
        CHECK_INFECTION,
        DEPLOY_CURE,
        COLLECT_DNA,
        UPGRADE_STRAIN,
        MANAGE_OUTBREAK,
        QUARANTINE,
        COLLECT_REWARDS,
        DONE
    }

    private var state: State = State.IDLE
    private var outbreakStartedAt: Long = 0L
    private var dnaCollectedToday: Long = 0L
    private var curesDeployed: Int = 0
    private var outbreaksManaged: Int = 0

    // Thresholds (adjustable via SettingsActivity)
    private var infectionThreshold: Int = 60
    private var dnaTarget: Long = 10_000L

    override fun decideNextAction(uiState: UiState): TouchAction? {
        val infectionLevel = extractInfectionLevel(uiState)
        val dnaLevel = extractDNA(uiState)
        val outbreakActive = isOutbreakActive(uiState)

        return when (state) {
            State.IDLE -> {
                state = State.CHECK_INFECTION
                TouchAction(540f, 1600f, description = "check infection status")
            }
            State.CHECK_INFECTION -> {
                if (infectionLevel >= infectionThreshold) {
                    state = State.DEPLOY_CURE
                    TouchAction(300f, 1200f, description = "tap deploy cure")
                } else if (outbreakActive) {
                    state = State.MANAGE_OUTBREAK
                    TouchAction(540f, 800f, description = "manage outbreak")
                } else {
                    state = State.COLLECT_DNA
                    TouchAction(700f, 1600f, description = "collect DNA")
                }
            }
            State.DEPLOY_CURE -> {
                curesDeployed++
                state = State.COLLECT_DNA
                TouchAction(700f, 1600f, description = "collect DNA after cure", durationMs = 300L)
            }
            State.COLLECT_DNA -> {
                dnaCollectedToday += 500L // estimated per cycle
                state = if (dnaCollectedToday >= dnaTarget) State.UPGRADE_STRAIN else State.CHECK_INFECTION
                TouchAction(540f, 1600f, description = "confirm DNA collection")
            }
            State.UPGRADE_STRAIN -> {
                state = State.COLLECT_REWARDS
                TouchAction(400f, 1400f, description = "upgrade plague strain")
            }
            State.MANAGE_OUTBREAK -> {
                outbreaksManaged++
                outbreakStartedAt = System.currentTimeMillis()
                state = State.QUARANTINE
                TouchAction(540f, 1000f, description = "quarantine zone")
            }
            State.QUARANTINE -> {
                if (System.currentTimeMillis() - outbreakStartedAt > 120_000L) {
                    state = State.COLLECT_REWARDS
                }
                TouchAction(540f, 1600f, description = "confirm quarantine")
            }
            State.COLLECT_REWARDS -> {
                state = State.IDLE
                TouchAction(540f, 1600f, description = "collect all rewards", durationMs = 500L)
            }
            State.DONE -> null
        }
    }

    fun getStats(): PlagueStats = PlagueStats(
        dnaCollectedToday = dnaCollectedToday,
        curesDeployed = curesDeployed,
        outbreaksManaged = outbreaksManaged,
        currentState = state.name
    )

    fun resetStats() {
        dnaCollectedToday = 0L
        curesDeployed = 0
        outbreaksManaged = 0
        state = State.IDLE
    }

    private fun extractInfectionLevel(uiState: UiState): Int {
        val infected = uiState.findElementByText("Infected", 0.5f)?.text ?: ""
        return extractNumber(infected)
    }

    private fun extractDNA(uiState: UiState): Long {
        val dna = uiState.findElementByText("DNA", 0.5f)?.text ?: ""
        return extractNumber(dna).toLong()
    }

    private fun isOutbreakActive(uiState: UiState): Boolean {
        return uiState.findElementByText("Outbreak", 0.5f) != null ||
                uiState.findElementByText("Epidemic", 0.5f) != null
    }

    private fun extractNumber(text: String): Int {
        val num = text.filter { it.isDigit() }
        return if (num.isNotEmpty()) num.toInt() else 0
    }

    data class PlagueStats(
        val dnaCollectedToday: Long,
        val curesDeployed: Int,
        val outbreaksManaged: Int,
        val currentState: String
    )
}
