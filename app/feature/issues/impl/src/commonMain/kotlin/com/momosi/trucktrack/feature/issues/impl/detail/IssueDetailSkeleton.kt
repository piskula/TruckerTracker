package com.momosi.trucktrack.feature.issues.impl.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.momosi.trucktrack.core.uilibrary.components.SkeletonBox
import com.momosi.trucktrack.core.uilibrary.modifier.ShimmerGroup
import com.momosi.trucktrack.core.uilibrary.theme.AppTheme
import com.momosi.trucktrack.core.uilibrary.theme.Shapes
import com.momosi.trucktrack.core.uilibrary.theme.TruckTrackTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
internal fun IssueDetailSkeleton(modifier: Modifier = Modifier) {
    ShimmerGroup {
        Column(modifier = modifier.fillMaxSize()) {
            PeopleStripSkeleton()
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                HeaderCardSkeleton()
                DescriptionCardSkeleton()
                PhotosCardSkeleton()
                HistoryCardSkeleton()
            }
        }
    }
}

@Composable
private fun PeopleStripSkeleton(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(AppTheme.colors.surfaceContainerHighest)
            .padding(horizontal = 16.dp, vertical = 10.dp),
    ) {
        PersonCellSkeleton(modifier = Modifier.weight(1f))
        PersonCellSkeleton(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun PersonCellSkeleton(modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
    ) {
        SkeletonBox(modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            SkeletonBox(modifier = Modifier.width(50.dp).height(16.dp))
            SkeletonBox(modifier = Modifier.width(90.dp).height(16.dp))
        }
    }
}

@Composable
private fun HeaderCardSkeleton(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(Shapes.CardShape)
            .background(AppTheme.colors.surfaceContainerLowest, Shapes.CardShape)
            .padding(start = 12.dp, end = 14.dp, top = 12.dp, bottom = 12.dp),
    ) {
        Column {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    SkeletonBox(modifier = Modifier.fillMaxWidth(0.55f).height(20.dp))
                }
                SkeletonBox(modifier = Modifier.width(64.dp).height(24.dp), shape = Shapes.CardShape)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                MetaItemSkeleton(spacing = 2.dp, labelWidth = 40.dp)
                MetaItemSkeleton(spacing = 3.dp, labelWidth = 56.dp)
                Spacer(modifier = Modifier.weight(1f))
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

@Composable
private fun DescriptionCardSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(Shapes.CardShape)
            .background(AppTheme.colors.surfaceContainerLowest, Shapes.CardShape)
            .padding(16.dp),
    ) {
        SkeletonBox(modifier = Modifier.width(90.dp).height(20.dp).padding(bottom = 12.dp))
        SkeletonBox(modifier = Modifier.fillMaxWidth().height(24.dp))
        Spacer(modifier = Modifier.height(4.dp))
        SkeletonBox(modifier = Modifier.fillMaxWidth(0.6f).height(24.dp))
    }
}

@Composable
private fun HistoryCardSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(Shapes.CardShape)
            .background(AppTheme.colors.surfaceContainerLowest, Shapes.CardShape)
            .padding(16.dp),
    ) {
        SkeletonBox(modifier = Modifier.width(70.dp).height(20.dp).padding(bottom = 12.dp))
        repeat(3) { index ->
            TimelineStepSkeleton(isLast = index == 2)
        }
        Spacer(modifier = Modifier.height(14.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(AppTheme.colors.surfaceVariant),
        )
        Spacer(modifier = Modifier.height(14.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppTheme.colors.surfaceContainer, RoundedCornerShape(10.dp))
                .padding(start = 12.dp, end = 12.dp, top = 10.dp, bottom = 10.dp),
        ) {
            SkeletonBox(modifier = Modifier.fillMaxWidth(0.6f).height(16.dp))
        }
    }
}

@Composable
private fun TimelineStepSkeleton(isLast: Boolean, modifier: Modifier = Modifier) {
    Row(modifier = modifier.height(IntrinsicSize.Min)) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .width(32.dp)
                .fillMaxHeight(),
        ) {
            SkeletonBox(modifier = Modifier.size(32.dp), shape = CircleShape)
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .width(2.dp)
                        .background(AppTheme.colors.surfaceContainerHighest),
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.padding(bottom = if (isLast) 0.dp else 18.dp)) {
            SkeletonBox(modifier = Modifier.fillMaxWidth(0.5f).height(20.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                SkeletonBox(modifier = Modifier.width(90.dp).height(16.dp))
            }
        }
    }
}

@Composable
private fun PhotosCardSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(Shapes.CardShape)
            .background(AppTheme.colors.surfaceContainerLowest, Shapes.CardShape)
            .padding(16.dp),
    ) {
        SkeletonBox(modifier = Modifier.width(70.dp).height(20.dp).padding(bottom = 12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(3) {
                SkeletonBox(modifier = Modifier.size(80.dp), shape = RoundedCornerShape(8.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun IssueDetailSkeletonPreview() {
    TruckTrackTheme {
        IssueDetailSkeleton()
    }
}
