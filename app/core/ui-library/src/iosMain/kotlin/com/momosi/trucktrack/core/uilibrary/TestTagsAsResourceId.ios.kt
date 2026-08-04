package com.momosi.trucktrack.core.uilibrary

import androidx.compose.ui.Modifier

// No-op: testTagsAsResourceId is an Android-only semantics property (exposes testTag to
// UiAutomator/Espresso as resource-id); iOS UI testing uses accessibility identifiers instead.
actual fun Modifier.markTestTagsAsResourceId(): Modifier = this
