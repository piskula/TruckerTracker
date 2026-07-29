package com.momosi.trucktrack.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable

interface ResultKey

private val ResultKey.identity: String
    get() = this::class.qualifiedName ?: error("ResultKey must not be an anonymous object")

@Stable
class ResultStore {
    private val results = mutableMapOf<String, Any?>()

    @Suppress("UNCHECKED_CAST")
    operator fun <T> get(key: ResultKey): T? = results[key.identity] as? T

    operator fun <T> set(key: ResultKey, value: T) {
        results[key.identity] = value
    }

    fun remove(key: ResultKey): Any? = results.remove(key.identity)

    companion object {
        val Saver = Saver<ResultStore, Map<String, Any?>>(
            save = { it.results.toMap() },
            restore = { ResultStore().apply { results.putAll(it) } },
        )
    }
}

@Composable
fun rememberResultStore(): ResultStore = rememberSaveable(
    saver = ResultStore.Saver,
) {
    ResultStore()
}
