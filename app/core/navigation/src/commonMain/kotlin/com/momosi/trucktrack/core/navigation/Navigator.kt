package com.momosi.trucktrack.core.navigation

import androidx.compose.runtime.snapshots.Snapshot
import androidx.navigation3.runtime.NavKey

class Navigator(val navigationState: NavigationState) {

    fun navigate(key: NavKey) {
        navigationState.backStack.add(key)
    }

    fun navigate(root: NavKey, destination: NavKey) {
        if (navigationState.currentKey == destination) return
        val keys = if (root == destination) listOf(root) else listOf(root, destination)
        Snapshot.withMutableSnapshot {
            navigationState.backStack.clear()
            navigationState.backStack.addAll(keys)
        }
    }

    fun navigateToRoot(key: NavKey) {
        navigationState.backStack.clear()
        navigationState.backStack.add(key)
    }

    fun goBack() {
        if (navigationState.backStack.size <= 1) return
        navigationState.backStack.removeLastOrNull()
    }
}
