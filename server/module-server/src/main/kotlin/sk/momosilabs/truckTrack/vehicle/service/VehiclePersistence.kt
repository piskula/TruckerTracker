package sk.momosilabs.truckTrack.vehicle.service

import sk.momosilabs.truckTrack.vehicle.model.VehicleModel

interface VehiclePersistence {

    fun findAll(): List<VehicleModel>

    fun findById(id: Long): VehicleModel

    fun findByIdOrNull(id: Long): VehicleModel?
}
