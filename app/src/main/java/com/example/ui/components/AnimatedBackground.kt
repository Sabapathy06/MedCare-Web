package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalConfiguration
import com.example.ui.theme.ElegantPurple
import com.example.ui.theme.ElegantTeal
import kotlin.random.Random

@Composable
fun FloatingPillsBackground(
    reducedMotion: Boolean = false
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.toFloat()
    val screenHeight = configuration.screenHeightDp.toFloat()

    val pillCount = 12
    val pills = remember {
        List(pillCount) {
            PillState(
                x = Random.nextFloat() * screenWidth,
                y = Random.nextFloat() * screenHeight,
                size = Random.nextFloat() * 40 + 20,
                speed = Random.nextFloat() * 0.5f + 0.2f,
                rotationSpeed = Random.nextFloat() * 30 + 10,
                color = if (Random.nextBoolean()) ElegantPurple.copy(alpha = 0.15f) else ElegantTeal.copy(alpha = 0.15f)
            )
        }
    }

    if (!reducedMotion) {
        val infiniteTransition = rememberInfiniteTransition(label = "pills")
        val animationProgress by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(10000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "progress"
        )

        Canvas(modifier = Modifier.fillMaxSize()) {
            pills.forEach { pill ->
                val currentY = (pill.y + animationProgress * screenHeight * pill.speed) % (screenHeight + 100) - 50
                val currentRotation = animationProgress * 360 * (pill.rotationSpeed / 10)
                
                rotate(degrees = currentRotation, pivot = Offset(pill.x, currentY)) {
                    // Draw a simple capsule shape
                    drawRoundRect(
                        color = pill.color,
                        topLeft = Offset(pill.x - pill.size / 2, currentY - pill.size / 4),
                        size = androidx.compose.ui.geometry.Size(pill.size, pill.size / 2),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(pill.size / 4)
                    )
                }
            }
        }
    }
}

data class PillState(
    val x: Float,
    val y: Float,
    val size: Float,
    val speed: Float,
    val rotationSpeed: Float,
    val color: Color
)
