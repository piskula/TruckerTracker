package com.momosi.trucktrack.app

import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.momosi.trucktrack.core.common.crashreporting.CrashReporting
import com.momosi.trucktrack.core.common.logger.Logger
import com.momosi.trucktrack.core.navigation.Navigator
import com.momosi.trucktrack.core.navigation.rememberNavigationState
import com.momosi.trucktrack.core.navigation.rememberResultStore
import com.momosi.trucktrack.core.navigation.toEntries
import com.momosi.trucktrack.core.uilibrary.components.ErrorSnackbarHost
import com.momosi.trucktrack.core.uilibrary.components.rememberErrorSnackbarHostState
import com.momosi.trucktrack.core.uilibrary.modifier.LocalSharedTransitionScope
import com.momosi.trucktrack.core.uilibrary.theme.TruckTrackTheme
import com.momosi.trucktrack.feature.issues.api.IssuesNavKey
import com.momosi.trucktrack.feature.issues.impl.navigation.issuesEntries
import com.momosi.trucktrack.feature.profile.impl.navigation.profileEntries
import com.momosi.trucktrack.feature.signin.api.SignInNavKey
import com.momosi.trucktrack.feature.signin.impl.navigation.signInEntries
import com.momosi.trucktrack.user.model.AuthenticationState
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun TruckTrackApp(modifier: Modifier = Modifier, viewModel: TruckTrackViewModel = koinViewModel()) {
    val startKey: NavKey = remember {
        when (viewModel.authenticationState.value) {
            AuthenticationState.Authorized -> IssuesNavKey
            AuthenticationState.Guest -> SignInNavKey
        }
    }

    val navigationState = rememberNavigationState(
        startKey = startKey,
        savedStateConfiguration = appSavedStateConfiguration,
    )
    val navigator = remember { Navigator(navigationState) }
    val resultStore = rememberResultStore()
    val errorSnackbarHostState = rememberErrorSnackbarHostState()

    LaunchedEffect(navigationState.currentKey) {
        Logger.i("Nav", "-> ${navigationState.currentKey}")
        CrashReporting.setCustomKey("screen", navigationState.currentKey.toString())
    }

    LaunchedEffect(Unit) {
        viewModel.errors.collect { error -> errorSnackbarHostState.showError(error) }
    }

    TruckTrackTheme {
        SharedTransitionLayout(
            modifier = modifier,
        ) {
            CompositionLocalProvider(
                LocalSharedTransitionScope provides this,
            ) {
                Scaffold(
                    modifier = Modifier.fillMaxSize().imePadding(),
                    contentWindowInsets = WindowInsets(0, 0, 0, 0),
                    snackbarHost = { ErrorSnackbarHost(errorSnackbarHostState) },
                ) { _ ->
                    NavDisplay(
                        modifier = Modifier.fillMaxSize(),
                        entries = navigationState.toEntries(
                            entryProvider {
                                signInEntries(navigator)
                                issuesEntries(navigator, resultStore)
                                profileEntries(navigator)
                            },
                        ),
                        onBack = navigator::goBack,
                        sharedTransitionScope = this,
                    )
                }
            }
        }
    }
}
