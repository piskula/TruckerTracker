package com.momosi.trucktrack.feature.profile.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.momosi.trucktrack.core.common.formatter.DateFormatter
import com.momosi.trucktrack.core.common.logger.Logger
import com.momosi.trucktrack.core.common.version.AppVersionProvider
import com.momosi.trucktrack.core.common.version.VersionRepository
import com.momosi.trucktrack.user.AuthManager
import com.momosi.trucktrack.user.UserRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProfileViewModel(
    userRepository: UserRepository,
    private val authManager: AuthManager,
    private val versionRepository: VersionRepository,
    appVersionProvider: AppVersionProvider,
    private val dateFormatter: DateFormatter,
    private val testCrashManager: TestCrashManager,
) : ViewModel() {

    private val _event = Channel<ProfileEvent>(Channel.BUFFERED)
    val event: Flow<ProfileEvent> = _event.receiveAsFlow()

    private val appVersion = "${appVersionProvider.versionName} (${appVersionProvider.versionCode})"

    private val isSigningOut = MutableStateFlow(false)
    private val isVersionDialogVisible = MutableStateFlow(false)
    private val serverVersion = MutableStateFlow<ServerVersionContent>(ServerVersionContent.Loading)

    val state: StateFlow<ProfileState> = combine(
        userRepository.user,
        isSigningOut,
        isVersionDialogVisible,
        serverVersion,
    ) { user, signingOut, versionDialogVisible, serverVersionState ->
        ProfileState(
            user = user,
            isSigningOut = signingOut,
            appVersion = appVersion,
            isVersionDialogVisible = versionDialogVisible,
            serverVersion = serverVersionState,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ProfileState(appVersion = appVersion),
    )

    fun onAction(action: ProfileAction) {
        Logger.i("Action:Profile", action.toString())
        when (action) {
            is ProfileAction.SignOut -> signOut()
            is ProfileAction.ShowVersionInfo -> showVersionInfo()
            is ProfileAction.DismissVersionInfo -> isVersionDialogVisible.value = false
            is ProfileAction.TapAppVersion -> testCrashManager.registerAppVersionTap()
        }
    }

    private fun showVersionInfo() {
        isVersionDialogVisible.value = true
        if (serverVersion.value !is ServerVersionContent.Loaded) {
            loadServerVersion()
        }
    }

    private fun loadServerVersion() {
        viewModelScope.launch {
            serverVersion.value = versionRepository.getServerVersion().fold(
                onSuccess = { ServerVersionContent.Loaded("${it.version} · ${dateFormatter.formatShortDate(it.builtAt)}") },
                onFailure = { ServerVersionContent.Error },
            )
        }
    }

    private fun signOut() {
        viewModelScope.launch {
            isSigningOut.value = true
            authManager.signOut()
            isSigningOut.value = false
            _event.send(ProfileEvent.NavigateToSignIn)
        }
    }
}
