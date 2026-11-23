package sk.momosilabs.truckTrack.push.service

import sk.momosilabs.truckTrack.push.model.PushRegistrationModel
import java.util.UUID

interface PushRegistrationPersistence {

    fun deleteRegistrationsOfAccountId(accountId: UUID)

    fun setNewRegistration(accountId: UUID, model: PushRegistrationModel)
}
