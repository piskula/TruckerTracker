package com.momosi.trucktrack.feature.issues.impl.list

import androidx.compose.animation.Crossfade
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
import androidx.compose.ui.platform.testTag
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
import com.momosi.trucktrack.core.uilibrary.modifier.ShimmerGroup
import com.momosi.trucktrack.core.uilibrary.theme.AppTheme
import com.momosi.trucktrack.core.uilibrary.theme.TruckTrackTheme
import com.momosi.trucktrack.core.vehicle.model.Vehicle
import com.momosi.trucktrack.core.vehicle.model.VehicleType
import com.momosi.trucktrack.feature.issues.impl.resources.Res
import com.momosi.trucktrack.feature.issues.impl.resources.issues_title
import com.momosi.trucktrack.feature.issues.impl.resources.my_issues_empty
import com.momosi.trucktrack.feature.issues.impl.resources.my_issues_error_message
import com.momosi.trucktrack.feature.issues.impl.resources.my_issues_filter_all
import com.momosi.trucktrack.feature.issues.impl.resources.my_issues_filter_my_completed
import com.momosi.trucktrack.feature.issues.impl.resources.my_issues_filter_my_issues
import com.momosi.trucktrack.feature.issues.impl.resources.my_issues_filter_my_resolved
import com.momosi.trucktrack.feature.issues.impl.resources.my_issues_filter_my_work
import com.momosi.trucktrack.feature.issues.impl.resources.my_issues_filter_open
import com.momosi.trucktrack.feature.issues.impl.resources.my_issues_retry
import com.momosi.trucktrack.feature.issues.impl.resources.my_issues_subtitle_driver
import com.momosi.trucktrack.feature.issues.impl.resources.my_issues_subtitle_mechanic
import com.momosi.trucktrack.user.model.UserRole
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
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

private enum class IssuesContentPhase { Loading, Error, Empty, List }

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
    val availableFilters = filtersByRole
        .filterKeys { it in (userInfo?.roles ?: emptySet()) }
        .values
        .flatten()
        .distinct()
        .sorted()
        .toImmutableList()

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            DashboardTopBar(
                title = stringResource(Res.string.issues_title),
                subtitle = userInfo?.let { info ->
                    val roleLabel = info.roles.map { stringResource(it.labelRes()) }.joinToString(" · ")
                    "${info.name} · $roleLabel"
                }.orEmpty(),
                actions = {
                    TopBarIconButton(
                        icon = TruckTrackIcons.AccountCircle,
                        onClick = onNavigateToProfile,
                        modifier = Modifier.testTag("issues_profile_button"),
                    )
                },
            )
            FilterChipRow(
                items = availableFilters,
                selectedItem = state.selectedFilter,
                labelSelector = { it.label() },
                onSelect = onSelectFilter,
            )
            val contentPhase = when (pagingItems.loadState.refresh) {
                is LoadState.Loading -> IssuesContentPhase.Loading

                is LoadState.Error -> IssuesContentPhase.Error

                is LoadState.NotLoading -> if (pagingItems.itemCount == 0) {
                    IssuesContentPhase.Empty
                } else {
                    IssuesContentPhase.List
                }
            }
            Crossfade(targetState = contentPhase) { phase ->
                when (phase) {
                    IssuesContentPhase.Loading -> LoadingContent()

                    IssuesContentPhase.Error -> ErrorContent(onRetry = onRetry)

                    IssuesContentPhase.Empty -> EmptyContent()

                    IssuesContentPhase.List -> IssueList(
                        pagingItems = pagingItems,
                        filter = state.selectedFilter,
                        dateFormatter = dateFormatter,
                        onOpenIssue = onNavigateToIssueDetail,
                        onRefresh = onRefresh,
                    )
                }
            }
        }
        if (userInfo?.roles?.contains(UserRole.Driver) == true) {
            FloatingActionButton(
                icon = TruckTrackIcons.Add,
                onClick = onNavigateToCreateIssue,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .navigationBarsPadding()
                    .padding(end = 20.dp, bottom = 20.dp)
                    .testTag("issues_create_fab"),
            )
        }
    }
}

@Composable
private fun IssueList(
    pagingItems: LazyPagingItems<Issue>,
    filter: IssueFilter,
    dateFormatter: DateFormatter,
    onOpenIssue: (Long) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val navBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    PullToRefresh(
        isRefreshing = false,
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
                    issue = issue,
                    filter = filter,
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
    ShimmerGroup {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 12.dp, end = 12.dp, bottom = 4.dp, top = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(count = SKELETON_ITEM_COUNT) { index ->
                IssueCardSkeleton(
                    titleWidthFraction = skeletonTitleWidthFractions[index % skeletonTitleWidthFractions.size],
                )
            }
        }
    }
}

private const val SKELETON_ITEM_COUNT = 6

private val skeletonTitleWidthFractions = listOf(0.85f, 0.6f, 0.75f, 0.5f, 0.7f)

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
            Button(
                text = stringResource(Res.string.my_issues_retry),
                onClick = onRetry,
                modifier = Modifier.testTag("issues_retry_button"),
            )
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
        IssueFilter.MyIssues -> Res.string.my_issues_filter_my_issues
        IssueFilter.MyResolved -> Res.string.my_issues_filter_my_resolved
        IssueFilter.MyWork -> Res.string.my_issues_filter_my_work
        IssueFilter.MyCompleted -> Res.string.my_issues_filter_my_completed
        IssueFilter.Open -> Res.string.my_issues_filter_open
        IssueFilter.All -> Res.string.my_issues_filter_all
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
                selectedFilter = IssueFilter.MyIssues,
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
                selectedFilter = IssueFilter.MyWork,
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
