package com.momosi.trucktrack.core.uilibrary.icons.vectors

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

internal val Stat2VectorIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "Stat2",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        addPath(
            pathData = PathParser().parsePathString(
                "M6,17.59 L7.41,19 L12,14.42 L16.59,19 L18,17.59 L12,11.59 Z" +
                    " M6,11.59 L7.41,13 L12,8.42 L16.59,13 L18,11.59 L12,5.59 Z",
            ).toNodes().toList(),
            fill = SolidColor(Color.Black),
        )
    }.build()
}
