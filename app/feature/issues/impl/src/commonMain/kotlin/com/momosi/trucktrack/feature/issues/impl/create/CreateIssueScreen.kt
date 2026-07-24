package com.momosi.trucktrack.feature.issues.impl.create

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.momosi.trucktrack.core.common.io.PhotoData
import com.momosi.trucktrack.core.issue.model.IssuePriority
import com.momosi.trucktrack.core.uilibrary.components.Button
import com.momosi.trucktrack.core.uilibrary.components.ButtonRole
import com.momosi.trucktrack.core.uilibrary.components.Icon
import com.momosi.trucktrack.core.uilibrary.components.LoadingSpinner
import com.momosi.trucktrack.core.uilibrary.components.Text
import com.momosi.trucktrack.core.uilibrary.components.Toolbar
import com.momosi.trucktrack.core.uilibrary.icons.TruckTrackIcons
import com.momosi.trucktrack.core.uilibrary.theme.AppTheme
import com.momosi.trucktrack.core.uilibrary.theme.Shapes
import com.momosi.trucktrack.core.uilibrary.theme.TruckTrackTheme
import com.momosi.trucktrack.core.vehicle.model.Vehicle
import com.momosi.trucktrack.core.vehicle.model.VehicleType
import com.momosi.trucktrack.feature.issues.impl.navigation.PhotoSource
import com.momosi.trucktrack.feature.issues.impl.resources.Res
import com.momosi.trucktrack.feature.issues.impl.resources.create_issue_add_photos
import com.momosi.trucktrack.feature.issues.impl.resources.create_issue_camera_or_gallery
import com.momosi.trucktrack.feature.issues.impl.resources.create_issue_description
import com.momosi.trucktrack.feature.issues.impl.resources.create_issue_details
import com.momosi.trucktrack.feature.issues.impl.resources.create_issue_photos
import com.momosi.trucktrack.feature.issues.impl.resources.create_issue_priority
import com.momosi.trucktrack.feature.issues.impl.resources.create_issue_priority_high_hint
import com.momosi.trucktrack.feature.issues.impl.resources.create_issue_priority_low_hint
import com.momosi.trucktrack.feature.issues.impl.resources.create_issue_priority_medium_hint
import com.momosi.trucktrack.feature.issues.impl.resources.create_issue_select_vehicle
import com.momosi.trucktrack.feature.issues.impl.resources.create_issue_short_title
import com.momosi.trucktrack.feature.issues.impl.resources.create_issue_submit
import com.momosi.trucktrack.feature.issues.impl.resources.create_issue_title
import com.momosi.trucktrack.feature.issues.impl.resources.create_issue_vehicle
import com.momosi.trucktrack.feature.issues.impl.resources.issue_priority_high
import com.momosi.trucktrack.feature.issues.impl.resources.issue_priority_low
import com.momosi.trucktrack.feature.issues.impl.resources.issue_priority_medium
import io.github.vinceglb.filekit.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.core.PickerMode
import io.github.vinceglb.filekit.core.PickerType
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun CreateIssueScreen(
    onBack: () -> Unit,
    onIssueCreate: (Long) -> Unit,
    onNavigateToFullScreenPhoto: (PhotoSource) -> Unit,
    viewModel: CreateIssueViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is CreateIssueEvent.IssueCreated -> onIssueCreate(event.issueId)
                is CreateIssueEvent.CreationFailed -> Unit
            }
        }
    }

    CreateIssueContent(
        state = state,
        onAction = viewModel::onAction,
        onBack = onBack,
        onNavigateToFullScreenPhoto = onNavigateToFullScreenPhoto,
    )
}

