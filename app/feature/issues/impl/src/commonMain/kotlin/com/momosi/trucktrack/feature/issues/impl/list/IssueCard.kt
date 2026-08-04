package com.momosi.trucktrack.feature.issues.impl.list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.momosi.trucktrack.core.common.formatter.DateFormatter
import com.momosi.trucktrack.core.common.formatter.TimeAgo
import com.momosi.trucktrack.core.issue.model.Account
import com.momosi.trucktrack.core.issue.model.Issue
import com.momosi.trucktrack.core.issue.model.IssuePriority
import com.momosi.trucktrack.core.issue.model.IssueStatus
import com.momosi.trucktrack.core.uilibrary.components.Icon
import com.momosi.trucktrack.core.uilibrary.components.Text
import com.momosi.trucktrack.core.uilibrary.icons.TruckTrackIcons
import com.momosi.trucktrack.core.uilibrary.theme.AppTheme
import com.momosi.trucktrack.core.uilibrary.theme.Shapes
import com.momosi.trucktrack.core.uilibrary.theme.TruckTrackTheme
import com.momosi.trucktrack.core.vehicle.model.Vehicle
import com.momosi.trucktrack.core.vehicle.model.VehicleType
import com.momosi.trucktrack.feature.issues.impl.resources.Res
import com.momosi.trucktrack.feature.issues.impl.resources.issue_priority_high
import com.momosi.trucktrack.feature.issues.impl.resources.issue_priority_low
import com.momosi.trucktrack.feature.issues.impl.resources.issue_priority_medium
import com.momosi.trucktrack.feature.issues.impl.resources.issue_status_done
import com.momosi.trucktrack.feature.issues.impl.resources.issue_status_in_progress
import com.momosi.trucktrack.feature.issues.impl.resources.issue_status_open
import com.momosi.trucktrack.feature.issues.impl.resources.issue_unassigned
import com.momosi.trucktrack.feature.issues.impl.resources.time_ago_days
import com.momosi.trucktrack.feature.issues.impl.resources.time_ago_hours
import com.momosi.trucktrack.feature.issues.impl.resources.time_ago_just_now
import com.momosi.trucktrack.feature.issues.impl.resources.time_ago_minutes
import com.momosi.trucktrack.feature.issues.impl.resources.time_ago_yesterday
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.time.Clock
import kotlin.time.Instant

