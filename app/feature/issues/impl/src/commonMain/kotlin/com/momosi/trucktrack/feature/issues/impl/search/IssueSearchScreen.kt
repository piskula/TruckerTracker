package com.momosi.trucktrack.feature.issues.impl.search

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.momosi.trucktrack.core.common.formatter.DateFormatter
import com.momosi.trucktrack.core.issue.model.Account
import com.momosi.trucktrack.core.issue.model.Issue
import com.momosi.trucktrack.core.issue.model.IssuePriority
import com.momosi.trucktrack.core.issue.model.IssueStatus
import com.momosi.trucktrack.core.uilibrary.components.SearchBarActive
import com.momosi.trucktrack.core.uilibrary.components.Text
import com.momosi.trucktrack.core.uilibrary.components.TopBarIconButton
import com.momosi.trucktrack.core.uilibrary.icons.TruckTrackIcons
import com.momosi.trucktrack.core.uilibrary.modifier.ShimmerGroup
import com.momosi.trucktrack.core.uilibrary.theme.AppTheme
import com.momosi.trucktrack.core.uilibrary.theme.TruckTrackTheme
import com.momosi.trucktrack.core.vehicle.model.Vehicle
import com.momosi.trucktrack.core.vehicle.model.VehicleType
import com.momosi.trucktrack.feature.issues.impl.list.IssueCard
import com.momosi.trucktrack.feature.issues.impl.list.IssueCardSkeleton
import com.momosi.trucktrack.feature.issues.impl.list.IssueFilter
import com.momosi.trucktrack.feature.issues.impl.resources.Res
import com.momosi.trucktrack.feature.issues.impl.resources.issue_search_error
import com.momosi.trucktrack.feature.issues.impl.resources.issue_search_not_found
import com.momosi.trucktrack.feature.issues.impl.resources.issue_search_placeholder
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Clock

@Composable
internal fun IssueSearchScreen(
    onBack: () -> Unit,
    onNavigateToIssueDetail: (Long) -> Unit,
    viewModel: IssueSearchViewModel = koinViewModel(),
    dateFormatter: DateFormatter = koinInject(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    IssueSearchScreenContent(
        state = state,
        dateFormatter = dateFormatter,
        onAction = viewModel::onAction,
        onBack = onBack,
        onNavigateToIssueDetail = onNavigateToIssueDetail,
    )
}

@Composable
private fun IssueSearchScreenContent(
    state: IssueSearchState,
    dateFormatter: DateFormatter,
    onAction: (IssueSearchAction) -> Unit,
    onBack: () -> Unit,
    onNavigateToIssueDetail: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize().background(AppTheme.colors.surfaceContainer)) {
        SearchToolbar(
            query = state.query,
            onQueryChange = { onAction(IssueSearchAction.ChangeQuery(it)) },
            onBack = onBack,
        )
        Crossfade(targetState = state.content, modifier = Modifier.weight(1f)) { content ->
            when (content) {
                is IssueSearchContent.Blank -> Box(modifier = Modifier.fillMaxSize())

                is IssueSearchContent.Loading -> LoadingContent()

                is IssueSearchContent.NotFound -> MessageContent(
                    message = stringResource(Res.string.issue_search_not_found),
                    testTag = "issue_search_not_found",
                )

                is IssueSearchContent.Error -> MessageContent(
                    message = stringResource(Res.string.issue_search_error),
                    testTag = "issue_search_error",
                )

                is IssueSearchContent.Found -> ResultContent(
                    issue = content.issue,
                    dateFormatter = dateFormatter,
                    onOpenIssue = { onNavigateToIssueDetail(content.issue.id) },
                )
            }
        }
    }
}

@Composable
private fun SearchToolbar(
    query: String,
    onQueryChange: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .background(AppTheme.colors.surfaceDim)
            .statusBarsPadding()
            .padding(start = 4.dp, end = 12.dp, top = 8.dp, bottom = 8.dp),
    ) {
        TopBarIconButton(
            icon = TruckTrackIcons.Back,
            onClick = onBack,
            modifier = Modifier.testTag("issue_search_back_button"),
        )
        SearchBarActive(
            query = query,
            onQueryChange = onQueryChange,
            placeholder = stringResource(Res.string.issue_search_placeholder),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            autoFocus = true,
            modifier = Modifier.weight(1f).padding(start = 4.dp).testTag("issue_search_input"),
        )
    }
}

@Composable
private fun LoadingContent(modifier: Modifier = Modifier) {
    ShimmerGroup {
        Box(modifier = modifier.fillMaxSize().padding(searchResultPadding)) {
            IssueCardSkeleton()
        }
    }
}

@Composable
private fun ResultContent(
    issue: Issue,
    dateFormatter: DateFormatter,
    onOpenIssue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize().padding(searchResultPadding)) {
        IssueCard(
            issue = issue,
            filter = IssueFilter.All,
            dateFormatter = dateFormatter,
            onClick = onOpenIssue,
        )
    }
}

@Composable
private fun MessageContent(
    message: String,
    testTag: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize().padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = message,
            style = AppTheme.typography.bodyLarge,
            color = AppTheme.colors.onSurfaceVariant,
            modifier = Modifier.testTag(testTag),
        )
    }
}

private val searchResultPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 4.dp)

private val previewIssue = Issue(
    id = 42,
    title = "Engine warning light — truck won't start",
    description = "",
    status = IssueStatus.InProgress,
    priority = IssuePriority.High,
    vehicle = Vehicle(
        id = 1,
        licensePlate = "MA-204-TT",
        make = "DAF",
        model = "XF",
        type = VehicleType.Truck,
    ),
    reportedBy = Account(
        id = "1",
        username = "mschumacher",
        firstName = "Michael",
        lastName = "Schumacher",
    ),
    assignedTo = null,
    repairType = null,
    vehicleSystem = null,
    createdAt = Clock.System.now(),
    updatedAt = Clock.System.now(),
)

@Preview
@Composable
private fun IssueSearchFoundPreview() {
    TruckTrackTheme {
        IssueSearchScreenContent(
            state = IssueSearchState(query = "42", content = IssueSearchContent.Found(previewIssue)),
            dateFormatter = DateFormatter(),
            onAction = {},
            onBack = {},
            onNavigateToIssueDetail = {},
        )
    }
}

@Preview
@Composable
private fun IssueSearchLoadingPreview() {
    TruckTrackTheme {
        IssueSearchScreenContent(
            state = IssueSearchState(query = "42", content = IssueSearchContent.Loading),
            dateFormatter = DateFormatter(),
            onAction = {},
            onBack = {},
            onNavigateToIssueDetail = {},
        )
    }
}

@Preview
@Composable
private fun IssueSearchNotFoundPreview() {
    TruckTrackTheme {
        IssueSearchScreenContent(
            state = IssueSearchState(query = "999", content = IssueSearchContent.NotFound),
            dateFormatter = DateFormatter(),
            onAction = {},
            onBack = {},
            onNavigateToIssueDetail = {},
        )
    }
}
