package com.momosi.trucktrack.core.uilibrary.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.momosi.trucktrack.core.uilibrary.theme.AppTheme
import com.momosi.trucktrack.core.uilibrary.theme.TruckTrackTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun TextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = AppTheme.typography.bodyLarge.copy(color = AppTheme.colors.onSurface),
    enabled: Boolean = true,
    singleLine: Boolean = false,
    minLines: Int = 1,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    decorationBox: @Composable (innerTextField: @Composable () -> Unit) -> Unit = { innerTextField -> innerTextField() },
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        textStyle = textStyle,
        cursorBrush = SolidColor(AppTheme.colors.onSurface),
        enabled = enabled,
        singleLine = singleLine,
        minLines = minLines,
        keyboardOptions = keyboardOptions,
        decorationBox = decorationBox,
    )
}

@Preview
@Composable
private fun TextFieldPreview() {
    TruckTrackTheme {
        TextField(value = "Flat tire on trailer 42", onValueChange = {}, modifier = Modifier.padding(16.dp))
    }
}

@Preview
@Composable
private fun TextFieldEmptyPreview() {
    TruckTrackTheme {
        TextField(value = "", onValueChange = {}, modifier = Modifier.padding(16.dp))
    }
}
