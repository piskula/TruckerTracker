package com.momosi.trucktrack.feature.issues.impl

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.momosi.trucktrack.core.uilibrary.components.Icon
import com.momosi.trucktrack.core.uilibrary.icons.TruckTrackIcons
import com.momosi.trucktrack.feature.issues.impl.navigation.PhotoSource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

private const val MIN_ZOOM = 1f
private const val MAX_ZOOM = 5f
private const val DOUBLE_TAP_ZOOM = 3f

@Composable
internal fun FullScreenPhotoScreen(
    source: PhotoSource,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FullScreenPhotoViewModel = koinViewModel(parameters = { parametersOf(source) }),
) {
    val isSaving by viewModel.isSaving.collectAsStateWithLifecycle()
    FullScreenPhoto(
        model = when (source) {
            is PhotoSource.Attachment -> source.url
            is PhotoSource.Bytes -> source.bytes
        },
        isSaving = isSaving,
        onDismiss = onBack,
        onSave = { viewModel.onAction(FullScreenPhotoAction.SavePhoto) },
        modifier = modifier,
    )
}

@Composable
private fun FullScreenPhoto(
    model: Any,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var scale by remember { mutableFloatStateOf(MIN_ZOOM) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("full_screen_photo"),
        contentAlignment = Alignment.Center,
    ) {
        AsyncImage(
            model = model,
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        val newScale = (scale * zoom).coerceIn(MIN_ZOOM, MAX_ZOOM)
                        scale = newScale
                        offset = if (newScale <= MIN_ZOOM) Offset.Zero else offset + pan
                    }
                }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = {
                            if (scale > MIN_ZOOM) {
                                scale = MIN_ZOOM
                                offset = Offset.Zero
                            } else {
                                scale = DOUBLE_TAP_ZOOM
                            }
                        },
                    )
                }
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offset.x,
                    translationY = offset.y,
                ),
        )
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            FullScreenPhotoIconButton(
                icon = TruckTrackIcons.Download,
                enabled = !isSaving,
                onClick = onSave,
                testTag = "full_screen_photo_download_button",
            )
            FullScreenPhotoIconButton(
                icon = TruckTrackIcons.Close,
                onClick = onDismiss,
                testTag = "full_screen_photo_close_button",
            )
        }
    }
}

@Composable
private fun FullScreenPhotoIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
            .clickable(enabled = enabled, onClick = onClick)
            .testTag(testTag),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            tint = if (enabled) Color.White else Color.White.copy(alpha = 0.4f),
            modifier = Modifier.size(24.dp),
        )
    }
}
