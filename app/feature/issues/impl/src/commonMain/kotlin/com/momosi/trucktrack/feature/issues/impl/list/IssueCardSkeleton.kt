package com.momosi.trucktrack.feature.issues.impl.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.momosi.trucktrack.core.uilibrary.components.SkeletonBox
import com.momosi.trucktrack.core.uilibrary.theme.AppTheme
import com.momosi.trucktrack.core.uilibrary.theme.Shapes
import com.momosi.trucktrack.core.uilibrary.theme.TruckTrackTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
internal fun IssueCardSkeleton(modifier: Modifier = Modifier, titleWidthFraction: Float = 0.7f) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = Shapes.CardShape,
                ambientColor = AppTheme.colors.shadow,
                spotColor = AppTheme.colors.shadow,
            )
            .clip(Shapes.CardShape)
            .background(AppTheme.colors.surfaceContainerLowest)
            .padding(start = 14.dp, end = 16.dp, top = 14.dp, bottom = 14.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    SkeletonBox(modifier = Modifier.fillMaxWidth(titleWidthFraction).height(20.dp))
                }
                SkeletonBox(modifier = Modifier.width(64.dp).height(24.dp), shape = Shapes.CardShape)
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                MetaItemSkeleton(spacing = 2.dp, labelWidth = 40.dp)
                MetaItemSkeleton(spacing = 3.dp, labelWidth = 56.dp)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                MetaItemSkeleton(spacing = 3.dp, labelWidth = 72.dp)
                SkeletonBox(modifier = Modifier.width(48.dp).height(16.dp))
            }
        }
    }
}

@Composable
private fun MetaItemSkeleton(spacing: Dp, labelWidth: Dp) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing),
    ) {
        SkeletonBox(modifier = Modifier.size(15.dp))
        SkeletonBox(modifier = Modifier.width(labelWidth).height(16.dp))
    }
}

@Preview(showBackground = true)
@Composable
private fun IssueCardSkeletonPreview() {
    TruckTrackTheme {
        IssueCardSkeleton(modifier = Modifier.padding(12.dp))
    }
}
