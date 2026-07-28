package com.momosi.trucktrack.feature.issues.impl.detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.momosi.trucktrack.core.issue.model.IssueHistoryType
import com.momosi.trucktrack.core.issue.model.IssuePriority
import com.momosi.trucktrack.core.issue.model.IssueStatus
import com.momosi.trucktrack.core.uilibrary.BackHandler
import com.momosi.trucktrack.core.uilibrary.components.Button
import com.momosi.trucktrack.core.uilibrary.components.ButtonRole
import com.momosi.trucktrack.core.uilibrary.components.ButtonStyle
import com.momosi.trucktrack.core.uilibrary.components.ConfirmationDialog
import com.momosi.trucktrack.core.uilibrary.components.Icon
import com.momosi.trucktrack.core.uilibrary.components.LoadingSpinner
import com.momosi.trucktrack.core.uilibrary.components.Text
import com.momosi.trucktrack.core.uilibrary.components.Toolbar
import com.momosi.trucktrack.core.uilibrary.icons.TruckTrackIcons
import com.momosi.trucktrack.core.uilibrary.theme.AppTheme
import com.momosi.trucktrack.core.uilibrary.theme.Shapes
import com.momosi.trucktrack.core.uilibrary.theme.TruckTrackTheme
import com.momosi.trucktrack.core.vehicle.model.VehicleType
import com.momosi.trucktrack.feature.issues.impl.resources.Res
import com.momosi.trucktrack.feature.issues.impl.resources.issue_detail_assigned
import com.momosi.trucktrack.feature.issues.impl.resources.issue_detail_comment_placeholder
import com.momosi.trucktrack.feature.issues.impl.resources.issue_detail_description
import com.momosi.trucktrack.feature.issues.impl.resources.issue_detail_error
import com.momosi.trucktrack.feature.issues.impl.resources.issue_detail_history
import com.momosi.trucktrack.feature.issues.impl.resources.issue_detail_history_empty
import com.momosi.trucktrack.feature.issues.impl.resources.issue_detail_history_reassigned
import com.momosi.trucktrack.feature.issues.impl.resources.issue_detail_photos
import com.momosi.trucktrack.feature.issues.impl.resources.issue_detail_photos_loading
import com.momosi.trucktrack.feature.issues.impl.resources.issue_detail_reassign_description
import com.momosi.trucktrack.feature.issues.impl.resources.issue_detail_reassign_to_me
import com.momosi.trucktrack.feature.issues.impl.resources.issue_detail_reporter
import com.momosi.trucktrack.feature.issues.impl.resources.issue_detail_resolve_confirm_cancel
import com.momosi.trucktrack.feature.issues.impl.resources.issue_detail_resolve_confirm_confirm
import com.momosi.trucktrack.feature.issues.impl.resources.issue_detail_resolve_confirm_message
import com.momosi.trucktrack.feature.issues.impl.resources.issue_detail_resolve_confirm_title
import com.momosi.trucktrack.feature.issues.impl.resources.issue_detail_resolve_issue
import com.momosi.trucktrack.feature.issues.impl.resources.issue_detail_start_working
import com.momosi.trucktrack.feature.issues.impl.resources.issue_detail_title
import com.momosi.trucktrack.feature.issues.impl.resources.issue_priority_high
import com.momosi.trucktrack.feature.issues.impl.resources.issue_priority_low
import com.momosi.trucktrack.feature.issues.impl.resources.issue_priority_medium
import com.momosi.trucktrack.feature.issues.impl.resources.issue_status_done
import com.momosi.trucktrack.feature.issues.impl.resources.issue_status_in_progress
import com.momosi.trucktrack.feature.issues.impl.resources.issue_status_open
import com.momosi.trucktrack.feature.issues.impl.resources.my_issues_retry
import io.github.vinceglb.filekit.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.core.PickerType
import io.github.vinceglb.filekit.core.PlatformFile
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
internal fun IssueDetailScreen(
    issueId: Long,
    onBack: (shouldReload: Boolean) -> Unit,
    onNavigateToFullScreenPhoto: (String) -> Unit,
    justCreated: Boolean = false,
    viewModel: IssueDetailViewModel = koinViewModel(parameters = { parametersOf(issueId) }),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val shouldReload = state.statusChanged || justCreated
    BackHandler(enabled = shouldReload) {
        onBack(true)
    }
    IssueDetailScreenContent(
        state = state,
        onBack = { onBack(shouldReload) },
        onRetry = { viewModel.onAction(IssueDetailAction.Retry) },
        onUpdateComment = { viewModel.onAction(IssueDetailAction.UpdateComment(it)) },
        onSendComment = { viewModel.onAction(IssueDetailAction.SendComment) },
        onStartWorking = { viewModel.onAction(IssueDetailAction.StartWorking) },
        onResolveIssue = { viewModel.onAction(IssueDetailAction.ResolveIssue) },
        onReassignToMe = { viewModel.onAction(IssueDetailAction.ReassignToMe) },
        onUploadPhoto = { viewModel.onAction(IssueDetailAction.UploadPhoto(it)) },
        onNavigateToFullScreenPhoto = onNavigateToFullScreenPhoto,
    )
}

