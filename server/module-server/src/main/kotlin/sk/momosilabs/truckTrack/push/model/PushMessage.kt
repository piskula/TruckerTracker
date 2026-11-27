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
        const val DEEP_LINK_DATA_KEY = "deepLink"
        const val CHECKLIST_DEEP_LINK = "https://transroute.org/checklist"

        fun deviceUnregistered(newDeviceName: String) = PushMessage(
            type = PushMessageType.DEVICE_UNREGISTERED,
            title = PushText(key = "push.device-unregistered.title"),
            body = PushText(key = "push.device-unregistered.body", arguments = listOf(newDeviceName)),
            data = mapOf(DEVICE_NAME_DATA_KEY to newDeviceName),
        )

        fun checklistReminderWeekStart() = checklistReminder(keyPrefix = "push.checklist-reminder.week-start")

        fun checklistReminderTwoDaysLeft() = checklistReminder(keyPrefix = "push.checklist-reminder.two-days-left")

        private fun checklistReminder(keyPrefix: String) = PushMessage(
            type = PushMessageType.CHECKLIST_REMINDER,
            title = PushText(key = "$keyPrefix.title"),
            body = PushText(key = "$keyPrefix.body"),
            data = mapOf(DEEP_LINK_DATA_KEY to CHECKLIST_DEEP_LINK),
        )
    }
}
