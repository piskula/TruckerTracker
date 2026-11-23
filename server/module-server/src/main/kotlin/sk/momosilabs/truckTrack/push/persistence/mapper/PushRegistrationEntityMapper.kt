package sk.momosilabs.truckTrack.push.persistence.mapper

import sk.momosilabs.truckTrack.account.entity.AccountEntity
import sk.momosilabs.truckTrack.push.entity.PushRegistrationEntity
import sk.momosilabs.truckTrack.push.model.PushRegistrationModel
import sk.momosilabs.truckTrack.util.toUtcLocalDateTime

fun PushRegistrationModel.asNewEntity(account: AccountEntity) = PushRegistrationEntity(
    account = account,
    installationId = installationId,
    platform = platform,
    deviceLabel = deviceLabel,
    registeredAtUtc = registeredAt.toUtcLocalDateTime(),
)