@Composable
private fun IssueDetailScreenContent(
    state: IssueDetailState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onUpdateComment: (String) -> Unit,
    onSendComment: () -> Unit,
    onStartWorking: () -> Unit,
    onResolveIssue: () -> Unit,
    onReassignToMe: () -> Unit,
    onUploadPhoto: (PlatformFile) -> Unit,
    onNavigateToFullScreenPhoto: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showResolveConfirmation by remember { mutableStateOf(false) }

    if (showResolveConfirmation) {
        ConfirmationDialog(
            title = stringResource(Res.string.issue_detail_resolve_confirm_title),
            message = stringResource(Res.string.issue_detail_resolve_confirm_message),
            confirmText = stringResource(Res.string.issue_detail_resolve_confirm_confirm),
            dismissText = stringResource(Res.string.issue_detail_resolve_confirm_cancel),
            onConfirm = {
                showResolveConfirmation = false
                onResolveIssue()
            },
            onDismiss = { showResolveConfirmation = false },
            confirmButtonRole = ButtonRole.Positive,
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppTheme.colors.surfaceContainer),
    ) {
        when (val content = state.content) {
            is IssueDetailContent.Loading -> {
                Toolbar(title = "", onBack = onBack)
                PeopleStrip(reportedByName = "", assignedToName = "")
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    LoadingSpinner()
                }
            }

            is IssueDetailContent.Error -> {
                Toolbar(title = "", onBack = onBack)
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = stringResource(Res.string.issue_detail_error),
                            style = AppTheme.typography.bodyLarge,
                            color = AppTheme.colors.onSurface,
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(text = stringResource(Res.string.my_issues_retry), onClick = onRetry)
                    }
                }
            }

            is IssueDetailContent.Loaded -> {
                LoadedContent(
                    issue = content.issue,
                    history = content.history,
                    photosContent = state.photosContent,
                    commentText = state.commentText,
                    isSendingComment = state.isSendingComment,
                    mechanicAction = state.mechanicAction,
                    isMechanicActionLoading = state.isMechanicActionLoading,
                    isUploadingPhoto = state.isUploadingPhoto,
                    onBack = onBack,
                    onUpdateComment = onUpdateComment,
                    onSendComment = onSendComment,
                    onStartWorking = onStartWorking,
                    onResolveIssue = { showResolveConfirmation = true },
                    onReassignToMe = onReassignToMe,
                    onUploadPhoto = onUploadPhoto,
                    onPhotoClick = onNavigateToFullScreenPhoto,
                )
            }
        }
    }
}

