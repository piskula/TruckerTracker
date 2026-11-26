package sk.momosilabs.truckTrack.checklist.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import sk.momosilabs.truckTrack.checklist.entity.ChecklistReminderStage
import sk.momosilabs.truckTrack.push.model.PushMessage
import sk.momosilabs.truckTrack.push.model.PushMessageType
import sk.momosilabs.truckTrack.push.service.PushNotificationService
import java.util.UUID

class ChecklistReminderDueListenerTest {

    private val sent = mutableListOf<Pair<UUID, PushMessage>>()
    private val listener = ChecklistReminderDueListener(object : PushNotificationService {
        override fun sendToAccount(accountId: UUID, message: PushMessage) {
            sent += accountId to message
        }

        override fun sendToDevice(firebaseInstallationId: String, message: PushMessage) = throw UnsupportedOperationException()
    })

    @Test
    fun `each stage sends its own checklist reminder text to the driver's account with the checklist deep link`() {
        ChecklistReminderStage.entries.forEach { stage ->
            listener.onChecklistReminderDue(ChecklistReminderDueEvent(accountId = DRIVER_ID, stage = stage))
        }

        assertThat(sent.map { it.first }).containsOnly(DRIVER_ID)
        assertThat(sent.map { it.second.type }).containsOnly(PushMessageType.CHECKLIST_REMINDER)
        assertThat(sent.map { it.second.data }).containsOnly(mapOf("deepLink" to "https://transroute.org/checklist"))
        assertThat(sent.map { it.second.title.key to it.second.body.key }).containsExactly(
            "push.checklist-reminder.week-start.title" to "push.checklist-reminder.week-start.body",
            "push.checklist-reminder.two-days-left.title" to "push.checklist-reminder.two-days-left.body",
        )
    }

    private companion object {
        val DRIVER_ID: UUID = UUID.fromString("33333333-3333-3333-3333-333333333333")
    }
}
