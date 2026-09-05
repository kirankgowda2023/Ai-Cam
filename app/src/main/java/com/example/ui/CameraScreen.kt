package com.example.ui

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageCapture
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.FlashAuto
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.GridOff
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.model.PoseRecommendation
import com.example.model.SceneCategory
import com.example.ui.theme.CameraDarkBackground
import com.example.ui.theme.CameraDarkSurface
import com.example.ui.theme.CameraOnSurface
import com.example.ui.theme.CameraOnSurfaceSubtle
import com.example.ui.theme.CameraPrimary
import com.example.ui.theme.CameraSecondary

@Composable
fun CameraScreen(
    viewModel: CameraViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val hasPermission by viewModel.hasCameraPermission.collectAsStateWithLifecycle()
    val isFrontCamera by viewModel.isFrontCamera.collectAsStateWithLifecycle()
    val flashMode by viewModel.flashMode.collectAsStateWithLifecycle()
    val isGridVisible by viewModel.isGridVisible.collectAsStateWithLifecycle()
    val zoomRatio by viewModel.zoomRatio.collectAsStateWithLifecycle()
    val isCapturing by viewModel.isCapturing.collectAsStateWithLifecycle()
    val shutterTrigger by viewModel.shutterFlashTrigger.collectAsStateWithLifecycle()
    val capturedPhotos by viewModel.capturedPhotos.collectAsStateWithLifecycle()
    val selectedPhoto by viewModel.selectedPhoto.collectAsStateWithLifecycle()
    val statusMessage by viewModel.statusMessage.collectAsStateWithLifecycle()

    // AI Pose States
    val isAiPoseMode by viewModel.isAiPoseMode.collectAsStateWithLifecycle()
    val isAnalyzingScene by viewModel.isAnalyzingScene.collectAsStateWithLifecycle()
    val sceneAnalysis by viewModel.sceneAnalysis.collectAsStateWithLifecycle()
    val selectedEnvironment by viewModel.selectedEnvironment.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val selectedPose by viewModel.selectedPose.collectAsStateWithLifecycle()
    val isPoseMirrored by viewModel.isPoseMirrored.collectAsStateWithLifecycle()
    val poseOpacity by viewModel.poseOpacity.collectAsStateWithLifecycle()
    val showPoseAnnotations by viewModel.showPoseAnnotations.collectAsStateWithLifecycle()
    val isPoseDrawerExpanded by viewModel.isPoseDrawerExpanded.collectAsStateWithLifecycle()

    var activeImageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var tapFocusOffset by remember { mutableStateOf<Offset?>(null) }
    var bitmapProvider by remember { mutableStateOf<(() -> Bitmap?)?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.setPermissionGranted(isGranted)
    }

    LaunchedEffect(Unit) {
        val currentPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        viewModel.setPermissionGranted(currentPermission)
        if (!currentPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    LaunchedEffect(statusMessage) {
        statusMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearStatusMessage()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CameraDarkBackground)
    ) {
        if (!hasPermission) {
            CameraPermissionView(
                onRequestPermission = {
                    permissionLauncher.launch(Manifest.permission.CAMERA)
                }
            )
        } else {
            // Live Camera Viewfinder
            CameraPreview(
                isFrontCamera = isFrontCamera,
                flashMode = flashMode,
                zoomRatio = zoomRatio,
                onTapToFocus = { offset ->
                    tapFocusOffset = offset
                },
                bindImageCapture = { capture ->
                    activeImageCapture = capture
                },
                onProvideBitmapProvider = { provider ->
                    bitmapProvider = provider
                },
                modifier = Modifier.fillMaxSize()
            )

            // Grid Overlay
            if (isGridVisible) {
                GridOverlay()
            }

            // AI Pose Silhouette Overlay on Camera Viewfinder
            if (isAiPoseMode && selectedPose != null) {
                PoseSilhouetteOverlay(
                    pose = selectedPose!!,
                    isMirrored = isPoseMirrored,
                    alpha = poseOpacity,
                    showAnnotations = showPoseAnnotations,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Tap Focus Indicator
            FocusIndicator(focusOffset = tapFocusOffset)

            // Shutter Flash Animation
            ShutterFlashOverlay(triggerTime = shutterTrigger)

            // Top HUD Overlay
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
            ) {
                TopControlBar(
                    flashMode = flashMode,
                    isGridVisible = isGridVisible,
                    isAiPoseMode = isAiPoseMode,
                    isAnalyzingScene = isAnalyzingScene,
                    photosCount = capturedPhotos.size,
                    onCycleFlash = { viewModel.cycleFlashMode() },
                    onToggleGrid = { viewModel.toggleGrid() },
                    onToggleAiPose = {
                        viewModel.toggleAiPoseMode(bitmapProvider)
                    }
                )

                // Scene Insight Pill when AI mode is active
                AnimatedVisibility(
                    visible = isAiPoseMode,
                    enter = fadeIn() + slideInVertically(),
                    exit = fadeOut() + slideOutVertically()
                ) {
                    AiSceneInsightBanner(
                        sceneAnalysis = sceneAnalysis,
                        isAnalyzing = isAnalyzingScene,
                        onReanalyzeClicked = {
                            viewModel.analyzeScene(bitmapProvider?.invoke())
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Bottom Controls Area (AI Pose Carousel + Zoom + Shutter HUD)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
            ) {
                // AI Pose Carousel Drawer (above shutter)
                AnimatedVisibility(
                    visible = isAiPoseMode,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
                    exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 })
                ) {
                    AiPoseCarouselDrawer(
                        selectedEnvironment = selectedEnvironment,
                        selectedCategory = selectedCategory,
                        selectedPose = selectedPose,
                        isAnalyzing = isAnalyzingScene,
                        isMirrored = isPoseMirrored,
                        showAnnotations = showPoseAnnotations,
                        isExpanded = isPoseDrawerExpanded,
                        onEnvironmentSelected = { env -> viewModel.selectEnvironment(env) },
                        onCategorySelected = { cat -> viewModel.selectCategory(cat) },
                        onPoseSelected = { pose -> viewModel.selectPose(pose) },
                        onReanalyzeScene = { viewModel.analyzeScene(bitmapProvider?.invoke()) },
                        onToggleMirror = { viewModel.toggleMirrorPose() },
                        onToggleAnnotations = { viewModel.togglePoseAnnotations() },
                        onToggleExpanded = { viewModel.togglePoseDrawer() },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Zoom Selector Chips
                ZoomSelector(
                    currentZoom = zoomRatio,
                    onZoomSelected = { zoom -> viewModel.setZoomRatio(zoom) },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Bottom Shutter Bar
                BottomShutterBar(
                    latestPhotoUri = capturedPhotos.firstOrNull()?.file,
                    isCapturing = isCapturing,
                    isFrontCamera = isFrontCamera,
                    onCaptureClicked = {
                        val outputFile = viewModel.getOutputMediaFile()
                        capturePhoto(
                            context = context,
                            imageCapture = activeImageCapture,
                            outputFile = outputFile,
                            onStart = { viewModel.onCaptureStart() },
                            onSuccess = { file -> viewModel.onPhotoCaptured(file) },
                            onError = { error -> viewModel.onCaptureError(error) }
                        )
                    },
                    onThumbnailClicked = {
                        capturedPhotos.firstOrNull()?.let { photo ->
                            viewModel.selectPhoto(photo)
                        }
                    },
                    onFlipCameraClicked = {
                        viewModel.toggleCameraLens()
                    }
                )
            }
        }

        // Snackbar Host for status notifications
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 120.dp)
        )

        // Photo Review Sheet/Dialog
        selectedPhoto?.let { photo ->
            PhotoReviewDialog(
                photo = photo,
                onDismiss = { viewModel.selectPhoto(null) },
                onDelete = { toDelete ->
                    viewModel.deletePhoto(toDelete)
                }
            )
        }
    }
}

@Composable
fun TopControlBar(
    flashMode: Int,
    isGridVisible: Boolean,
    isAiPoseMode: Boolean,
    isAnalyzingScene: Boolean,
    photosCount: Int,
    onCycleFlash: () -> Unit,
    onToggleGrid: () -> Unit,
    onToggleAiPose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.Black.copy(alpha = 0.75f),
                        Color.Transparent
                    )
                )
            )
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Flash Mode Toggle Button
            IconButton(
                onClick = onCycleFlash,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.45f))
                    .testTag("flash_toggle_button")
            ) {
                val (flashIcon, flashColor) = when (flashMode) {
                    ImageCapture.FLASH_MODE_ON -> Pair(Icons.Default.FlashOn, CameraSecondary)
                    ImageCapture.FLASH_MODE_AUTO -> Pair(Icons.Default.FlashAuto, CameraPrimary)
                    else -> Pair(Icons.Default.FlashOff, Color.White.copy(alpha = 0.85f))
                }
                Icon(
                    imageVector = flashIcon,
                    contentDescription = "Cycle Flash Mode",
                    tint = flashColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            // Top Center: AI Option Toggle (As requested: "intergrate AI option in top and when it is ON it should recommand pose")
            Surface(
                onClick = onToggleAiPose,
                shape = RoundedCornerShape(22.dp),
                color = if (isAiPoseMode) Color(0xFF1E2530) else Color.Black.copy(alpha = 0.45f),
                border = androidx.compose.foundation.BorderStroke(
                    width = if (isAiPoseMode) 1.5.dp else 1.dp,
                    brush = if (isAiPoseMode) {
                        Brush.horizontalGradient(listOf(CameraPrimary, CameraSecondary))
                    } else {
                        Brush.horizontalGradient(listOf(Color.White.copy(0.25f), Color.White.copy(0.15f)))
                    }
                ),
                modifier = Modifier.testTag("ai_pose_mode_toggle")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (isAnalyzingScene) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = CameraPrimary
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI Pose Guide",
                            tint = if (isAiPoseMode) CameraPrimary else Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Text(
                        text = if (isAiPoseMode) "AI POSE ON" else "AI POSE",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isAiPoseMode) CameraPrimary else Color.White
                    )

                    if (isAiPoseMode) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(CameraSecondary, CircleShape)
                        )
                    }
                }
            }

            // Right Action: Grid Toggle Button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = onToggleGrid,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.45f))
                        .testTag("grid_toggle_button")
                ) {
                    Icon(
                        imageVector = if (isGridVisible) Icons.Default.GridOn else Icons.Default.GridOff,
                        contentDescription = "Toggle Grid",
                        tint = if (isGridVisible) CameraSecondary else Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ZoomSelector(
    currentZoom: Float,
    onZoomSelected: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val zoomLevels = listOf(1f, 2f, 3f)

    Surface(
        color = Color.Black.copy(alpha = 0.55f),
        shape = RoundedCornerShape(20.dp),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            zoomLevels.forEach { zoom ->
                val isSelected = (currentZoom - zoom).let { kotlin.math.abs(it) < 0.2f }
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (isSelected) CameraSecondary else Color.Transparent)
                        .clickable { onZoomSelected(zoom) }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${zoom.toInt()}x",
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.Black else CameraOnSurface
                    )
                }
            }
        }
    }
}

