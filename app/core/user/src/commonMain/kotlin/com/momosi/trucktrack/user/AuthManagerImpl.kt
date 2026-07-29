package com.momosi.trucktrack.user

import com.momosi.trucktrack.core.common.coroutines.mapCatchingCancellable
import com.momosi.trucktrack.core.common.coroutines.runCatchingCancellable
import com.momosi.trucktrack.core.common.logger.Logger
import com.momosi.trucktrack.core.common.network.ConnectivityManager
import com.momosi.trucktrack.user.internal.TokenVerifier
import com.momosi.trucktrack.user.internal.UserStorage
import com.momosi.trucktrack.user.model.AuthActionResult
import com.momosi.trucktrack.user.model.AuthenticationState
import com.momosi.trucktrack.user.model.TokenResponse
import com.momosi.trucktrack.user.model.User
import com.momosi.trucktrack.user.model.UserRole
import com.momosi.trucktrack.user.model.toStoredTokens
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.publicvalue.multiplatform.oidc.OpenIdConnectClient
import org.publicvalue.multiplatform.oidc.OpenIdConnectException
import org.publicvalue.multiplatform.oidc.flows.CodeAuthFlowFactory
import org.publicvalue.multiplatform.oidc.types.Jwt
import org.publicvalue.multiplatform.oidc.types.remote.AccessTokenResponse

private const val TAG = "UserAuthManager"

