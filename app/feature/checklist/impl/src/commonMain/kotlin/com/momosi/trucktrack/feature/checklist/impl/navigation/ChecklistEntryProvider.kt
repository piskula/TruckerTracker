package com.momosi.trucktrack.feature.checklist.impl.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.momosi.trucktrack.core.navigation.Navigator
import com.momosi.trucktrack.core.uilibrary.animation.bottomEntryMetadata
import com.momosi.trucktrack.feature.checklist.api.ChecklistNavKey
import com.momosi.trucktrack.feature.checklist.impl.ChecklistScreen

fun EntryProviderScope<NavKey>.checklistEntries(navigator: Navigator) {
    entry<ChecklistNavKey>(metadata = bottomEntryMetadata()) {
        ChecklistScreen(onClose = navigator::goBack)
    }
}
