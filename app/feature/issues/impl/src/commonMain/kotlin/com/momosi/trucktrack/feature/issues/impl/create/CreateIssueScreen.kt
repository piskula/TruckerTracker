package com.momosi.trucktrack.feature.issues.impl.create

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.momosi.trucktrack.core.common.io.PhotoData
import com.momosi.trucktrack.core.issue.model.IssuePriority
import com.momosi.trucktrack.core.uilibrary.components.Button
import com.momosi.trucktrack.core.uilibrary.components.ButtonRole
import com.momosi.trucktrack.core.uilibrary.components.Icon
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
import com.momosi.trucktrack.feature.issues.impl.SubmitStatus
import com.momosi.trucktrack.feature.issues.impl.VehicleSelector
import com.momosi.trucktrack.feature.issues.impl.VehiclesContent
import com.momosi.trucktrack.feature.issues.impl.navigation.PhotoSource
import com.momosi.trucktrack.feature.issues.impl.resources.Res
import com.momosi.trucktrack.feature.issues.impl.resources.create_issue_add_photos
import com.momosi.trucktrack.feature.issues.impl.resources.create_issue_camera_or_gallery
import com.momosi.trucktrack.feature.issues.impl.resources.create_issue_description
import com.momosi.trucktrack.feature.issues.impl.resources.create_issue_details
import com.momosi.trucktrack.feature.issues.impl.resources.create_issue_error_submission_failed
import com.momosi.trucktrack.feature.issues.impl.resources.create_issue_photos
import com.momosi.trucktrack.feature.issues.impl.resources.create_issue_priority
import com.momosi.trucktrack.feature.issues.impl.resources.create_issue_short_title
import com.momosi.trucktrack.feature.issues.impl.resources.create_issue_submit
import com.momosi.trucktrack.feature.issues.impl.resources.create_issue_title
import com.momosi.trucktrack.feature.issues.impl.resources.create_issue_vehicle
import com.momosi.trucktrack.feature.issues.impl.resources.issue_error_title_required
import com.momosi.trucktrack.feature.issues.impl.resources.issue_error_vehicle_required
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
                    testTagPrefix = "create_issue",
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
                    onValueChange = { onAction(CreateIssueAction.UpdateTitle(it)) },
                    isError = state.titleError,
                    fieldTestTag = "create_issue_title_field",
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
                    onValueChange = { onAction(CreateIssueAction.UpdateDescription(it)) },
                    minLines = 4,
                    fieldTestTag = "create_issue_description_field",
                )
            }

            Card(title = stringResource(Res.string.create_issue_priority)) {
                PrioritySelector(
                    selected = state.selectedPriority,
                    onSelect = { onAction(CreateIssueAction.SelectPriority(it)) },
                    testTagPrefix = "create_issue",
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
                        onPhotoClick = { onNavigateToFullScreenPhoto(PhotoSource.Bytes(it.bytes, it.fileName)) },
                    )
                }
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
                text = stringResource(Res.string.create_issue_submit),
                onClick = { onAction(CreateIssueAction.Submit) },
                enabled = true,
                loading = state.isSubmitting,
                modifier = Modifier.fillMaxWidth().testTag("create_issue_submit_button"),
                role = ButtonRole.Open,
                icon = TruckTrackIcons.RadioButtonUnchecked,
            )
            if (state.submissionFailed) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(Res.string.create_issue_error_submission_failed),
                    style = AppTheme.typography.bodySmall,
                    color = AppTheme.colors.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
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
            .padding(20.dp)
            .testTag("create_issue_add_photo_button"),
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
                        .clickable { onPhotoClick(photo) }
                        .testTag("create_issue_photo_$index"),
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(2.dp)
                        .size(18.dp)
                        .background(AppTheme.colors.outlineVariant, CircleShape)
                        .clickable { onRemove(photo.fileName) }
                        .testTag("create_issue_remove_photo_$index"),
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

@Preview
@Composable
private fun CreateIssueValidationErrorPreview() {
    TruckTrackTheme {
        CreateIssueContent(
            state = CreateIssueState(
                vehicles = VehiclesContent.Loaded(
                    persistentListOf(
                        Vehicle(1, "MA-204-TT", "Volvo", "FH16", VehicleType.Truck),
                    ),
                ),
                submitStatus = SubmitStatus.ValidationError,
            ),
            onAction = {},
            onBack = {},
            onNavigateToFullScreenPhoto = {},
        )
    }
}

// endregion
