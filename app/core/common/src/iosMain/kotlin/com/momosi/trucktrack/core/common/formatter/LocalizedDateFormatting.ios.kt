package com.momosi.trucktrack.core.common.formatter

import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.dateWithTimeIntervalSince1970
import kotlin.time.Instant

internal actual fun createPlatformDateFormatter(): PlatformDateFormatter = IosPlatformDateFormatter()

private class IosPlatformDateFormatter : PlatformDateFormatter {

    override fun formatDateTime(instant: Instant): String {
        val formatter = NSDateFormatter()
        formatter.setLocalizedDateFormatFromTemplate("MMM d, j:mm")
        return formatter.stringFromDate(instant.toNSDate())
    }

    override fun formatShortDate(instant: Instant): String {
        val formatter = NSDateFormatter()
        formatter.setLocalizedDateFormatFromTemplate("MMM d")
        return formatter.stringFromDate(instant.toNSDate())
    }
}

private fun Instant.toNSDate(): NSDate = NSDate.dateWithTimeIntervalSince1970(epochSeconds.toDouble())
