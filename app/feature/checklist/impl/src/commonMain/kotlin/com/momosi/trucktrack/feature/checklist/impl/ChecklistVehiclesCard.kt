package com.momosi.trucktrack.feature.checklist.impl

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.momosi.trucktrack.core.uilibrary.components.Text
import com.momosi.trucktrack.core.uilibrary.modifier.card
import com.momosi.trucktrack.core.uilibrary.theme.AppTheme
import com.momosi.trucktrack.core.uilibrary.vehicle.VehicleSelector
import com.momosi.trucktrack.core.vehicle.model.VehicleType
import com.momosi.trucktrack.feature.checklist.impl.resources.Res
import com.momosi.trucktrack.feature.checklist.impl.resources.checklist_error_vehicles_required
import com.momosi.trucktrack.feature.checklist.impl.resources.checklist_select_trailer
import com.momosi.trucktrack.feature.checklist.impl.resources.checklist_select_truck
import com.momosi.trucktrack.feature.checklist.impl.resources.checklist_vehicles
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ChecklistVehiclesCard(
    state: ChecklistState,
    content: ChecklistContent.Loaded,
    onAction: (ChecklistAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .card()
            .padding(horizontal = 8.dp, vertical = 16.dp),
    ) {
        Text(
            text = stringResource(Res.string.checklist_vehicles),
            style = AppTheme.typography.labelLarge,
            color = AppTheme.colors.onSurfaceVariant,
            modifier = Modifier.padding(start = 8.dp, bottom = 4.dp),
        )
        MissingHighlight(isMissing = state.truckMissing) {
            VehicleSelector(
                vehicles = content.trucks,
                selectedVehicle = state.selectedTruck,
                expanded = state.expandedPicker == VehicleType.Truck,
                onToggle = { onAction(ChecklistAction.TogglePicker(VehicleType.Truck)) },
                onSelect = { onAction(ChecklistAction.SelectVehicle(it)) },
                testTagPrefix = "checklist_truck",
                placeholder = stringResource(Res.string.checklist_select_truck),
                placeholderType = VehicleType.Truck,
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .height(1.dp)
                .background(AppTheme.colors.outlineVariant),
        )
        MissingHighlight(isMissing = state.trailerMissing) {
            VehicleSelector(
                vehicles = content.trailers,
                selectedVehicle = state.selectedTrailer,
                expanded = state.expandedPicker == VehicleType.Trailer,
                onToggle = { onAction(ChecklistAction.TogglePicker(VehicleType.Trailer)) },
                onSelect = { onAction(ChecklistAction.SelectVehicle(it)) },
                testTagPrefix = "checklist_trailer",
                placeholder = stringResource(Res.string.checklist_select_trailer),
                placeholderType = VehicleType.Trailer,
            )
        }
        if (state.truckMissing || state.trailerMissing) {
            Text(
                text = stringResource(Res.string.checklist_error_vehicles_required),
                style = AppTheme.typography.bodySmall,
                color = AppTheme.colors.error,
                modifier = Modifier.padding(start = 8.dp, top = 4.dp),
            )
        }
    }
}

@Composable
private fun MissingHighlight(
    isMissing: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val highlight = AppTheme.colors.errorContainer
    val background by animateColorAsState(if (isMissing) highlight else highlight.copy(alpha = 0f))

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(MissingHighlightShape)
            .background(background, MissingHighlightShape)
            .padding(horizontal = 8.dp),
    ) {
        content()
    }
}

private val MissingHighlightShape = RoundedCornerShape(12.dp)
