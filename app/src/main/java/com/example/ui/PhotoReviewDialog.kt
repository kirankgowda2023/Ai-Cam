package com.example.ui

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.model.CapturedPhoto
import com.example.ui.theme.CameraDarkBackground
import com.example.ui.theme.CameraOnSurface
import com.example.ui.theme.CameraOnSurfaceSubtle
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PhotoReviewDialog(
    photo: CapturedPhoto,
    onDismiss: () -> Unit,
    onDelete: (CapturedPhoto) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val formattedDate = remember(photo.timestamp) {
        val sdf = SimpleDateFormat("MMM d, yyyy · h:mm a", Locale.getDefault())
        sdf.format(Date(photo.timestamp))
    }

    val formattedSize = remember(photo.sizeBytes) {
        val kb = photo.sizeBytes / 1024.0
        if (kb > 1024) {
            String.format(Locale.getDefault(), "%.1f MB", kb / 1024.0)
        } else {
            String.format(Locale.getDefault(), "%.0f KB", kb)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CameraDarkBackground)
    ) {
        // High resolution photo preview
        AsyncImage(
            model = photo.file,
            contentDescription = "Captured photo preview",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 72.dp)
        )

        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.5f))
                    .testTag("back_to_camera_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back to camera",
                    tint = CameraOnSurface
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = photo.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = CameraOnSurface,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "$formattedDate • $formattedSize",
                    style = MaterialTheme.typography.bodySmall,
                    color = CameraOnSurfaceSubtle
                )
            }

            // Share button
            IconButton(
                onClick = { sharePhoto(context, photo) },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.5f))
                    .testTag("share_photo_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share photo",
                    tint = CameraOnSurface
                )
            }
        }

        // Bottom Action Bar
        Surface(
            color = Color.Black.copy(alpha = 0.65f),
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledTonalButton(
                    onClick = { showDeleteConfirm = true },
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = Color.Red.copy(alpha = 0.2f),
                        contentColor = Color(0xFFFF6B6B)
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.testTag("delete_photo_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text("Delete")
                }

                FilledTonalButton(
                    onClick = { sharePhoto(context, photo) },
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text("Share")
                }
            }
        }

        if (showDeleteConfirm) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirm = false },
                title = { Text("Delete Picture?") },
                text = { Text("This will remove the photo permanently from your device.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDeleteConfirm = false
                            onDelete(photo)
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFFF6B6B))
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirm = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

private fun sharePhoto(context: Context, photo: CapturedPhoto) {
    try {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/jpeg"
            putExtra(Intent.EXTRA_STREAM, photo.uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share picture"))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
