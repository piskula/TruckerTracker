package com.momosi.trucktrack.user

import com.momosi.trucktrack.user.model.AuthActionResult
import com.momosi.trucktrack.user.model.AuthenticationState
import com.momosi.trucktrack.user.model.TokenResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface AuthManager {

    val authenticationState: StateFlow<AuthenticationState>
    val authenticationInProgress: StateFlow<Boolean>
    val loginCompletions: Flow<Unit>

    suspend fun signIn(): AuthActionResult
    suspend fun signOut(): AuthActionResult

    suspend fun token(): TokenResponse
    suspend fun invalidateSession(rejectedAccessToken: String)
}
