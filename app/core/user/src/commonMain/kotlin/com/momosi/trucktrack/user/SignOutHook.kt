package com.momosi.trucktrack.user

fun interface SignOutHook {
    suspend fun beforeSessionCleared()
}
