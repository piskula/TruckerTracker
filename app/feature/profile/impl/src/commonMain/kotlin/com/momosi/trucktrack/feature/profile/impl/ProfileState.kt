package com.momosi.trucktrack.feature.profile.impl

import androidx.compose.runtime.Immutable
import com.momosi.trucktrack.core.common.language.AppLanguage
import com.momosi.trucktrack.user.model.User

@Immutable
data class ProfileState(
    val user: User? = null,
    val isSigningOut: Boolean = false,
    val appVersion: String = "",
    val isVersionDialogVisible: Boolean = false,
    val serverVersion: ServerVersionContent = ServerVersionContent.Loading,
    val language: AppLanguage = AppLanguage.English,
    val isLanguageDialogVisible: Boolean = false,
    val isRestartNoticeVisible: Boolean = false,
)

sealed interface ServerVersionContent {
    data object Loading : ServerVersionContent
    data class Loaded(val text: String) : ServerVersionContent
    data object Error : ServerVersionContent
}
