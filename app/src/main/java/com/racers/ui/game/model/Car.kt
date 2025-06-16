package com.racers.ui.game.model

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Size

data class Car(
    val id: Int,
    var position: Offset,
    var speed: Float = 0f,
    val color: Color,
    val isAICar: Boolean = false,
    var lane: Int = 0,
    var size: Size = Size(40f, 60f),
    var targetLane: Int? = null,
    var acceleration: Float = 0f,
    var behavior: AIBehavior = AIBehavior.NORMAL
)

enum class AIBehavior {
    AGGRESSIVE,    // Frequently changes lanes, high speed
    NORMAL,        // Occasional lane changes, medium speed
    CAUTIOUS      // Rare lane changes, lower speed
}