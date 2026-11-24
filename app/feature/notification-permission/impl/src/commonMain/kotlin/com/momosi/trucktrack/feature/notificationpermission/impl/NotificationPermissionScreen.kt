package com.momosi.trucktrack.feature.notificationpermission.impl

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.momosi.trucktrack.core.push.permission.rememberNotificationPermissionRequester
import com.momosi.trucktrack.core.uilibrary.components.Button
import com.momosi.trucktrack.core.uilibrary.components.ButtonStyle
import com.momosi.trucktrack.core.uilibrary.components.Text
import com.momosi.trucktrack.core.uilibrary.illustrations.TruckTrackIllustrations
import com.momosi.trucktrack.core.uilibrary.theme.AppTheme
import com.momosi.trucktrack.feature.notificationpermission.impl.resources.Res
import com.momosi.trucktrack.feature.notificationpermission.impl.resources.notification_permission_allow
import com.momosi.trucktrack.feature.notificationpermission.impl.resources.notification_permission_open_settings
import com.momosi.trucktrack.feature.notificationpermission.impl.resources.notification_permission_rationale
import com.momosi.trucktrack.feature.notificationpermission.impl.resources.notification_permission_settings_hint
import com.momosi.trucktrack.feature.notificationpermission.impl.resources.notification_permission_sign_out
import com.momosi.trucktrack.feature.notificationpermission.impl.resources.notification_permission_title
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun NotificationPermissionScreen(viewModel: NotificationPermissionViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val requestPermission = rememberNotificationPermissionRequester(
        onResult = { viewModel.onAction(NotificationPermissionAction.RefreshPermission) },
    )

    LifecycleResumeEffect(viewModel) {
        viewModel.onAction(NotificationPermissionAction.RefreshPermission)
        onPauseOrDispose {}
    }

    NotificationPermissionContent(
        state = state,
        onRequestPermission = requestPermission,
        onAction = viewModel::onAction,
    )
}

@Composable
internal fun NotificationPermissionContent(
    state: NotificationPermissionState,
    onRequestPermission: () -> Unit,
    onAction: (NotificationPermissionAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppTheme.colors.surface)
            .statusBarsPadding()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        BoxWithConstraints(modifier = Modifier.weight(1f)) {
            NotificationPermissionExplanation(
                requestRoute = state.requestRoute,
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .heightIn(min = maxHeight)
                    .padding(vertical = 24.dp),
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Button(
                text = stringResource(state.requestRoute.primaryButtonText()),
                onClick = {
                    when (state.requestRoute) {
                        PermissionRequestRoute.SystemDialog -> onRequestPermission()
                        PermissionRequestRoute.Settings -> onAction(NotificationPermissionAction.OpenSettings)
                    }
                },
                enabled = !state.isSigningOut,
                modifier = Modifier.fillMaxWidth().testTag("notification_permission_primary_button"),
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                text = stringResource(Res.string.notification_permission_sign_out),
                onClick = { onAction(NotificationPermissionAction.SignOut) },
                loading = state.isSigningOut,
                style = ButtonStyle.Outlined,
                modifier = Modifier.fillMaxWidth().testTag("notification_permission_sign_out_button"),
            )
        }
    }
}

@Composable
private fun NotificationPermissionExplanation(requestRoute: PermissionRequestRoute, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Image(
            painter = painterResource(TruckTrackIllustrations.NotificationPermission),
            contentDescription = null,
            modifier = Modifier.size(220.dp),
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(Res.string.notification_permission_title),
            style = AppTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = AppTheme.colors.onSurface,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(Res.string.notification_permission_rationale),
            style = AppTheme.typography.bodyLarge,
            color = AppTheme.colors.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        if (requestRoute == PermissionRequestRoute.Settings) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(Res.string.notification_permission_settings_hint),
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.colors.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.testTag("notification_permission_settings_hint"),
            )
        }
    }
}

private fun PermissionRequestRoute.primaryButtonText() = when (this) {
    PermissionRequestRoute.SystemDialog -> Res.string.notification_permission_allow
    PermissionRequestRoute.Settings -> Res.string.notification_permission_open_settings
}