@Composable
internal fun IssueCard(
    issue: Issue,
    filter: IssueFilter,
    dateFormatter: DateFormatter,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .testTag("issue_card_${issue.id}")
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = Shapes.CardShape,
                ambientColor = AppTheme.colors.shadow,
                spotColor = AppTheme.colors.shadow,
            )
            .clip(Shapes.CardShape)
            .background(AppTheme.colors.surfaceContainerLowest)
            .clickable(onClick = onClick)
            .padding(start = 14.dp, end = 16.dp, top = 14.dp, bottom = 14.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Text(
                    text = issue.title,
                    style = AppTheme.typography.titleSmall,
                    color = AppTheme.colors.onSurface,
                    modifier = Modifier.weight(1f),
                )
                StatusChip(
                    label = issue.status.displayName(),
                    containerColor = issue.status.containerColor(),
                    contentColor = issue.status.contentColor(),
                    icon = issue.status.icon(),
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PriorityIndicator(
                    label = issue.priority.displayName(),
                    color = issue.priority.indicatorColor(),
                    icon = issue.priority.indicatorIcon(),
                )
                issue.vehicle?.let { vehicle ->
                    MetaItem(
                        icon = vehicle.type.vehicleIcon(),
                        text = vehicle.licensePlate,
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                when (filter) {
                    IssueFilter.MyWork,
                    IssueFilter.MyCompleted,
                    IssueFilter.Open,
                    -> issue.reportedBy?.let { reporter ->
                        MetaItem(icon = TruckTrackIcons.Edit, text = reporter.fullName)
                    }

                    IssueFilter.MyIssues,
                    IssueFilter.MyResolved,
                    -> MetaItem(
                        icon = TruckTrackIcons.AssignmentInd,
                        text = issue.assignedTo?.fullName ?: stringResource(Res.string.issue_unassigned),
                    )

                    IssueFilter.All -> Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        issue.reportedBy?.let { reporter ->
                            MetaItem(icon = TruckTrackIcons.Edit, text = reporter.fullName)
                        }
                        issue.assignedTo?.let { assignee ->
                            MetaItem(icon = TruckTrackIcons.AssignmentInd, text = assignee.fullName)
                        }
                    }
                }
                Text(
                    text = issue.createdAt.timeAgo(dateFormatter),
                    style = AppTheme.typography.labelMedium,
                    color = AppTheme.colors.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun StatusChip(
    label: String,
    containerColor: Color,
    contentColor: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
            .background(color = containerColor, shape = Shapes.CardShape)
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Icon(
            imageVector = icon,
            tint = contentColor,
            modifier = Modifier.size(13.dp),
        )
        Text(
            text = label.uppercase(),
            style = AppTheme.typography.labelSmall,
            color = contentColor,
        )
    }
}

@Composable
private fun PriorityIndicator(
    label: String,
    color: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        modifier = modifier,
    ) {
        Icon(
            imageVector = icon,
            tint = color,
            modifier = Modifier.size(15.dp),
        )
        Text(
            text = label.uppercase(),
            style = AppTheme.typography.labelSmall,
            color = color,
        )
    }
}

@Composable
private fun MetaItem(
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        modifier = modifier,
    ) {
        Icon(
            imageVector = icon,
            tint = AppTheme.colors.onSurfaceVariant,
            modifier = Modifier.size(15.dp),
        )
        Text(
            text = text,
            style = AppTheme.typography.bodySmall.copy(fontSize = 13.sp),
            color = AppTheme.colors.onSurfaceVariant,
        )
    }
}

@Composable
private fun IssueStatus.containerColor() = when (this) {
    IssueStatus.Open -> AppTheme.colors.open
    IssueStatus.InProgress -> AppTheme.colors.warning
    IssueStatus.Done -> AppTheme.colors.positive
}

@Composable
private fun IssueStatus.contentColor() = when (this) {
    IssueStatus.Open -> AppTheme.colors.onOpen
    IssueStatus.InProgress -> AppTheme.colors.onWarning
    IssueStatus.Done -> AppTheme.colors.onPositive
}

private fun IssueStatus.icon() = when (this) {
    IssueStatus.Open -> TruckTrackIcons.RadioButtonUnchecked
    IssueStatus.InProgress -> TruckTrackIcons.Build
    IssueStatus.Done -> TruckTrackIcons.Check
}

@Composable
private fun IssueStatus.displayName(): String = stringResource(
    when (this) {
        IssueStatus.Open -> Res.string.issue_status_open
        IssueStatus.InProgress -> Res.string.issue_status_in_progress
        IssueStatus.Done -> Res.string.issue_status_done
    },
)

@Composable
private fun IssuePriority.indicatorColor() = when (this) {
    IssuePriority.High -> AppTheme.colors.error
    IssuePriority.Medium -> AppTheme.colors.warning
    IssuePriority.Low -> AppTheme.colors.primary
}

private fun IssuePriority.indicatorIcon() = when (this) {
    IssuePriority.High -> TruckTrackIcons.Stat2
    IssuePriority.Medium -> TruckTrackIcons.Equal
    IssuePriority.Low -> TruckTrackIcons.ArrowDownward
}

@Composable
private fun IssuePriority.displayName(): String = stringResource(
    when (this) {
        IssuePriority.High -> Res.string.issue_priority_high
        IssuePriority.Medium -> Res.string.issue_priority_medium
        IssuePriority.Low -> Res.string.issue_priority_low
    },
)

@Composable
private fun Instant.timeAgo(dateFormatter: DateFormatter): String = when (val result = dateFormatter.timeAgoComponents(this)) {
    is TimeAgo.JustNow -> stringResource(Res.string.time_ago_just_now)
    is TimeAgo.Minutes -> stringResource(Res.string.time_ago_minutes, result.count)
    is TimeAgo.Hours -> stringResource(Res.string.time_ago_hours, result.count)
    is TimeAgo.Yesterday -> stringResource(Res.string.time_ago_yesterday)
    is TimeAgo.Days -> stringResource(Res.string.time_ago_days, result.count)
    is TimeAgo.OlderThanWeek -> result.formatted
}

private fun VehicleType.vehicleIcon() = when (this) {
    VehicleType.Trailer -> TruckTrackIcons.Trailer
    VehicleType.Truck -> TruckTrackIcons.Truck
}

private val sampleIssue = Issue(
    id = 1,
    title = "Engine warning light — truck won't start",
    description = "The engine warning light is on and the truck won't start.",
    status = IssueStatus.InProgress,
    priority = IssuePriority.High,
    vehicle = Vehicle(
        id = 1,
        licensePlate = "MA-204-TT",
        make = "DAF",
        model = "XF",
        type = VehicleType.Truck,
    ),
    reportedBy = Account(
        id = "1",
        username = "mschumacher",
        firstName = "Michael",
        lastName = "Schumacher",
    ),
    assignedTo = null,
    createdAt = Clock.System.now().minus(kotlin.time.Duration.parse("2h")),
    updatedAt = Clock.System.now().minus(kotlin.time.Duration.parse("1h")),
)

@Preview(showBackground = true)
@Composable
private fun IssueCardDriverMyIssuesPreview() {
    TruckTrackTheme {
        IssueCard(
            issue = sampleIssue,
            filter = IssueFilter.MyIssues,
            dateFormatter = DateFormatter(),
            onClick = {},
            modifier = Modifier.padding(12.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun IssueCardMechanicMyWorkPreview() {
    TruckTrackTheme {
        IssueCard(
            issue = sampleIssue,
            filter = IssueFilter.MyWork,
            dateFormatter = DateFormatter(),
            onClick = {},
            modifier = Modifier.padding(12.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun IssueCardAllPreview() {
    TruckTrackTheme {
        IssueCard(
            issue = sampleIssue.copy(
                assignedTo = Account(id = "2", username = "mbinotto", firstName = "Mattia", lastName = "Binotto"),
            ),
            filter = IssueFilter.All,
            dateFormatter = DateFormatter(),
            onClick = {},
            modifier = Modifier.padding(12.dp),
        )
    }
}
