package sk.momosilabs.truckTrack.push.controller

import sk.momosilabs.truckTrack.api.push.dto.PushPlatformDto
import sk.momosilabs.truckTrack.api.push.dto.PushRegistrationDto
import sk.momosilabs.truckTrack.push.entity.PushPlatform
import sk.momosilabs.truckTrack.push.service.updateRegisteredDevice.UpdateRegisteredDeviceCommand

fun PushRegistrationDto.toCommand() = UpdateRegisteredDeviceCommand(
    installationId = installationId,
    platform = platform.toModel(),
    deviceLabel = deviceLabel,
)

fun PushPlatformDto.toModel(): PushPlatform = PushPlatform.valueOf(name)
