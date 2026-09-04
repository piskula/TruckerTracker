package com.momosi.trucktrack.feature.issues.impl.edit

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.momosi.trucktrack.core.issue.model.EditingCapabilities
import com.momosi.trucktrack.core.issue.model.IssuePriority
import com.momosi.trucktrack.core.uilibrary.components.Button
import com.momosi.trucktrack.core.uilibrary.components.ButtonRole
import com.momosi.trucktrack.core.uilibrary.components.LoadingSpinner
import com.momosi.trucktrack.core.uilibrary.components.Text
import com.momosi.trucktrack.core.uilibrary.components.Toolbar
import com.momosi.trucktrack.core.uilibrary.icons.TruckTrackIcons
import com.momosi.trucktrack.core.uilibrary.theme.AppTheme
import com.momosi.trucktrack.core.uilibrary.theme.TruckTrackTheme
import com.momosi.trucktrack.core.vehicle.model.Vehicle
import com.momosi.trucktrack.core.vehicle.model.VehicleType
import com.momosi.trucktrack.feature.issues.impl.Card
import com.momosi.trucktrack.feature.issues.impl.InputField
import com.momosi.trucktrack.feature.issues.impl.PrioritySelector
import com.momosi.trucktrack.feature.issues.impl.VehicleSelector
import com.momosi.trucktrack.feature.issues.impl.VehiclesContent
import com.momosi.trucktrack.feature.issues.impl.resources.Res
import com.momosi.trucktrack.feature.issues.impl.resources.create_issue_description
import com.momosi.trucktrack.feature.issues.impl.resources.create_issue_details
import com.momosi.trucktrack.feature.issues.impl.resources.create_issue_priority
import com.momosi.trucktrack.feature.issues.impl.resources.create_issue_short_title
import com.momosi.trucktrack.feature.issues.impl.resources.create_issue_vehicle
import com.momosi.trucktrack.feature.issues.impl.resources.edit_issue_error
import com.momosi.trucktrack.feature.issues.impl.resources.edit_issue_error_submission_failed
import com.momosi.trucktrack.feature.issues.impl.resources.edit_issue_submit
import com.momosi.trucktrack.feature.issues.impl.resources.edit_issue_title
import com.momosi.trucktrack.feature.issues.impl.resources.issue_error_title_required
import com.momosi.trucktrack.feature.issues.impl.resources.issue_error_vehicle_required
import com.momosi.trucktrack.feature.issues.impl.resources.my_issues_retry
import kotlinx.collections.immutable.persistentListOf
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
internal fun EditIssueScreen(
    issueId: Long,
    onBack: () -> Unit,
    onIssueUpdate: () -> Unit,
    viewModel: EditIssueViewModel = koinViewModel(parameters = { parametersOf(issueId) }),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is EditIssueEvent.IssueUpdated -> onIssueUpdate()
            }
        }
    }

    EditIssueContent(
        state = state,
        onAction = viewModel::onAction,
        onBack = onBack,
    )
}

@Composable
private fun EditIssueContent(
    state: EditIssueState,
    onAction: (EditIssueAction) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize().background(AppTheme.colors.surfaceContainer)) {
        Toolbar(title = stringResource(Res.string.edit_issue_title), onBack = onBack)

        Crossfade(targetState = state.loadState, modifier = Modifier.weight(1f)) { loadState ->
            when (loadState) {
                is EditIssueLoadState.Loading -> LoadingSpinner(modifier = Modifier.fillMaxSize())

                is EditIssueLoadState.Error -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = stringResource(Res.string.edit_issue_error),
                            style = AppTheme.typography.bodyLarge,
                            color = AppTheme.colors.onSurface,
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            text = stringResource(Res.string.my_issues_retry),
                            onClick = { onAction(EditIssueAction.Retry) },
                            modifier = Modifier.testTag("edit_issue_retry_button"),
                        )
                    }
                }

                is EditIssueLoadState.Ready -> EditIssueForm(
                    state = state,
                    editing = loadState.editing,
                    onAction = onAction,
                )
            }
        }
    }
}

