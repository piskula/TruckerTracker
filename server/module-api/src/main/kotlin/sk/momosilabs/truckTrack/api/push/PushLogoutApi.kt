package sk.momosilabs.truckTrack.api.push

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam

@Tag(name = "Push")
interface PushLogoutApi {

    companion object {
        private const val ENDPOINT = "/api/v1/push/backchannel-logout"
    }

    @Operation(summary = "Handle an OpenID Connect back-channel logout notification from Keycloak")
    @PostMapping(ENDPOINT, consumes = ["application/x-www-form-urlencoded"])
    fun processBackchannelLogout(@RequestParam("logout_token") logoutToken: String): ResponseEntity<Unit>
}