@Composable
private fun CreateIssueContent(
    state: CreateIssueState,
    onAction: (CreateIssueAction) -> Unit,
    onBack: () -> Unit,
    onNavigateToFullScreenPhoto: (PhotoSource) -> Unit,
    modifier: Modifier = Modifier,
) {
    val photoPickerLauncher = rememberFilePickerLauncher(
        type = PickerType.Image,
        mode = PickerMode.Multiple(),
        onResult = { files -> if (!files.isNullOrEmpty()) onAction(CreateIssueAction.AddPhotos(files)) },
    )

    Column(modifier = modifier.fillMaxSize().background(AppTheme.colors.surfaceContainer)) {
        Toolbar(title = stringResource(Res.string.create_issue_title), onBack = onBack)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Card(title = stringResource(Res.string.create_issue_vehicle)) {
                VehicleSelector(
                    vehicles = state.vehicles,
                    selectedVehicle = state.selectedVehicle,
                    expanded = state.vehicleDropdownExpanded,
                    onToggle = { onAction(CreateIssueAction.ToggleVehicleDropdown) },
                    onSelect = { onAction(CreateIssueAction.SelectVehicle(it)) },
                )
            }

            Card(title = stringResource(Res.string.create_issue_description)) {
                InputField(
                    label = stringResource(Res.string.create_issue_short_title),
                    value = state.title,
                    onValueChange = { onAction(CreateIssueAction.UpdateTitle(it)) },
                )
                Spacer(modifier = Modifier.height(16.dp))
                InputField(
                    label = stringResource(Res.string.create_issue_details),
                    value = state.description,
                    onValueChange = { onAction(CreateIssueAction.UpdateDescription(it)) },
                    minLines = 4,
                )
            }

            Card(title = stringResource(Res.string.create_issue_priority)) {
                PrioritySelector(
                    selected = state.selectedPriority,
                    onSelect = { onAction(CreateIssueAction.SelectPriority(it)) },
                )
            }

            Card(title = stringResource(Res.string.create_issue_photos)) {
                PhotoUploadArea(
                    onClick = { photoPickerLauncher.launch() },
                )
                if (state.photos.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    PhotoPreviews(
                        photos = state.photos,
                        onRemove = { onAction(CreateIssueAction.RemovePhoto(it)) },
                        onPhotoClick = { onNavigateToFullScreenPhoto(PhotoSource.Bytes(it.bytes)) },
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppTheme.colors.surfaceContainerLowest)
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Button(
                text = stringResource(Res.string.create_issue_submit),
                onClick = { onAction(CreateIssueAction.Submit) },
                enabled = state.isSubmitEnabled,
                loading = state.isSubmitting,
                modifier = Modifier.fillMaxWidth(),
                role = ButtonRole.Open,
                icon = TruckTrackIcons.RadioButtonUnchecked,
            )
        }
    }
}

@Composable
private fun Card(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(Shapes.CardShape)
            .background(AppTheme.colors.surfaceContainerLowest, Shapes.CardShape)
            .padding(16.dp),
    ) {
        Text(
            text = title.uppercase(),
            style = AppTheme.typography.labelSmall,
            color = AppTheme.colors.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 12.dp),
        )
        content()
    }
}

@Composable
private fun VehicleSelector(
    vehicles: VehiclesContent,
    selectedVehicle: Vehicle?,
    expanded: Boolean,
    onToggle: () -> Unit,
    onSelect: (Vehicle) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggle)
                .padding(vertical = 8.dp),
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
                        text = stringResource(Res.string.create_issue_select_vehicle),
                        style = AppTheme.typography.titleSmall,
                        color = AppTheme.colors.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            Icon(
                imageVector = TruckTrackIcons.ArrowDropDown,
                tint = AppTheme.colors.primary,
                modifier = Modifier.size(24.dp),
            )
        }
        if (expanded && vehicles is VehiclesContent.Loaded) {
            Column {
                vehicles.vehicles.forEach { vehicle ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(vehicle) }
                            .padding(vertical = 10.dp, horizontal = 4.dp),
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
private fun InputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    minLines: Int = 1,
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = AppTheme.typography.bodySmall,
            color = AppTheme.colors.primary,
        )
        androidx.compose.foundation.text.BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = AppTheme.typography.bodyLarge.copy(color = AppTheme.colors.onSurface),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            minLines = minLines,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            decorationBox = { innerTextField ->
                Column {
                    innerTextField()
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .background(
                                if (value.isNotEmpty()) AppTheme.colors.primary else AppTheme.colors.surfaceVariant,
                            ),
                    )
                }
            },
        )
    }
}