@Composable
private fun LoadedContent(
    issue: IssueUi,
    history: ImmutableList<IssueHistoryUi>,
    photosContent: IssuePhotosContent,
    commentText: String,
    isSendingComment: Boolean,
    mechanicAction: MechanicActionType?,
    isMechanicActionLoading: Boolean,
    isUploadingPhoto: Boolean,
    onBack: () -> Unit,
    onUpdateComment: (String) -> Unit,
    onSendComment: () -> Unit,
    onStartWorking: () -> Unit,
    onResolveIssue: () -> Unit,
    onReassignToMe: () -> Unit,
    onUploadPhoto: (PlatformFile) -> Unit,
    onPhotoClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val photoPickerLauncher = rememberFilePickerLauncher(
        type = PickerType.Image,
        onResult = { file -> file?.let { onUploadPhoto(it) } },
    )

    Column(modifier = modifier.fillMaxSize()) {
        Toolbar(title = stringResource(Res.string.issue_detail_title, issue.id), onBack = onBack)

        PeopleStrip(reportedByName = issue.reportedByName, assignedToName = issue.assignedToName)

        val navBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 12.dp + navBarBottom),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item { HeaderCard(issue = issue) }
            if (issue.description.isNotBlank()) {
                item { DescriptionCard(description = issue.description) }
            }
            item {
                AnimatedVisibility(
                    visible = mechanicAction == MechanicActionType.Reassign,
                ) {
                    ReassignCard(
                        isLoading = isMechanicActionLoading,
                        onReassignToMe = onReassignToMe,
                    )
                }
            }
            item {
                HistoryCard(
                    history = history,
                    commentText = commentText,
                    isSending = isSendingComment,
                    onUpdateComment = onUpdateComment,
                    onSend = onSendComment,
                )
            }
            item {
                PhotosCard(
                    photosContent = photosContent,
                    isUploading = isUploadingPhoto,
                    onPhotoClick = onPhotoClick,
                    onAddPhoto = { photoPickerLauncher.launch() },
                )
            }
        }

        if (mechanicAction != null) {
            val isImeVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
            AnimatedVisibility(
                visible = !isImeVisible,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                MechanicActionBar(
                    actionType = mechanicAction,
                    isLoading = isMechanicActionLoading,
                    onStartWorking = onStartWorking,
                    onResolveIssue = onResolveIssue,
                )
            }
        }
    }
}

@Composable
private fun PeopleStrip(
    reportedByName: String,
    assignedToName: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(AppTheme.colors.surfaceContainerHighest)
            .padding(horizontal = 16.dp, vertical = 10.dp),
    ) {
        PersonCell(
            role = stringResource(Res.string.issue_detail_reporter),
            name = reportedByName,
            icon = TruckTrackIcons.Edit,
            modifier = Modifier.weight(1f),
        )
        PersonCell(
            role = stringResource(Res.string.issue_detail_assigned),
            name = assignedToName,
            icon = TruckTrackIcons.AssignmentInd,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun PersonCell(
    role: String,
    name: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
    ) {
        Icon(imageVector = icon, tint = AppTheme.colors.onSurface, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = role.uppercase(),
                style = AppTheme.typography.labelSmall,
                color = AppTheme.colors.onSurfaceVariant,
            )
            Text(
                text = name,
                style = AppTheme.typography.bodySmall,
                color = AppTheme.colors.onSurface,
            )
        }
    }
}

