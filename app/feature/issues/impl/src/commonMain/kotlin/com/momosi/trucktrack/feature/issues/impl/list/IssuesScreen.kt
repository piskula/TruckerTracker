package com.momosi.trucktrack.feature.issues.impl.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.momosi.trucktrack.core.common.formatter.DateFormatter
import com.momosi.trucktrack.core.issue.model.Account
import com.momosi.trucktrack.core.issue.model.Issue
import com.momosi.trucktrack.core.issue.model.IssuePriority
import com.momosi.trucktrack.core.issue.model.IssueStatus
import com.momosi.trucktrack.core.uilibrary.components.Button
import com.momosi.trucktrack.core.uilibrary.components.DashboardTopBar
import com.momosi.trucktrack.core.uilibrary.components.FilterChipRow
import com.momosi.trucktrack.core.uilibrary.components.FloatingActionButton
import com.momosi.trucktrack.core.uilibrary.components.LoadingSpinner
import com.momosi.trucktrack.core.uilibrary.components.PullToRefresh
import com.momosi.trucktrack.core.uilibrary.components.Text
import com.momosi.trucktrack.core.uilibrary.components.TopBarIconButton
import com.momosi.trucktrack.core.uilibrary.icons.TruckTrackIcons
import com.momosi.trucktrack.core.uilibrary.theme.AppTheme
import com.momosi.trucktrack.core.uilibrary.theme.TruckTrackTheme
import com.momosi.trucktrack.core.vehicle.model.Vehicle
import com.momosi.trucktrack.core.vehicle.model.VehicleType
import com.momosi.trucktrack.feature.issues.impl.resources.Res
import com.momosi.trucktrack.feature.issues.impl.resources.my_issues_empty
import com.momosi.trucktrack.feature.issues.impl.resources.my_issues_error_message
import com.momosi.trucktrack.feature.issues.impl.resources.my_issues_filter_all
import com.momosi.trucktrack.feature.issues.impl.resources.my_issues_filter_in_progress
import com.momosi.trucktrack.feature.issues.impl.resources.my_issues_filter_my_closed
import com.momosi.trucktrack.feature.issues.impl.resources.my_issues_filter_my_issues
import com.momosi.trucktrack.feature.issues.impl.resources.my_issues_filter_my_open
import com.momosi.trucktrack.feature.issues.impl.resources.my_issues_filter_open
import com.momosi.trucktrack.feature.issues.impl.resources.my_issues_retry
import com.momosi.trucktrack.feature.issues.impl.resources.my_issues_subtitle_driver
import com.momosi.trucktrack.feature.issues.impl.resources.my_issues_subtitle_mechanic
import com.momosi.trucktrack.feature.issues.impl.resources.my_issues_title_driver
import com.momosi.trucktrack.feature.issues.impl.resources.my_issues_title_mechanic
import com.momosi.trucktrack.user.model.UserRole
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.flowOf
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Clock

