package com.lastasylum.automation

/**
 * Simple data class representing a clickable UI element in the game.
 * Used by various helpers and for debugging.
 */
data class GameElement(
    val text: String,
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int,
    val confidence: Float
)
