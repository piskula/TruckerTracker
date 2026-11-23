package com.momosi.trucktrack.core.applinks

import com.momosi.trucktrack.user.AuthManager
import com.momosi.trucktrack.user.model.AuthActionResult
import com.momosi.trucktrack.user.model.AuthenticationState
import com.momosi.trucktrack.user.model.TokenResponse
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AppLinksManagerTest {
    private val authManager = FakeAuthManager()
    private val appLinks: AppLinksManager = AppLinksManagerImpl(authManager)

    @Test
    fun acceptsIssueLinksWithOptionalTrailingSlashQueryAndFragment() = runTest {
        listOf(
            "https://transroute.org/issue/5",
            "https://transroute.org/issue/5/",
            "https://transroute.org/issue/5?source=email#details",
            "https://TRANSROUTE.ORG/issue/5",
            "https://transroute.org:443/issue/5",
        ).forEach { url ->
            assertTrue(appLinks.openUrl(url), url)
            assertEquals(AppLinkAction.Issue(5), appLinks.actions.first(), url)
        }
        assertTrue(appLinks.openUrl("https://transroute.org/issue/${Long.MAX_VALUE}"))
        assertEquals(AppLinkAction.Issue(Long.MAX_VALUE), appLinks.actions.first())
    }

    @Test
    fun rejectsOtherOriginsAndUnsupportedRoutes() {
        listOf(
            "http://transroute.org/issue/5",
            "https://sso.transroute.org/issue/5",
            "https://transroute.org.evil.example/issue/5",
            "https://transroute.org@evil.example/issue/5",
            "https://transroute.org:444/issue/5",
            "https://transroute.org/issues/5",
            "https://transroute.org/ISSUE/5",
            "https://transroute.org/issue/5/edit",
            "https://transroute.org/issue/5//",
            "https://transroute.org//issue/5",
            "https://transroute.org/profile",
            "https://transroute.org/vehicle/5",
            "https://name@transroute.org/issue/5",
        ).forEach { url -> assertFalse(appLinks.openUrl(url), url) }
    }

    @Test
    fun rejectsMissingNonPositiveAndOverflowingIds() {
        listOf("", "x", "0", "-1", "+1", "1.5", "9223372036854775808", "%35")
            .forEach { id -> assertFalse(appLinks.openUrl("https://transroute.org/issue/$id"), id) }
    }

    @Test
    fun rejectsMalformedUrlsWithoutThrowing() {
        listOf(
            "",
            "not a URL",
            "/issue/5",
            "//transroute.org/issue/5",
            "https://transroute.org:invalid/issue/5",
            "https://[invalid/issue/5",
            "https://transroute.org\\issue\\5",
            "https://transroute.org/issue/5\n",
        ).forEach { url -> assertFalse(appLinks.openUrl(url), url) }
    }

    @Test
    fun keepsLatestLinkUntilCollectorIsReady() = runTest {
        assertTrue(appLinks.openUrl("https://transroute.org/issue/5"))
        assertTrue(appLinks.openUrl("https://transroute.org/issue/6"))
        assertFalse(appLinks.openUrl("https://other.example/issue/7"))
        assertEquals(AppLinkAction.Issue(6), appLinks.actions.first())
    }

    @Test
    fun signedOutLinkIsDiscardedAndDoesNotReplayAfterSignIn() = runTest {
        authManager.authenticationState.value = AuthenticationState.Guest
        assertFalse(appLinks.openUrl("https://transroute.org/issue/5"))
        authManager.authenticationState.value = AuthenticationState.Authorized
        val delivered = mutableListOf<AppLinkAction>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            appLinks.actions.collect { delivered.add(it) }
        }
        assertTrue(delivered.isEmpty())
    }

    @Test
    fun queuedLinkIsDiscardedIfUserSignsOutBeforeDelivery() = runTest {
        val delivered = mutableListOf<AppLinkAction>()

        assertTrue(appLinks.openUrl("https://transroute.org/issue/5"))
        authManager.authenticationState.value = AuthenticationState.Guest
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            appLinks.actions.collect { delivered.add(it) }
        }
        authManager.authenticationState.value = AuthenticationState.Authorized
        assertTrue(delivered.isEmpty())
        assertTrue(appLinks.openUrl("https://transroute.org/issue/6"))
        assertEquals(listOf<AppLinkAction>(AppLinkAction.Issue(6)), delivered)
    }

    @Test
    fun signedInLinkIsDeliveredOnceAndConsumed() = runTest {
        val delivered = mutableListOf<AppLinkAction>()

        assertTrue(appLinks.openUrl("https://transroute.org/issue/5"))
        val collector = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            appLinks.actions.collect { delivered.add(it) }
        }
        assertEquals(listOf<AppLinkAction>(AppLinkAction.Issue(5)), delivered)
        collector.cancel()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            appLinks.actions.collect { delivered.add(it) }
        }
        assertEquals(listOf<AppLinkAction>(AppLinkAction.Issue(5)), delivered)
    }

    private class FakeAuthManager : AuthManager {
        override val authenticationState = MutableStateFlow(AuthenticationState.Authorized)
        override val authenticationInProgress = MutableStateFlow(false)

        override suspend fun signIn(): AuthActionResult = error("Not used")

        override suspend fun signOut(): Unit = error("Not used")

        override suspend fun token(): TokenResponse = error("Not used")

        override suspend fun invalidateSession(rejectedAccessToken: String): Unit = error("Not used")
    }
}
