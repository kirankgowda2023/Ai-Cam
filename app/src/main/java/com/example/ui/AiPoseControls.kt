package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.automirrored.filled.LabelOff
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.SceneAnalysisResult
import com.example.model.EnvironmentType
import com.example.model.PoseCatalog
import com.example.model.PoseRecommendation
import com.example.model.PoseSilhouetteType
import com.example.model.SceneCategory
import com.example.ui.theme.CameraPrimary
import com.example.ui.theme.CameraSecondary

@Composable
fun AiSceneInsightBanner(
    sceneAnalysis: SceneAnalysisResult?,
    isAnalyzing: Boolean,
    onReanalyzeClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("ai_scene_insight_banner"),
        color = Color.Black.copy(alpha = 0.78f),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            Brush.horizontalGradient(
                listOf(
                    CameraPrimary.copy(alpha = 0.8f),
                    CameraSecondary.copy(alpha = 0.8f)
                )
            )
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (isAnalyzing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = CameraPrimary
                )
                Text(
                    text = "AI analyzing background environment...",
                    color = Color.White,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
            } else {
                val icon = sceneAnalysis?.category?.emoji ?: "✨"
                Text(text = icon, fontSize = 18.sp)

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = sceneAnalysis?.sceneTitle ?: "AI Pose Guidance Active",
                            color = Color.White,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        sceneAnalysis?.environment?.let { env ->
                            if (env != EnvironmentType.ALL) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = if (env == EnvironmentType.INDOOR) Color(0xFF4A3418) else Color(0xFF1B3D2B),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = env.displayName,
                                        color = if (env == EnvironmentType.INDOOR) Color(0xFFFFCC80) else Color(0xFF81C784),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                    Text(
                        text = sceneAnalysis?.poseAdvice ?: "Tap refresh to detect background scenery",
                        color = Color.White.copy(alpha = 0.75f),
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(
                    onClick = onReanalyzeClicked,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.12f))
                        .testTag("reanalyze_scene_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Re-analyze Background",
                        tint = CameraPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AiPoseCarouselDrawer(
    selectedEnvironment: EnvironmentType,
    selectedCategory: SceneCategory?,
    selectedPose: PoseRecommendation?,
    isAnalyzing: Boolean,
    isMirrored: Boolean,
    showAnnotations: Boolean,
    isExpanded: Boolean,
    onEnvironmentSelected: (EnvironmentType) -> Unit,
    onCategorySelected: (SceneCategory?) -> Unit,
    onPoseSelected: (PoseRecommendation) -> Unit,
    onReanalyzeScene: () -> Unit,
    onToggleMirror: () -> Unit,
    onToggleAnnotations: () -> Unit,
    onToggleExpanded: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Filter poses based on environment and category
    val displayedPoses = PoseCatalog.ALL_POSES.filter { pose ->
        val matchesEnv = when (selectedEnvironment) {
            EnvironmentType.ALL -> true
            EnvironmentType.INDOOR -> pose.environment == EnvironmentType.INDOOR
            EnvironmentType.OUTDOOR -> pose.environment == EnvironmentType.OUTDOOR
        }
        val matchesCat = selectedCategory == null || pose.category == selectedCategory
        matchesEnv && matchesCat
    }

    // Categories filtered by the selected environment
    val availableCategories = when (selectedEnvironment) {
        EnvironmentType.ALL -> SceneCategory.values().toList()
        EnvironmentType.INDOOR -> SceneCategory.values().filter { it.environment == EnvironmentType.INDOOR }
        EnvironmentType.OUTDOOR -> SceneCategory.values().filter { it.environment == EnvironmentType.OUTDOOR }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .testTag("ai_pose_carousel_drawer"),
        color = Color(0xEE121418),
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            Color.White.copy(alpha = 0.15f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp)
        ) {
            // Header Row: Title & Action Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.clickable { onToggleExpanded() }
                ) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .background(
                                Brush.linearGradient(listOf(CameraPrimary, CameraSecondary)),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Text(
                        text = "AI Pose Guide",
                        color = Color.White,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    // Count badge
                    Surface(
                        color = Color.White.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "${displayedPoses.size} poses",
                            color = CameraPrimary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                        contentDescription = "Expand/Collapse",
                        tint = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Quick Tools Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Scan Background Scene Button
                    IconButton(
                        onClick = onReanalyzeScene,
                        enabled = !isAnalyzing,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(
                                if (isAnalyzing) CameraPrimary.copy(alpha = 0.3f)
                                else Color.White.copy(alpha = 0.12f)
                            )
                            .testTag("scan_scene_button")
                    ) {
                        if (isAnalyzing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = CameraPrimary
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Scan Scene Background",
                                tint = CameraPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Mirror / Flip Pose
                    IconButton(
                        onClick = onToggleMirror,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(if (isMirrored) CameraPrimary.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.12f))
                            .testTag("mirror_pose_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Flip,
                            contentDescription = "Mirror Pose",
                            tint = if (isMirrored) CameraPrimary else Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Toggle Annotations
                    IconButton(
                        onClick = onToggleAnnotations,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(if (showAnnotations) CameraPrimary.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.12f))
                            .testTag("toggle_annotations_button")
                    ) {
                        Icon(
                            imageVector = if (showAnnotations) Icons.AutoMirrored.Filled.Label else Icons.AutoMirrored.Filled.LabelOff,
                            contentDescription = "Toggle Annotation Tags",
                            tint = if (showAnnotations) CameraPrimary else Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Spacer(modifier = Modifier.height(8.dp))

                    // Tier 1: Indoor / Outdoor / All Environment Filter Tabs
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        EnvironmentFilterTab(
                            title = "All",
                            count = PoseCatalog.ALL_POSES.size,
                            isSelected = selectedEnvironment == EnvironmentType.ALL,
                            onClick = { onEnvironmentSelected(EnvironmentType.ALL) },
                            modifier = Modifier.weight(1f)
                        )
                        EnvironmentFilterTab(
                            title = "🏡 Indoor",
                            count = PoseCatalog.INDOOR_POSES.size,
                            isSelected = selectedEnvironment == EnvironmentType.INDOOR,
                            onClick = { onEnvironmentSelected(EnvironmentType.INDOOR) },
                            modifier = Modifier.weight(1.2f)
                        )
                        EnvironmentFilterTab(
                            title = "🌳 Outdoor",
                            count = PoseCatalog.OUTDOOR_POSES.size,
                            isSelected = selectedEnvironment == EnvironmentType.OUTDOOR,
                            onClick = { onEnvironmentSelected(EnvironmentType.OUTDOOR) },
                            modifier = Modifier.weight(1.2f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Tier 2: Scene Category Filter Chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 14.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // "All Categories" chip
                        FilterChip(
                            selected = selectedCategory == null,
                            onClick = { onCategorySelected(null) },
                            label = { Text("All", fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CameraPrimary.copy(alpha = 0.25f),
                                selectedLabelColor = CameraPrimary,
                                containerColor = Color.White.copy(alpha = 0.08f),
                                labelColor = Color.White.copy(alpha = 0.8f)
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selectedCategory == null,
                                borderColor = Color.White.copy(alpha = 0.2f),
                                selectedBorderColor = CameraPrimary
                            )
                        )

                        availableCategories.forEach { cat ->
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = { onCategorySelected(cat) },
                                label = { Text("${cat.emoji} ${cat.displayName}", fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CameraPrimary.copy(alpha = 0.25f),
                                    selectedLabelColor = CameraPrimary,
                                    containerColor = Color.White.copy(alpha = 0.08f),
                                    labelColor = Color.White.copy(alpha = 0.8f)
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = selectedCategory == cat,
                                    borderColor = Color.White.copy(alpha = 0.2f),
                                    selectedBorderColor = CameraPrimary
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Poses Carousel Cards
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 14.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(displayedPoses, key = { it.id }) { pose ->
                            val isSelected = selectedPose?.id == pose.id
                            PoseThumbnailCard(
                                pose = pose,
                                isSelected = isSelected,
                                onClick = { onPoseSelected(pose) }
                            )
                        }
                    }

                    // Active Pose Tip Snippet
                    selectedPose?.let { pose ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp)
                                .background(Color.White.copy(alpha = 0.06f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = CameraPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${pose.category.emoji} ${pose.title}",
                                        color = CameraPrimary,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "• ${pose.environment.displayName}",
                                        color = Color.White.copy(alpha = 0.6f),
                                        fontSize = 10.sp
                                    )
                                }
                                Text(
                                    text = pose.tips,
                                    color = Color.White.copy(alpha = 0.9f),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 11.sp,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EnvironmentFilterTab(
    title: String,
    count: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .height(34.dp)
            .testTag("env_tab_$title"),
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) CameraPrimary.copy(alpha = 0.22f) else Color.White.copy(alpha = 0.06f),
        border = androidx.compose.foundation.BorderStroke(
            if (isSelected) 1.5.dp else 1.dp,
            if (isSelected) CameraPrimary else Color.White.copy(alpha = 0.12f)
        )
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                color = if (isSelected) CameraPrimary else Color.White.copy(alpha = 0.85f),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 11.sp
            )
            Spacer(modifier = Modifier.width(4.dp))
            Surface(
                color = if (isSelected) CameraPrimary else Color.White.copy(alpha = 0.12f),
                shape = CircleShape
            ) {
                Text(
                    text = "$count",
                    color = if (isSelected) Color.Black else Color.White.copy(alpha = 0.8f),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                )
            }
        }
    }
}

@Composable
fun PoseThumbnailCard(
    pose: PoseRecommendation,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .width(110.dp)
            .height(126.dp)
            .testTag("pose_card_${pose.id}"),
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) Color(0xFF1E2530) else Color(0xFF16181D),
        border = androidx.compose.foundation.BorderStroke(
            if (isSelected) 2.dp else 1.dp,
            if (isSelected) CameraPrimary else Color.White.copy(alpha = 0.12f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Miniature silhouette preview box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.Black.copy(alpha = 0.65f)),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize().padding(4.dp)) {
                    val strokeColor = if (isSelected) CameraPrimary else Color.White.copy(alpha = 0.9f)
                    drawPosePreviewMini(pose.silhouetteType, size.width, size.height, strokeColor)
                }

                // Environment badge in thumbnail
                Surface(
                    color = if (pose.environment == EnvironmentType.INDOOR) Color(0xCC3E2723) else Color(0xCC1B5E20),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(3.dp)
                ) {
                    Text(
                        text = if (pose.environment == EnvironmentType.INDOOR) "🏡 In" else "🌳 Out",
                        color = Color.White,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
                    )
                }

                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .size(16.dp)
                            .background(CameraPrimary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = Color.Black,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }

            // Pose Title & Category
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = pose.title,
                    color = if (isSelected) CameraPrimary else Color.White,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${pose.category.emoji} ${pose.category.displayName}",
                    color = Color.White.copy(alpha = 0.55f),
                    fontSize = 9.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

private fun DrawScope.drawPosePreviewMini(
    type: PoseSilhouetteType,
    w: Float,
    h: Float,
    color: Color
) {
    val path = androidx.compose.ui.graphics.Path()

    when (type) {
        PoseSilhouetteType.GRADUATION_KICK_DIPLOMA -> {
            // Mortarboard Cap
            path.moveTo(w * 0.4f, h * 0.22f)
            path.lineTo(w * 0.5f, h * 0.16f)
            path.lineTo(w * 0.6f, h * 0.22f)
            path.lineTo(w * 0.5f, h * 0.28f)
            path.close()
            // Diploma arm
            path.moveTo(w * 0.45f, h * 0.38f)
            path.lineTo(w * 0.22f, h * 0.30f)
            path.lineTo(w * 0.20f, h * 0.18f)
            // Gown torso
            path.moveTo(w * 0.5f, h * 0.30f)
            path.lineTo(w * 0.5f, h * 0.62f)
            // Kicking leg
            path.moveTo(w * 0.5f, h * 0.62f)
            path.lineTo(w * 0.82f, h * 0.76f)
            // Standing leg
            path.moveTo(w * 0.5f, h * 0.62f)
            path.lineTo(w * 0.4f, h * 0.90f)
        }

        PoseSilhouetteType.BENCH_SITTING_CROSSED -> {
            // Bench backrest
            path.moveTo(w * 0.15f, h * 0.52f)
            path.lineTo(w * 0.85f, h * 0.52f)
            // Head
            drawCircle(color = color, radius = 5.dp.toPx(), center = Offset(w * 0.5f, h * 0.30f), style = Stroke(1.5.dp.toPx()))
            // Arm on backrest
            path.moveTo(w * 0.45f, h * 0.42f)
            path.lineTo(w * 0.26f, h * 0.48f)
            // Torso
            path.moveTo(w * 0.5f, h * 0.38f)
            path.lineTo(w * 0.5f, h * 0.64f)
            // Crossed legs
            path.moveTo(w * 0.5f, h * 0.64f)
            path.lineTo(w * 0.64f, h * 0.74f)
            path.lineTo(w * 0.60f, h * 0.90f)
        }

        PoseSilhouetteType.GRADUATION_CAP_TOSS -> {
            // Cap flying high
            path.moveTo(w * 0.46f, h * 0.10f)
            path.lineTo(w * 0.54f, h * 0.08f)
            path.lineTo(w * 0.58f, h * 0.12f)
            path.lineTo(w * 0.50f, h * 0.14f)
            path.close()
            // Head looking up
            drawCircle(color = color, radius = 4.dp.toPx(), center = Offset(w * 0.5f, h * 0.25f), style = Stroke(1.5.dp.toPx()))
            // Both arms reaching up
            path.moveTo(w * 0.44f, h * 0.32f)
            path.lineTo(w * 0.36f, h * 0.18f)
            path.moveTo(w * 0.56f, h * 0.32f)
            path.lineTo(w * 0.64f, h * 0.18f)
            // Body & gown
            path.moveTo(w * 0.5f, h * 0.32f)
            path.lineTo(w * 0.5f, h * 0.66f)
            path.moveTo(w * 0.44f, h * 0.66f)
            path.lineTo(w * 0.42f, h * 0.90f)
            path.moveTo(w * 0.56f, h * 0.66f)
            path.lineTo(w * 0.58f, h * 0.90f)
        }

        PoseSilhouetteType.CAMPUS_STAIRS_SEATED -> {
            // Steps lines
            path.moveTo(w * 0.15f, h * 0.60f)
            path.lineTo(w * 0.85f, h * 0.60f)
            path.moveTo(w * 0.15f, h * 0.76f)
            path.lineTo(w * 0.85f, h * 0.76f)
            // Head
            drawCircle(color = color, radius = 4.5.dp.toPx(), center = Offset(w * 0.48f, h * 0.26f), style = Stroke(1.5.dp.toPx()))
            // Body
            path.moveTo(w * 0.48f, h * 0.34f)
            path.lineTo(w * 0.48f, h * 0.60f)
            // Bent knees on step
            path.moveTo(w * 0.48f, h * 0.60f)
            path.lineTo(w * 0.38f, h * 0.74f)
            path.moveTo(w * 0.48f, h * 0.60f)
            path.lineTo(w * 0.60f, h * 0.74f)
        }

        PoseSilhouetteType.URBAN_STRIDE_WALK -> {
            // Head
            drawCircle(color = color, radius = 4.dp.toPx(), center = Offset(w * 0.5f, h * 0.22f), style = Stroke(1.5.dp.toPx()))
            // Body
            path.moveTo(w * 0.5f, h * 0.28f)
            path.lineTo(w * 0.5f, h * 0.56f)
            // Striding legs
            path.moveTo(w * 0.5f, h * 0.56f)
            path.lineTo(w * 0.34f, h * 0.88f)
            path.moveTo(w * 0.5f, h * 0.56f)
            path.lineTo(w * 0.66f, h * 0.86f)
            // Swinging arms
            path.moveTo(w * 0.46f, h * 0.36f)
            path.lineTo(w * 0.32f, h * 0.48f)
            path.moveTo(w * 0.54f, h * 0.36f)
            path.lineTo(w * 0.68f, h * 0.46f)
        }

        PoseSilhouetteType.URBAN_WALL_LEAN -> {
            // Vertical wall
            path.moveTo(w * 0.24f, h * 0.15f)
            path.lineTo(w * 0.24f, h * 0.90f)
            // Head
            drawCircle(color = color, radius = 4.dp.toPx(), center = Offset(w * 0.44f, h * 0.26f), style = Stroke(1.5.dp.toPx()))
            // Torso leaning back
            path.moveTo(w * 0.42f, h * 0.34f)
            path.lineTo(w * 0.36f, h * 0.58f)
            // Legs
            path.moveTo(w * 0.36f, h * 0.58f)
            path.lineTo(w * 0.40f, h * 0.88f)
            path.moveTo(w * 0.36f, h * 0.58f)
            path.lineTo(w * 0.54f, h * 0.72f)
            path.lineTo(w * 0.46f, h * 0.88f)
        }

        PoseSilhouetteType.SCENIC_ARMS_WIDE -> {
            // Head
            drawCircle(color = color, radius = 4.dp.toPx(), center = Offset(w * 0.5f, h * 0.22f), style = Stroke(1.5.dp.toPx()))
            // Arms wide open
            path.moveTo(w * 0.5f, h * 0.32f)
            path.lineTo(w * 0.16f, h * 0.24f)
            path.moveTo(w * 0.5f, h * 0.32f)
            path.lineTo(w * 0.84f, h * 0.24f)
            // Torso & legs
            path.moveTo(w * 0.5f, h * 0.28f)
            path.lineTo(w * 0.5f, h * 0.60f)
            path.moveTo(w * 0.5f, h * 0.60f)
            path.lineTo(w * 0.40f, h * 0.90f)
            path.moveTo(w * 0.5f, h * 0.60f)
            path.lineTo(w * 0.60f, h * 0.90f)
        }

        PoseSilhouetteType.BEACH_WALK_BAREFOOT -> {
            // Water ripples
            path.moveTo(w * 0.18f, h * 0.88f)
            path.lineTo(w * 0.82f, h * 0.88f)
            // Head
            drawCircle(color = color, radius = 4.dp.toPx(), center = Offset(w * 0.5f, h * 0.22f), style = Stroke(1.5.dp.toPx()))
            // Body
            path.moveTo(w * 0.5f, h * 0.28f)
            path.lineTo(w * 0.5f, h * 0.62f)
            // One arm carrying sandals
            path.moveTo(w * 0.44f, h * 0.34f)
            path.lineTo(w * 0.30f, h * 0.52f)
            // Legs
            path.moveTo(w * 0.5f, h * 0.62f)
            path.lineTo(w * 0.42f, h * 0.86f)
            path.moveTo(w * 0.5f, h * 0.62f)
            path.lineTo(w * 0.60f, h * 0.84f)
        }

        PoseSilhouetteType.CAFE_TABLE_PROP -> {
            // Table
            path.moveTo(w * 0.15f, h * 0.58f)
            path.lineTo(w * 0.85f, h * 0.58f)
            // Head resting on hands
            drawCircle(color = color, radius = 5.dp.toPx(), center = Offset(w * 0.5f, h * 0.30f), style = Stroke(1.5.dp.toPx()))
            // Forearms on table
            path.moveTo(w * 0.44f, h * 0.40f)
            path.lineTo(w * 0.36f, h * 0.58f)
            path.moveTo(w * 0.56f, h * 0.40f)
            path.lineTo(w * 0.64f, h * 0.58f)
            // Cup
            path.moveTo(w * 0.46f, h * 0.52f)
            path.lineTo(w * 0.54f, h * 0.52f)
            path.lineTo(w * 0.53f, h * 0.58f)
            path.lineTo(w * 0.47f, h * 0.58f)
            path.close()
        }

        PoseSilhouetteType.CAFE_WINDOW_SIP -> {
            // Window vertical line
            path.moveTo(w * 0.74f, h * 0.18f)
            path.lineTo(w * 0.74f, h * 0.80f)
            // Table
            path.moveTo(w * 0.20f, h * 0.62f)
            path.lineTo(w * 0.70f, h * 0.62f)
            // Head
            drawCircle(color = color, radius = 4.5.dp.toPx(), center = Offset(w * 0.48f, h * 0.26f), style = Stroke(1.5.dp.toPx()))
            // Hand lifting cup
            path.moveTo(w * 0.46f, h * 0.38f)
            path.lineTo(w * 0.54f, h * 0.32f)
            // Torso
            path.moveTo(w * 0.46f, h * 0.34f)
            path.lineTo(w * 0.46f, h * 0.62f)
        }

        PoseSilhouetteType.SOFA_LEAN_CUSHION -> {
            // Sofa lines
            path.moveTo(w * 0.15f, h * 0.50f)
            path.lineTo(w * 0.85f, h * 0.50f)
            path.moveTo(w * 0.15f, h * 0.74f)
            path.lineTo(w * 0.85f, h * 0.74f)
            // Head near cushion
            drawCircle(color = color, radius = 4.5.dp.toPx(), center = Offset(w * 0.40f, h * 0.32f), style = Stroke(1.5.dp.toPx()))
            // Curled tucked body
            path.moveTo(w * 0.40f, h * 0.40f)
            path.lineTo(w * 0.62f, h * 0.62f)
            path.lineTo(w * 0.48f, h * 0.72f)
        }

        PoseSilhouetteType.SOFA_CHILL_STRETCH -> {
            // Sofa horizontal
            path.moveTo(w * 0.12f, h * 0.66f)
            path.lineTo(w * 0.88f, h * 0.66f)
            // Head reclined
            drawCircle(color = color, radius = 4.5.dp.toPx(), center = Offset(w * 0.30f, h * 0.38f), style = Stroke(1.5.dp.toPx()))
            // Stretched body across couch
            path.moveTo(w * 0.34f, h * 0.44f)
            path.lineTo(w * 0.52f, h * 0.58f)
            path.lineTo(w * 0.80f, h * 0.64f)
        }

        PoseSilhouetteType.RUG_CROSS_LEGGED -> {
            // Rug oval hint
            path.moveTo(w * 0.22f, h * 0.82f)
            path.lineTo(w * 0.78f, h * 0.82f)
            // Head
            drawCircle(color = color, radius = 4.5.dp.toPx(), center = Offset(w * 0.5f, h * 0.28f), style = Stroke(1.5.dp.toPx()))
            // Torso
            path.moveTo(w * 0.5f, h * 0.36f)
            path.lineTo(w * 0.5f, h * 0.62f)
            // Crossed legs
            path.moveTo(w * 0.36f, h * 0.74f)
            path.lineTo(w * 0.64f, h * 0.74f)
        }

        PoseSilhouetteType.MIRROR_PHONE_TILT -> {
            // Mirror frame
            path.moveTo(w * 0.20f, h * 0.10f)
            path.lineTo(w * 0.80f, h * 0.10f)
            path.lineTo(w * 0.80f, h * 0.90f)
            path.lineTo(w * 0.20f, h * 0.90f)
            path.close()
            // Head
            drawCircle(color = color, radius = 4.dp.toPx(), center = Offset(w * 0.5f, h * 0.22f), style = Stroke(1.5.dp.toPx()))
            // Phone at chest
            path.moveTo(w * 0.46f, h * 0.34f)
            path.lineTo(w * 0.54f, h * 0.34f)
            path.lineTo(w * 0.54f, h * 0.44f)
            path.lineTo(w * 0.46f, h * 0.44f)
            path.close()
            // Popped hip legs
            path.moveTo(w * 0.5f, h * 0.44f)
            path.lineTo(w * 0.42f, h * 0.84f)
            path.moveTo(w * 0.5f, h * 0.44f)
            path.lineTo(w * 0.62f, h * 0.84f)
        }

        PoseSilhouetteType.BOOK_PULL_SHELF -> {
            // Shelf vertical
            path.moveTo(w * 0.24f, h * 0.12f)
            path.lineTo(w * 0.24f, h * 0.88f)
            path.moveTo(w * 0.14f, h * 0.32f)
            path.lineTo(w * 0.28f, h * 0.32f)
            // Head
            drawCircle(color = color, radius = 4.dp.toPx(), center = Offset(w * 0.50f, h * 0.26f), style = Stroke(1.5.dp.toPx()))
            // Arm reaching to book
            path.moveTo(w * 0.46f, h * 0.34f)
            path.lineTo(w * 0.26f, h * 0.30f)
            // Body
            path.moveTo(w * 0.50f, h * 0.34f)
            path.lineTo(w * 0.50f, h * 0.86f)
        }

        PoseSilhouetteType.STUDIO_HANDS_POCKET -> {
            // Head
            drawCircle(color = color, radius = 4.5.dp.toPx(), center = Offset(w * 0.50f, h * 0.22f), style = Stroke(1.5.dp.toPx()))
            // Torso
            path.moveTo(w * 0.50f, h * 0.30f)
            path.lineTo(w * 0.50f, h * 0.56f)
            // Arms tucked in pockets
            path.moveTo(w * 0.42f, h * 0.34f)
            path.lineTo(w * 0.38f, h * 0.54f)
            path.moveTo(w * 0.58f, h * 0.34f)
            path.lineTo(w * 0.62f, h * 0.54f)
            // Legs
            path.moveTo(w * 0.46f, h * 0.56f)
            path.lineTo(w * 0.42f, h * 0.88f)
            path.moveTo(w * 0.54f, h * 0.56f)
            path.lineTo(w * 0.58f, h * 0.88f)
        }

        else -> {
            // Generic stick figure silhouette
            drawCircle(color = color, radius = 4.dp.toPx(), center = Offset(w * 0.5f, h * 0.26f), style = Stroke(1.5.dp.toPx()))
            path.moveTo(w * 0.5f, h * 0.32f)
            path.lineTo(w * 0.5f, h * 0.64f)
            // Arms
            path.moveTo(w * 0.3f, h * 0.42f)
            path.lineTo(w * 0.7f, h * 0.42f)
            // Legs
            path.moveTo(w * 0.5f, h * 0.64f)
            path.lineTo(w * 0.36f, h * 0.90f)
            path.moveTo(w * 0.5f, h * 0.64f)
            path.lineTo(w * 0.64f, h * 0.90f)
        }
    }

    drawPath(
        path = path,
        color = color,
        style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round)
    )
}
