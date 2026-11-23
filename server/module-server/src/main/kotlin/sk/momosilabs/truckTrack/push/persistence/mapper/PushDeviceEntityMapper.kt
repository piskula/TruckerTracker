package sk.momosilabs.truckTrack.push.persistence.mapper

import sk.momosilabs.truckTrack.account.entity.AccountEntity
import sk.momosilabs.truckTrack.push.entity.PushDeviceEntity
import sk.momosilabs.truckTrack.push.model.PushDeviceModel
import sk.momosilabs.truckTrack.util.toUtcLocalDateTime
import sk.momosilabs.truckTrack.util.toUtcOffsetDateTime
import java.util.UUID

fun PushDeviceEntity.toModel() = PushDeviceModel(
    id = id,
    accountId = account.id,
    firebaseInstallationId = firebaseInstallationId,
    deviceName = deviceName,
    platform = platform,
    registeredAt = registeredAtUtc.toUtcOffsetDateTime(),
    updatedAt = updatedAtUtc.toUtcOffsetDateTime(),
)

fun PushDeviceModel.asNewEntity(accountResolver: (UUID) -> AccountEntity) = PushDeviceEntity(
    account = accountResolver(accountId),
    firebaseInstallationId = firebaseInstallationId,
    deviceName = deviceName,
    platform = platform,
    registeredAtUtc = registeredAt.toUtcLocalDateTime(),
    updatedAtUtc = updatedAt.toUtcLocalDateTime(),
)
