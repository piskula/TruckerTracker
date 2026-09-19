package com.momosi.trucktrack.feature.issues.impl.detail.startworking

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.momosi.trucktrack.core.issue.model.Issue
import com.momosi.trucktrack.core.issue.model.RepairType
import com.momosi.trucktrack.core.issue.model.VehicleSystem
import com.momosi.trucktrack.core.uilibrary.components.BottomSheet
import com.momosi.trucktrack.core.uilibrary.components.Button
import com.momosi.trucktrack.core.uilibrary.components.ButtonRole
import com.momosi.trucktrack.core.uilibrary.components.Icon
import com.momosi.trucktrack.core.uilibrary.components.Text
import com.momosi.trucktrack.core.uilibrary.icons.TruckTrackIcons
import com.momosi.trucktrack.core.uilibrary.theme.AppTheme
import com.momosi.trucktrack.core.uilibrary.theme.TruckTrackTheme
import com.momosi.trucktrack.feature.issues.impl.resources.Res
import com.momosi.trucktrack.feature.issues.impl.resources.issue_detail_start_working
import com.momosi.trucktrack.feature.issues.impl.resources.issue_error_repair_type_required
import com.momosi.trucktrack.feature.issues.impl.resources.issue_error_vehicle_system_required
import com.momosi.trucktrack.feature.issues.impl.resources.issue_field_repair_type
import com.momosi.trucktrack.feature.issues.impl.resources.issue_field_vehicle_system
import com.momosi.trucktrack.feature.issues.impl.resources.repair_type_damage
import com.momosi.trucktrack.feature.issues.impl.resources.repair_type_fault
import com.momosi.trucktrack.feature.issues.impl.resources.repair_type_installation
import com.momosi.trucktrack.feature.issues.impl.resources.vehicle_system_air
import com.momosi.trucktrack.feature.issues.impl.resources.vehicle_system_body
import com.momosi.trucktrack.feature.issues.impl.resources.vehicle_system_brakes
import com.momosi.trucktrack.feature.issues.impl.resources.vehicle_system_cooling
import com.momosi.trucktrack.feature.issues.impl.resources.vehicle_system_drivetrain
import com.momosi.trucktrack.feature.issues.impl.resources.vehicle_system_electrical
import com.momosi.trucktrack.feature.issues.impl.resources.vehicle_system_engine
import com.momosi.trucktrack.feature.issues.impl.resources.vehicle_system_other
import com.momosi.trucktrack.feature.issues.impl.resources.vehicle_system_tarp
import com.momosi.trucktrack.feature.issues.impl.resources.vehicle_system_tires
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
internal fun StartWorkingSheet(
    issueId: Long,
    onIssueStart: (Issue) -> Unit,
    onDismiss: () -> Unit,
    viewModel: StartWorkingViewModel = koinViewModel(parameters = { parametersOf(issueId) }),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is StartWorkingEvent.Started -> onIssueStart(event.issue)
            }
        }
    }

    BottomSheet(onDismiss = onDismiss) {
        StartWorkingSheetContent(
            state = state,
            onSelectRepairType = { viewModel.onAction(StartWorkingAction.SelectRepairType(it)) },
            onSelectVehicleSystem = { viewModel.onAction(StartWorkingAction.SelectVehicleSystem(it)) },
            onConfirm = { viewModel.onAction(StartWorkingAction.Confirm) },
        )
    }
}

