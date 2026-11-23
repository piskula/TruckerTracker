package sk.momosilabs.truckTrack.security

import java.time.Instant
import java.util.LinkedHashMap

internal class LogoutTokenReplayGuard {

    private val seenJtis = LinkedHashMap<String, Long>()
    private val replayLock = Any()

    fun markProcessed(token: VerifiedLogoutToken) {
        synchronized(replayLock) {
            evictExpiredJtis()
            while (seenJtis.size >= MAX_CACHED_JTIS) {
                seenJtis.remove(seenJtis.keys.first())
            }
            if (seenJtis.putIfAbsent(token.jti, token.expiryEpochSecond) != null) {
                LogoutTokenRejection.reject(REASON_REPLAYED)
            }
        }
    }

    private fun evictExpiredJtis() {
        val now = Instant.now().epochSecond
        seenJtis.entries.removeAll { it.value <= now }
    }

    companion object {
        private const val MAX_CACHED_JTIS = 10_000
        private const val REASON_REPLAYED = "replayed_token"
    }
}
