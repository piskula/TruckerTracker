package com.momosi.trucktrack.core.uilibrary

import androidx.compose.ui.platform.Clipboard

expect suspend fun Clipboard.setText(text: String)
