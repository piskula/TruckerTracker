package com.momosi.trucktrack.feature.profile.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.momosi.trucktrack.core.common.coroutines.combine
import com.momosi.trucktrack.core.common.error.ErrorReporter
import com.momosi.trucktrack.core.common.formatter.DateFormatter
import com.momosi.trucktrack.core.common.language.AppLanguage
import com.momosi.trucktrack.core.common.language.LanguageRepository
import com.momosi.trucktrack.core.common.logger.Logger
import com.momosi.trucktrack.core.common.network.ApiException
import com.momosi.trucktrack.core.common.network.FailureReason
import com.momosi.trucktrack.core.common.network.toFailureReason
import com.momosi.trucktrack.core.common.version.AppVersionProvider
import com.momosi.trucktrack.core.common.version.VersionRepository
import com.momosi.trucktrack.core.push.firebase.FirebaseInstallationIdProvider
import com.momosi.trucktrack.core.push.registration.PushDeviceState
import com.momosi.trucktrack.core.push.registration.PushRegistrationManager
import com.momosi.trucktrack.user.AuthManager
import com.momosi.trucktrack.user.UserRepository
import com.momosi.trucktrack.user.model.AuthActionResult
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
    private val languageRepository: LanguageRepository,
    private val pushRegistrationManager: PushRegistrationManager,
    private val errorReporter: ErrorReporter,
    private val firebaseInstallationIdProvider: FirebaseInstallationIdProvider,
) : ViewModel() {

    private val _event = Channel<ProfileEvent>(Channel.BUFFERED)
    val event: Flow<ProfileEvent> = _event.receiveAsFlow()

    private val appVersion = "${appVersionProvider.versionName} (${appVersionProvider.versionCode})"

    private val isSigningOut = MutableStateFlow(false)
    private val isVersionDialogVisible = MutableStateFlow(false)
    private val serverVersion = MutableStateFlow<ServerVersionContent>(ServerVersionContent.Loading)
    private val firebaseInstallationId = MutableStateFlow<FirebaseInstallationIdContent>(FirebaseInstallationIdContent.Loading)
    private val isLanguageDialogVisible = MutableStateFlow(false)
    private val isRestartNoticeVisible = MutableStateFlow(false)
    private val pushLoadFailure = MutableStateFlow<FailureReason?>(null)
    private val pushRequest = MutableStateFlow<PushNotificationsRequest>(PushNotificationsRequest.Idle)

    private val pushNotifications: Flow<PushNotificationsState> = combine(
        pushRegistrationManager.state,
        pushLoadFailure,
        pushRequest,
    ) { deviceState, loadFailure, request ->
        PushNotificationsState(target = deviceState.toPushNotificationsTarget(loadFailure), request = request)
    }

    val state: StateFlow<ProfileState> = combine(
        userRepository.user,
        isSigningOut,
        isVersionDialogVisible,
        serverVersion,
        languageRepository.language,
        isLanguageDialogVisible,
        isRestartNoticeVisible,
        pushNotifications,
    ) { user, signingOut, versionDialogVisible, serverVersionState, language, languageDialogVisible, restartNoticeVisible, pushNotificationsState ->
        ProfileState(
            user = user,
            isSigningOut = signingOut,
            appVersion = appVersion,
            isVersionDialogVisible = versionDialogVisible,
            serverVersion = serverVersionState,
            language = language,
            isLanguageDialogVisible = languageDialogVisible,
            isRestartNoticeVisible = restartNoticeVisible,
            pushNotifications = pushNotificationsState,
        )
    }.combine(firebaseInstallationId) { profileState, firebaseInstallationIdContent ->
        profileState.copy(firebaseInstallationId = firebaseInstallationIdContent)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ProfileState(appVersion = appVersion),
    )

    init {
        refreshPushNotifications()
    }

    fun onAction(action: ProfileAction) {
        Logger.d("Action:Profile", action.toString())
        when (action) {
            is ProfileAction.SignOut -> signOut()
            is ProfileAction.ShowVersionInfo -> showVersionInfo()
            is ProfileAction.DismissVersionInfo -> isVersionDialogVisible.value = false
            is ProfileAction.TapAppVersion -> testCrashManager.registerAppVersionTap()
            is ProfileAction.ShowLanguageSelector -> isLanguageDialogVisible.value = true
            is ProfileAction.DismissLanguageSelector -> isLanguageDialogVisible.value = false
            is ProfileAction.SelectLanguage -> selectLanguage(action.language)
            is ProfileAction.DismissRestartNotice -> isRestartNoticeVisible.value = false
            is ProfileAction.ChangeReceivePushNotifications -> changeReceivePushNotifications(action.enabled)
            is ProfileAction.ConfirmPushNotificationsTakeover -> updatePushRegistration { pushRegistrationManager.registerThisDevice() }
            is ProfileAction.DismissPushNotificationsTakeover -> pushRequest.value = PushNotificationsRequest.Idle
            is ProfileAction.RetryPushNotifications -> refreshPushNotifications()
        }
    }

    private fun selectLanguage(language: AppLanguage) {
        languageRepository.setLanguage(language)
        isLanguageDialogVisible.value = false
        if (languageRepository.requiresRestartToApply) {
            isRestartNoticeVisible.value = true
        }
    }

    private fun showVersionInfo() {
        isVersionDialogVisible.value = true
        loadFirebaseInstallationId()
        if (serverVersion.value !is ServerVersionContent.Loaded) {
            loadServerVersion()
        }
    }

    private fun loadFirebaseInstallationId() {
        firebaseInstallationId.value = FirebaseInstallationIdContent.Loading
        viewModelScope.launch {
            firebaseInstallationId.value = firebaseInstallationIdProvider.currentFirebaseInstallationId()
                ?.let { FirebaseInstallationIdContent.Loaded(it) }
                ?: FirebaseInstallationIdContent.Unavailable
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

    private fun changeReceivePushNotifications(enabled: Boolean) {
        if (pushRequest.value != PushNotificationsRequest.Idle) return
        val currentDevice = pushRegistrationManager.state.value
        when {
            !enabled -> updatePushRegistration { pushRegistrationManager.unregisterThisDevice() }
            currentDevice is PushDeviceState.OtherDevice -> pushRequest.value = PushNotificationsRequest.ConfirmingTakeover(currentDevice.deviceName)
            else -> updatePushRegistration { pushRegistrationManager.registerThisDevice() }
        }
    }

    private fun updatePushRegistration(change: suspend () -> Result<Unit>) {
        viewModelScope.launch {
            pushRequest.value = PushNotificationsRequest.Updating
            change().onFailure { errorReporter.report(it) }
            loadPushDeviceState()
            pushRequest.value = PushNotificationsRequest.Idle
        }
    }

    private fun refreshPushNotifications() {
        pushLoadFailure.value = null
        viewModelScope.launch { loadPushDeviceState() }
    }

    private suspend fun loadPushDeviceState() {
        pushRegistrationManager.refreshState()
            .onSuccess { pushLoadFailure.value = null }
            .onFailure { pushLoadFailure.value = it.toFailureReason() }
    }

    private fun signOut() {
        viewModelScope.launch {
            isSigningOut.value = true
            val result = authManager.signOut()
            isSigningOut.value = false
            when (result) {
                is AuthActionResult.Success -> _event.send(ProfileEvent.NavigateToSignIn)
                AuthActionResult.Failed.NoInternet -> errorReporter.report(ApiException.NoConnection)
                else -> Unit
            }
        }
    }
}

private fun PushDeviceState.toPushNotificationsTarget(loadFailure: FailureReason?): PushNotificationsTarget = when (this) {
    PushDeviceState.Unknown -> loadFailure?.let { PushNotificationsTarget.LoadFailed(it) } ?: PushNotificationsTarget.Loading
    PushDeviceState.ThisDevice -> PushNotificationsTarget.ThisDevice
    is PushDeviceState.OtherDevice -> PushNotificationsTarget.OtherDevice(deviceName)
    PushDeviceState.None -> PushNotificationsTarget.NoDevice
}
