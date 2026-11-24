package sk.momosilabs.truckTrack.push.model

data class PushMessage(
    val type: PushMessageType,
    val title: PushText,
    val body: PushText,
    val data: Map<String, String> = emptyMap(),
) {

    companion object {
        const val TYPE_DATA_KEY = "type"
        const val DEVICE_NAME_DATA_KEY = "deviceName"

        fun deviceUnregistered(newDeviceName: String) = PushMessage(
            type = PushMessageType.DEVICE_UNREGISTERED,
            title = PushText(key = "push.device-unregistered.title"),
            body = PushText(key = "push.device-unregistered.body", arguments = listOf(newDeviceName)),
            data = mapOf(DEVICE_NAME_DATA_KEY to newDeviceName),
        )
    }
}
