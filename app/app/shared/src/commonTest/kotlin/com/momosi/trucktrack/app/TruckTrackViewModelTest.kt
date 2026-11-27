package com.momosi.trucktrack.app

import com.momosi.trucktrack.core.applinks.AppLinkAction
import com.momosi.trucktrack.core.applinks.AppLinksManager
import com.momosi.trucktrack.core.checklist.ChecklistRepository
import com.momosi.trucktrack.core.checklist.model.ChecklistQuestion
import com.momosi.trucktrack.core.checklist.model.ChecklistResponse
import com.momosi.trucktrack.core.checklist.model.ChecklistStatus
import com.momosi.trucktrack.core.checklist.model.ChecklistSubmission
import com.momosi.trucktrack.core.common.error.ErrorReporter
import com.momosi.trucktrack.core.push.permission.NotificationPermission
import com.momosi.trucktrack.core.push.permission.NotificationPermissionStatus
import com.momosi.trucktrack.feature.checklist.api.ChecklistNavKey
import com.momosi.trucktrack.feature.issues.api.IssuesNavKey
import com.momosi.trucktrack.user.AuthManager
import com.momosi.trucktrack.user.model.AuthActionResult
import com.momosi.trucktrack.user.model.AuthenticationState
import com.momosi.trucktrack.user.model.TokenResponse
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class TruckTrackViewModelTest {

    private val authenticationState = MutableStateFlow(AuthenticationState.Authorized)

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun aPendingChecklistIsCheckedAndOpenedOnceOnStartAndNotAgainOnResume() = runTest {
        val checklist = FakeChecklistRepository(ChecklistStatus.Pending)
        val (viewModel, requests) = startViewModel(checklist)

        viewModel.onResume()
        viewModel.onResume()

        assertEquals(1, requests.size)
        assertEquals(1, checklist.refreshCount)
    }

    @Test
    fun onlyAPendingChecklistIsOpened() = runTest {
        listOf(ChecklistStatus.NotRequired, ChecklistStatus.Unavailable).forEach { status ->
            val (_, requests) = startViewModel(FakeChecklistRepository(status))

            assertEquals(0, requests.size, status.toString())
        }
    }

    @Test
    fun signingOutClearsTheStatusAndTheNextLoginChecksAgain() = runTest {
        val checklist = FakeChecklistRepository(ChecklistStatus.Pending)
        val (_, requests) = startViewModel(checklist)

        authenticationState.value = AuthenticationState.Guest
        authenticationState.value = AuthenticationState.Authorized

        assertEquals(1, checklist.clearCount)
        assertEquals(2, requests.size)
    }

    @Test
    fun leavingTheIssuesListWhileTheCheckRunsCancelsTheAutomaticOpen() = runTest {
        val response = CompletableDeferred<ChecklistStatus>()
        val (viewModel, requests) = startViewModel(FakeChecklistRepository { response.await() })

        viewModel.onScreenChange(ChecklistNavKey)
        viewModel.onScreenChange(IssuesNavKey)
        response.complete(ChecklistStatus.Pending)

        assertEquals(0, requests.size)
    }

    private fun TestScope.startViewModel(checklist: ChecklistRepository): Pair<TruckTrackViewModel, List<Unit>> {
        val viewModel = TruckTrackViewModel(FakeAuthManager(authenticationState), FakeErrorReporter, FakeAppLinksManager, FakeNotificationPermission, checklist)
        val requests = mutableListOf<Unit>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.openChecklistRequests.collect { requests += it } }
        return viewModel to requests
    }

    private class FakeChecklistRepository(private val result: suspend () -> ChecklistStatus) : ChecklistRepository {
        constructor(status: ChecklistStatus) : this({ status })

        var refreshCount = 0
            private set
        var clearCount = 0
            private set

        override val status: StateFlow<ChecklistStatus> = MutableStateFlow(ChecklistStatus.Unknown)

        override suspend fun getQuestions(): Result<List<ChecklistQuestion>> = Result.success(emptyList())

        override suspend fun submitResponse(submission: ChecklistSubmission): Result<ChecklistResponse> = Result.failure(UnsupportedOperationException())

        override suspend fun refreshStatus(): Result<ChecklistStatus> {
            refreshCount++
            return Result.success(result())
        }

        override fun clearStatus() {
            clearCount++
        }
    }

    private class FakeAuthManager(override val authenticationState: StateFlow<AuthenticationState>) : AuthManager {
        override val authenticationInProgress: StateFlow<Boolean> = MutableStateFlow(false)
        override val loginCompletions: Flow<Unit> = emptyFlow()

        override suspend fun signIn(): AuthActionResult = AuthActionResult.Success(AuthenticationState.Authorized)

        override suspend fun signOut(): AuthActionResult = AuthActionResult.Success(AuthenticationState.Guest)

        override suspend fun token(): TokenResponse = TokenResponse.GuestWithoutToken

        override suspend fun invalidateSession(rejectedAccessToken: String) = Unit
    }

    private object FakeErrorReporter : ErrorReporter {
        override val errors: Flow<Throwable> = emptyFlow()

        override fun report(error: Throwable) = Unit
    }

    private object FakeAppLinksManager : AppLinksManager {
        override val actions: Flow<AppLinkAction> = emptyFlow()

        override fun openUrl(url: String): Boolean = false
    }

    private object FakeNotificationPermission : NotificationPermission {
        override val currentStatus: StateFlow<NotificationPermissionStatus?> = MutableStateFlow(NotificationPermissionStatus.Granted)

        override suspend fun status(): NotificationPermissionStatus = NotificationPermissionStatus.Granted

        override fun openSettings() = Unit
    }
}