@Composable
internal fun IssuesScreen(
    onNavigateToProfile: () -> Unit,
    onNavigateToCreateIssue: () -> Unit,
    onNavigateToIssueDetail: (Long) -> Unit,
    issueStatusChange: Boolean = false,
    viewModel: IssuesViewModel = koinViewModel(),
    dateFormatter: DateFormatter = koinInject(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val pagingItems = viewModel.pagingData.collectAsLazyPagingItems()

    LaunchedEffect(issueStatusChange) {
        if (issueStatusChange) {
            pagingItems.refresh()
        }
    }

    IssuesScreenContent(
        state = state,
        pagingItems = pagingItems,
        dateFormatter = dateFormatter,
        onSelectFilter = { viewModel.onAction(IssuesAction.SelectFilter(it)) },
        onRetry = { pagingItems.retry() },
        onRefresh = { pagingItems.refresh() },
        onNavigateToProfile = onNavigateToProfile,
        onNavigateToCreateIssue = onNavigateToCreateIssue,
        onNavigateToIssueDetail = onNavigateToIssueDetail,
    )
}

@Composable
private fun IssuesScreenContent(
    state: IssuesState,
    pagingItems: LazyPagingItems<Issue>,
    dateFormatter: DateFormatter,
    onSelectFilter: (IssueFilter) -> Unit,
    onRetry: () -> Unit,
    onRefresh: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToCreateIssue: () -> Unit,
    onNavigateToIssueDetail: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val userInfo = state.userInfo
    val isDualRole = userInfo?.isDualRole == true
    val isMechanic = !isDualRole && userInfo?.isMechanic == true
    val isDriver = !isDualRole && !isMechanic
    val role = if (isMechanic) IssueCardRole.Mechanic else IssueCardRole.Driver

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            DashboardTopBar(
                title = stringResource(
                    if (isDriver) Res.string.my_issues_title_driver else Res.string.my_issues_title_mechanic,
                ),
                subtitle = userInfo?.let { info ->
                    val roleLabel = info.roles.map { stringResource(it.labelRes()) }.joinToString(" · ")
                    "${info.name} · $roleLabel"
                }.orEmpty(),
                actions = {
                    TopBarIconButton(icon = TruckTrackIcons.AccountCircle, onClick = onNavigateToProfile)
                },
            )
            FilterChipRow(
                items = when {
                    isDualRole -> persistentListOf(
                        IssueFilter.DualRole.Open,
                        IssueFilter.DualRole.InProgress,
                        IssueFilter.DualRole.All,
                    )

                    isMechanic -> persistentListOf(
                        IssueFilter.Mechanic.MyIssues,
                        IssueFilter.Mechanic.Open,
                        IssueFilter.Mechanic.All,
                    )

                    else -> persistentListOf(
                        IssueFilter.Driver.MyOpen,
                        IssueFilter.Driver.MyClosed,
                        IssueFilter.Driver.All,
                    )
                },
                selectedItem = state.selectedFilter,
                labelSelector = { it.label() },
                onSelect = onSelectFilter,
            )
            when (pagingItems.loadState.refresh) {
                is LoadState.Loading -> LoadingContent()

                is LoadState.Error -> ErrorContent(onRetry = onRetry)

                is LoadState.NotLoading -> {
                    if (pagingItems.itemCount == 0) {
                        EmptyContent()
                    } else {
                        IssueList(
                            pagingItems = pagingItems,
                            role = role,
                            dateFormatter = dateFormatter,
                            onOpenIssue = onNavigateToIssueDetail,
                            onRefresh = onRefresh,
                        )
                    }
                }
            }
        }
        if (isDriver || isDualRole) {
            FloatingActionButton(
                icon = TruckTrackIcons.Add,
                onClick = onNavigateToCreateIssue,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .navigationBarsPadding()
                    .padding(end = 20.dp, bottom = 20.dp),
            )
        }
    }
}

@Composable
private fun IssueList(
    pagingItems: LazyPagingItems<Issue>,
    role: IssueCardRole,
    dateFormatter: DateFormatter,
    onOpenIssue: (Long) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isRefreshing = pagingItems.loadState.refresh is LoadState.Loading
    val navBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    PullToRefresh(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
    ) {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 12.dp, end = 12.dp, bottom = 4.dp, top = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(
                count = pagingItems.itemCount,
                key = { index -> pagingItems.peek(index)?.id ?: index },
            ) { index ->
                val issue = pagingItems[index] ?: return@items
                IssueCard(
                    state = IssueCardState(
                        issue = issue,
                        role = role,
                    ),
                    dateFormatter = dateFormatter,
                    onClick = { onOpenIssue(issue.id) },
                )
            }
            if (pagingItems.loadState.append is LoadState.Loading) {
                item {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        LoadingSpinner()
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(72.dp + navBarBottom)) }
        }
    }
}

@Composable
private fun LoadingContent(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        LoadingSpinner()
    }
}

@Composable
private fun ErrorContent(onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(Res.string.my_issues_error_message),
                style = AppTheme.typography.bodyLarge,
                color = AppTheme.colors.onSurface,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(text = stringResource(Res.string.my_issues_retry), onClick = onRetry)
        }
    }
}

@Composable
private fun EmptyContent(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(Res.string.my_issues_empty),
            style = AppTheme.typography.bodyLarge,
            color = AppTheme.colors.onSurfaceVariant,
        )
    }
}