@Composable
private fun StartWorkingSheetContent(
    state: StartWorkingState,
    onSelectRepairType: (RepairType) -> Unit,
    onSelectVehicleSystem: (VehicleSystem) -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .weight(1f, fill = false)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(Res.string.issue_field_repair_type),
                style = AppTheme.typography.titleMedium,
                color = AppTheme.colors.onSurface,
            )
            RadioOptionList(
                items = RepairType.entries,
                selected = state.selectedRepairType,
                labelSelector = { it.displayName() },
                onSelect = onSelectRepairType,
                testTagPrefix = "start_working_repair_type",
            )
            if (state.repairTypeError) {
                Text(
                    text = stringResource(Res.string.issue_error_repair_type_required),
                    style = AppTheme.typography.bodySmall,
                    color = AppTheme.colors.error,
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(Res.string.issue_field_vehicle_system),
                style = AppTheme.typography.titleMedium,
                color = AppTheme.colors.onSurface,
            )
            RadioOptionList(
                items = VehicleSystem.entries,
                selected = state.selectedVehicleSystem,
                labelSelector = { it.displayName() },
                onSelect = onSelectVehicleSystem,
                testTagPrefix = "start_working_vehicle_system",
            )
            if (state.vehicleSystemError) {
                Text(
                    text = stringResource(Res.string.issue_error_vehicle_system_required),
                    style = AppTheme.typography.bodySmall,
                    color = AppTheme.colors.error,
                )
            }
        }
        Button(
            text = stringResource(Res.string.issue_detail_start_working),
            onClick = onConfirm,
            enabled = true,
            loading = state.isSubmitting,
            icon = TruckTrackIcons.Build,
            role = ButtonRole.Warning,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp, bottom = 16.dp)
                .navigationBarsPadding()
                .testTag("start_working_confirm_button"),
        )
    }
}

@Composable
private fun <T> RadioOptionList(
    items: List<T>,
    selected: T?,
    labelSelector: @Composable (T) -> String,
    onSelect: (T) -> Unit,
    testTagPrefix: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        items.forEach { item ->
            val isSelected = item == selected
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelect(item) }
                    .padding(vertical = 10.dp)
                    .testTag("${testTagPrefix}_option_$item"),
            ) {
                Icon(
                    imageVector = if (isSelected) TruckTrackIcons.CheckCircle else TruckTrackIcons.RadioButtonUnchecked,
                    tint = if (isSelected) AppTheme.colors.primary else AppTheme.colors.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = labelSelector(item),
                    style = AppTheme.typography.bodyMedium,
                    color = AppTheme.colors.onSurface,
                )
            }
        }
    }
}

@Composable
private fun RepairType.displayName(): String = stringResource(
    when (this) {
        RepairType.Damage -> Res.string.repair_type_damage
        RepairType.Fault -> Res.string.repair_type_fault
        RepairType.Installation -> Res.string.repair_type_installation
    },
)

@Composable
private fun VehicleSystem.displayName(): String = stringResource(
    when (this) {
        VehicleSystem.Electrical -> Res.string.vehicle_system_electrical
        VehicleSystem.Tires -> Res.string.vehicle_system_tires
        VehicleSystem.Body -> Res.string.vehicle_system_body
        VehicleSystem.Engine -> Res.string.vehicle_system_engine
        VehicleSystem.Drivetrain -> Res.string.vehicle_system_drivetrain
        VehicleSystem.Brakes -> Res.string.vehicle_system_brakes
        VehicleSystem.Air -> Res.string.vehicle_system_air
        VehicleSystem.Tarp -> Res.string.vehicle_system_tarp
        VehicleSystem.Cooling -> Res.string.vehicle_system_cooling
        VehicleSystem.Other -> Res.string.vehicle_system_other
    },
)

@Preview
@Composable
private fun StartWorkingSheetEmptySelectionPreview() {
    TruckTrackTheme {
        Box(modifier = Modifier.fillMaxWidth().background(AppTheme.colors.surfaceContainerLowest)) {
            StartWorkingSheetContent(
                state = StartWorkingState(),
                onSelectRepairType = {},
                onSelectVehicleSystem = {},
                onConfirm = {},
            )
        }
    }
}

@Preview
@Composable
private fun StartWorkingSheetValidationErrorPreview() {
    TruckTrackTheme {
        Box(modifier = Modifier.fillMaxWidth().background(AppTheme.colors.surfaceContainerLowest)) {
            StartWorkingSheetContent(
                state = StartWorkingState(showValidationErrors = true),
                onSelectRepairType = {},
                onSelectVehicleSystem = {},
                onConfirm = {},
            )
        }
    }
}

@Preview
@Composable
private fun StartWorkingSheetReadyToConfirmPreview() {
    TruckTrackTheme {
        Box(modifier = Modifier.fillMaxWidth().background(AppTheme.colors.surfaceContainerLowest)) {
            StartWorkingSheetContent(
                state = StartWorkingState(
                    selectedRepairType = RepairType.Damage,
                    selectedVehicleSystem = VehicleSystem.Engine,
                ),
                onSelectRepairType = {},
                onSelectVehicleSystem = {},
                onConfirm = {},
            )
        }
    }
}
