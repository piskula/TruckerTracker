package com.momosi.trucktrack.feature.signin.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.momosi.trucktrack.core.common.logger.Logger
import com.momosi.trucktrack.user.AuthManager
import com.momosi.trucktrack.user.model.AuthActionResult
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class SignInViewModel(private val authManager: AuthManager) : ViewModel() {

    private val _state = MutableStateFlow<SignInState>(SignInState.Idle)
    val state: StateFlow<SignInState> = _state

    private val _event = Channel<SignInEvent>(Channel.BUFFERED)
    val event = _event.receiveAsFlow()

    fun onAction(action: SignInAction) {
        Logger.d("Action:SignIn", action.toString())
        when (action) {
            SignInAction.SignInClick -> signIn()
        }
    }

    private fun signIn() {
        _state.value = SignInState.Loading
        viewModelScope.launch {
            _state.value = when (authManager.signIn()) {
                is AuthActionResult.Success -> {
                    _event.send(SignInEvent.NavigateToMyIssues)
                    SignInState.Idle
                }

                is AuthActionResult.Failed.UserCancelled -> SignInState.Idle

                is AuthActionResult.Failed.NoInternet -> SignInState.Error.NoInternet

                is AuthActionResult.Failed.NoActivity -> SignInState.Error.NoActivity

                is AuthActionResult.Failed.Error -> SignInState.Error.Generic
            }
        }
    }
}
