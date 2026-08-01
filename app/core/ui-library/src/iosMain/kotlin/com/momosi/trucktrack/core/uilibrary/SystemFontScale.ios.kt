package com.momosi.trucktrack.core.uilibrary

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSOperationQueue
import platform.UIKit.UIApplication
import platform.UIKit.UIContentSizeCategory
import platform.UIKit.UIContentSizeCategoryAccessibilityExtraExtraExtraLarge
import platform.UIKit.UIContentSizeCategoryAccessibilityExtraExtraLarge
import platform.UIKit.UIContentSizeCategoryAccessibilityExtraLarge
import platform.UIKit.UIContentSizeCategoryAccessibilityLarge
import platform.UIKit.UIContentSizeCategoryAccessibilityMedium
import platform.UIKit.UIContentSizeCategoryDidChangeNotification
import platform.UIKit.UIContentSizeCategoryExtraExtraExtraLarge
import platform.UIKit.UIContentSizeCategoryExtraExtraLarge
import platform.UIKit.UIContentSizeCategoryExtraLarge
import platform.UIKit.UIContentSizeCategoryExtraSmall
import platform.UIKit.UIContentSizeCategoryLarge
import platform.UIKit.UIContentSizeCategoryMedium
import platform.UIKit.UIContentSizeCategorySmall

@Composable
actual fun rememberSystemFontScale(): Float {
    var scale by remember { mutableStateOf(UIApplication.sharedApplication.preferredContentSizeCategory.toFontScale()) }

    DisposableEffect(Unit) {
        val observer = NSNotificationCenter.defaultCenter.addObserverForName(
            name = UIContentSizeCategoryDidChangeNotification,
            `object` = null,
            queue = NSOperationQueue.mainQueue,
        ) {
            scale = UIApplication.sharedApplication.preferredContentSizeCategory.toFontScale()
        }
        onDispose { NSNotificationCenter.defaultCenter.removeObserver(observer) }
    }

    return scale
}

// UIContentSizeCategory has no built-in numeric scale; values are chosen to mirror the
// range Android's Configuration.fontScale exposes for its own text-size/display-size settings.
private fun UIContentSizeCategory.toFontScale(): Float = when (this) {
    UIContentSizeCategoryExtraSmall -> 0.8f
    UIContentSizeCategorySmall -> 0.85f
    UIContentSizeCategoryMedium -> 0.9f
    UIContentSizeCategoryLarge -> 1.0f
    UIContentSizeCategoryExtraLarge -> 1.1f
    UIContentSizeCategoryExtraExtraLarge -> 1.2f
    UIContentSizeCategoryExtraExtraExtraLarge -> 1.3f
    UIContentSizeCategoryAccessibilityMedium -> 1.4f
    UIContentSizeCategoryAccessibilityLarge -> 1.6f
    UIContentSizeCategoryAccessibilityExtraLarge -> 1.9f
    UIContentSizeCategoryAccessibilityExtraExtraLarge -> 2.2f
    UIContentSizeCategoryAccessibilityExtraExtraExtraLarge -> 2.5f
    else -> 1.0f
}
