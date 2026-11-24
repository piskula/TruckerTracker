package com.momosi.trucktrack.core.push.message

import com.momosi.trucktrack.core.common.coroutines.runCatchingCancellable
import com.momosi.trucktrack.core.common.logger.Logger
import com.momosi.trucktrack.core.push.firebase.PushMessagingClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

private const val TAG = "PushMessageDispatcher"
private const val MESSAGE_BUFFER = 8

internal class PushMessageDispatcherImpl(messagingClient: PushMessagingClient, private val handlers: List<PushMessageHandler>, scope: CoroutineScope) : PushMessageDispatcher {

    override val messages: Flow<IncomingPushMessage>
        field = MutableSharedFlow<IncomingPushMessage>(extraBufferCapacity = MESSAGE_BUFFER)

    init {
        messagingClient.messages
            .map { it.toIncomingPushMessage() }
            .onEach { dispatch(it) }
            .launchIn(scope)
    }

    private suspend fun dispatch(message: IncomingPushMessage) {
        Logger.i(TAG, "Push message received: ${message.type}")
        messages.tryEmit(message)
        handlers
            .filter { it.type == message.type }
            .forEach { handler ->
                runCatchingCancellable { handler.handle(message) }
                    .onFailure { Logger.e(TAG, it, "Handling the ${message.type} push message failed") }
            }
    }
}