@Composable
private fun PrioritySelector(
    selected: IssuePriority,
    onSelect: (IssuePriority) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        PriorityOption(
            priority = IssuePriority.High,
            label = stringResource(Res.string.issue_priority_high),
            subtitle = stringResource(Res.string.create_issue_priority_high_hint),
            isSelected = selected == IssuePriority.High,
            onClick = { onSelect(IssuePriority.High) },
        )
        PriorityOption(
            priority = IssuePriority.Medium,
            label = stringResource(Res.string.issue_priority_medium),
            subtitle = stringResource(Res.string.create_issue_priority_medium_hint),
            isSelected = selected == IssuePriority.Medium,
            onClick = { onSelect(IssuePriority.Medium) },
        )
        PriorityOption(
            priority = IssuePriority.Low,
            label = stringResource(Res.string.issue_priority_low),
            subtitle = stringResource(Res.string.create_issue_priority_low_hint),
            isSelected = selected == IssuePriority.Low,
            onClick = { onSelect(IssuePriority.Low) },
        )
    }
}

@Composable
private fun PriorityOption(
    priority: IssuePriority,
    label: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderColor = if (isSelected) priority.accentColor() else AppTheme.colors.surfaceVariant
    val backgroundColor = if (isSelected) priority.containerColor() else AppTheme.colors.surfaceContainerLowest
    val textColor = if (isSelected) priority.onContainerColor() else AppTheme.colors.onSurface
    val subtitleColor = if (isSelected) priority.onContainerColor() else AppTheme.colors.onSurfaceVariant

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .border(2.dp, borderColor, RoundedCornerShape(10.dp))
            .background(backgroundColor, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
    ) {
        Icon(
            imageVector = priority.icon(),
            tint = if (isSelected) textColor else priority.accentColor(),
            modifier = Modifier.size(20.dp),
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(text = label, style = AppTheme.typography.titleSmall, color = textColor)
            Text(text = subtitle, style = AppTheme.typography.labelSmall, color = subtitleColor)
        }
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

@Composable
private fun PhotoUploadArea(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(2.dp, AppTheme.colors.surfaceVariant, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(20.dp),
    ) {
        Icon(
            imageVector = TruckTrackIcons.AddPhoto,
            tint = AppTheme.colors.surfaceVariant,
            modifier = Modifier.size(36.dp),
        )
        Text(
            text = stringResource(Res.string.create_issue_add_photos),
            style = AppTheme.typography.bodyMedium,
            color = AppTheme.colors.onSurfaceVariant,
        )
        Text(
            text = stringResource(Res.string.create_issue_camera_or_gallery),
            style = AppTheme.typography.bodySmall,
            color = AppTheme.colors.surfaceVariant,
        )
    }
}

@Composable
private fun PhotoPreviews(
    photos: ImmutableList<PhotoData>,
    onRemove: (String) -> Unit,
    onPhotoClick: (PhotoData) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(0.dp),
    ) {
        items(count = photos.size, key = { photos[it].fileName }) { index ->
            val photo = photos[index]
            Box(modifier = Modifier.size(72.dp)) {
                AsyncImage(
                    model = photo.bytes,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onPhotoClick(photo) },
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(2.dp)
                        .size(18.dp)
                        .background(AppTheme.colors.outlineVariant, CircleShape)
                        .clickable { onRemove(photo.fileName) },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = TruckTrackIcons.Close,
                        tint = AppTheme.colors.surface,
                        modifier = Modifier.size(12.dp),
                    )
                }
            }
        }
    }
}

// region Previews

private fun VehicleType?.vehicleIcon() = when (this) {
    VehicleType.Trailer -> TruckTrackIcons.Trailer
    VehicleType.Truck, null -> TruckTrackIcons.Truck
}

@Preview
@Composable
private fun CreateIssuePreview() {
    TruckTrackTheme {
        CreateIssueContent(
            state = CreateIssueState(
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
            onNavigateToFullScreenPhoto = {},
        )
    }
}

@Preview
@Composable
private fun CreateIssueEmptyPreview() {
    TruckTrackTheme {
        CreateIssueContent(
            state = CreateIssueState(),
            onAction = {},
            onBack = {},
            onNavigateToFullScreenPhoto = {},
        )
    }
}

// endregion