@Composable
private fun IssueFilter.label(): String = stringResource(
    when (this) {
        IssueFilter.Driver.MyOpen -> Res.string.my_issues_filter_my_open
        IssueFilter.Driver.MyClosed -> Res.string.my_issues_filter_my_closed
        IssueFilter.Driver.All -> Res.string.my_issues_filter_all
        IssueFilter.Mechanic.MyIssues -> Res.string.my_issues_filter_my_issues
        IssueFilter.Mechanic.Open -> Res.string.my_issues_filter_open
        IssueFilter.Mechanic.All -> Res.string.my_issues_filter_all
        IssueFilter.DualRole.Open -> Res.string.my_issues_filter_open
        IssueFilter.DualRole.InProgress -> Res.string.my_issues_filter_in_progress
        IssueFilter.DualRole.All -> Res.string.my_issues_filter_all
    },
)

private fun UserRole.labelRes(): StringResource = when (this) {
    UserRole.Driver -> Res.string.my_issues_subtitle_driver
    UserRole.Mechanic -> Res.string.my_issues_subtitle_mechanic
}

// region Previews

private val previewVehicle = Vehicle(
    id = 1,
    licensePlate = "MA-204-TT",
    make = "DAF",
    model = "XF",
    type = VehicleType.Truck,
)

private val previewReporter = Account(
    id = "1",
    username = "mschumacher",
    firstName = "Michael",
    lastName = "Schumacher",
)

private val previewNow = Clock.System.now()

private val previewIssues = listOf(
    Issue(
        id = 1,
        title = "Engine warning light — truck won't start",
        description = "",
        status = IssueStatus.InProgress,
        priority = IssuePriority.High,
        vehicle = previewVehicle,
        reportedBy = previewReporter,
        assignedTo = null,
        createdAt = previewNow.minus(kotlin.time.Duration.parse("2h")),
        updatedAt = previewNow,
    ),
    Issue(
        id = 2,
        title = "Left rear tire pressure critically low",
        description = "",
        status = IssueStatus.Open,
        priority = IssuePriority.High,
        vehicle = previewVehicle.copy(id = 2, licensePlate = "MA-118-AB"),
        reportedBy = previewReporter,
        assignedTo = null,
        createdAt = previewNow.minus(kotlin.time.Duration.parse("5h")),
        updatedAt = previewNow,
    ),
)

@Preview
@Composable
private fun IssuesDriverPreview() {
    val pagingItems = flowOf(PagingData.from(previewIssues)).collectAsLazyPagingItems()
    TruckTrackTheme {
        IssuesScreenContent(
            state = IssuesState(
                userInfo = IssuesUserInfo(
                    name = "Michael Schumacher",
                    roles = persistentListOf(UserRole.Driver),
                ),
                selectedFilter = IssueFilter.Driver.MyOpen,
            ),
            pagingItems = pagingItems,
            dateFormatter = DateFormatter(),
            onSelectFilter = {},
            onRetry = {},
            onRefresh = {},
            onNavigateToProfile = {},
            onNavigateToCreateIssue = {},
            onNavigateToIssueDetail = {},
        )
    }
}

@Preview
@Composable
private fun IssuesMechanicPreview() {
    val pagingItems = flowOf(PagingData.from(previewIssues)).collectAsLazyPagingItems()
    TruckTrackTheme {
        IssuesScreenContent(
            state = IssuesState(
                userInfo = IssuesUserInfo(
                    name = "Mattia Binotto",
                    roles = persistentListOf(UserRole.Mechanic),
                ),
                selectedFilter = IssueFilter.Mechanic.MyIssues,
            ),
            pagingItems = pagingItems,
            dateFormatter = DateFormatter(),
            onSelectFilter = {},
            onRetry = {},
            onRefresh = {},
            onNavigateToProfile = {},
            onNavigateToCreateIssue = {},
            onNavigateToIssueDetail = {},
        )
    }
}

@Preview
@Composable
private fun IssuesEmptyPreview() {
    val pagingItems = flowOf(PagingData.empty<Issue>()).collectAsLazyPagingItems()
    TruckTrackTheme {
        IssuesScreenContent(
            state = IssuesState(
                userInfo = IssuesUserInfo(
                    name = "Test User",
                    roles = persistentListOf(UserRole.Driver),
                ),
            ),
            pagingItems = pagingItems,
            dateFormatter = DateFormatter(),
            onSelectFilter = {},
            onRetry = {},
            onRefresh = {},
            onNavigateToProfile = {},
            onNavigateToCreateIssue = {},
            onNavigateToIssueDetail = {},
        )
    }
}

// endregion
