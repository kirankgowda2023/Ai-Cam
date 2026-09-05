package com.example.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CameraSecondary
import kotlin.math.roundToInt

@Composable
fun GridOverlay(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        val gridLineColor = Color.White.copy(alpha = 0.35f)
        val stroke = 1.25f

        // Two vertical lines
        val x1 = width / 3f
        val x2 = (width * 2f) / 3f
        drawLine(color = gridLineColor, start = Offset(x1, 0f), end = Offset(x1, height), strokeWidth = stroke)
        drawLine(color = gridLineColor, start = Offset(x2, 0f), end = Offset(x2, height), strokeWidth = stroke)

        // Two horizontal lines
        val y1 = height / 3f
        val y2 = (height * 2f) / 3f
        drawLine(color = gridLineColor, start = Offset(0f, y1), end = Offset(width, y1), strokeWidth = stroke)
        drawLine(color = gridLineColor, start = Offset(0f, y2), end = Offset(width, y2), strokeWidth = stroke)
    }
}

@Composable
fun FocusIndicator(
    focusOffset: Offset?,
    modifier: Modifier = Modifier
) {
    if (focusOffset == null) return

    val scale = remember(focusOffset) { Animatable(1.5f) }
    val alpha = remember(focusOffset) { Animatable(1f) }

    LaunchedEffect(focusOffset) {
        scale.snapTo(1.5f)
        alpha.snapTo(1f)
        scale.animateTo(1f, animationSpec = tween(220, easing = FastOutSlowInEasing))
        alpha.animateTo(0f, animationSpec = tween(600, delayMillis = 400))
    }

    if (alpha.value > 0.05f) {
        val sizeDp = 64.dp
        Box(
            modifier = modifier
                .offset {
                    IntOffset(
                        (focusOffset.x - (sizeDp.toPx() * scale.value / 2)).roundToInt(),
                        (focusOffset.y - (sizeDp.toPx() * scale.value / 2)).roundToInt()
                    )
                }
                .size(sizeDp * scale.value)
                .alpha(alpha.value)
                .border(1.75.dp, CameraSecondary, RoundedCornerShape(8.dp))
        )
    }
}

@Composable
fun ShutterFlashOverlay(
    triggerTime: Long,
    modifier: Modifier = Modifier
) {
    if (triggerTime == 0L) return

    val flashAlpha = remember(triggerTime) { Animatable(0.85f) }

    LaunchedEffect(triggerTime) {
        flashAlpha.snapTo(0.85f)
        flashAlpha.animateTo(0f, animationSpec = tween(180))
    }

    if (flashAlpha.value > 0.01f) {
        Canvas(modifier = modifier.fillMaxSize().alpha(flashAlpha.value)) {
            drawRect(Color.White)
        }
    }
}
