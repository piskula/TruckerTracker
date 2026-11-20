package com.momosi.trucktrack.user

import com.momosi.trucktrack.user.model.AuthActionResult
import com.momosi.trucktrack.user.model.AuthenticationState
import com.momosi.trucktrack.user.model.TokenResponse
import kotlinx.coroutines.flow.StateFlow

interface AuthManager {

    val authenticationState: StateFlow<AuthenticationState>
    val authenticationInProgress: StateFlow<Boolean>

    suspend fun signIn(): AuthActionResult
    suspend fun signOut()

    suspend fun token(): TokenResponse
    suspend fun invalidateSession(rejectedAccessToken: String)
}
