package com.momosi.trucktrack.feature.checklist.impl

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.momosi.trucktrack.core.common.network.FailureReason
import com.momosi.trucktrack.core.uilibrary.components.Button
import com.momosi.trucktrack.core.uilibrary.components.ButtonRole
import com.momosi.trucktrack.core.uilibrary.components.Text
import com.momosi.trucktrack.core.uilibrary.icons.TruckTrackIcons
import com.momosi.trucktrack.core.uilibrary.theme.AppTheme
import com.momosi.trucktrack.core.uilibrary.theme.TruckTrackTheme
import com.momosi.trucktrack.core.uilibrary.vehicle.VehiclesContent
import com.momosi.trucktrack.feature.checklist.impl.resources.Res
import com.momosi.trucktrack.feature.checklist.impl.resources.checklist_error_submit_already_submitted
import com.momosi.trucktrack.feature.checklist.impl.resources.checklist_error_submit_no_connection
import com.momosi.trucktrack.feature.checklist.impl.resources.checklist_error_submit_other
import com.momosi.trucktrack.feature.checklist.impl.resources.checklist_problem_count
import com.momosi.trucktrack.feature.checklist.impl.resources.checklist_progress
import com.momosi.trucktrack.feature.checklist.impl.resources.checklist_submit
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ChecklistSubmitBar(
    state: ChecklistState,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isComplete = state.totalCount > 0 && state.answeredCount == state.totalCount

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(AppTheme.colors.surfaceContainerLowest)
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        ChecklistProgress(state = state, isComplete = isComplete)
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            text = stringResource(Res.string.checklist_submit),
            onClick = onSubmit,
            loading = state.isSubmitting,
            icon = TruckTrackIcons.Send,
            role = if (isComplete) ButtonRole.Positive else ButtonRole.Primary,
            modifier = Modifier.fillMaxWidth().testTag("checklist_submit_button"),
        )
        val failureMessage = state.submitStatus.failureMessageRes()
        if (failureMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(failureMessage),
                style = AppTheme.typography.bodySmall,
                color = AppTheme.colors.error,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().testTag("checklist_submit_error"),
            )
        }
    }
}

@Composable
private fun ChecklistProgress(
    state: ChecklistState,
    isComplete: Boolean,
    modifier: Modifier = Modifier,
) {
    val progress by animateFloatAsState(
        if (state.totalCount == 0) 0f else state.answeredCount.toFloat() / state.totalCount,
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(Res.string.checklist_progress, state.answeredCount, state.totalCount),
                style = AppTheme.typography.labelLarge,
                color = AppTheme.colors.onSurface,
                modifier = Modifier.weight(1f).testTag("checklist_progress"),
            )
            if (state.problemCount > 0) {
                Text(
                    text = pluralStringResource(Res.plurals.checklist_problem_count, state.problemCount, state.problemCount),
                    style = AppTheme.typography.labelLarge,
                    color = AppTheme.colors.error,
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape)
                .background(AppTheme.colors.surfaceVariant),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress)
                    .clip(CircleShape)
                    .background(if (isComplete) AppTheme.colors.positive else AppTheme.colors.primary),
            )
        }
    }
}

private fun ChecklistSubmitStatus.failureMessageRes(): StringResource? = when (this) {
    is ChecklistSubmitStatus.Failed -> reason.submitMessageRes()
    ChecklistSubmitStatus.AlreadySubmittedThisWeek -> Res.string.checklist_error_submit_already_submitted
    else -> null
}

private fun FailureReason.submitMessageRes() = when (this) {
    FailureReason.NoConnection -> Res.string.checklist_error_submit_no_connection
    FailureReason.Other -> Res.string.checklist_error_submit_other
}

private val previewQuestions = persistentListOf(
    ChecklistQuestionItem("OIL_1", "Oil level check"),
    ChecklistQuestionItem("WHEELS_1", "Visual tyre check (damage, wear)"),
    ChecklistQuestionItem("LIGHTS_1", "Lights check"),
)

private val previewVehicles = VehiclesContent.Loaded()

@Preview
@Composable
private fun ChecklistSubmitBarPartialPreview() {
    TruckTrackTheme {
        ChecklistSubmitBar(
            state = ChecklistState(
                content = ChecklistContent.Loaded(previewQuestions, previewVehicles, previewVehicles),
                answers = persistentMapOf("OIL_1" to CheckResult.Ok, "WHEELS_1" to CheckResult.Problem),
            ),
            onSubmit = {},
        )
    }
}

@Preview
@Composable
private fun ChecklistSubmitBarAlreadySubmittedPreview() {
    TruckTrackTheme {
        ChecklistSubmitBar(
            state = ChecklistState(
                content = ChecklistContent.Loaded(previewQuestions, previewVehicles, previewVehicles),
                answers = persistentMapOf(*previewQuestions.map { it.identifier to CheckResult.Ok }.toTypedArray()),
                submitStatus = ChecklistSubmitStatus.AlreadySubmittedThisWeek,
            ),
            onSubmit = {},
        )
    }
}

@Preview
@Composable
private fun ChecklistSubmitBarFailedPreview() {
    TruckTrackTheme {
        ChecklistSubmitBar(
            state = ChecklistState(
                content = ChecklistContent.Loaded(previewQuestions, previewVehicles, previewVehicles),
                answers = persistentMapOf(*previewQuestions.map { it.identifier to CheckResult.Ok }.toTypedArray()),
                submitStatus = ChecklistSubmitStatus.Failed(FailureReason.Other),
            ),
            onSubmit = {},
        )
    }
}
