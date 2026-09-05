package com.example.ui

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import androidx.camera.core.ImageCapture
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.AiSceneAnalyzer
import com.example.ai.SceneAnalysisResult
import com.example.model.CapturedPhoto
import com.example.model.PoseCatalog
import com.example.model.PoseRecommendation
import com.example.model.SceneCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class CameraViewModel(application: Application) : AndroidViewModel(application) {

    private val aiSceneAnalyzer = AiSceneAnalyzer()

    private val _hasCameraPermission = MutableStateFlow(false)
    val hasCameraPermission: StateFlow<Boolean> = _hasCameraPermission.asStateFlow()

    private val _isFrontCamera = MutableStateFlow(false)
    val isFrontCamera: StateFlow<Boolean> = _isFrontCamera.asStateFlow()

    private val _flashMode = MutableStateFlow(ImageCapture.FLASH_MODE_OFF)
    val flashMode: StateFlow<Int> = _flashMode.asStateFlow()

    private val _isGridVisible = MutableStateFlow(false)
    val isGridVisible: StateFlow<Boolean> = _isGridVisible.asStateFlow()

    private val _zoomRatio = MutableStateFlow(1f)
    val zoomRatio: StateFlow<Float> = _zoomRatio.asStateFlow()

    private val _isCapturing = MutableStateFlow(false)
    val isCapturing: StateFlow<Boolean> = _isCapturing.asStateFlow()

    private val _shutterFlashTrigger = MutableStateFlow(0L)
    val shutterFlashTrigger: StateFlow<Long> = _shutterFlashTrigger.asStateFlow()

    private val _capturedPhotos = MutableStateFlow<List<CapturedPhoto>>(emptyList())
    val capturedPhotos: StateFlow<List<CapturedPhoto>> = _capturedPhotos.asStateFlow()

    private val _selectedPhoto = MutableStateFlow<CapturedPhoto?>(null)
    val selectedPhoto: StateFlow<CapturedPhoto?> = _selectedPhoto.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    // --- AI Pose Guidance States ---
    private val _isAiPoseMode = MutableStateFlow(false)
    val isAiPoseMode: StateFlow<Boolean> = _isAiPoseMode.asStateFlow()

    private val _isAnalyzingScene = MutableStateFlow(false)
    val isAnalyzingScene: StateFlow<Boolean> = _isAnalyzingScene.asStateFlow()

    private val _sceneAnalysis = MutableStateFlow<SceneAnalysisResult?>(null)
    val sceneAnalysis: StateFlow<SceneAnalysisResult?> = _sceneAnalysis.asStateFlow()

    private val _selectedEnvironment = MutableStateFlow(com.example.model.EnvironmentType.ALL)
    val selectedEnvironment: StateFlow<com.example.model.EnvironmentType> = _selectedEnvironment.asStateFlow()

    private val _selectedCategory = MutableStateFlow<SceneCategory?>(null)
    val selectedCategory: StateFlow<SceneCategory?> = _selectedCategory.asStateFlow()

    private val _selectedPose = MutableStateFlow<PoseRecommendation?>(null)
    val selectedPose: StateFlow<PoseRecommendation?> = _selectedPose.asStateFlow()

    private val _isPoseMirrored = MutableStateFlow(false)
    val isPoseMirrored: StateFlow<Boolean> = _isPoseMirrored.asStateFlow()

    private val _poseOpacity = MutableStateFlow(0.85f)
    val poseOpacity: StateFlow<Float> = _poseOpacity.asStateFlow()

    private val _showPoseAnnotations = MutableStateFlow(true)
    val showPoseAnnotations: StateFlow<Boolean> = _showPoseAnnotations.asStateFlow()

    private val _isPoseDrawerExpanded = MutableStateFlow(true)
    val isPoseDrawerExpanded: StateFlow<Boolean> = _isPoseDrawerExpanded.asStateFlow()

    init {
        loadSavedPhotos()
    }

    fun setPermissionGranted(granted: Boolean) {
        _hasCameraPermission.value = granted
    }

    fun toggleCameraLens() {
        _isFrontCamera.value = !_isFrontCamera.value
    }

    fun toggleAiPoseMode(getBitmap: (() -> Bitmap?)? = null) {
        val newState = !_isAiPoseMode.value
        _isAiPoseMode.value = newState
        if (newState) {
            _statusMessage.value = "AI Pose Guide Enabled"
            // If no pose is selected yet, choose a default
            if (_selectedPose.value == null) {
                _selectedPose.value = PoseCatalog.ALL_POSES.firstOrNull()
                _selectedCategory.value = _selectedPose.value?.category
            }
            // Trigger background analysis with current frame
            val currentBitmap = getBitmap?.invoke()
            analyzeScene(currentBitmap)
        } else {
            _statusMessage.value = "AI Pose Guide Disabled"
        }
    }

    fun analyzeScene(bitmap: Bitmap?, requestedCategory: SceneCategory? = null) {
        viewModelScope.launch {
            _isAnalyzingScene.value = true
            val result = aiSceneAnalyzer.analyzeBackground(bitmap, requestedCategory)
            _sceneAnalysis.value = result
            _selectedCategory.value = result.category
            _selectedEnvironment.value = result.environment

            // Pick recommended pose for this scene
            val matchingPoses = PoseCatalog.ALL_POSES.filter { it.category == result.category }
            if (matchingPoses.isNotEmpty()) {
                _selectedPose.value = matchingPoses.first()
            }

            _isAnalyzingScene.value = false
            _statusMessage.value = "AI Detected: ${result.sceneTitle} (${result.environment.displayName})"
        }
    }

    fun selectEnvironment(env: com.example.model.EnvironmentType) {
        _selectedEnvironment.value = env
        val currentCategory = _selectedCategory.value
        if (env != com.example.model.EnvironmentType.ALL && currentCategory?.environment != env) {
            val firstCategoryInEnv = SceneCategory.values().firstOrNull { it.environment == env }
            selectCategory(firstCategoryInEnv)
        }
    }

    fun selectPose(pose: PoseRecommendation?) {
        _selectedPose.value = pose
        if (pose != null) {
            _selectedCategory.value = pose.category
            _selectedEnvironment.value = pose.environment
        }
    }

    fun selectCategory(category: SceneCategory?) {
        _selectedCategory.value = category
        if (category != null) {
            _selectedEnvironment.value = category.environment
            val matching = PoseCatalog.ALL_POSES.filter { it.category == category }
            if (matching.isNotEmpty() && _selectedPose.value?.category != category) {
                _selectedPose.value = matching.first()
            }
        }
    }

    fun toggleMirrorPose() {
        _isPoseMirrored.value = !_isPoseMirrored.value
    }

    fun setPoseOpacity(opacity: Float) {
        _poseOpacity.value = opacity.coerceIn(0.2f, 1.0f)
    }

    fun togglePoseAnnotations() {
        _showPoseAnnotations.value = !_showPoseAnnotations.value
    }

    fun togglePoseDrawer() {
        _isPoseDrawerExpanded.value = !_isPoseDrawerExpanded.value
    }

    fun cycleFlashMode() {
        _flashMode.value = when (_flashMode.value) {
            ImageCapture.FLASH_MODE_OFF -> ImageCapture.FLASH_MODE_ON
            ImageCapture.FLASH_MODE_ON -> ImageCapture.FLASH_MODE_AUTO
            else -> ImageCapture.FLASH_MODE_OFF
        }
    }

    fun toggleGrid() {
        _isGridVisible.value = !_isGridVisible.value
    }

    fun setZoomRatio(ratio: Float) {
        _zoomRatio.value = ratio.coerceIn(1f, 5f)
    }

    fun onCaptureStart() {
        _isCapturing.value = true
        _shutterFlashTrigger.value = System.currentTimeMillis()
    }

    fun onPhotoCaptured(file: File) {
        viewModelScope.launch {
            val context = getApplication<Application>()
            val photo = createCapturedPhoto(context, file)
            _capturedPhotos.value = listOf(photo) + _capturedPhotos.value
            _isCapturing.value = false
            _statusMessage.value = "Photo saved!"
        }
    }

    fun onCaptureError(message: String) {
        _isCapturing.value = false
        _statusMessage.value = message
    }

    fun selectPhoto(photo: CapturedPhoto?) {
        _selectedPhoto.value = photo
    }

    fun deletePhoto(photo: CapturedPhoto) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (photo.file.exists()) {
                    photo.file.delete()
                }
                withContext(Dispatchers.Main) {
                    _capturedPhotos.value = _capturedPhotos.value.filter { it.file.absolutePath != photo.file.absolutePath }
                    if (_selectedPhoto.value?.file?.absolutePath == photo.file.absolutePath) {
                        _selectedPhoto.value = null
                    }
                    _statusMessage.value = "Photo deleted"
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _statusMessage.value = "Failed to delete: ${e.message}"
                }
            }
        }
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    fun getOutputMediaFile(): File {
        val context = getApplication<Application>()
        val photoDir = File(context.filesDir, "photos")
        if (!photoDir.exists()) {
            photoDir.mkdirs()
        }
        val timestamp = System.currentTimeMillis()
        return File(photoDir, "IMG_$timestamp.jpg")
    }

    private fun loadSavedPhotos() {
        viewModelScope.launch(Dispatchers.IO) {
            val context = getApplication<Application>()
            val photoDir = File(context.filesDir, "photos")
            if (photoDir.exists()) {
                val files = photoDir.listFiles { file ->
                    file.isFile && (file.extension.equals("jpg", ignoreCase = true) || file.extension.equals("png", ignoreCase = true))
                }?.sortedByDescending { it.lastModified() } ?: emptyList()

                val photos = files.mapNotNull { file ->
                    try {
                        createCapturedPhoto(context, file)
                    } catch (e: Exception) {
                        null
                    }
                }
                withContext(Dispatchers.Main) {
                    _capturedPhotos.value = photos
                }
            }
        }
    }

    private fun createCapturedPhoto(context: Context, file: File): CapturedPhoto {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        return CapturedPhoto(
            file = file,
            uri = uri,
            timestamp = file.lastModified(),
            name = file.name,
            sizeBytes = file.length()
        )
    }
}
