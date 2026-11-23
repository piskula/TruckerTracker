package sk.momosilabs.truckTrack.push.controller

import org.springframework.http.CacheControl
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import sk.momosilabs.truckTrack.api.push.PushLogoutApi
import sk.momosilabs.truckTrack.push.service.processBackchannelLogout.ProcessBackchannelLogoutUseCase

@RestController
class PushLogoutController(
    private val processBackchannelLogout: ProcessBackchannelLogoutUseCase,
) : PushLogoutApi {

    override fun processBackchannelLogout(logoutToken: String): ResponseEntity<Unit> {
        processBackchannelLogout.process(logoutToken)
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).build()
    }
}
