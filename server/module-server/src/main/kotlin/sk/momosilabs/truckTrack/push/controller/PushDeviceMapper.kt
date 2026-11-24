package sk.momosilabs.truckTrack.push.controller

import sk.momosilabs.truckTrack.api.push.dto.PushDeviceDto
import sk.momosilabs.truckTrack.api.push.dto.PushDeviceRegistrationDto
import sk.momosilabs.truckTrack.api.push.dto.PushPlatformDto
import sk.momosilabs.truckTrack.push.entity.PushPlatform
import sk.momosilabs.truckTrack.push.model.PushDeviceModel
import sk.momosilabs.truckTrack.push.service.registerPushDevice.RegisterPushDeviceCommand

fun PushDeviceRegistrationDto.toCommand() = RegisterPushDeviceCommand(
    firebaseInstallationId = firebaseInstallationId,
    deviceName = deviceName,
    platform = PushPlatform.valueOf(platform.name),
)

fun PushDeviceModel.toDto() = PushDeviceDto(
    firebaseInstallationId = firebaseInstallationId,
    deviceName = deviceName,
    platform = PushPlatformDto.valueOf(platform.name),
    registeredAt = registeredAt,
)
