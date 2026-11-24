package com.momosi.trucktrack.core.push.message

import com.momosi.trucktrack.shared.push.PushMessageTypeDto

interface PushMessageHandler {

    val type: PushMessageTypeDto

    suspend fun handle(message: IncomingPushMessage)
}
