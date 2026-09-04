package com.momosi.trucktrack.feature.issues.impl

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.momosi.trucktrack.core.issue.model.IssuePriority
import com.momosi.trucktrack.core.uilibrary.components.Icon
import com.momosi.trucktrack.core.uilibrary.components.LoadingSpinner
import com.momosi.trucktrack.core.uilibrary.components.Text
import com.momosi.trucktrack.core.uilibrary.components.TextField
import com.momosi.trucktrack.core.uilibrary.icons.TruckTrackIcons
import com.momosi.trucktrack.core.uilibrary.theme.AppTheme
import com.momosi.trucktrack.core.uilibrary.theme.Shapes
import com.momosi.trucktrack.core.vehicle.model.Vehicle
import com.momosi.trucktrack.core.vehicle.model.VehicleType
import com.momosi.trucktrack.feature.issues.impl.resources.Res
import com.momosi.trucktrack.feature.issues.impl.resources.issue_priority_high
import com.momosi.trucktrack.feature.issues.impl.resources.issue_priority_high_hint
import com.momosi.trucktrack.feature.issues.impl.resources.issue_priority_low
import com.momosi.trucktrack.feature.issues.impl.resources.issue_priority_low_hint
import com.momosi.trucktrack.feature.issues.impl.resources.issue_priority_medium
import com.momosi.trucktrack.feature.issues.impl.resources.issue_priority_medium_hint
import com.momosi.trucktrack.feature.issues.impl.resources.issue_select_vehicle
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun Card(
    title: String,
    modifier: Modifier = Modifier,
    locked: Boolean = false,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(Shapes.CardShape)
            .background(AppTheme.colors.surfaceContainerLowest, Shapes.CardShape)
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 12.dp)) {
            Text(
                text = title,
                style = AppTheme.typography.labelLarge,
                color = AppTheme.colors.onSurfaceVariant,
            )
            if (locked) {
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = TruckTrackIcons.Lock,
                    tint = AppTheme.colors.onSurfaceVariant,
                    modifier = Modifier.size(14.dp),
                )
            }
        }
        Column(modifier = if (locked) Modifier.alpha(0.55f) else Modifier) {
            content()
        }
    }
}

@Composable
internal fun InputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    fieldTestTag: String,
    modifier: Modifier = Modifier,
    minLines: Int = 1,
    isError: Boolean = false,
    enabled: Boolean = true,
    locked: Boolean = false,
) {
    Column(modifier = modifier.then(if (locked) Modifier.alpha(0.55f) else Modifier)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label,
                style = AppTheme.typography.bodySmall,
                color = AppTheme.colors.primary,
            )
            if (locked) {
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = TruckTrackIcons.Lock,
                    tint = AppTheme.colors.primary,
                    modifier = Modifier.size(12.dp),
                )
            }
        }
        TextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            minLines = minLines,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
                .testTag(fieldTestTag),
            decorationBox = { innerTextField ->
                Column {
                    innerTextField()
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .background(
                                when {
                                    isError -> AppTheme.colors.error
                                    value.isNotEmpty() -> AppTheme.colors.primary
                                    else -> AppTheme.colors.surfaceVariant
                                },
                            ),
                    )
                }
            },
        )
    }
}

