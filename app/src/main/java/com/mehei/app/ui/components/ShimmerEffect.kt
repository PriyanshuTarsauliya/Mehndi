package com.mehei.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import com.mehei.app.ui.theme.MeheiSurfaceVariant

/**
 * Warm henna-tinted shimmer effect using the MEHEI palette.
 * Uses a diagonal sweep for a more premium feel.
 */
fun Modifier.shimmerEffect(): Modifier = composed {
    val shimmerBase = MeheiSurfaceVariant
    val shimmerHighlight = MaterialTheme.colorScheme.surface

    val shimmerColors = listOf(
        shimmerBase.copy(alpha = 0.6f),
        shimmerHighlight.copy(alpha = 0.3f),
        shimmerBase.copy(alpha = 0.6f),
    )

    val transition = rememberInfiniteTransition(label = "shimmer_transition")
    val translateAnimation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1200f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1200,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )

    background(
        brush = Brush.linearGradient(
            colors = shimmerColors,
            start = Offset(translateAnimation - 400f, translateAnimation - 400f),
            end = Offset(translateAnimation, translateAnimation)
        )
    )
}
