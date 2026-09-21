package com.lastasylum.automation

import com.lastasylum.automation.modules.*

/**
 * Central repository of all available game-automation modules.
 *
 * The UI shows these by name; the automation loop fetches the
 * concrete Module by its ID.
 */
object ModuleRepository {
    private val modules = listOf(
        AutoScavengeModule(),
        ResourceFarmModule(),
        CureHealingModule(),
        ReconModule(),
        RaidsRalliesModule(),
        DailyQuestsModule()
    )

    fun getById(id: String): Module? = modules.firstOrNull { it.id == id }
    fun getAll() = modules
    fun getDefaultId() = AutoScavengeModule().id
}
