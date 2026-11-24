package com.momosi.trucktrack.core.uilibrary

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.Clipboard

@OptIn(ExperimentalComposeUiApi::class)
actual suspend fun Clipboard.setText(text: String) {
    setClipEntry(ClipEntry.withPlainText(text))
}
