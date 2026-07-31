package com.momosi.trucktrack.core.uilibrary.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.momosi.trucktrack.core.uilibrary.modifier.shimmer
import com.momosi.trucktrack.core.uilibrary.theme.AppTheme

@Composable
fun SkeletonBox(modifier: Modifier = Modifier, shape: Shape = RoundedCornerShape(4.dp)) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(AppTheme.colors.surfaceVariant)
            .shimmer(),
    )
}