@Composable
private fun EditIssueForm(
    state: EditIssueState,
    editing: EditingCapabilities,
    onAction: (EditIssueAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Card(title = stringResource(Res.string.create_issue_vehicle), locked = !editing.canEditVehicle) {
                VehicleSelector(
                    vehicles = state.vehicles,
                    selectedVehicle = state.selectedVehicle,
                    expanded = state.vehicleDropdownExpanded,
                    onToggle = { onAction(EditIssueAction.ToggleVehicleDropdown) },
                    onSelect = { onAction(EditIssueAction.SelectVehicle(it)) },
                    testTagPrefix = "edit_issue",
                    enabled = editing.canEditVehicle,
                )
                if (state.vehicleError) {
                    Text(
                        text = stringResource(Res.string.issue_error_vehicle_required),
                        style = AppTheme.typography.bodySmall,
                        color = AppTheme.colors.error,
                    )
                }
            }

            Card(title = stringResource(Res.string.create_issue_description)) {
                InputField(
                    label = stringResource(Res.string.create_issue_short_title),
                    value = state.title,
                    onValueChange = { onAction(EditIssueAction.UpdateTitle(it)) },
                    isError = state.titleError,
                    fieldTestTag = "edit_issue_title_field",
                    enabled = editing.canEditTitle,
                    locked = !editing.canEditTitle,
                )
                if (state.titleError) {
                    Text(
                        text = stringResource(Res.string.issue_error_title_required),
                        style = AppTheme.typography.bodySmall,
                        color = AppTheme.colors.error,
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                InputField(
                    label = stringResource(Res.string.create_issue_details),
                    value = state.description,
                    onValueChange = { onAction(EditIssueAction.UpdateDescription(it)) },
                    minLines = 4,
                    fieldTestTag = "edit_issue_description_field",
                    enabled = editing.canEditDescription,
                    locked = !editing.canEditDescription,
                )
            }

            Card(title = stringResource(Res.string.create_issue_priority), locked = !editing.canEditPriority) {
                PrioritySelector(
                    selected = state.selectedPriority,
                    onSelect = { onAction(EditIssueAction.SelectPriority(it)) },
                    testTagPrefix = "edit_issue",
                    enabled = editing.canEditPriority,
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppTheme.colors.surfaceContainerLowest)
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Button(
                text = stringResource(Res.string.edit_issue_submit),
                onClick = { onAction(EditIssueAction.Submit) },
                enabled = true,
                loading = state.isSubmitting,
                modifier = Modifier.fillMaxWidth().testTag("edit_issue_submit_button"),
                role = ButtonRole.Open,
                icon = TruckTrackIcons.RadioButtonUnchecked,
            )
            if (state.submissionFailed) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(Res.string.edit_issue_error_submission_failed),
                    style = AppTheme.typography.bodySmall,
                    color = AppTheme.colors.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

// region Previews

@Preview
@Composable
private fun EditIssuePreview() {
    TruckTrackTheme {
        EditIssueContent(
            state = EditIssueState(
                loadState = EditIssueLoadState.Ready(
                    EditingCapabilities(
                        canEditTitle = true,
                        canEditDescription = true,
                        canEditPriority = true,
                        canEditVehicle = true,
                    ),
                ),
                vehicles = VehiclesContent.Loaded(
                    persistentListOf(
                        Vehicle(1, "MA-204-TT", "Volvo", "FH16", VehicleType.Truck),
                        Vehicle(2, "MA-118-AB", "DAF", "XF", VehicleType.Truck),
                    ),
                ),
                selectedVehicle = Vehicle(1, "MA-204-TT", "Volvo", "FH16", VehicleType.Truck),
                title = "Engine warning light — won't start",
                description = "Tried to start the engine this morning and the warning light came on.",
                selectedPriority = IssuePriority.High,
            ),
            onAction = {},
            onBack = {},
        )
    }
}

@Preview
@Composable
private fun EditIssueVehicleOnlyPreview() {
    TruckTrackTheme {
        EditIssueContent(
            state = EditIssueState(
                loadState = EditIssueLoadState.Ready(
                    EditingCapabilities(
                        canEditTitle = false,
                        canEditDescription = false,
                        canEditPriority = false,
                        canEditVehicle = true,
                    ),
                ),
                vehicles = VehiclesContent.Loaded(
                    persistentListOf(
                        Vehicle(1, "MA-204-TT", "Volvo", "FH16", VehicleType.Truck),
                    ),
                ),
                selectedVehicle = Vehicle(1, "MA-204-TT", "Volvo", "FH16", VehicleType.Truck),
                title = "Engine warning light — won't start",
                description = "Tried to start the engine this morning and the warning light came on.",
                selectedPriority = IssuePriority.High,
            ),
            onAction = {},
            onBack = {},
        )
    }
}

@Preview
@Composable
private fun EditIssueLoadingPreview() {
    TruckTrackTheme {
        EditIssueContent(
            state = EditIssueState(),
            onAction = {},
            onBack = {},
        )
    }
}

// endregion
