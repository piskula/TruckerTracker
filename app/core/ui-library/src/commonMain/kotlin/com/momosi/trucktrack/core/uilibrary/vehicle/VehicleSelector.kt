package com.momosi.trucktrack.core.uilibrary.vehicle

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.momosi.trucktrack.core.uilibrary.components.Icon
import com.momosi.trucktrack.core.uilibrary.components.LoadingSpinner
import com.momosi.trucktrack.core.uilibrary.components.SectionLabel
import com.momosi.trucktrack.core.uilibrary.components.Text
import com.momosi.trucktrack.core.uilibrary.icons.TruckTrackIcons
import com.momosi.trucktrack.core.uilibrary.resources.Res
import com.momosi.trucktrack.core.uilibrary.resources.vehicle_section_all
import com.momosi.trucktrack.core.uilibrary.resources.vehicle_section_recent
import com.momosi.trucktrack.core.uilibrary.resources.vehicle_select
import com.momosi.trucktrack.core.uilibrary.theme.AppTheme
import com.momosi.trucktrack.core.uilibrary.theme.TruckTrackTheme
import com.momosi.trucktrack.core.vehicle.model.Vehicle
import com.momosi.trucktrack.core.vehicle.model.VehicleType
import kotlinx.collections.immutable.persistentListOf
import org.jetbrains.compose.resources.stringResource

@Composable
fun VehicleSelector(
    vehicles: VehiclesContent,
    selectedVehicle: Vehicle?,
    expanded: Boolean,
    onToggle: () -> Unit,
    onSelect: (Vehicle) -> Unit,
    testTagPrefix: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    placeholder: String = stringResource(Res.string.vehicle_select),
    placeholderType: VehicleType? = null,
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
                imageVector = (selectedVehicle?.type ?: placeholderType).vehicleIcon(),
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
                        text = selectedVehicle.displayName(),
                        style = AppTheme.typography.titleSmall,
                        color = AppTheme.colors.onSurface,
                        modifier = Modifier.weight(1f),
                    )
                }

                else -> {
                    Text(
                        text = placeholder,
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
            val preferredVehicles = listOfNotNull(vehicles.preferredTruck, vehicles.preferredTrailer)
            Column {
                if (preferredVehicles.isNotEmpty()) {
                    SectionLabel(
                        text = stringResource(Res.string.vehicle_section_recent),
                        color = AppTheme.colors.primary,
                        modifier = Modifier.testTag("${testTagPrefix}_vehicle_section_recent"),
                    )
                }
                preferredVehicles.forEach { vehicle ->
                    VehicleOption(
                        vehicle = vehicle,
                        onSelect = onSelect,
                        testTag = "${testTagPrefix}_vehicle_preferred_${vehicle.id}",
                    )
                }
                if (preferredVehicles.isNotEmpty() && vehicles.otherVehicles.isNotEmpty()) {
                    SectionLabel(
                        text = stringResource(Res.string.vehicle_section_all),
                        color = AppTheme.colors.primary,
                        modifier = Modifier.padding(top = 8.dp).testTag("${testTagPrefix}_vehicle_section_all"),
                    )
                }
                vehicles.otherVehicles.forEach { vehicle ->
                    VehicleOption(
                        vehicle = vehicle,
                        onSelect = onSelect,
                        testTag = "${testTagPrefix}_vehicle_option_${vehicle.id}",
                    )
                }
            }
        }
    }
}

@Composable
private fun VehicleOption(
    vehicle: Vehicle,
    onSelect: (Vehicle) -> Unit,
    testTag: String,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clickable { onSelect(vehicle) }
            .padding(vertical = 10.dp, horizontal = 4.dp)
            .testTag(testTag),
    ) {
        Icon(
            imageVector = vehicle.type.vehicleIcon(),
            tint = AppTheme.colors.onSurfaceVariant,
            modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = vehicle.displayName(),
            style = AppTheme.typography.bodyMedium,
            color = AppTheme.colors.onSurface,
        )
    }
}

private fun Vehicle.displayName(): String = "$licensePlate · $make $model"

fun VehicleType?.vehicleIcon() = when (this) {
    VehicleType.Trailer -> TruckTrackIcons.Trailer
    VehicleType.Truck, null -> TruckTrackIcons.Truck
}

@Preview
@Composable
private fun VehicleSelectorCollapsedPreview() {
    TruckTrackTheme {
        VehicleSelector(
            vehicles = VehiclesContent.Loaded(),
            selectedVehicle = Vehicle(1, "MA-204-TT", "Volvo", "FH16", VehicleType.Truck),
            expanded = false,
            onToggle = {},
            onSelect = {},
            testTagPrefix = "preview",
        )
    }
}

@Preview
@Composable
private fun VehicleSelectorExpandedPreview() {
    TruckTrackTheme {
        VehicleSelector(
            vehicles = VehiclesContent.Loaded(
                preferredTrailer = Vehicle(3, "MA-330-CD", "Schmitz", "S.KO", VehicleType.Trailer),
                otherVehicles = persistentListOf(
                    Vehicle(4, "MA-402-EF", "Krone", "SD", VehicleType.Trailer),
                ),
            ),
            selectedVehicle = null,
            expanded = true,
            onToggle = {},
            onSelect = {},
            testTagPrefix = "preview",
            placeholder = "Select a trailer",
            placeholderType = VehicleType.Trailer,
        )
    }
}
