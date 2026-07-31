package com.momosi.trucktrack.core.uilibrary

import androidx.compose.runtime.Composable

@Composable
actual fun rememberSystemFontScale(): Float {
    // No-op: Android already threads Configuration.fontScale into LocalDensity automatically.
    return 1f
}