@Composable
private fun HeaderCard(issue: IssueUi, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(Shapes.CardShape)
            .background(AppTheme.colors.surfaceContainerLowest, Shapes.CardShape)
            .padding(start = 12.dp, end = 14.dp, top = 12.dp, bottom = 12.dp),
    ) {
        Column {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = issue.title,
                    style = AppTheme.typography.titleSmall,
                    color = AppTheme.colors.onSurface,
                    modifier = Modifier.weight(1f),
                )
                StatusChip(status = issue.status)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PriorityIndicator(priority = issue.priority)
                if (issue.vehicleLabel.isNotEmpty()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                    ) {
                        Icon(imageVector = issue.vehicleType.vehicleIcon(), tint = AppTheme.colors.onSurfaceVariant, modifier = Modifier.size(15.dp))
                        Text(text = issue.vehicleLabel, style = AppTheme.typography.bodySmall, color = AppTheme.colors.onSurfaceVariant)
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = issue.createdAtFormatted,
                    style = AppTheme.typography.labelSmall,
                    color = AppTheme.colors.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun StatusChip(status: IssueStatus, modifier: Modifier = Modifier) {
    val containerColor = status.containerColor()
    val contentColor = status.contentColor()
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
            .background(containerColor, Shapes.CardShape)
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Icon(imageVector = status.icon(), tint = contentColor, modifier = Modifier.size(13.dp))
        Text(text = status.displayName().uppercase(), style = AppTheme.typography.labelSmall, color = contentColor)
    }
}

@Composable
private fun PriorityIndicator(priority: IssuePriority, modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        modifier = modifier,
    ) {
        Icon(imageVector = priority.indicatorIcon(), tint = priority.indicatorColor(), modifier = Modifier.size(15.dp))
        Text(text = priority.displayName().uppercase(), style = AppTheme.typography.labelSmall, color = priority.indicatorColor())
    }
}

@Composable
private fun DescriptionCard(description: String, modifier: Modifier = Modifier) {
    CardContainer(title = stringResource(Res.string.issue_detail_description), modifier = modifier) {
        Text(
            text = description,
            style = AppTheme.typography.bodyMedium,
            color = AppTheme.colors.onSurface,
        )
    }
}