@Composable
fun BottomShutterBar(
    latestPhotoUri: java.io.File?,
    isCapturing: Boolean,
    isFrontCamera: Boolean,
    onCaptureClicked: () -> Unit,
    onThumbnailClicked: () -> Unit,
    onFlipCameraClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val shutterScale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1f,
        label = "shutterScale"
    )

    var flipRotation by remember { mutableFloatStateOf(0f) }
    val animatedFlipRotation by animateFloatAsState(
        targetValue = flipRotation,
        animationSpec = tween(350),
        label = "flipRotation"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.Black.copy(alpha = 0.85f)
                    )
                )
            )
            .navigationBarsPadding()
            .padding(horizontal = 28.dp, vertical = 24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Recent photo thumbnail
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.15f))
                    .border(2.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                    .clickable(enabled = latestPhotoUri != null, onClick = onThumbnailClicked)
                    .testTag("gallery_thumbnail_button"),
                contentAlignment = Alignment.Center
            ) {
                if (latestPhotoUri != null && latestPhotoUri.exists()) {
                    AsyncImage(
                        model = latestPhotoUri,
                        contentDescription = "Last captured photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.PhotoLibrary,
                        contentDescription = "No photos yet",
                        tint = CameraOnSurfaceSubtle,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Capture Shutter Button
            Box(
                modifier = Modifier
                    .size(84.dp)
                    .scale(shutterScale)
                    .clip(CircleShape)
                    .border(4.dp, Color.White, CircleShape)
                    .padding(6.dp)
                    .clip(CircleShape)
                    .background(if (isCapturing) CameraSecondary else Color.White)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        enabled = !isCapturing,
                        onClick = onCaptureClicked
                    )
                    .testTag("shutter_button"),
                contentAlignment = Alignment.Center
            ) {
                if (isCapturing) {
                    CircularProgressIndicator(
                        color = Color.Black,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            // Flip Camera Button
            IconButton(
                onClick = {
                    flipRotation += 180f
                    onFlipCameraClicked()
                },
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.15f))
                    .testTag("flip_camera_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Cameraswitch,
                    contentDescription = "Flip camera lens",
                    tint = CameraOnSurface,
                    modifier = Modifier
                        .size(28.dp)
                        .rotate(animatedFlipRotation)
                )
            }
        }
    }
}

@Composable
fun CameraPermissionView(
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(CameraDarkSurface)
                .border(1.dp, CameraPrimary.copy(alpha = 0.4f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.PhotoCamera,
                contentDescription = null,
                tint = CameraPrimary,
                modifier = Modifier.size(48.dp)
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "Camera Access Required",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = CameraOnSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "To take pictures with your mobile device, please allow camera permissions.",
            style = MaterialTheme.typography.bodyMedium,
            color = CameraOnSurfaceSubtle,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(36.dp))

        Button(
            onClick = onRequestPermission,
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CameraPrimary),
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(52.dp)
                .testTag("request_permission_button")
        ) {
            Text(
                text = "Enable Camera",
                fontWeight = FontWeight.SemiBold,
                color = Color.Black,
                fontSize = 16.sp
            )
        }
    }
}
