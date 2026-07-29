package com.momosi.trucktrack.feature.issues.impl.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.momosi.trucktrack.core.navigation.Navigator
import com.momosi.trucktrack.core.navigation.ResultKey
import com.momosi.trucktrack.core.navigation.ResultStore
import com.momosi.trucktrack.core.uilibrary.animation.slideFromEndEntryMetadata
import com.momosi.trucktrack.feature.issues.api.IssuesNavKey
import com.momosi.trucktrack.feature.issues.impl.FullScreenPhotoScreen
import com.momosi.trucktrack.feature.issues.impl.create.CreateIssueScreen
import com.momosi.trucktrack.feature.issues.impl.detail.IssueDetailScreen
import com.momosi.trucktrack.feature.issues.impl.list.IssuesScreen
import com.momosi.trucktrack.feature.profile.api.ProfileNavKey

private data object IssueStatusChangedKey : ResultKey

fun EntryProviderScope<NavKey>.issuesEntries(navigator: Navigator, resultStore: ResultStore) {
    entry<IssuesNavKey> {
        val statusChanged = resultStore[IssueStatusChangedKey] ?: false
        IssuesScreen(
            issueStatusChange = statusChanged,
            onNavigateToProfile = { navigator.navigate(ProfileNavKey) },
            onNavigateToCreateIssue = { navigator.navigate(CreateIssueNavKey) },
            onNavigateToIssueDetail = { issueId -> navigator.navigate(IssueDetailNavKey(issueId)) },
        )
    }
    entry<CreateIssueNavKey> {
        CreateIssueScreen(
            onBack = navigator::goBack,
            onIssueCreate = { issueId ->
                navigator.goBack()
                navigator.navigate(IssueDetailNavKey(issueId, justCreated = true))
            },
            onNavigateToFullScreenPhoto = { source ->
                navigator.navigate(FullScreenPhotoNavKey(source))
            },
        )
    }
    entry<IssueDetailNavKey>(metadata = slideFromEndEntryMetadata()) { key ->
        IssueDetailScreen(
            issueId = key.issueId,
            justCreated = key.justCreated,
            onBack = { shouldReload ->
                resultStore[IssueStatusChangedKey] = shouldReload
                navigator.goBack()
            },
            onNavigateToFullScreenPhoto = { url ->
                navigator.navigate(FullScreenPhotoNavKey(PhotoSource.Url(url)))
            },
        )
    }
    entry<FullScreenPhotoNavKey> { key ->
        FullScreenPhotoScreen(
            source = key.source,
            onBack = navigator::goBack,
        )
    }
}