@Composable
private fun HistoryCard(
    history: ImmutableList<IssueHistoryUi>,
    commentText: String,
    isSending: Boolean,
    onUpdateComment: (String) -> Unit,
    onSend: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val isImeVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    LaunchedEffect(isImeVisible, commentText) {
        if (isImeVisible) {
            delay(100)
            bringIntoViewRequester.bringIntoView()
        }
    }
    CardContainer(
        title = stringResource(Res.string.issue_detail_history),
        modifier = modifier.animateContentSize().bringIntoViewRequester(bringIntoViewRequester),
    ) {
        if (history.isEmpty()) {
            Text(
                text = stringResource(Res.string.issue_detail_history_empty),
                style = AppTheme.typography.bodySmall,
                color = AppTheme.colors.onSurfaceVariant,
            )
        } else {
            Column {
                history.forEachIndexed { index, entry ->
                    TimelineStep(entry = entry, isLast = index == history.lastIndex)
                }
            }
        }
        Spacer(modifier = Modifier.height(14.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(AppTheme.colors.surfaceVariant),
        )
        Spacer(modifier = Modifier.height(14.dp))
        val sendEnabled = !isSending && commentText.isNotBlank()
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppTheme.colors.surfaceContainer, RoundedCornerShape(10.dp))
                .padding(start = 12.dp, end = 12.dp, top = 10.dp, bottom = 10.dp),
        ) {
            BasicTextField(
                value = commentText,
                onValueChange = onUpdateComment,
                textStyle = AppTheme.typography.bodyMedium.copy(color = AppTheme.colors.onSurface),
                minLines = 2,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                decorationBox = { inner ->
                    if (commentText.isEmpty()) {
                        Text(
                            text = stringResource(Res.string.issue_detail_comment_placeholder),
                            style = AppTheme.typography.bodyMedium,
                            color = AppTheme.colors.onSurfaceVariant,
                        )
                    }
                    inner()
                },
            )
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.BottomEnd)
                    .clickable(enabled = sendEnabled && !isSending, onClick = onSend),
                contentAlignment = Alignment.Center,
            ) {
                if (isSending) {
                    LoadingSpinner(size = 18.dp, strokeWidth = 2.dp)
                } else {
                    Icon(
                        imageVector = TruckTrackIcons.Send,
                        tint = if (sendEnabled) AppTheme.colors.primary else AppTheme.colors.onSurfaceVariant,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun TimelineStep(
    entry: IssueHistoryUi,
    isLast: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier.height(IntrinsicSize.Min)) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .width(32.dp)
                .fillMaxHeight(),
        ) {
            when (entry.type) {
                IssueHistoryType.StatusChange -> {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(entry.statusTo.dotColor(), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = entry.statusTo.icon(),
                            tint = entry.statusTo.dotIconColor(),
                            modifier = Modifier.size(17.dp),
                        )
                    }
                }

                IssueHistoryType.Comment -> {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .background(AppTheme.colors.surfaceContainerHighest, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = TruckTrackIcons.Comment,
                            tint = AppTheme.colors.onSurfaceVariant,
                            modifier = Modifier.size(14.dp),
                        )
                    }
                }

                IssueHistoryType.AssigneeChange -> {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(AppTheme.colors.warning, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = TruckTrackIcons.EmojiPeople,
                            tint = AppTheme.colors.onWarning,
                            modifier = Modifier.size(17.dp),
                        )
                    }
                }
            }
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .width(2.dp)
                        .background(AppTheme.colors.surfaceContainerHighest),
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.padding(bottom = if (isLast) 0.dp else 18.dp)) {
            when (entry.type) {
                IssueHistoryType.StatusChange -> {
                    Text(
                        text = entry.statusTo.displayName(),
                        style = AppTheme.typography.titleSmall,
                        color = AppTheme.colors.onSurface,
                    )
                }

                IssueHistoryType.Comment -> {
                    Text(
                        text = entry.commentText ?: "",
                        style = AppTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
                        color = AppTheme.colors.onSurface,
                    )
                }

                IssueHistoryType.AssigneeChange -> {
                    Text(
                        text = stringResource(Res.string.issue_detail_history_reassigned),
                        style = AppTheme.typography.titleSmall,
                        color = AppTheme.colors.onSurface,
                    )
                }
            }
            val footer = listOfNotNull(entry.performedByName, entry.createdAtFormatted).joinToString(", ")
            Text(
                text = footer,
                style = AppTheme.typography.labelSmall.copy(fontWeight = FontWeight.Normal),
                color = AppTheme.colors.outline,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun PhotosCard(
    photosContent: IssuePhotosContent,
    isUploading: Boolean,
    onPhotoClick: (String) -> Unit,
    onAddPhoto: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val title = when (photosContent) {
        is IssuePhotosContent.Loading -> stringResource(Res.string.issue_detail_photos_loading)
        is IssuePhotosContent.Loaded -> stringResource(Res.string.issue_detail_photos, photosContent.items.size)
    }
    CardContainer(title = title, modifier = modifier) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (photosContent is IssuePhotosContent.Loaded) {
                photosContent.items.forEach { photo ->
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(AppTheme.colors.surfaceVariant)
                            .clickable { onPhotoClick(photo.url) },
                        contentAlignment = Alignment.BottomCenter,
                    ) {
                        AsyncImage(
                            model = photo.url,
                            contentDescription = photo.filename,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.matchParentSize(),
                        )
                    }
                }
            }
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(AppTheme.colors.surfaceVariant)
                    .clickable(enabled = !isUploading, onClick = onAddPhoto),
                contentAlignment = Alignment.Center,
            ) {
                if (isUploading) {
                    LoadingSpinner(size = 24.dp, strokeWidth = 2.dp)
                } else {
                    Icon(
                        imageVector = TruckTrackIcons.Add,
                        tint = AppTheme.colors.onSurfaceVariant,
                        modifier = Modifier.size(28.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun ReassignCard(
    isLoading: Boolean,
    onReassignToMe: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(Shapes.CardShape)
            .background(AppTheme.colors.surfaceContainerLowest, Shapes.CardShape)
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Text(
            text = stringResource(Res.string.issue_detail_reassign_description),
            style = AppTheme.typography.bodySmall,
            color = AppTheme.colors.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            text = stringResource(Res.string.issue_detail_reassign_to_me),
            onClick = onReassignToMe,
            loading = isLoading,
            icon = TruckTrackIcons.EmojiPeople,
            role = ButtonRole.Warning,
            style = ButtonStyle.Tonal,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
        )
    }
}

@Composable
private fun MechanicActionBar(
    actionType: MechanicActionType,
    isLoading: Boolean,
    onStartWorking: () -> Unit,
    onResolveIssue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(AppTheme.colors.surfaceContainerLowest)
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        when (actionType) {
            MechanicActionType.StartWorking -> Button(
                text = stringResource(Res.string.issue_detail_start_working),
                onClick = onStartWorking,
                loading = isLoading,
                icon = TruckTrackIcons.Build,
                role = ButtonRole.Warning,
                modifier = Modifier.fillMaxWidth(),
            )

            MechanicActionType.ResolveIssue -> Button(
                text = stringResource(Res.string.issue_detail_resolve_issue),
                onClick = onResolveIssue,
                loading = isLoading,
                icon = TruckTrackIcons.Check,
                role = ButtonRole.Positive,
                modifier = Modifier.fillMaxWidth(),
            )

            MechanicActionType.Reassign -> Unit
        }
    }
}

@Composable
private fun CardContainer(
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
            text = title,
            style = AppTheme.typography.labelLarge,
            color = AppTheme.colors.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 12.dp),
        )
        content()
    }
}

