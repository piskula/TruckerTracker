package com.momosi.trucktrack.core.uilibrary

import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowInsetsControllerCompat

@Composable
actual fun DarkBackgroundSystemBars() {
    val view = LocalView.current
    val activity = LocalActivity.current ?: return
    if (view.isInEditMode) return

    DisposableEffect(view, activity) {
        val controller = WindowInsetsControllerCompat(activity.window, view)
        val previousStatusBarLight = controller.isAppearanceLightStatusBars
        val previousNavigationBarLight = controller.isAppearanceLightNavigationBars

        controller.isAppearanceLightStatusBars = false
        controller.isAppearanceLightNavigationBars = false

        onDispose {
            controller.isAppearanceLightStatusBars = previousStatusBarLight
            controller.isAppearanceLightNavigationBars = previousNavigationBarLight
        }
    }
}
