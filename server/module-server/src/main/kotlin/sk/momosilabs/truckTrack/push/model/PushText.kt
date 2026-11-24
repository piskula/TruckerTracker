package sk.momosilabs.truckTrack.push.model

data class PushText(
    val key: String,
    val arguments: List<String> = emptyList(),
)
