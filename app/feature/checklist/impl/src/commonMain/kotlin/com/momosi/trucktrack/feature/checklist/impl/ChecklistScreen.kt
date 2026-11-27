package com.momosi.trucktrack.feature.checklist.impl

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.momosi.trucktrack.core.common.formatter.DateFormatter
import com.momosi.trucktrack.core.common.network.FailureReason
import com.momosi.trucktrack.core.uilibrary.BackHandler
import com.momosi.trucktrack.core.uilibrary.components.ButtonRole
import com.momosi.trucktrack.core.uilibrary.components.ConfirmationDialog
import com.momosi.trucktrack.core.uilibrary.components.ErrorState
import com.momosi.trucktrack.core.uilibrary.components.SectionLabel
import com.momosi.trucktrack.core.uilibrary.components.SkeletonBox
import com.momosi.trucktrack.core.uilibrary.components.Toolbar
import com.momosi.trucktrack.core.uilibrary.lazylist.itemsPositioned
import com.momosi.trucktrack.core.uilibrary.modifier.ShimmerGroup
import com.momosi.trucktrack.core.uilibrary.theme.AppTheme
import com.momosi.trucktrack.core.uilibrary.theme.Shapes
import com.momosi.trucktrack.core.uilibrary.theme.TruckTrackTheme
import com.momosi.trucktrack.core.uilibrary.vehicle.VehiclesContent
import com.momosi.trucktrack.core.vehicle.model.Vehicle
import com.momosi.trucktrack.core.vehicle.model.VehicleType
import com.momosi.trucktrack.feature.checklist.impl.resources.Res
import com.momosi.trucktrack.feature.checklist.impl.resources.checklist_checks
import com.momosi.trucktrack.feature.checklist.impl.resources.checklist_completed_next
import com.momosi.trucktrack.feature.checklist.impl.resources.checklist_completed_title
import com.momosi.trucktrack.feature.checklist.impl.resources.checklist_discard_confirm
import com.momosi.trucktrack.feature.checklist.impl.resources.checklist_discard_keep
import com.momosi.trucktrack.feature.checklist.impl.resources.checklist_discard_message
import com.momosi.trucktrack.feature.checklist.impl.resources.checklist_discard_title
import com.momosi.trucktrack.feature.checklist.impl.resources.checklist_error_load
import com.momosi.trucktrack.feature.checklist.impl.resources.checklist_submitted_title
import com.momosi.trucktrack.feature.checklist.impl.resources.checklist_title
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

private enum class LeadingItem { Vehicles, ChecksLabel }
private val SKELETON_CHECKS_HEIGHT = 420.dp

@Composable
internal fun ChecklistScreen(
    onClose: () -> Unit,
    viewModel: ChecklistViewModel = koinViewModel(),
    dateFormatter: DateFormatter = koinInject(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                ChecklistEvent.Close -> onClose()
                is ChecklistEvent.ScrollToMissing -> listState.animateScrollToItem(event.target.listIndex())
            }
        }
    }

    BackHandler(enabled = true) { viewModel.onAction(ChecklistAction.RequestClose) }

    ChecklistScreenContent(
        state = state,
        listState = listState,
        dateFormatter = dateFormatter,
        onAction = viewModel::onAction,
        onClose = onClose,
    )
}

private fun MissingTarget.listIndex(): Int = when (this) {
    MissingTarget.Vehicles -> LeadingItem.Vehicles.ordinal
    is MissingTarget.Check -> LeadingItem.entries.size + questionIndex
}

