package com.momosi.trucktrack.core.uilibrary

import android.content.ClipData
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.Clipboard

actual suspend fun Clipboard.setText(text: String) {
    setClipEntry(ClipEntry(ClipData.newPlainText("", text)))
}
