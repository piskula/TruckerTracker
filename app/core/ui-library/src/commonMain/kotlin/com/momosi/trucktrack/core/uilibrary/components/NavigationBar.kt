package com.momosi.trucktrack.core.uilibrary.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.navigation3.runtime.NavKey
import kotlinx.collections.immutable.ImmutableList

@Stable
data class NavigationBarItem(val navKey: NavKey, val selectedIcon: ImageVector, val unselectedIcon: ImageVector, val title: String)

@Composable
fun NavigationBar(
    items: ImmutableList<NavigationBarItem>,
    selectedKey: NavKey,
    isVisible: Boolean,
    onSelectKey: (NavKey) -> Unit,
    modifier: Modifier = Modifier,
) {
    BottomAppVarVisibility(
        isVisible = isVisible,
        modifier = modifier
            .fillMaxWidth()
            .background(BottomAppBarDefaults.containerColor),
    ) {
        BottomAppBar {
            items.forEach { item ->
                val isSelected = item.navKey == selectedKey
                NavigationBarItem(
                    selected = isSelected,
                    onClick = { onSelectKey(item.navKey) },
                    modifier = Modifier.testTag("nav_bar_item_${item.navKey::class.simpleName}"),
                    icon = {
                        Icon(
                            imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                            contentDescription = item.title,
                        )
                    },
                    label = {
                        Text(item.title)
                    },
                )
            }
        }
    }
}

@Composable
private fun BottomAppVarVisibility(
    isVisible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    AnimatedVisibility(
        visible = isVisible,
        modifier = modifier,
        enter = expandVertically(
            animationSpec = tween(
                durationMillis = 200,
                delayMillis = 100,
            ),
        ) + fadeIn(
            animationSpec = tween(
                durationMillis = 200,
                delayMillis = 100,
            ),
        ),
        exit = shrinkVertically(
            animationSpec = tween(
                durationMillis = 200,
                delayMillis = 100,
            ),
        ) + fadeOut(
            animationSpec = tween(
                durationMillis = 200,
                delayMillis = 100,
            ),
        ),
    ) {
        content()
    }
}