@Composable
private fun ChecklistScreenContent(
    state: ChecklistState,
    listState: LazyListState,
    dateFormatter: DateFormatter,
    onAction: (ChecklistAction) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val content = state.content
    val phase = state.phase

    Column(modifier = modifier.fillMaxSize().background(AppTheme.colors.surfaceContainer)) {
        Toolbar(
            title = stringResource(Res.string.checklist_title),
            onBack = { onAction(ChecklistAction.RequestClose) },
        )
        AnimatedContent(
            targetState = phase,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            modifier = Modifier.weight(1f),
        ) { targetPhase ->
            when (targetPhase) {
                ChecklistPhase.Loading -> ChecklistSkeleton()

                ChecklistPhase.Error -> ErrorState(
                    reason = (content as? ChecklistContent.Error)?.reason ?: FailureReason.Other,
                    message = stringResource(Res.string.checklist_error_load),
                    onRetry = { onAction(ChecklistAction.Retry) },
                    retryButtonModifier = Modifier.testTag("checklist_retry_button"),
                )

                ChecklistPhase.Form -> if (content is ChecklistContent.Loaded) {
                    ChecklistForm(state = state, content = content, listState = listState, onAction = onAction)
                }

                ChecklistPhase.Submitted -> (state.submitStatus as? ChecklistSubmitStatus.Submitted)?.let { submitted ->
                    ChecklistSubmittedContent(
                        title = stringResource(Res.string.checklist_submitted_title),
                        summary = submitted.summary,
                        dateFormatter = dateFormatter,
                        onDone = onClose,
                    )
                }

                ChecklistPhase.AlreadyCompleted -> (content as? ChecklistContent.AlreadyCompleted)?.let { completed ->
                    ChecklistSubmittedContent(
                        title = stringResource(Res.string.checklist_completed_title),
                        note = stringResource(Res.string.checklist_completed_next),
                        summary = completed.summary,
                        dateFormatter = dateFormatter,
                        onDone = onClose,
                    )
                }
            }
        }
    }

    if (state.isDiscardDialogVisible) {
        ConfirmationDialog(
            title = stringResource(Res.string.checklist_discard_title),
            message = stringResource(Res.string.checklist_discard_message),
            confirmText = stringResource(Res.string.checklist_discard_confirm),
            dismissText = stringResource(Res.string.checklist_discard_keep),
            onConfirm = { onAction(ChecklistAction.ConfirmDiscard) },
            onDismiss = { onAction(ChecklistAction.DismissDiscard) },
            confirmButtonRole = ButtonRole.Warning,
        )
    }
}

@Composable
private fun ChecklistForm(
    state: ChecklistState,
    content: ChecklistContent.Loaded,
    listState: LazyListState,
    onAction: (ChecklistAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(16.dp),
            modifier = Modifier.weight(1f).testTag("checklist_list"),
        ) {
            LeadingItem.entries.forEach { leadingItem ->
                item(key = leadingItem) {
                    when (leadingItem) {
                        LeadingItem.Vehicles -> ChecklistVehiclesCard(state = state, content = content, onAction = onAction)

                        LeadingItem.ChecksLabel -> SectionLabel(
                            text = stringResource(Res.string.checklist_checks),
                            modifier = Modifier.padding(top = 20.dp, bottom = 4.dp),
                        )
                    }
                }
            }
            itemsPositioned(content.questions, key = { _, question -> question.identifier }) { index, position, question ->
                CheckRow(
                    number = index + 1,
                    question = question,
                    position = position,
                    result = state.answers[question.identifier],
                    isMissing = state.showMissing && question.identifier !in state.answers,
                    onAnswer = { onAction(ChecklistAction.Answer(question.identifier, it)) },
                )
            }
        }
        ChecklistSubmitBar(state = state, onSubmit = { onAction(ChecklistAction.Submit) })
    }
}

@Composable
private fun ChecklistSkeleton(modifier: Modifier = Modifier) {
    ShimmerGroup {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = modifier.fillMaxSize().padding(16.dp),
        ) {
            SkeletonBox(modifier = Modifier.fillMaxWidth().height(124.dp), shape = Shapes.CardShape)
            Spacer(modifier = Modifier.height(20.dp))
            SkeletonBox(modifier = Modifier.fillMaxWidth().height(SKELETON_CHECKS_HEIGHT), shape = Shapes.CardShape)
        }
    }
}

private val previewTruck = Vehicle(1, "MA151E0", "Volvo", "FH16", VehicleType.Truck)
private val previewTrailer = Vehicle(3, "MA777TR", "Schmitz", "S.KO", VehicleType.Trailer)

