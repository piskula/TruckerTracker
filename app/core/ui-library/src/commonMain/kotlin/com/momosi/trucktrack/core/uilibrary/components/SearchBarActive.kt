package com.momosi.trucktrack.core.uilibrary.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.momosi.trucktrack.core.uilibrary.icons.TruckTrackIcons
import com.momosi.trucktrack.core.uilibrary.modifier.sharedElement
import com.momosi.trucktrack.core.uilibrary.theme.AppTheme
import com.momosi.trucktrack.core.uilibrary.theme.TruckTrackTheme

@Composable
fun SearchBarActive(
    query: String,
    placeholder: String,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    autoFocus: Boolean = false,
    onQueryChange: (String) -> Unit = {},
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(autoFocus) {
        if (autoFocus) {
            focusRequester.requestFocus()
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .sharedElement(key = "search-bar")
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .background(
                color = AppTheme.colors.surfaceContainerLowest,
                shape = RoundedCornerShape(24.dp),
            )
            .padding(vertical = 8.dp),
    ) {
        Icon(
            imageVector = TruckTrackIcons.Search,
            contentDescription = null,
            tint = if (query.isEmpty()) AppTheme.colors.onSurfaceVariant else AppTheme.colors.onSurface,
            modifier = Modifier
                .padding(start = 16.dp)
                .size(24.dp),
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp),
        ) {
            val textStyle = AppTheme.typography.bodyLarge.copy(color = AppTheme.colors.onSurface)
            if (query.isEmpty()) {
                Text(
                    text = placeholder,
                    style = textStyle,
                    color = AppTheme.colors.onSurfaceVariant,
                )
            }
            TextField(
                value = query,
                onValueChange = onQueryChange,
                textStyle = textStyle,
                singleLine = true,
                keyboardOptions = keyboardOptions,
                modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
            )
        }
        if (query.isNotEmpty()) {
            IconButton(
                imageVector = TruckTrackIcons.Clear,
                onClick = { onQueryChange("") },
                style = IconButtonStyle.Standard,
                modifier = Modifier.padding(end = 8.dp).size(32.dp),
            )
        }
    }
}

@Preview
@Composable
private fun SearchBarActiveEmptyPreview() {
    TruckTrackTheme {
        SearchBarActive(
            query = "",
            onQueryChange = {},
            placeholder = "Search 342 apps…",
        )
    }
}

@Preview
@Composable
private fun SearchBarActiveWithQueryPreview() {
    TruckTrackTheme {
        SearchBarActive(
            query = "instagram",
            onQueryChange = {},
            placeholder = "Search apps",
        )
    }
}
