package com.lastasylum.automation.modules

import com.lastasylum.automation.Module
import com.lastasylum.automation.TouchAction
import com.lastasylum.automation.UiState

/**
 * Resource-Farm module:
 *  1. Visit resource buildings (farm, mine, sawmill)
 *  2. Tap collect when resources are full
 *  3. Repeat
 */
class ResourceFarmModule : Module {
    override val id: String = "resource_farm"
    override val name: String = "Resource Farm"

    private val buildings = listOf(
        Triple("Farm", 300f, 800f),
        Triple("Mine", 540f, 800f),
        Triple("Sawmill", 780f, 800f),
        Triple("Warehouse", 300f, 1100f)
    )
    private var currentIndex = 0

    override fun decideNextAction(uiState: UiState): TouchAction? {
        val (name, x, y) = buildings[currentIndex]

        // Check for "Collect" text near this building
        val collect = uiState.findElementByText("Collect", 0.6f)
        if (collect != null) {
            return TouchAction(collect.x + collect.width / 2f, collect.y + collect.height / 2f,
                    description = "collect $name", durationMs = 300L)
        }

        // Move to next building
        currentIndex = (currentIndex + 1) % buildings.size
        return TouchAction(x, y, description = "visit $name")
    }
}