private val previewContent = ChecklistContent.Loaded(
    questions = persistentListOf(
        ChecklistQuestionItem("OIL_1", "Oil level check"),
        ChecklistQuestionItem("WHEELS_1", "Visual tyre check (damage, wear)"),
        ChecklistQuestionItem("WHEELS_2", "Tyre pressure check"),
        ChecklistQuestionItem("WHEELS_3", "Wheel nut tightness check"),
        ChecklistQuestionItem("LIGHTS_1", "Lights check"),
        ChecklistQuestionItem("BODYWORK_1", "Visual vehicle check (scratches, scuffs, steps, fluid leaks)"),
        ChecklistQuestionItem("LICENSE_1", "Documents check (Community licence, certificates, cards)"),
        ChecklistQuestionItem("TOLL_1", "Toll unit check (placement and axle count)"),
    ),
    trucks = VehiclesContent.Loaded(
        preferredTruck = previewTruck,
        otherVehicles = persistentListOf(Vehicle(2, "MA204TT", "DAF", "XF", VehicleType.Truck)),
    ),
    trailers = VehiclesContent.Loaded(
        preferredTrailer = previewTrailer,
        otherVehicles = persistentListOf(Vehicle(4, "MA402EF", "Krone", "SD", VehicleType.Trailer)),
    ),
)

@Preview
@Composable
private fun ChecklistInProgressPreview() {
    TruckTrackTheme {
        ChecklistScreenContent(
            listState = rememberLazyListState(),
            state = ChecklistState(
                content = previewContent,
                selectedTruck = previewTruck,
                selectedTrailer = previewTrailer,
                answers = persistentMapOf(
                    "OIL_1" to CheckResult.Ok,
                    "WHEELS_1" to CheckResult.Ok,
                    "WHEELS_2" to CheckResult.Problem,
                ),
            ),
            dateFormatter = DateFormatter(),
            onAction = {},
            onClose = {},
        )
    }
}

@Preview
@Composable
private fun ChecklistValidationErrorPreview() {
    TruckTrackTheme {
        ChecklistScreenContent(
            listState = rememberLazyListState(),
            state = ChecklistState(
                content = previewContent,
                selectedTruck = previewTruck,
                answers = persistentMapOf("OIL_1" to CheckResult.Ok),
                submitStatus = ChecklistSubmitStatus.ValidationError,
            ),
            dateFormatter = DateFormatter(),
            onAction = {},
            onClose = {},
        )
    }
}

@Preview
@Composable
private fun ChecklistSubmitFailedPreview() {
    TruckTrackTheme {
        ChecklistScreenContent(
            listState = rememberLazyListState(),
            state = ChecklistState(
                content = previewContent,
                selectedTruck = previewTruck,
                selectedTrailer = previewTrailer,
                answers = persistentMapOf(*previewContent.questions.map { it.identifier to CheckResult.Ok }.toTypedArray()),
                submitStatus = ChecklistSubmitStatus.Failed(FailureReason.NoConnection),
            ),
            dateFormatter = DateFormatter(),
            onAction = {},
            onClose = {},
        )
    }
}

@Preview
@Composable
private fun ChecklistLoadingPreview() {
    TruckTrackTheme {
        ChecklistScreenContent(
            listState = rememberLazyListState(),
            state = ChecklistState(),
            dateFormatter = DateFormatter(),
            onAction = {},
            onClose = {},
        )
    }
}

@Preview
@Composable
private fun ChecklistErrorPreview() {
    TruckTrackTheme {
        ChecklistScreenContent(
            listState = rememberLazyListState(),
            state = ChecklistState(content = ChecklistContent.Error(FailureReason.NoConnection)),
            dateFormatter = DateFormatter(),
            onAction = {},
            onClose = {},
        )
    }
}

@Preview
@Composable
private fun ChecklistDiscardDialogPreview() {
    TruckTrackTheme {
        ChecklistScreenContent(
            listState = rememberLazyListState(),
            state = ChecklistState(
                content = previewContent,
                selectedTruck = previewTruck,
                selectedTrailer = previewTrailer,
                answers = persistentMapOf("OIL_1" to CheckResult.Ok),
                isDiscardDialogVisible = true,
            ),
            dateFormatter = DateFormatter(),
            onAction = {},
            onClose = {},
        )
    }
}