@Composable
private fun IssuePriority.indicatorColor(): Color = when (this) {
    IssuePriority.High -> AppTheme.colors.error
    IssuePriority.Medium -> AppTheme.colors.warning
    IssuePriority.Low -> AppTheme.colors.primary
}

@Composable
private fun IssuePriority.displayName(): String = stringResource(
    when (this) {
        IssuePriority.High -> Res.string.issue_priority_high
        IssuePriority.Medium -> Res.string.issue_priority_medium
        IssuePriority.Low -> Res.string.issue_priority_low
    },
)

private fun IssuePriority.indicatorIcon() = when (this) {
    IssuePriority.High -> TruckTrackIcons.Stat2
    IssuePriority.Medium -> TruckTrackIcons.Equal
    IssuePriority.Low -> TruckTrackIcons.ArrowDownward
}

@Composable
private fun IssueStatus.containerColor(): Color = when (this) {
    IssueStatus.Open -> AppTheme.colors.open
    IssueStatus.InProgress -> AppTheme.colors.warning
    IssueStatus.Done -> AppTheme.colors.positive
}

@Composable
private fun IssueStatus.contentColor(): Color = when (this) {
    IssueStatus.Open -> AppTheme.colors.onOpen
    IssueStatus.InProgress -> AppTheme.colors.onWarning
    IssueStatus.Done -> AppTheme.colors.onPositive
}

private fun IssueStatus?.icon() = when (this) {
    IssueStatus.Open -> TruckTrackIcons.RadioButtonUnchecked
    IssueStatus.InProgress -> TruckTrackIcons.Build
    IssueStatus.Done -> TruckTrackIcons.Check
    null -> TruckTrackIcons.RadioButtonUnchecked
}

@Composable
private fun IssueStatus?.displayName(): String = when (this) {
    IssueStatus.Open -> stringResource(Res.string.issue_status_open)
    IssueStatus.InProgress -> stringResource(Res.string.issue_status_in_progress)
    IssueStatus.Done -> stringResource(Res.string.issue_status_done)
    null -> ""
}

@Composable
private fun IssueStatus?.dotColor(): Color = when (this) {
    IssueStatus.Open -> AppTheme.colors.open
    IssueStatus.InProgress -> AppTheme.colors.warning
    IssueStatus.Done -> AppTheme.colors.positive
    null -> AppTheme.colors.surfaceVariant
}

