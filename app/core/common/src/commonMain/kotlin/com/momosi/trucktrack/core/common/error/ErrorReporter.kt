package com.momosi.trucktrack.core.common.error

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

interface ErrorReporter {
    val errors: Flow<Throwable>
    fun report(error: Throwable)
}

internal class ErrorReporterImpl : ErrorReporter {
    private val _errors = MutableSharedFlow<Throwable>(extraBufferCapacity = 8)
    override val errors: Flow<Throwable> = _errors.asSharedFlow()

    override fun report(error: Throwable) {
        _errors.tryEmit(error)
    }
}
