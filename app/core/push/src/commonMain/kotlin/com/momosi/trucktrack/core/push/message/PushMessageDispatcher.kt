package com.momosi.trucktrack.core.push.message

import kotlinx.coroutines.flow.Flow

interface PushMessageDispatcher {

    val messages: Flow<IncomingPushMessage>
}