@Composable
private fun IssueStatus?.dotIconColor(): Color = when (this) {
    IssueStatus.Open -> AppTheme.colors.onOpen
    IssueStatus.InProgress -> AppTheme.colors.onWarning
    IssueStatus.Done -> AppTheme.colors.onPositive
    else -> AppTheme.colors.onSurface
}

private fun VehicleType?.vehicleIcon() = when (this) {
    VehicleType.Trailer -> TruckTrackIcons.Trailer
    VehicleType.Truck, null -> TruckTrackIcons.Truck
}

private val previewIssue = IssueUi(
    id = 1042,
    title = "Engine warning light — truck won't start",
    description = "Tried to start the engine this morning and the warning light came on. The truck won't start at all. Located at depot gate 3.",
    status = IssueStatus.InProgress,
    priority = IssuePriority.High,
    vehicleLabel = "MA-204-TT · Volvo FH16",
    vehicleType = VehicleType.Truck,
    reportedByName = "Michael Schumacher",
    assignedToName = "Mattia Binotto",
    createdAtFormatted = "Jun 17, 08:00",
)

private val previewHistory = listOf(
    IssueHistoryUi("1", IssueHistoryType.StatusChange, IssueStatus.Open, "Michael Schumacher", "Jun 17, 08:00", null),
    IssueHistoryUi("2", IssueHistoryType.StatusChange, IssueStatus.InProgress, "Mattia Binotto", "Jun 17, 09:00", null),
    IssueHistoryUi("3", IssueHistoryType.AssigneeChange, null, "Lewis Hamilton", "Jun 17, 09:30", null),
    IssueHistoryUi("4", IssueHistoryType.Comment, null, "Mattia Binotto", "Jun 17, 10:00", "Issue diagnosed, spare parts ordered"),
    IssueHistoryUi("5", IssueHistoryType.Comment, null, "Mattia Binotto", "Jun 17, 13:00", "Parts order delayed, ETA tomorrow morning"),
).toImmutableList()

@Preview
@Composable
private fun IssueDetailLoadedPreview() {
    TruckTrackTheme {
        IssueDetailScreenContent(
            state = IssueDetailState(
                content = IssueDetailContent.Loaded(issue = previewIssue, history = previewHistory),
                photosContent = IssuePhotosContent.Loaded(),
                mechanicAction = MechanicActionType.Reassign,
            ),
            onBack = {},
            onRetry = {},
            onUpdateComment = {},
            onSendComment = {},
            onStartWorking = {},
            onResolveIssue = {},
            onReassignToMe = {},
            onUploadPhoto = {},
            onNavigateToFullScreenPhoto = {},
        )
    }
}

@Preview
@Composable
private fun IssueDetailHistoryEmptyPreview() {
    TruckTrackTheme {
        IssueDetailScreenContent(
            state = IssueDetailState(
                content = IssueDetailContent.Loaded(issue = previewIssue, history = persistentListOf()),
                photosContent = IssuePhotosContent.Loaded(),
            ),
            onBack = {},
            onRetry = {},
            onUpdateComment = {},
            onSendComment = {},
            onStartWorking = {},
            onResolveIssue = {},
            onReassignToMe = {},
            onUploadPhoto = {},
            onNavigateToFullScreenPhoto = {},
        )
    }
}

@Preview
@Composable
private fun IssueDetailFullLoadingPreview() {
    TruckTrackTheme {
        IssueDetailScreenContent(
            state = IssueDetailState(),
            onBack = {},
            onRetry = {},
            onUpdateComment = {},
            onSendComment = {},
            onStartWorking = {},
            onResolveIssue = {},
            onReassignToMe = {},
            onUploadPhoto = {},
            onNavigateToFullScreenPhoto = {},
        )
    }
}
