package com.example.model

import android.net.Uri
import java.io.File

data class CapturedPhoto(
    val file: File,
    val uri: Uri,
    val timestamp: Long = System.currentTimeMillis(),
    val name: String = file.name,
    val sizeBytes: Long = file.length()
)
