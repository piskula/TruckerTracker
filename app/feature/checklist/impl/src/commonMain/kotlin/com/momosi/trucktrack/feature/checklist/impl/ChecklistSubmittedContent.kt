package com.momosi.trucktrack.feature.checklist.impl

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.momosi.trucktrack.core.common.formatter.DateFormatter
import com.momosi.trucktrack.core.uilibrary.components.Button
import com.momosi.trucktrack.core.uilibrary.components.Icon
import com.momosi.trucktrack.core.uilibrary.components.Text
import com.momosi.trucktrack.core.uilibrary.icons.TruckTrackIcons
import com.momosi.trucktrack.core.uilibrary.theme.AppTheme
import com.momosi.trucktrack.core.uilibrary.theme.TruckTrackTheme
import com.momosi.trucktrack.feature.checklist.impl.resources.Res
import com.momosi.trucktrack.feature.checklist.impl.resources.checklist_submitted_done
import com.momosi.trucktrack.feature.checklist.impl.resources.checklist_submitted_no_problems
import com.momosi.trucktrack.feature.checklist.impl.resources.checklist_submitted_problems
import com.momosi.trucktrack.feature.checklist.impl.resources.checklist_submitted_problems_hint
import com.momosi.trucktrack.feature.checklist.impl.resources.checklist_submitted_title
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Clock

@Composable
internal fun ChecklistSubmittedContent(
    summary: ChecklistSummary,
    dateFormatter: DateFormatter,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val hasProblems = summary.problemCount > 0

    Column(modifier = modifier.fillMaxSize()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
        ) {
            SuccessMark()
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = stringResource(Res.string.checklist_submitted_title),
                style = AppTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = AppTheme.colors.onSurface,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(16.dp))
            SubmittedVehicles(truckPlate = summary.truckPlate, trailerPlate = summary.trailerPlate)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = dateFormatter.formatDateTime(summary.answeredAt),
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.colors.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(24.dp))
            ResultBadge(problemCount = summary.problemCount)
            if (hasProblems) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(Res.string.checklist_submitted_problems_hint),
                    style = AppTheme.typography.bodyMedium,
                    color = AppTheme.colors.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }
        Button(
            text = stringResource(Res.string.checklist_submitted_done),
            onClick = onDone,
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .testTag("checklist_done_button"),
        )
    }
}

@Composable
private fun SuccessMark(modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(96.dp)
            .background(AppTheme.colors.positiveContainer, CircleShape),
    ) {
        Icon(
            imageVector = TruckTrackIcons.Check,
            tint = AppTheme.colors.onPositiveContainer,
            modifier = Modifier.size(56.dp),
        )
    }
}

@Composable
private fun SubmittedVehicles(
    truckPlate: String,
    trailerPlate: String,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier,
    ) {
        Icon(imageVector = TruckTrackIcons.Truck, tint = AppTheme.colors.onSurfaceVariant, modifier = Modifier.size(18.dp))
        Text(text = truckPlate, style = AppTheme.typography.titleMedium, color = AppTheme.colors.onSurface)
        Spacer(modifier = Modifier.size(6.dp))
        Icon(imageVector = TruckTrackIcons.Trailer, tint = AppTheme.colors.onSurfaceVariant, modifier = Modifier.size(18.dp))
        Text(text = trailerPlate, style = AppTheme.typography.titleMedium, color = AppTheme.colors.onSurface)
    }
}

@Composable
private fun ResultBadge(problemCount: Int, modifier: Modifier = Modifier) {
    val hasProblems = problemCount > 0
    val contentColor = if (hasProblems) AppTheme.colors.onErrorContainer else AppTheme.colors.onPositiveContainer

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .background(
                if (hasProblems) AppTheme.colors.errorContainer else AppTheme.colors.positiveContainer,
                CircleShape,
            )
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .testTag("checklist_submitted_result"),
    ) {
        Icon(
            imageVector = if (hasProblems) TruckTrackIcons.Error else TruckTrackIcons.CheckCircle,
            tint = contentColor,
            modifier = Modifier.size(20.dp),
        )
        Text(
            text = if (hasProblems) {
                pluralStringResource(Res.plurals.checklist_submitted_problems, problemCount, problemCount)
            } else {
                stringResource(Res.string.checklist_submitted_no_problems)
            },
            style = AppTheme.typography.labelLarge,
            color = contentColor,
        )
    }
}

@Preview
@Composable
private fun ChecklistSubmittedAllOkPreview() {
    TruckTrackTheme {
        ChecklistSubmittedContent(
            summary = ChecklistSummary("MA151E0", "MA777TR", Clock.System.now(), problemCount = 0, totalCount = 8),
            dateFormatter = DateFormatter(),
            onDone = {},
        )
    }
}

@Preview
@Composable
private fun ChecklistSubmittedProblemsPreview() {
    TruckTrackTheme {
        ChecklistSubmittedContent(
            summary = ChecklistSummary("MA151E0", "MA777TR", Clock.System.now(), problemCount = 2, totalCount = 8),
            dateFormatter = DateFormatter(),
            onDone = {},
        )
    }
}