class AuthManagerImpl(
    private val connectivityManager: ConnectivityManager,
    private val authFlowFactory: CodeAuthFlowFactory,
    private val client: OpenIdConnectClient,
    private val userStorage: UserStorage,
    private val tokenVerifier: TokenVerifier,
    appCoroutineScope: CoroutineScope,
) : AuthManager {

    override val authenticationState: StateFlow<AuthenticationState>
        field = MutableStateFlow(
            if (userStorage.tokens != null) AuthenticationState.Authorized else AuthenticationState.Guest,
        )

    override val authenticationInProgress: StateFlow<Boolean>
        field = MutableStateFlow(false)

    private val refreshTokenMutex = Mutex()

    init {
        authenticationState
            .onEach {
                val authState = when (it) {
                    AuthenticationState.Guest -> "Guest"
                    AuthenticationState.Authorized -> "Authorized"
                }
                Logger.i(TAG, "Auth state: $authState")
            }
            .launchIn(appCoroutineScope)

        appCoroutineScope.launch {
            resumePendingFlowsIfAny()
        }
    }

    private suspend fun resumePendingFlowsIfAny() {
        runCatchingCancellable {
            val authFlow = authFlowFactory.createAuthFlow(client)
            if (authFlow.canContinueLogin()) {
                Logger.d(TAG, "Resuming pending login after process restart")
                verifyAndStore(authFlow.continueLogin())
                authenticationState.value = AuthenticationState.Authorized
            }
        }.onFailure { Logger.e(TAG, it, "Failed to resume pending login") }

        runCatchingCancellable {
            val endSessionFlow = authFlowFactory.createEndSessionFlow(client)
            if (endSessionFlow.canContinueLogout()) {
                Logger.d(TAG, "Resuming pending logout after process restart")
                endSessionFlow.continueLogout()
                invalidateAuth()
            }
        }.onFailure { Logger.e(TAG, it, "Failed to resume pending logout") }
    }

    override suspend fun signIn(): AuthActionResult {
        if (!connectivityManager.isNetworkAvailable) {
            return AuthActionResult.Failed.NoInternet
        }

        authenticationInProgress.value = true
        Logger.d(TAG, "Start authorization web flow")

        return runCatchingCancellable {
            val authFlow = authFlowFactory.createAuthFlow(client)
            authFlow.getAccessToken(configureAuthUrl = { parameters.append("prompt", "login") })
        }
            .onSuccess { Logger.d(TAG, "Authorization finished with success response") }
            .mapCatchingCancellable { tokens ->
                verifyAndStore(tokens)
                authenticationInProgress.value = false
                authenticationState.value = AuthenticationState.Authorized
                AuthActionResult.Success(AuthenticationState.Authorized)
            }
            .getOrElse { exception ->
                authenticationInProgress.value = false
                when (exception) {
                    is OpenIdConnectException.AuthenticationCancelled -> {
                        Logger.d(TAG, "User closed auth flow")
                        AuthActionResult.Failed.UserCancelled
                    }

                    is IllegalStateException -> {
                        Logger.e(TAG, exception, "No activity available to start auth flow")
                        AuthActionResult.Failed.NoActivity
                    }

                    else -> {
                        Logger.e(TAG, exception, "Authorization failed")
                        invalidateAuth()
                        AuthActionResult.Failed.Error(exception)
                    }
                }
            }
    }

    override suspend fun signOut() {
        if (authenticationState.value != AuthenticationState.Authorized) return

        if (!connectivityManager.isNetworkAvailable) {
            invalidateAuth()
            return
        }

        authenticationInProgress.value = true
        val idToken = userStorage.tokens?.idToken

        Logger.d(TAG, "Start end session")

        runCatchingCancellable {
            authFlowFactory.createEndSessionFlow(client).endSession(idToken)
        }
            .onSuccess {
                Logger.d(TAG, "End session successful")
                invalidateAuth()
            }
            .onFailure { exception ->
                if (exception is OpenIdConnectException.AuthenticationCancelled) {
                    Logger.d(TAG, "User cancelled end session")
                    authenticationInProgress.value = false
                } else {
                    Logger.e(TAG, exception, "End session failed")
                    invalidateAuth()
                }
            }
    }

    override suspend fun token(): TokenResponse = when (authenticationState.value) {
        AuthenticationState.Authorized -> getFreshAccessToken()
        AuthenticationState.Guest -> TokenResponse.GuestWithoutToken
    }

    private suspend fun getFreshAccessToken(): TokenResponse = refreshTokenMutex.withLock {
        val tokens = userStorage.tokens
            ?: return TokenResponse.TokenError(IllegalStateException("Authorized user without stored tokens"))

        if (!tokens.needsRefresh()) {
            return TokenResponse.Token(tokens.accessToken)
        }

        val refreshToken = tokens.refreshToken ?: run {
            Logger.e(TAG, "Token needs refresh but no refresh token is available")
            invalidateAuth()
            return TokenResponse.TokenError(IllegalStateException("No refresh token available"))
        }

        Logger.d(TAG, "Auth needs token refresh")

        runCatchingCancellable { client.refreshToken(refreshToken = refreshToken) }
            .onSuccess { Logger.d(TAG, "Auth refresh token successful") }
            .mapCatchingCancellable {
                verifyAndStore(it)
                TokenResponse.Token(it.access_token)
            }
            .getOrElse { exception ->
                Logger.e(TAG, exception, "Auth refresh token error")
                if (exception is OpenIdConnectException.UnsuccessfulTokenRequest) {
                    invalidateAuth()
                }
                TokenResponse.TokenError(exception)
            }
    }

    private suspend fun verifyAndStore(tokens: AccessTokenResponse) {
        val jwt = tokenVerifier.verifyAndParse(tokens.access_token).getOrThrow()
        Logger.d(TAG, "Token verified")
        userStorage.user = jwt.toUser()
        userStorage.tokens = tokens.toStoredTokens()
    }

    private fun invalidateAuth() {
        userStorage.tokens = null
        userStorage.user = null
        authenticationInProgress.value = false
        authenticationState.value = AuthenticationState.Guest
    }
}

private fun Jwt.toUser(): User {
    val claims = payload.additionalClaims
    val roles = ((claims["realm_access"] as? Map<*, *>)?.get("roles") as? List<*>)
        ?.filterIsInstance<String>()
        ?.filterNot { it.startsWith("default-roles") || it in setOf("offline_access", "uma_authorization") }
        .orEmpty()

    return User(
        id = payload.sub ?: "",
        name = claims["name"] as? String ?: "",
        email = claims["email"] as? String ?: "",
        roles = roles.mapNotNullTo(mutableSetOf()) { it.toUserRole() }.ifEmpty { setOf(UserRole.Driver) },
    )
}

private fun String.toUserRole(): UserRole? = when (this) {
    "ROLE_DRIVER" -> UserRole.Driver
    "ROLE_MECHANIC" -> UserRole.Mechanic
    else -> null
}
