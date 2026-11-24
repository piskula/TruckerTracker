package com.momosi.trucktrack.feature.checklist.impl

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.momosi.trucktrack.core.uilibrary.components.Icon
import com.momosi.trucktrack.core.uilibrary.components.Text
import com.momosi.trucktrack.core.uilibrary.icons.TruckTrackIcons
import com.momosi.trucktrack.core.uilibrary.lazylist.ListItemPosition
import com.momosi.trucktrack.core.uilibrary.theme.AppTheme
import com.momosi.trucktrack.core.uilibrary.theme.TruckTrackTheme
import com.momosi.trucktrack.feature.checklist.impl.resources.Res
import com.momosi.trucktrack.feature.checklist.impl.resources.checklist_answer_ok
import com.momosi.trucktrack.feature.checklist.impl.resources.checklist_answer_problem
import com.momosi.trucktrack.feature.checklist.impl.resources.checklist_error_answer_required
import org.jetbrains.compose.resources.stringResource

private val CardCorner = 16.dp
private val ToggleShape = RoundedCornerShape(12.dp)

@Composable
internal fun CheckRow(
    number: Int,
    question: ChecklistQuestionItem,
    result: CheckResult?,
    isMissing: Boolean,
    position: ListItemPosition,
    onAnswer: (CheckResult) -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = position.shape()
    val background by animateColorAsState(if (isMissing) AppTheme.colors.errorContainer else AppTheme.colors.surfaceContainerLowest)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(background, shape)
            .testTag("checklist_check_${question.identifier}"),
    ) {
        if (position == ListItemPosition.Middle || position == ListItemPosition.Last) {
            Box(
                modifier = Modifier
                    .padding(start = 16.dp)
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(AppTheme.colors.outlineVariant),
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 16.dp, end = 12.dp, top = 10.dp, bottom = 10.dp),
        ) {
            Text(
                text = number.toString(),
                style = AppTheme.typography.labelMedium,
                color = if (isMissing) AppTheme.colors.onErrorContainer else AppTheme.colors.onSurfaceVariant,
                modifier = Modifier.width(24.dp),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = question.text,
                    style = AppTheme.typography.bodyMedium,
                    color = if (isMissing) AppTheme.colors.onErrorContainer else AppTheme.colors.onSurface,
                )
                if (isMissing) {
                    Text(
                        text = stringResource(Res.string.checklist_error_answer_required),
                        style = AppTheme.typography.bodySmall,
                        color = AppTheme.colors.onErrorContainer,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            AnswerToggle(result = result, onAnswer = onAnswer, testTagPrefix = "checklist_check_${question.identifier}")
        }
    }
}

@Composable
private fun AnswerToggle(
    result: CheckResult?,
    onAnswer: (CheckResult) -> Unit,
    testTagPrefix: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .height(IntrinsicSize.Min)
            .clip(ToggleShape)
            .border(1.5.dp, AppTheme.colors.outlineVariant, ToggleShape)
            .selectableGroup(),
    ) {
        AnswerSegment(
            icon = TruckTrackIcons.Close,
            description = stringResource(Res.string.checklist_answer_problem),
            selected = result == CheckResult.Problem,
            selectedContainer = AppTheme.colors.errorContainer,
            selectedContent = AppTheme.colors.onErrorContainer,
            onSelect = { onAnswer(CheckResult.Problem) },
            testTag = "${testTagPrefix}_problem",
        )
        Box(modifier = Modifier.width(1.5.dp).fillMaxHeight().background(AppTheme.colors.outlineVariant))
        AnswerSegment(
            icon = TruckTrackIcons.Check,
            description = stringResource(Res.string.checklist_answer_ok),
            selected = result == CheckResult.Ok,
            selectedContainer = AppTheme.colors.positiveContainer,
            selectedContent = AppTheme.colors.onPositiveContainer,
            onSelect = { onAnswer(CheckResult.Ok) },
            testTag = "${testTagPrefix}_ok",
        )
    }
}

@Composable
private fun AnswerSegment(
    icon: ImageVector,
    description: String,
    selected: Boolean,
    selectedContainer: Color,
    selectedContent: Color,
    onSelect: () -> Unit,
    testTag: String,
) {
    val containerColor by animateColorAsState(if (selected) selectedContainer else selectedContainer.copy(alpha = 0f))
    val contentColor by animateColorAsState(if (selected) selectedContent else AppTheme.colors.onSurfaceVariant)

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(width = 52.dp, height = 48.dp)
            .background(containerColor)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onSelect)
            .semantics { contentDescription = description }
            .testTag(testTag),
    ) {
        Icon(imageVector = icon, tint = contentColor, modifier = Modifier.size(22.dp))
    }
}

private fun ListItemPosition.shape(): Shape = when (this) {
    ListItemPosition.Single -> RoundedCornerShape(CardCorner)
    ListItemPosition.First -> RoundedCornerShape(topStart = CardCorner, topEnd = CardCorner)
    ListItemPosition.Middle -> RoundedCornerShape(0.dp)
    ListItemPosition.Last -> RoundedCornerShape(bottomStart = CardCorner, bottomEnd = CardCorner)
}

@Preview
@Composable
private fun CheckRowUnansweredPreview() {
    TruckTrackTheme {
        CheckRow(
            number = 2,
            question = ChecklistQuestionItem("WHEELS_1", "Visual tyre check (damage, wear)"),
            result = null,
            isMissing = false,
            position = ListItemPosition.First,
            onAnswer = {},
        )
    }
}

@Preview
@Composable
private fun CheckRowProblemPreview() {
    TruckTrackTheme {
        CheckRow(
            number = 6,
            question = ChecklistQuestionItem("BODYWORK_1", "Visual vehicle check (scratches, scuffs, steps, fluid leaks)"),
            result = CheckResult.Problem,
            isMissing = false,
            position = ListItemPosition.Middle,
            onAnswer = {},
        )
    }
}

@Preview
@Composable
private fun CheckRowMissingPreview() {
    TruckTrackTheme {
        CheckRow(
            number = 8,
            question = ChecklistQuestionItem("TOLL_1", "Toll unit check (placement and axle count)"),
            result = null,
            isMissing = true,
            position = ListItemPosition.Last,
            onAnswer = {},
        )
    }
}