@Composable
internal fun VehicleSelector(
    vehicles: VehiclesContent,
    selectedVehicle: Vehicle?,
    expanded: Boolean,
    onToggle: () -> Unit,
    onSelect: (Vehicle) -> Unit,
    testTagPrefix: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Column(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = enabled, onClick = onToggle)
                .padding(vertical = 8.dp)
                .testTag("${testTagPrefix}_vehicle_selector"),
        ) {
            Icon(
                imageVector = selectedVehicle?.type.vehicleIcon(),
                tint = AppTheme.colors.primary,
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            when {
                vehicles is VehiclesContent.Loading -> {
                    LoadingSpinner(size = 20.dp, strokeWidth = 2.dp)
                }

                selectedVehicle != null -> {
                    Text(
                        text = "${selectedVehicle.licensePlate} · ${selectedVehicle.make} ${selectedVehicle.model}",
                        style = AppTheme.typography.titleSmall,
                        color = AppTheme.colors.onSurface,
                        modifier = Modifier.weight(1f),
                    )
                }

                else -> {
                    Text(
                        text = stringResource(Res.string.issue_select_vehicle),
                        style = AppTheme.typography.titleSmall,
                        color = AppTheme.colors.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            if (enabled) {
                Icon(
                    imageVector = TruckTrackIcons.ArrowDropDown,
                    tint = AppTheme.colors.primary,
                    modifier = Modifier.size(24.dp),
                )
            }
        }
        if (expanded && vehicles is VehiclesContent.Loaded) {
            Column {
                vehicles.vehicles.forEach { vehicle ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(vehicle) }
                            .padding(vertical = 10.dp, horizontal = 4.dp)
                            .testTag("${testTagPrefix}_vehicle_option_${vehicle.id}"),
                    ) {
                        Icon(
                            imageVector = vehicle.type.vehicleIcon(),
                            tint = AppTheme.colors.onSurfaceVariant,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${vehicle.licensePlate} · ${vehicle.make} ${vehicle.model}",
                            style = AppTheme.typography.bodyMedium,
                            color = AppTheme.colors.onSurface,
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun PrioritySelector(
    selected: IssuePriority,
    onSelect: (IssuePriority) -> Unit,
    testTagPrefix: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .border(1.5.dp, AppTheme.colors.outlineVariant, CircleShape)
                .clip(CircleShape),
        ) {
            PrioritySegment(
                priority = IssuePriority.Low,
                isSelected = selected == IssuePriority.Low,
                onClick = { onSelect(IssuePriority.Low) },
                testTagPrefix = testTagPrefix,
                enabled = enabled,
                modifier = Modifier.weight(1f),
            )
            Box(modifier = Modifier.width(1.5.dp).fillMaxHeight().background(AppTheme.colors.outlineVariant))
            PrioritySegment(
                priority = IssuePriority.Medium,
                isSelected = selected == IssuePriority.Medium,
                onClick = { onSelect(IssuePriority.Medium) },
                testTagPrefix = testTagPrefix,
                enabled = enabled,
                modifier = Modifier.weight(1f),
            )
            Box(modifier = Modifier.width(1.5.dp).fillMaxHeight().background(AppTheme.colors.outlineVariant))
            PrioritySegment(
                priority = IssuePriority.High,
                isSelected = selected == IssuePriority.High,
                onClick = { onSelect(IssuePriority.High) },
                testTagPrefix = testTagPrefix,
                enabled = enabled,
                modifier = Modifier.weight(1f),
            )
        }
        PriorityDescription(priority = selected)
    }
}

@Composable
private fun PrioritySegment(
    priority: IssuePriority,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTagPrefix: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val backgroundColor = if (isSelected) priority.containerColor() else AppTheme.colors.surfaceContainerLowest
    val contentColor = if (isSelected) priority.onContainerColor() else AppTheme.colors.onSurface
    val iconColor = if (isSelected) priority.onContainerColor() else priority.accentColor()

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
            .background(backgroundColor)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 8.dp)
            .testTag("${testTagPrefix}_priority_${priority.name}"),
    ) {
        Icon(
            imageVector = priority.icon(),
            tint = iconColor,
            modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = stringResource(priority.labelRes()),
            style = AppTheme.typography.labelLarge,
            color = contentColor,
        )
    }
}

@Composable
private fun PriorityDescription(priority: IssuePriority, modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier
            .fillMaxWidth()
            .background(priority.containerColor(), RoundedCornerShape(10.dp))
            .padding(12.dp),
    ) {
        Icon(
            imageVector = priority.icon(),
            tint = priority.onContainerColor(),
            modifier = Modifier.size(20.dp),
        )
        Text(
            text = stringResource(priority.hintRes()),
            style = AppTheme.typography.bodySmall,
            color = priority.onContainerColor(),
        )
    }
}

@Composable
private fun IssuePriority.accentColor() = when (this) {
    IssuePriority.High -> AppTheme.colors.error
    IssuePriority.Medium -> AppTheme.colors.warning
    IssuePriority.Low -> AppTheme.colors.primary
}

@Composable
private fun IssuePriority.containerColor() = when (this) {
    IssuePriority.High -> AppTheme.colors.errorContainer
    IssuePriority.Medium -> AppTheme.colors.warningContainer
    IssuePriority.Low -> AppTheme.colors.primaryContainer
}

@Composable
private fun IssuePriority.onContainerColor() = when (this) {
    IssuePriority.High -> AppTheme.colors.onErrorContainer
    IssuePriority.Medium -> AppTheme.colors.onWarningContainer
    IssuePriority.Low -> AppTheme.colors.onPrimaryContainer
}

private fun IssuePriority.icon() = when (this) {
    IssuePriority.High -> TruckTrackIcons.Stat2
    IssuePriority.Medium -> TruckTrackIcons.Equal
    IssuePriority.Low -> TruckTrackIcons.ArrowDownward
}

private fun IssuePriority.labelRes() = when (this) {
    IssuePriority.High -> Res.string.issue_priority_high
    IssuePriority.Medium -> Res.string.issue_priority_medium
    IssuePriority.Low -> Res.string.issue_priority_low
}

private fun IssuePriority.hintRes() = when (this) {
    IssuePriority.High -> Res.string.issue_priority_high_hint
    IssuePriority.Medium -> Res.string.issue_priority_medium_hint
    IssuePriority.Low -> Res.string.issue_priority_low_hint
}

internal fun VehicleType?.vehicleIcon() = when (this) {
    VehicleType.Trailer -> TruckTrackIcons.Trailer
    VehicleType.Truck, null -> TruckTrackIcons.Truck
}
