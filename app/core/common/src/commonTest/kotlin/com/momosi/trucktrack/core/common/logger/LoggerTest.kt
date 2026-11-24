package com.momosi.trucktrack.core.common.logger

import co.touchlab.kermit.platformLogWriter
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import co.touchlab.kermit.Logger as KermitLogger

class LoggerTest {
    private val originalWriters = KermitLogger.config.logWriterList
    private val originalMinSeverity = KermitLogger.config.minSeverity

    @AfterTest
    fun restoreLogging() {
        KermitLogger.setLogWriters(originalWriters)
        KermitLogger.setMinSeverity(originalMinSeverity)
    }

    @Test
    fun consoleInitializationReplacesDefaultWritersWithoutAccumulatingDuplicates() {
        KermitLogger.setLogWriters(platformLogWriter())

        Logger.init(logToConsole = true)

        assertEquals(1, KermitLogger.config.logWriterList.size)

        Logger.init(logToConsole = true)

        assertEquals(1, KermitLogger.config.logWriterList.size)
    }

    @Test
    fun disablingConsoleLoggingRemovesExistingConsoleWriters() {
        Logger.init(logToConsole = true)

        Logger.init(logToConsole = false)

        assertTrue(KermitLogger.config.logWriterList.isEmpty())
    }
}
