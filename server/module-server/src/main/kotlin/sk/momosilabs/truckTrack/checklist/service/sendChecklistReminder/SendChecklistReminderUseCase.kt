package sk.momosilabs.truckTrack.checklist.service.sendChecklistReminder

import sk.momosilabs.truckTrack.checklist.entity.ChecklistReminderStage

interface SendChecklistReminderUseCase {

    fun send(stage: ChecklistReminderStage): Int
}
