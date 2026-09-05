package com.example.ui

import android.content.Context
import android.view.ViewGroup
import androidx.camera.core.Camera
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import java.io.File
import java.util.concurrent.Executors

@Composable
fun CameraPreview(
    isFrontCamera: Boolean,
    flashMode: Int,
    zoomRatio: Float,
    onTapToFocus: (Offset) -> Unit,
    bindImageCapture: (ImageCapture) -> Unit,
    onProvideBitmapProvider: ((() -> android.graphics.Bitmap?) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var cameraControl by remember { mutableStateOf<CameraControl?>(null) }
    var previewViewRef by remember { mutableStateOf<PreviewView?>(null) }

    LaunchedEffect(previewViewRef) {
        if (previewViewRef != null) {
            onProvideBitmapProvider?.invoke { previewViewRef?.bitmap }
        }
    }

    val imageCapture = remember {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .setFlashMode(flashMode)
            .build()
    }

    LaunchedEffect(imageCapture) {
        bindImageCapture(imageCapture)
    }

    LaunchedEffect(flashMode) {
        imageCapture.flashMode = flashMode
    }

    LaunchedEffect(zoomRatio, cameraControl) {
        cameraControl?.setZoomRatio(zoomRatio)
    }

    LaunchedEffect(isFrontCamera) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()
                val cameraSelector = if (isFrontCamera) {
                    CameraSelector.DEFAULT_FRONT_CAMERA
                } else {
                    CameraSelector.DEFAULT_BACK_CAMERA
                }

                val preview = Preview.Builder().build()
                previewViewRef?.let {
                    preview.surfaceProvider = it.surfaceProvider
                }

                cameraProvider.unbindAll()
                val camera: Camera = cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )
                cameraControl = camera.cameraControl
                camera.cameraControl.setZoomRatio(zoomRatio)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(context))
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(previewViewRef) {
                detectTapGestures { offset ->
                    onTapToFocus(offset)
                    val pv = previewViewRef ?: return@detectTapGestures
                    val meteringPointFactory = pv.meteringPointFactory
                    val point = meteringPointFactory.createPoint(offset.x, offset.y)
                    val action = FocusMeteringAction.Builder(point).build()
                    cameraControl?.startFocusAndMetering(action)
                }
            }
    ) {
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    previewViewRef = this

                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener({
                        try {
                            val cameraProvider = cameraProviderFuture.get()
                            val cameraSelector = if (isFrontCamera) {
                                CameraSelector.DEFAULT_FRONT_CAMERA
                            } else {
                                CameraSelector.DEFAULT_BACK_CAMERA
                            }

                            val preview = Preview.Builder().build()
                            preview.surfaceProvider = surfaceProvider

                            cameraProvider.unbindAll()
                            val camera = cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                imageCapture
                            )
                            cameraControl = camera.cameraControl
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }, ContextCompat.getMainExecutor(ctx))
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}

fun capturePhoto(
    context: Context,
    imageCapture: ImageCapture?,
    outputFile: File,
    onStart: () -> Unit,
    onSuccess: (File) -> Unit,
    onError: (String) -> Unit
) {
    if (imageCapture == null) {
        onError("Camera is not ready")
        return
    }

    onStart()

    val outputOptions = ImageCapture.OutputFileOptions.Builder(outputFile).build()
    val executor = Executors.newSingleThreadExecutor()

    imageCapture.takePicture(
        outputOptions,
        executor,
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                ContextCompat.getMainExecutor(context).execute {
                    onSuccess(outputFile)
                }
            }

            override fun onError(exception: ImageCaptureException) {
                ContextCompat.getMainExecutor(context).execute {
                    onError(exception.message ?: "Failed to capture photo")
                }
            }
        }
    )
}
