package com.momosi.trucktrack.core.uilibrary.modifier

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

private val LocalShimmerProgress = compositionLocalOf<Float?> { null }

@Composable
fun ShimmerGroup(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalShimmerProgress provides rememberShimmerTransitionProgress(), content = content)
}

@Composable
private fun rememberShimmerTransitionProgress(): Float {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmer_progress",
    )
    return progress
}

@Composable
fun Modifier.shimmer(): Modifier {
    val sharedProgress = LocalShimmerProgress.current
    val progress = sharedProgress ?: rememberShimmerTransitionProgress()

    return drawWithContent {
        drawContent()
        val width = size.width
        val height = size.height
        val shimmerWidth = width * 0.6f
        val startX = width * progress * 1.6f - shimmerWidth

        val brush = Brush.linearGradient(
            colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.35f), Color.Transparent),
            start = Offset(startX, 0f),
            end = Offset(startX + shimmerWidth, height),
        )
        drawRect(brush = brush, size = size)
    }
}
