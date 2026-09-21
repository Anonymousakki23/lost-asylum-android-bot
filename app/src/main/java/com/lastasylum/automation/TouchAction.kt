package com.lastasylum.automation

import kotlin.math.max
import kotlin.math.min

/** A single humanized touch interaction to be performed on the game screen. */
data class TouchAction(
    val x: Float,
    val y: Float,
    val actionType: ActionType = ActionType.TAP,
    val durationMs: Long = 150L,
    val confidence: Float = 1.0f,
    val description: String = "tap"
) {
    enum class ActionType { TAP, LONG_PRESS, SWIPE, DRAG }
}

/** A screen state parsed from an OCR pass, containing actionable UI elements. */
data class UiState(
    val screenshotWidth: Int,
    val screenshotHeight: Int,
    val elements: List<Element>,
    val rawText: String
) {
    data class Element(
        val text: String,
        val x: Float,
        val y: Float,
        val width: Float,
        val height: Float,
        val confidence: Float,
        val elementType: ElementType = ElementType.UNKNOWN
    ) {
        enum class ElementType { BUTTON, TEXT, LIST_ITEM, TAB, INPUT, IMAGE, UNKNOWN }
    }

    fun elementAt(x: Float, y: Float): Element? = elements.firstOrNull {
        val centerX = it.x + it.width / 2f
        val centerY = it.y + it.height / 2f
        it.x <= x && x <= it.x + it.width && it.y <= y && y <= it.y + it.height
    }

    fun findElementByText(pattern: String, minConfidence: Float = 0.5f): Element? =
        elements.firstOrNull {
            it.text.contains(pattern, ignoreCase = true) && it.confidence >= minConfidence
        }

    fun findFirstOfType(type: Element.ElementType, minConfidence: Float = 0.5f): Element? =
        elements.firstOrNull { it.elementType == type && it.confidence >= minConfidence }
}
