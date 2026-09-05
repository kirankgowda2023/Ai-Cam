package com.example.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PoseAnnotation
import com.example.model.PoseRecommendation
import com.example.model.PoseSilhouetteType
import kotlin.math.roundToInt

@Composable
fun PoseSilhouetteOverlay(
    pose: PoseRecommendation,
    isMirrored: Boolean = false,
    alpha: Float = 0.85f,
    showAnnotations: Boolean = true,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val viewWidth = constraints.maxWidth.toFloat()
        val viewHeight = constraints.maxHeight.toFloat()

        // Canvas draws the white contour lines with contrasting shadow outline
        Canvas(modifier = Modifier.fillMaxSize().alpha(alpha * pulseAlpha)) {
            val strokeWidth = 3.5.dp.toPx()
            val shadowStrokeWidth = strokeWidth + 3.dp.toPx()

            // Draw Silhouette Path based on pose type
            val path = createPosePath(
                type = pose.silhouetteType,
                width = size.width,
                height = size.height,
                isMirrored = isMirrored
            )

            // Outer dark contrasting halo so line is visible on light/bright backgrounds
            drawPath(
                path = path,
                color = Color.Black.copy(alpha = 0.5f),
                style = Stroke(
                    width = shadowStrokeWidth,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )

            // Primary crisp white line
            drawPath(
                path = path,
                color = Color.White,
                style = Stroke(
                    width = strokeWidth,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )

            // Draw pointer lines for annotations
            if (showAnnotations) {
                pose.annotations.forEach { annotation ->
                    drawAnnotationPointer(
                        annotation = annotation,
                        canvasWidth = size.width,
                        canvasHeight = size.height,
                        isMirrored = isMirrored
                    )
                }
            }
        }

        // Render Compose Floating Badges for text notes (e.g. "Raise Diploma", "Lift Leg", "Cross Legs")
        if (showAnnotations) {
            val density = androidx.compose.ui.platform.LocalDensity.current
            pose.annotations.forEach { annotation ->
                val normX = if (isMirrored) 1f - annotation.targetNormX else annotation.targetNormX
                val targetPxX = normX * viewWidth
                val targetPxY = annotation.targetNormY * viewHeight
                val (offsetX, offsetY) = with(density) {
                    Pair(
                        (if (isMirrored) -annotation.labelOffsetX else annotation.labelOffsetX).dp.toPx(),
                        annotation.labelOffsetY.dp.toPx()
                    )
                }

                val finalX = (targetPxX + offsetX).roundToInt()
                val finalY = (targetPxY + offsetY).roundToInt()

                Box(
                    modifier = Modifier
                        .offset { IntOffset(finalX, finalY) }
                        .background(Color.Black.copy(alpha = 0.72f), RoundedCornerShape(12.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = annotation.text,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawAnnotationPointer(
    annotation: PoseAnnotation,
    canvasWidth: Float,
    canvasHeight: Float,
    isMirrored: Boolean
) {
    val normX = if (isMirrored) 1f - annotation.targetNormX else annotation.targetNormX
    val startX = normX * canvasWidth
    val startY = annotation.targetNormY * canvasHeight

    val offsetX = (if (isMirrored) -annotation.labelOffsetX else annotation.labelOffsetX).dp.toPx()
    val offsetY = annotation.labelOffsetY.dp.toPx()
    val endX = startX + offsetX
    val endY = startY + offsetY

    // Dot at anchor point
    drawCircle(
        color = Color.White,
        radius = 4.dp.toPx(),
        center = Offset(startX, startY)
    )

    // Connecting dashed or solid pointer line
    drawLine(
        color = Color.White.copy(alpha = 0.75f),
        start = Offset(startX, startY),
        end = Offset(endX, endY),
        strokeWidth = 1.5.dp.toPx(),
        cap = StrokeCap.Round
    )
}

private fun createPosePath(
    type: PoseSilhouetteType,
    width: Float,
    height: Float,
    isMirrored: Boolean
): Path {
    val path = Path()

    // Normalize coordinates in 0f..1f and map to viewport
    fun pX(x: Float): Float = if (isMirrored) (1f - x) * width else x * width
    fun pY(y: Float): Float = y * height

    when (type) {
        PoseSilhouetteType.GRADUATION_KICK_DIPLOMA -> {
            // Inspired directly by Image 1:
            // Mortarboard cap, gown, raised left hand with diploma scroll, right hand spread, right leg kicked out
            // Cap outline
            path.moveTo(pX(0.42f), pY(0.18f))
            path.lineTo(pX(0.50f), pY(0.13f))
            path.lineTo(pX(0.58f), pY(0.18f))
            path.lineTo(pX(0.50f), pY(0.23f))
            path.close()
            // Tassel
            path.moveTo(pX(0.50f), pY(0.18f))
            path.lineTo(pX(0.43f), pY(0.25f))

            // Head and Neck
            path.moveTo(pX(0.46f), pY(0.21f))
            path.cubicTo(pX(0.43f), pY(0.25f), pX(0.44f), pY(0.31f), pX(0.48f), pY(0.33f))
            path.lineTo(pX(0.53f), pY(0.33f))
            path.cubicTo(pX(0.57f), pY(0.31f), pX(0.57f), pY(0.25f), pX(0.54f), pY(0.21f))

            // Left Raised Arm with Diploma
            path.moveTo(pX(0.45f), pY(0.33f))
            path.lineTo(pX(0.32f), pY(0.38f))
            path.lineTo(pX(0.20f), pY(0.30f))
            // Hand holding diploma scroll
            path.lineTo(pX(0.18f), pY(0.24f))
            path.lineTo(pX(0.22f), pY(0.22f))
            path.lineTo(pX(0.23f), pY(0.29f))
            // Diploma scroll outline
            path.moveTo(pX(0.16f), pY(0.25f))
            path.lineTo(pX(0.17f), pY(0.16f))
            path.lineTo(pX(0.23f), pY(0.17f))
            path.lineTo(pX(0.22f), pY(0.26f))
            path.close()

            // Gown Collar & Torso Left Side
            path.moveTo(pX(0.45f), pY(0.34f))
            path.lineTo(pX(0.40f), pY(0.42f))
            path.lineTo(pX(0.36f), pY(0.58f))

            // Right Flaring Gown Sleeve & Spread Arm
            path.moveTo(pX(0.55f), pY(0.33f))
            path.lineTo(pX(0.68f), pY(0.26f))
            path.lineTo(pX(0.80f), pY(0.18f)) // Raised spread hand
            path.lineTo(pX(0.79f), pY(0.24f))
            path.lineTo(pX(0.68f), pY(0.38f))
            path.lineTo(pX(0.65f), pY(0.55f)) // Gown flow

            // Standing Support Leg (Left Leg)
            path.moveTo(pX(0.38f), pY(0.58f))
            path.lineTo(pX(0.36f), pY(0.75f))
            path.lineTo(pX(0.38f), pY(0.88f))
            path.lineTo(pX(0.34f), pY(0.89f))
            path.lineTo(pX(0.33f), pY(0.76f))
            path.lineTo(pX(0.43f), pY(0.58f))

            // Kicking Dynamic Leg (Right Leg stretched outward horizontally/upward)
            path.moveTo(pX(0.50f), pY(0.58f))
            path.lineTo(pX(0.68f), pY(0.68f))
            path.lineTo(pX(0.86f), pY(0.76f)) // Foot kicking out
            path.lineTo(pX(0.88f), pY(0.82f))
            path.lineTo(pX(0.82f), pY(0.82f))
            path.lineTo(pX(0.65f), pY(0.74f))
            path.lineTo(pX(0.48f), pY(0.62f))
        }

        PoseSilhouetteType.BENCH_SITTING_CROSSED -> {
            // Inspired directly by Image 2:
            // Bench backrest, seated figure, arm draped over backrest, legs crossed with foot angled
            // Bench backrest horizontal line
            path.moveTo(pX(0.14f), pY(0.54f))
            path.lineTo(pX(0.86f), pY(0.54f))
            path.moveTo(pX(0.18f), pY(0.46f))
            path.lineTo(pX(0.82f), pY(0.46f))

            // Head & Glasses silhouette
            path.moveTo(pX(0.50f), pY(0.25f))
            path.cubicTo(pX(0.44f), pY(0.25f), pX(0.44f), pY(0.35f), pX(0.50f), pY(0.36f))
            path.cubicTo(pX(0.56f), pY(0.35f), pX(0.56f), pY(0.25f), pX(0.50f), pY(0.25f))
            // Glasses hint
            path.moveTo(pX(0.46f), pY(0.30f))
            path.lineTo(pX(0.54f), pY(0.30f))

            // Arm Draped Across Bench Backrest
            path.moveTo(pX(0.44f), pY(0.38f))
            path.lineTo(pX(0.34f), pY(0.44f))
            path.lineTo(pX(0.26f), pY(0.44f))
            path.lineTo(pX(0.24f), pY(0.50f)) // Hand resting on back
            path.lineTo(pX(0.30f), pY(0.50f))
            path.lineTo(pX(0.38f), pY(0.46f))

            // Torso / Polo Shirt
            path.moveTo(pX(0.44f), pY(0.38f))
            path.lineTo(pX(0.42f), pY(0.56f))
            path.lineTo(pX(0.58f), pY(0.56f))
            path.lineTo(pX(0.58f), pY(0.38f))

            // Other Arm resting on lap/leg
            path.moveTo(pX(0.56f), pY(0.40f))
            path.lineTo(pX(0.64f), pY(0.48f))
            path.lineTo(pX(0.56f), pY(0.54f))

            // Crossed Legs (One leg over the other, elevated knee)
            path.moveTo(pX(0.43f), pY(0.56f))
            path.lineTo(pX(0.48f), pY(0.66f)) // Crossed knee
            path.lineTo(pX(0.52f), pY(0.82f)) // Shin angled down
            path.lineTo(pX(0.55f), pY(0.88f)) // Shoe tip
            path.lineTo(pX(0.48f), pY(0.87f))
            path.lineTo(pX(0.44f), pY(0.72f))

            // Lower supporting leg & ground shoe
            path.moveTo(pX(0.38f), pY(0.56f))
            path.lineTo(pX(0.36f), pY(0.72f))
            path.lineTo(pX(0.38f), pY(0.82f))
            path.lineTo(pX(0.33f), pY(0.82f))
            path.lineTo(pX(0.32f), pY(0.70f))
        }

        PoseSilhouetteType.GRADUATION_CAP_TOSS -> {
            // Cap tossed high up
            path.moveTo(pX(0.46f), pY(0.10f))
            path.lineTo(pX(0.54f), pY(0.07f))
            path.lineTo(pX(0.58f), pY(0.12f))
            path.lineTo(pX(0.50f), pY(0.15f))
            path.close()

            // Head looking up
            path.moveTo(pX(0.46f), pY(0.24f))
            path.cubicTo(pX(0.44f), pY(0.20f), pX(0.54f), pY(0.20f), pX(0.54f), pY(0.24f))
            path.cubicTo(pX(0.54f), pY(0.30f), pX(0.46f), pY(0.30f), pX(0.46f), pY(0.24f))

            // Both Arms Reaching Straight Up
            path.moveTo(pX(0.44f), pY(0.32f))
            path.lineTo(pX(0.36f), pY(0.18f))
            path.lineTo(pX(0.38f), pY(0.17f))
            path.lineTo(pX(0.46f), pY(0.30f))

            path.moveTo(pX(0.54f), pY(0.30f))
            path.lineTo(pX(0.62f), pY(0.17f))
            path.lineTo(pX(0.64f), pY(0.18f))
            path.lineTo(pX(0.56f), pY(0.32f))

            // Flowing Gown & Legs
            path.moveTo(pX(0.42f), pY(0.34f))
            path.lineTo(pX(0.38f), pY(0.62f))
            path.lineTo(pX(0.62f), pY(0.62f))
            path.lineTo(pX(0.58f), pY(0.34f))

            // Legs standing wide celebrating
            path.moveTo(pX(0.42f), pY(0.62f))
            path.lineTo(pX(0.40f), pY(0.86f))
            path.moveTo(pX(0.58f), pY(0.62f))
            path.lineTo(pX(0.60f), pY(0.86f))
        }

        PoseSilhouetteType.GRADUATION_PROUD_STAND -> {
            // Mortarboard cap
            path.moveTo(pX(0.43f), pY(0.16f))
            path.lineTo(pX(0.50f), pY(0.12f))
            path.lineTo(pX(0.57f), pY(0.16f))
            path.lineTo(pX(0.50f), pY(0.20f))
            path.close()

            // Head
            path.moveTo(pX(0.47f), pY(0.20f))
            path.cubicTo(pX(0.44f), pY(0.22f), pX(0.44f), pY(0.28f), pX(0.50f), pY(0.30f))
            path.cubicTo(pX(0.56f), pY(0.28f), pX(0.56f), pY(0.22f), pX(0.47f), pY(0.20f))

            // Torso & Gown
            path.moveTo(pX(0.42f), pY(0.32f))
            path.lineTo(pX(0.38f), pY(0.66f))
            path.lineTo(pX(0.62f), pY(0.66f))
            path.lineTo(pX(0.58f), pY(0.32f))

            // Hands folded holding diploma across chest
            path.moveTo(pX(0.40f), pY(0.42f))
            path.lineTo(pX(0.50f), pY(0.44f))
            path.lineTo(pX(0.60f), pY(0.42f))
            // Diploma cylinder
            path.moveTo(pX(0.44f), pY(0.42f))
            path.lineTo(pX(0.56f), pY(0.46f))

            // Standing legs
            path.moveTo(pX(0.45f), pY(0.66f))
            path.lineTo(pX(0.45f), pY(0.88f))
            path.moveTo(pX(0.55f), pY(0.66f))
            path.lineTo(pX(0.55f), pY(0.88f))
        }

        PoseSilhouetteType.GARDEN_LEAN_CHIN -> {
            // Tilted head
            path.moveTo(pX(0.48f), pY(0.22f))
            path.cubicTo(pX(0.44f), pY(0.20f), pX(0.44f), pY(0.30f), pX(0.50f), pY(0.32f))
            path.cubicTo(pX(0.56f), pY(0.32f), pX(0.56f), pY(0.22f), pX(0.48f), pY(0.22f))

            // One hand near chin
            path.moveTo(pX(0.56f), pY(0.42f))
            path.lineTo(pX(0.58f), pY(0.32f))
            path.lineTo(pX(0.54f), pY(0.30f))

            // Gentle torso lean
            path.moveTo(pX(0.42f), pY(0.34f))
            path.lineTo(pX(0.40f), pY(0.58f))
            path.lineTo(pX(0.56f), pY(0.60f))
            path.lineTo(pX(0.56f), pY(0.36f))

            // Lower body
            path.moveTo(pX(0.42f), pY(0.60f))
            path.lineTo(pX(0.45f), pY(0.86f))
            path.moveTo(pX(0.54f), pY(0.60f))
            path.lineTo(pX(0.52f), pY(0.86f))
        }

        PoseSilhouetteType.BENCH_CASUAL_LOOKBACK -> {
            // Bench seat
            path.moveTo(pX(0.20f), pY(0.58f))
            path.lineTo(pX(0.80f), pY(0.58f))

            // Angled head looking back over shoulder
            path.moveTo(pX(0.52f), pY(0.28f))
            path.cubicTo(pX(0.46f), pY(0.28f), pX(0.46f), pY(0.38f), pX(0.52f), pY(0.38f))
            path.cubicTo(pX(0.58f), pY(0.38f), pX(0.58f), pY(0.28f), pX(0.52f), pY(0.28f))

            // Torso turned sideways
            path.moveTo(pX(0.48f), pY(0.38f))
            path.lineTo(pX(0.44f), pY(0.58f))
            path.lineTo(pX(0.60f), pY(0.58f))
            path.lineTo(pX(0.56f), pY(0.38f))

            // Seated legs draped to one side
            path.moveTo(pX(0.52f), pY(0.58f))
            path.lineTo(pX(0.62f), pY(0.70f))
            path.lineTo(pX(0.64f), pY(0.84f))
        }

        PoseSilhouetteType.URBAN_STRIDE_WALK -> {
            // Head
            path.moveTo(pX(0.50f), pY(0.18f))
            path.cubicTo(pX(0.46f), pY(0.18f), pX(0.46f), pY(0.28f), pX(0.50f), pY(0.28f))
            path.cubicTo(pX(0.54f), pY(0.28f), pX(0.54f), pY(0.18f), pX(0.50f), pY(0.18f))

            // Torso with swinging jacket
            path.moveTo(pX(0.44f), pY(0.28f))
            path.lineTo(pX(0.40f), pY(0.54f))
            path.lineTo(pX(0.58f), pY(0.52f))
            path.lineTo(pX(0.54f), pY(0.28f))

            // Walking arms
            path.moveTo(pX(0.44f), pY(0.32f))
            path.lineTo(pX(0.34f), pY(0.44f))

            path.moveTo(pX(0.54f), pY(0.32f))
            path.lineTo(pX(0.62f), pY(0.42f))

            // Stride legs (one forward, one trailing)
            path.moveTo(pX(0.44f), pY(0.54f))
            path.lineTo(pX(0.34f), pY(0.76f))
            path.lineTo(pX(0.30f), pY(0.88f)) // Trailing foot

            path.moveTo(pX(0.52f), pY(0.53f))
            path.lineTo(pX(0.62f), pY(0.72f))
            path.lineTo(pX(0.68f), pY(0.86f)) // Forward stride foot
        }

        PoseSilhouetteType.URBAN_WALL_LEAN -> {
            // Vertical wall line on the side
            path.moveTo(pX(0.25f), pY(0.15f))
            path.lineTo(pX(0.25f), pY(0.90f))

            // Head tilted back slightly
            path.moveTo(pX(0.42f), pY(0.24f))
            path.cubicTo(pX(0.38f), pY(0.24f), pX(0.38f), pY(0.32f), pX(0.42f), pY(0.32f))
            path.cubicTo(pX(0.46f), pY(0.32f), pX(0.46f), pY(0.24f), pX(0.42f), pY(0.24f))

            // Leaning torso
            path.moveTo(pX(0.36f), pY(0.34f))
            path.lineTo(pX(0.34f), pY(0.56f))
            path.lineTo(pX(0.48f), pY(0.56f))
            path.lineTo(pX(0.46f), pY(0.34f))

            // Hand in pocket
            path.moveTo(pX(0.44f), pY(0.36f))
            path.lineTo(pX(0.48f), pY(0.48f))

            // Crossed or bent knee
            path.moveTo(pX(0.36f), pY(0.56f))
            path.lineTo(pX(0.38f), pY(0.88f))
            path.moveTo(pX(0.46f), pY(0.56f))
            path.lineTo(pX(0.54f), pY(0.70f))
            path.lineTo(pX(0.44f), pY(0.86f))
        }

        PoseSilhouetteType.SCENIC_ARMS_WIDE -> {
            // Head
            path.moveTo(pX(0.50f), pY(0.20f))
            path.cubicTo(pX(0.45f), pY(0.20f), pX(0.45f), pY(0.28f), pX(0.50f), pY(0.28f))
            path.cubicTo(pX(0.55f), pY(0.28f), pX(0.55f), pY(0.20f), pX(0.50f), pY(0.20f))

            // Wide Open Arms
            path.moveTo(pX(0.44f), pY(0.30f))
            path.lineTo(pX(0.22f), pY(0.26f))
            path.lineTo(pX(0.12f), pY(0.22f)) // Left arm wide open

            path.moveTo(pX(0.56f), pY(0.30f))
            path.lineTo(pX(0.78f), pY(0.26f))
            path.lineTo(pX(0.88f), pY(0.22f)) // Right arm wide open

            // Torso
            path.moveTo(pX(0.44f), pY(0.30f))
            path.lineTo(pX(0.43f), pY(0.58f))
            path.lineTo(pX(0.57f), pY(0.58f))
            path.lineTo(pX(0.56f), pY(0.30f))

            // Ground stance
            path.moveTo(pX(0.45f), pY(0.58f))
            path.lineTo(pX(0.42f), pY(0.88f))
            path.moveTo(pX(0.55f), pY(0.58f))
            path.lineTo(pX(0.58f), pY(0.88f))
        }

        PoseSilhouetteType.CAFE_TABLE_PROP -> {
            // Table horizontal surface
            path.moveTo(pX(0.18f), pY(0.58f))
            path.lineTo(pX(0.82f), pY(0.58f))

            // Head resting on hands
            path.moveTo(pX(0.50f), pY(0.28f))
            path.cubicTo(pX(0.44f), pY(0.28f), pX(0.44f), pY(0.38f), pX(0.50f), pY(0.38f))
            path.cubicTo(pX(0.56f), pY(0.38f), pX(0.56f), pY(0.28f), pX(0.50f), pY(0.28f))

            // Arms propped on table
            path.moveTo(pX(0.44f), pY(0.38f))
            path.lineTo(pX(0.38f), pY(0.58f))
            path.lineTo(pX(0.48f), pY(0.50f))

            path.moveTo(pX(0.56f), pY(0.38f))
            path.lineTo(pX(0.62f), pY(0.58f))
            path.lineTo(pX(0.52f), pY(0.50f))

            // Coffee Cup outline
            path.moveTo(pX(0.46f), pY(0.52f))
            path.lineTo(pX(0.47f), pY(0.58f))
            path.lineTo(pX(0.53f), pY(0.58f))
            path.lineTo(pX(0.54f), pY(0.52f))
            path.close()
        }

        PoseSilhouetteType.CAMPUS_STAIRS_SEATED -> {
            // Campus steps lines
            path.moveTo(pX(0.15f), pY(0.58f))
            path.lineTo(pX(0.85f), pY(0.58f))
            path.moveTo(pX(0.15f), pY(0.72f))
            path.lineTo(pX(0.85f), pY(0.72f))
            path.moveTo(pX(0.15f), pY(0.86f))
            path.lineTo(pX(0.85f), pY(0.86f))

            // Seated scholar with diploma on lap
            path.moveTo(pX(0.48f), pY(0.24f))
            path.cubicTo(pX(0.43f), pY(0.24f), pX(0.43f), pY(0.33f), pX(0.48f), pY(0.33f))
            path.cubicTo(pX(0.53f), pY(0.33f), pX(0.53f), pY(0.24f), pX(0.48f), pY(0.24f))

            // Torso
            path.moveTo(pX(0.44f), pY(0.33f))
            path.lineTo(pX(0.40f), pY(0.56f))
            path.lineTo(pX(0.58f), pY(0.56f))
            path.lineTo(pX(0.54f), pY(0.33f))

            // Bent knees resting on step
            path.moveTo(pX(0.42f), pY(0.56f))
            path.lineTo(pX(0.36f), pY(0.70f))
            path.lineTo(pX(0.38f), pY(0.84f))

            path.moveTo(pX(0.56f), pY(0.56f))
            path.lineTo(pX(0.62f), pY(0.66f))
            path.lineTo(pX(0.58f), pY(0.84f))

            // Diploma scroll on lap
            path.moveTo(pX(0.44f), pY(0.54f))
            path.lineTo(pX(0.56f), pY(0.54f))
        }

        PoseSilhouetteType.FLOWER_SMELL_BEND -> {
            // Floral stem line
            path.moveTo(pX(0.66f), pY(0.60f))
            path.cubicTo(pX(0.68f), pY(0.45f), pX(0.62f), pY(0.38f), pX(0.58f), pY(0.34f))

            // Head tilting softly towards flower
            path.moveTo(pX(0.46f), pY(0.20f))
            path.cubicTo(pX(0.41f), pY(0.20f), pX(0.41f), pY(0.29f), pX(0.46f), pY(0.30f))
            path.cubicTo(pX(0.51f), pY(0.29f), pX(0.51f), pY(0.20f), pX(0.46f), pY(0.20f))

            // Hand holding flower stem
            path.moveTo(pX(0.50f), pY(0.34f))
            path.lineTo(pX(0.56f), pY(0.36f))

            // Body
            path.moveTo(pX(0.42f), pY(0.32f))
            path.lineTo(pX(0.40f), pY(0.60f))
            path.lineTo(pX(0.54f), pY(0.60f))
            path.lineTo(pX(0.52f), pY(0.32f))

            // Legs
            path.moveTo(pX(0.42f), pY(0.60f))
            path.lineTo(pX(0.44f), pY(0.86f))
            path.moveTo(pX(0.52f), pY(0.60f))
            path.lineTo(pX(0.50f), pY(0.86f))
        }

        PoseSilhouetteType.STREET_CURB_SIT -> {
            // Curb horizontal edge
            path.moveTo(pX(0.12f), pY(0.70f))
            path.lineTo(pX(0.88f), pY(0.70f))

            // Head
            path.moveTo(pX(0.48f), pY(0.24f))
            path.cubicTo(pX(0.43f), pY(0.24f), pX(0.43f), pY(0.33f), pX(0.48f), pY(0.33f))
            path.cubicTo(pX(0.53f), pY(0.33f), pX(0.53f), pY(0.24f), pX(0.48f), pY(0.24f))

            // Torso leaning forward
            path.moveTo(pX(0.44f), pY(0.34f))
            path.lineTo(pX(0.42f), pY(0.58f))
            path.lineTo(pX(0.56f), pY(0.58f))
            path.lineTo(pX(0.54f), pY(0.34f))

            // Forearms on knees
            path.moveTo(pX(0.44f), pY(0.40f))
            path.lineTo(pX(0.38f), pY(0.54f))
            path.lineTo(pX(0.46f), pY(0.56f))

            path.moveTo(pX(0.54f), pY(0.40f))
            path.lineTo(pX(0.60f), pY(0.54f))
            path.lineTo(pX(0.52f), pY(0.56f))

            // Knees pulled up & shoes forward
            path.moveTo(pX(0.42f), pY(0.58f))
            path.lineTo(pX(0.36f), pY(0.62f))
            path.lineTo(pX(0.38f), pY(0.84f))

            path.moveTo(pX(0.56f), pY(0.58f))
            path.lineTo(pX(0.64f), pY(0.62f))
            path.lineTo(pX(0.66f), pY(0.84f))
        }

        PoseSilhouetteType.CROSSING_LOOKBACK -> {
            // Walking away, head looking back over shoulder
            path.moveTo(pX(0.52f), pY(0.22f))
            path.cubicTo(pX(0.47f), pY(0.22f), pX(0.47f), pY(0.31f), pX(0.52f), pY(0.31f))
            path.cubicTo(pX(0.57f), pY(0.31f), pX(0.57f), pY(0.22f), pX(0.52f), pY(0.22f))

            // Turned back torso
            path.moveTo(pX(0.46f), pY(0.32f))
            path.lineTo(pX(0.42f), pY(0.56f))
            path.lineTo(pX(0.58f), pY(0.56f))
            path.lineTo(pX(0.56f), pY(0.32f))

            // Walking legs
            path.moveTo(pX(0.44f), pY(0.56f))
            path.lineTo(pX(0.38f), pY(0.82f))

            path.moveTo(pX(0.54f), pY(0.56f))
            path.lineTo(pX(0.62f), pY(0.78f))
        }

        PoseSilhouetteType.CLIFF_EDGE_PERCH -> {
            // Scenic rock ledge
            path.moveTo(pX(0.18f), pY(0.64f))
            path.lineTo(pX(0.65f), pY(0.64f))
            path.lineTo(pX(0.75f), pY(0.90f))

            // Perched person
            path.moveTo(pX(0.46f), pY(0.24f))
            path.cubicTo(pX(0.41f), pY(0.24f), pX(0.41f), pY(0.33f), pX(0.46f), pY(0.33f))
            path.cubicTo(pX(0.51f), pY(0.33f), pX(0.51f), pY(0.24f), pX(0.46f), pY(0.24f))

            // Arms propped behind for support
            path.moveTo(pX(0.42f), pY(0.36f))
            path.lineTo(pX(0.32f), pY(0.56f))

            // Torso
            path.moveTo(pX(0.44f), pY(0.34f))
            path.lineTo(pX(0.46f), pY(0.60f))
            path.lineTo(pX(0.58f), pY(0.60f))
            path.lineTo(pX(0.54f), pY(0.34f))

            // One leg draped, one knee up
            path.moveTo(pX(0.48f), pY(0.60f))
            path.lineTo(pX(0.56f), pY(0.52f))
            path.lineTo(pX(0.58f), pY(0.64f))

            path.moveTo(pX(0.54f), pY(0.60f))
            path.lineTo(pX(0.62f), pY(0.80f))
        }

        PoseSilhouetteType.SUNSET_SILHOUETTE_SIDE -> {
            // Elegant side profile facing the sunset
            path.moveTo(pX(0.46f), pY(0.20f))
            path.cubicTo(pX(0.42f), pY(0.20f), pX(0.42f), pY(0.30f), pX(0.48f), pY(0.30f))
            path.lineTo(pX(0.54f), pY(0.27f)) // Nose profile
            path.lineTo(pX(0.50f), pY(0.20f))

            // Hand in hair
            path.moveTo(pX(0.46f), pY(0.34f))
            path.lineTo(pX(0.42f), pY(0.24f))
            path.lineTo(pX(0.44f), pY(0.18f))

            // Slim arched back profile
            path.moveTo(pX(0.44f), pY(0.32f))
            path.cubicTo(pX(0.40f), pY(0.44f), pX(0.42f), pY(0.54f), pX(0.44f), pY(0.62f))
            path.lineTo(pX(0.54f), pY(0.62f))
            path.lineTo(pX(0.52f), pY(0.32f))

            // Standing legs
            path.moveTo(pX(0.46f), pY(0.62f))
            path.lineTo(pX(0.44f), pY(0.88f))
            path.moveTo(pX(0.52f), pY(0.62f))
            path.lineTo(pX(0.54f), pY(0.88f))
        }

        PoseSilhouetteType.BEACH_WALK_BAREFOOT -> {
            // Foam water ripples at feet
            path.moveTo(pX(0.18f), pY(0.88f))
            path.cubicTo(pX(0.35f), pY(0.85f), pX(0.55f), pY(0.91f), pX(0.82f), pY(0.87f))

            // Head
            path.moveTo(pX(0.50f), pY(0.20f))
            path.cubicTo(pX(0.45f), pY(0.20f), pX(0.45f), pY(0.29f), pX(0.50f), pY(0.29f))
            path.cubicTo(pX(0.55f), pY(0.29f), pX(0.55f), pY(0.20f), pX(0.50f), pY(0.20f))

            // One hand carrying sandals
            path.moveTo(pX(0.44f), pY(0.32f))
            path.lineTo(pX(0.34f), pY(0.46f))
            path.lineTo(pX(0.32f), pY(0.54f))
            // Sandals silhouette
            path.moveTo(pX(0.30f), pY(0.54f))
            path.lineTo(pX(0.35f), pY(0.54f))

            // Flowing dress/shirt
            path.moveTo(pX(0.44f), pY(0.31f))
            path.lineTo(pX(0.40f), pY(0.62f))
            path.lineTo(pX(0.60f), pY(0.62f))
            path.lineTo(pX(0.56f), pY(0.31f))

            // Walking in surf
            path.moveTo(pX(0.44f), pY(0.62f))
            path.lineTo(pX(0.42f), pY(0.86f))
            path.moveTo(pX(0.56f), pY(0.62f))
            path.lineTo(pX(0.60f), pY(0.84f))
        }

        PoseSilhouetteType.CAFE_WINDOW_SIP -> {
            // Window frame behind
            path.moveTo(pX(0.72f), pY(0.15f))
            path.lineTo(pX(0.72f), pY(0.80f))
            path.moveTo(pX(0.65f), pY(0.38f))
            path.lineTo(pX(0.85f), pY(0.38f))

            // Table
            path.moveTo(pX(0.20f), pY(0.62f))
            path.lineTo(pX(0.70f), pY(0.62f))

            // Head looking towards window
            path.moveTo(pX(0.48f), pY(0.22f))
            path.cubicTo(pX(0.43f), pY(0.22f), pX(0.43f), pY(0.32f), pX(0.49f), pY(0.32f))
            path.lineTo(pX(0.54f), pY(0.27f))
            path.lineTo(pX(0.50f), pY(0.22f))

            // Hand raising coffee cup to lips
            path.moveTo(pX(0.46f), pY(0.36f))
            path.lineTo(pX(0.52f), pY(0.34f))
            path.lineTo(pX(0.54f), pY(0.29f))
            // Cup
            path.moveTo(pX(0.53f), pY(0.28f))
            path.lineTo(pX(0.57f), pY(0.28f))
            path.lineTo(pX(0.56f), pY(0.33f))
            path.lineTo(pX(0.53f), pY(0.33f))
            path.close()

            // Torso
            path.moveTo(pX(0.44f), pY(0.34f))
            path.lineTo(pX(0.42f), pY(0.62f))
            path.lineTo(pX(0.56f), pY(0.62f))
            path.lineTo(pX(0.54f), pY(0.34f))
        }

        PoseSilhouetteType.CAFE_CHEEK_REST -> {
            // Table surface
            path.moveTo(pX(0.18f), pY(0.58f))
            path.lineTo(pX(0.82f), pY(0.58f))

            // Head tilted
            path.moveTo(pX(0.48f), pY(0.24f))
            path.cubicTo(pX(0.43f), pY(0.24f), pX(0.43f), pY(0.33f), pX(0.48f), pY(0.34f))
            path.cubicTo(pX(0.54f), pY(0.33f), pX(0.54f), pY(0.24f), pX(0.48f), pY(0.24f))

            // Arm with elbow on table, palm holding cheek
            path.moveTo(pX(0.54f), pY(0.32f))
            path.lineTo(pX(0.60f), pY(0.58f))
            path.lineTo(pX(0.46f), pY(0.58f))

            // Torso
            path.moveTo(pX(0.44f), pY(0.34f))
            path.lineTo(pX(0.40f), pY(0.58f))
            path.lineTo(pX(0.56f), pY(0.58f))
        }

        PoseSilhouetteType.SOFA_LEAN_CUSHION -> {
            // Sofa backrest & seat cushion
            path.moveTo(pX(0.15f), pY(0.48f))
            path.lineTo(pX(0.85f), pY(0.48f))
            path.moveTo(pX(0.15f), pY(0.74f))
            path.lineTo(pX(0.85f), pY(0.74f))

            // Head resting near pillow
            path.moveTo(pX(0.40f), pY(0.28f))
            path.cubicTo(pX(0.35f), pY(0.28f), pX(0.35f), pY(0.37f), pX(0.40f), pY(0.37f))
            path.cubicTo(pX(0.45f), pY(0.37f), pX(0.45f), pY(0.28f), pX(0.40f), pY(0.28f))

            // Pillow / Cushion outline
            path.moveTo(pX(0.42f), pY(0.40f))
            path.lineTo(pX(0.56f), pY(0.40f))
            path.lineTo(pX(0.58f), pY(0.56f))
            path.lineTo(pX(0.40f), pY(0.56f))
            path.close()

            // Arms wrapped around cushion
            path.moveTo(pX(0.38f), pY(0.38f))
            path.lineTo(pX(0.44f), pY(0.50f))
            path.lineTo(pX(0.54f), pY(0.48f))

            // Tucked curled legs on sofa
            path.moveTo(pX(0.42f), pY(0.56f))
            path.lineTo(pX(0.64f), pY(0.64f))
            path.lineTo(pX(0.68f), pY(0.72f))
            path.lineTo(pX(0.50f), pY(0.72f))
        }

        PoseSilhouetteType.SOFA_CHILL_STRETCH -> {
            // Sofa horizontal cushions
            path.moveTo(pX(0.12f), pY(0.66f))
            path.lineTo(pX(0.88f), pY(0.66f))

            // Head on sofa armrest
            path.moveTo(pX(0.32f), pY(0.34f))
            path.cubicTo(pX(0.27f), pY(0.34f), pX(0.27f), pY(0.43f), pX(0.32f), pY(0.43f))
            path.cubicTo(pX(0.37f), pY(0.43f), pX(0.37f), pY(0.34f), pX(0.32f), pY(0.34f))

            // Arm behind head
            path.moveTo(pX(0.30f), pY(0.40f))
            path.lineTo(pX(0.26f), pY(0.30f))
            path.lineTo(pX(0.34f), pY(0.28f))

            // Reclined torso
            path.moveTo(pX(0.36f), pY(0.42f))
            path.lineTo(pX(0.54f), pY(0.56f))

            // Stretched legs across couch
            path.moveTo(pX(0.54f), pY(0.56f))
            path.lineTo(pX(0.78f), pY(0.62f))
            path.lineTo(pX(0.84f), pY(0.65f))
        }

        PoseSilhouetteType.RUG_CROSS_LEGGED -> {
            // Floor rug oval line
            path.moveTo(pX(0.20f), pY(0.82f))
            path.cubicTo(pX(0.20f), pY(0.76f), pX(0.80f), pY(0.76f), pX(0.80f), pY(0.82f))
            path.cubicTo(pX(0.80f), pY(0.88f), pX(0.20f), pY(0.88f), pX(0.20f), pY(0.82f))

            // Head
            path.moveTo(pX(0.50f), pY(0.24f))
            path.cubicTo(pX(0.45f), pY(0.24f), pX(0.45f), pY(0.34f), pX(0.50f), pY(0.34f))
            path.cubicTo(pX(0.55f), pY(0.34f), pX(0.55f), pY(0.24f), pX(0.50f), pY(0.24f))

            // Torso leaning forward
            path.moveTo(pX(0.45f), pY(0.35f))
            path.lineTo(pX(0.42f), pY(0.58f))
            path.lineTo(pX(0.58f), pY(0.58f))
            path.lineTo(pX(0.55f), pY(0.35f))

            // Crossed legs on floor
            path.moveTo(pX(0.42f), pY(0.58f))
            path.lineTo(pX(0.32f), pY(0.72f))
            path.lineTo(pX(0.62f), pY(0.78f))

            path.moveTo(pX(0.58f), pY(0.58f))
            path.lineTo(pX(0.68f), pY(0.72f))
            path.lineTo(pX(0.38f), pY(0.78f))
        }

        PoseSilhouetteType.MIRROR_PHONE_TILT -> {
            // Mirror rectangular frame border
            path.moveTo(pX(0.18f), pY(0.08f))
            path.lineTo(pX(0.82f), pY(0.08f))
            path.lineTo(pX(0.82f), pY(0.92f))
            path.lineTo(pX(0.18f), pY(0.92f))
            path.close()

            // Head
            path.moveTo(pX(0.50f), pY(0.18f))
            path.cubicTo(pX(0.45f), pY(0.18f), pX(0.45f), pY(0.27f), pX(0.50f), pY(0.27f))
            path.cubicTo(pX(0.55f), pY(0.27f), pX(0.55f), pY(0.18f), pX(0.50f), pY(0.18f))

            // Phone held at chest level
            path.moveTo(pX(0.46f), pY(0.32f))
            path.lineTo(pX(0.54f), pY(0.32f))
            path.lineTo(pX(0.54f), pY(0.42f))
            path.lineTo(pX(0.46f), pY(0.42f))
            path.close()
            // Camera lens dot
            path.moveTo(pX(0.48f), pY(0.34f))
            path.lineTo(pX(0.49f), pY(0.34f))

            // Popped hip torso
            path.moveTo(pX(0.44f), pY(0.28f))
            path.lineTo(pX(0.38f), pY(0.50f)) // Hip flare out
            path.lineTo(pX(0.54f), pY(0.54f))
            path.lineTo(pX(0.56f), pY(0.28f))

            // Legs with one forward foot pointed
            path.moveTo(pX(0.40f), pY(0.52f))
            path.lineTo(pX(0.42f), pY(0.86f)) // Straight weight leg
            path.moveTo(pX(0.52f), pY(0.54f))
            path.lineTo(pX(0.60f), pY(0.70f))
            path.lineTo(pX(0.62f), pY(0.86f)) // Pointed foot
        }

        PoseSilhouetteType.MIRROR_CROUCH_LOW -> {
            // Mirror frame
            path.moveTo(pX(0.20f), pY(0.12f))
            path.lineTo(pX(0.80f), pY(0.12f))
            path.lineTo(pX(0.80f), pY(0.92f))
            path.lineTo(pX(0.20f), pY(0.92f))
            path.close()

            // Head low in frame
            path.moveTo(pX(0.46f), pY(0.38f))
            path.cubicTo(pX(0.41f), pY(0.38f), pX(0.41f), pY(0.47f), pX(0.46f), pY(0.47f))
            path.cubicTo(pX(0.51f), pY(0.47f), pX(0.51f), pY(0.38f), pX(0.46f), pY(0.38f))

            // Smartphone angled down
            path.moveTo(pX(0.50f), pY(0.42f))
            path.lineTo(pX(0.58f), pY(0.44f))
            path.lineTo(pX(0.56f), pY(0.54f))
            path.lineTo(pX(0.48f), pY(0.52f))
            path.close()

            // Crouching body & deep knees
            path.moveTo(pX(0.42f), pY(0.48f))
            path.lineTo(pX(0.32f), pY(0.66f)) // Left squat knee
            path.lineTo(pX(0.42f), pY(0.84f))

            path.moveTo(pX(0.52f), pY(0.48f))
            path.lineTo(pX(0.66f), pY(0.66f)) // Right flared knee
            path.lineTo(pX(0.62f), pY(0.84f))
        }

        PoseSilhouetteType.BOOK_PULL_SHELF -> {
            // Bookshelf lines
            path.moveTo(pX(0.22f), pY(0.12f))
            path.lineTo(pX(0.22f), pY(0.88f))
            path.moveTo(pX(0.12f), pY(0.28f))
            path.lineTo(pX(0.28f), pY(0.28f))
            path.moveTo(pX(0.12f), pY(0.48f))
            path.lineTo(pX(0.28f), pY(0.48f))
            path.moveTo(pX(0.12f), pY(0.68f))
            path.lineTo(pX(0.28f), pY(0.68f))

            // Arm reaching up to book spine
            path.moveTo(pX(0.44f), pY(0.32f))
            path.lineTo(pX(0.30f), pY(0.28f))
            path.lineTo(pX(0.24f), pY(0.26f))

            // Head turned towards shelf
            path.moveTo(pX(0.48f), pY(0.22f))
            path.cubicTo(pX(0.43f), pY(0.22f), pX(0.43f), pY(0.31f), pX(0.48f), pY(0.31f))
            path.cubicTo(pX(0.53f), pY(0.31f), pX(0.53f), pY(0.22f), pX(0.48f), pY(0.22f))

            // Torso
            path.moveTo(pX(0.44f), pY(0.32f))
            path.lineTo(pX(0.42f), pY(0.58f))
            path.lineTo(pX(0.56f), pY(0.58f))
            path.lineTo(pX(0.54f), pY(0.32f))

            // Standing legs
            path.moveTo(pX(0.44f), pY(0.58f))
            path.lineTo(pX(0.42f), pY(0.86f))
            path.moveTo(pX(0.54f), pY(0.58f))
            path.lineTo(pX(0.56f), pY(0.86f))
        }

        PoseSilhouetteType.READING_IN_LIGHT -> {
            // Window rays / sill line
            path.moveTo(pX(0.68f), pY(0.18f))
            path.lineTo(pX(0.86f), pY(0.45f))
            path.moveTo(pX(0.62f), pY(0.68f))
            path.lineTo(pX(0.88f), pY(0.68f))

            // Head looking down at book
            path.moveTo(pX(0.46f), pY(0.24f))
            path.cubicTo(pX(0.41f), pY(0.24f), pX(0.41f), pY(0.33f), pX(0.46f), pY(0.34f))
            path.cubicTo(pX(0.51f), pY(0.33f), pX(0.51f), pY(0.24f), pX(0.46f), pY(0.24f))

            // Open book outline held in hands
            path.moveTo(pX(0.42f), pY(0.46f))
            path.lineTo(pX(0.48f), pY(0.49f)) // Spine
            path.lineTo(pX(0.54f), pY(0.46f))
            path.lineTo(pX(0.54f), pY(0.54f))
            path.lineTo(pX(0.48f), pY(0.56f))
            path.lineTo(pX(0.42f), pY(0.54f))
            path.close()

            // Hands supporting book
            path.moveTo(pX(0.42f), pY(0.36f))
            path.lineTo(pX(0.40f), pY(0.52f))
            path.moveTo(pX(0.52f), pY(0.36f))
            path.lineTo(pX(0.56f), pY(0.52f))

            // Seated posture
            path.moveTo(pX(0.44f), pY(0.56f))
            path.lineTo(pX(0.42f), pY(0.74f))
            path.lineTo(pX(0.58f), pY(0.74f))
        }

        PoseSilhouetteType.STUDIO_HANDS_POCKET -> {
            // Clean head
            path.moveTo(pX(0.50f), pY(0.18f))
            path.cubicTo(pX(0.45f), pY(0.18f), pX(0.45f), pY(0.28f), pX(0.50f), pY(0.28f))
            path.cubicTo(pX(0.55f), pY(0.28f), pX(0.55f), pY(0.18f), pX(0.50f), pY(0.18f))

            // Torso with dropped shoulder
            path.moveTo(pX(0.42f), pY(0.30f)) // Dropped left shoulder
            path.lineTo(pX(0.40f), pY(0.54f))
            path.lineTo(pX(0.58f), pY(0.54f))
            path.lineTo(pX(0.60f), pY(0.28f)) // Raised right shoulder

            // Thumbs hooked in pockets
            path.moveTo(pX(0.42f), pY(0.32f))
            path.lineTo(pX(0.36f), pY(0.46f))
            path.lineTo(pX(0.42f), pY(0.54f))

            path.moveTo(pX(0.58f), pY(0.30f))
            path.lineTo(pX(0.64f), pY(0.46f))
            path.lineTo(pX(0.58f), pY(0.54f))

            // Long fashion legs
            path.moveTo(pX(0.44f), pY(0.54f))
            path.lineTo(pX(0.42f), pY(0.88f))
            path.moveTo(pX(0.56f), pY(0.54f))
            path.lineTo(pX(0.58f), pY(0.88f))
        }

        PoseSilhouetteType.STUDIO_CROSS_ARMS -> {
            // Head
            path.moveTo(pX(0.50f), pY(0.18f))
            path.cubicTo(pX(0.45f), pY(0.18f), pX(0.45f), pY(0.28f), pX(0.50f), pY(0.28f))
            path.cubicTo(pX(0.55f), pY(0.28f), pX(0.55f), pY(0.18f), pX(0.50f), pY(0.18f))

            // Cross-arms block over chest
            path.moveTo(pX(0.40f), pY(0.36f))
            path.lineTo(pX(0.60f), pY(0.46f))
            path.lineTo(pX(0.58f), pY(0.50f))
            path.lineTo(pX(0.40f), pY(0.42f))

            path.moveTo(pX(0.60f), pY(0.36f))
            path.lineTo(pX(0.42f), pY(0.46f))

            // Torso
            path.moveTo(pX(0.42f), pY(0.29f))
            path.lineTo(pX(0.42f), pY(0.56f))
            path.lineTo(pX(0.58f), pY(0.56f))
            path.lineTo(pX(0.58f), pY(0.29f))

            // Confident stance
            path.moveTo(pX(0.44f), pY(0.56f))
            path.lineTo(pX(0.42f), pY(0.88f))
            path.moveTo(pX(0.56f), pY(0.56f))
            path.lineTo(pX(0.58f), pY(0.88f))
        }
    }

    return path
}
