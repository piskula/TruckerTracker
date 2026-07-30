package com.momosi.trucktrack.app

import androidx.lifecycle.ViewModel
import com.momosi.trucktrack.core.common.error.ErrorReporter
import com.momosi.trucktrack.user.AuthManager
import com.momosi.trucktrack.user.model.AuthenticationState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

class TruckTrackViewModel(authManager: AuthManager, errorReporter: ErrorReporter) : ViewModel() {

    val authenticationState: StateFlow<AuthenticationState> = authManager.authenticationState
    val errors: Flow<Throwable> = errorReporter.errors
}
