package com.momosi.trucktrack.feature.issues.impl

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.momosi.trucktrack.core.uilibrary.components.Icon
import com.momosi.trucktrack.core.uilibrary.icons.TruckTrackIcons
import com.momosi.trucktrack.feature.issues.impl.navigation.PhotoSource

@Composable
internal fun FullScreenPhotoScreen(
    source: PhotoSource,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FullScreenPhoto(
        model = when (source) {
            is PhotoSource.Url -> source.url
            is PhotoSource.Bytes -> source.bytes
        },
        onDismiss = onBack,
        modifier = modifier,
    )
}

@Composable
private fun FullScreenPhoto(
    model: Any,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(onClick = onDismiss)
            .testTag("full_screen_photo"),
        contentAlignment = Alignment.Center,
    ) {
        AsyncImage(
            model = model,
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize(),
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .size(40.dp)
                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                .clickable(onClick = onDismiss)
                .testTag("full_screen_photo_close_button"),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = TruckTrackIcons.Close,
                tint = Color.White,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}
