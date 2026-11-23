package com.momosi.trucktrack.core.navigation

import androidx.compose.runtime.snapshots.Snapshot
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import kotlin.test.Test
import kotlin.test.assertEquals

class NavigatorTest {
    @Test
    fun doesNotExposeIntermediateBackStackStates() {
        val backStack = NavBackStack<NavKey>(Other)
        val navigator = Navigator(NavigationState(Other, backStack))
        val observed = mutableListOf<List<NavKey>>()
        Snapshot.sendApplyNotifications()
        val observer = Snapshot.registerGlobalWriteObserver {
            observed.add(backStack.toList())
        }
        try {
            navigator.navigate(root = Root, destination = Detail(5))
        } finally {
            observer.dispose()
        }
        observed.add(backStack.toList())

        assertEquals(listOf(listOf<NavKey>(Root, Detail(5))), observed.distinct())
    }

    @Test
    fun opensDestinationUnderRequestedRootAndBackReturnsToRoot() {
        val backStack = NavBackStack<NavKey>(Other)
        val navigator = Navigator(NavigationState(Other, backStack))

        navigator.navigate(root = Root, destination = Detail(5))
        assertEquals(listOf<NavKey>(Root, Detail(5)), backStack.toList())
        navigator.goBack()
        assertEquals(Root, navigator.navigationState.currentKey)
    }

    @Test
    fun doesNotDuplicateCurrentDestination() {
        val backStack = NavBackStack<NavKey>(Root, Detail(5))
        val navigator = Navigator(NavigationState(Root, backStack))

        navigator.navigate(root = Root, destination = Detail(5))
        navigator.navigate(root = Root, destination = Detail(5))

        assertEquals(listOf<NavKey>(Root, Detail(5)), backStack.toList())
    }

    @Test
    fun replacesPreviousDestinationAndClearsUnrelatedHistory() {
        val backStack = NavBackStack<NavKey>(Root, Detail(5), Other)
        val navigator = Navigator(NavigationState(Root, backStack))

        navigator.navigate(root = Root, destination = Detail(6))

        assertEquals(listOf<NavKey>(Root, Detail(6)), backStack.toList())
    }

    @Test
    fun rootDestinationIsAddedOnlyOnce() {
        val backStack = NavBackStack<NavKey>(Other)
        val navigator = Navigator(NavigationState(Other, backStack))

        navigator.navigate(root = Root, destination = Root)

        assertEquals(listOf<NavKey>(Root), backStack.toList())
    }

    private data object Root : NavKey

    private data object Other : NavKey

    private data class Detail(val id: Long) : NavKey
}
