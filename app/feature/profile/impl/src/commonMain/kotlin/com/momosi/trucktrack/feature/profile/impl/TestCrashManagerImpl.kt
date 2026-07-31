package com.momosi.trucktrack.feature.profile.impl

import com.momosi.trucktrack.core.common.environment.AppEnvironment
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

internal class TestCrashManagerImpl : TestCrashManager {

    private var tapCount = 0
    private var firstTapAt: Instant? = null

    override fun registerAppVersionTap() {
        if (!AppEnvironment.isDebug) return

        val now = Clock.System.now()
        val firstTap = firstTapAt
        if (firstTap == null || now - firstTap > 1.seconds) {
            firstTapAt = now
            tapCount = 1
            return
        }

        tapCount++
        if (tapCount >= 3) {
            tapCount = 0
            firstTapAt = null
            throw TestCrashException()
        }
    }
}
