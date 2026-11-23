package sk.momosilabs.truckTrack.push.service.processBackchannelLogout

interface ProcessBackchannelLogoutUseCase {

    fun process(logoutToken: String)
}
