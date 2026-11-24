package sk.momosilabs.truckTrack.vehicle.persistence

import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import sk.momosilabs.truckTrack.vehicle.model.VehicleModel
import sk.momosilabs.truckTrack.vehicle.persistence.mapper.toModel
import sk.momosilabs.truckTrack.vehicle.persistence.repository.VehicleRepository
import sk.momosilabs.truckTrack.vehicle.service.VehiclePersistence
import kotlin.jvm.optionals.getOrNull

@Repository
class VehiclePersistenceProvider(
    private val vehicleRepository: VehicleRepository,
) : VehiclePersistence {

    @Transactional(readOnly = true)
    override fun findAll(): List<VehicleModel> =
        vehicleRepository.findAll().map { it.toModel() }

    @Transactional(readOnly = true)
    override fun findById(id: Long): VehicleModel =
        vehicleRepository.getReferenceById(id).toModel()

    @Transactional(readOnly = true)
    override fun findByIdOrNull(id: Long): VehicleModel? =
        vehicleRepository.findById(id).getOrNull()?.toModel()
}
