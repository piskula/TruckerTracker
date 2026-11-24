package sk.momosilabs.truckTrack.security

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import sk.momosilabs.truckTrack.config.GlobalBadRequestException
import java.time.Instant
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicInteger

class LogoutTokenReplayGuardTest {

    private val guard = LogoutTokenReplayGuard()

    @Test
    fun `a logout token seen for the first time is admitted`() {
        assertThatCode { guard.markProcessed(token(JTI)) }.doesNotThrowAnyException()
    }

    @Test
    fun `a logout token whose jti was already processed is rejected as replayed`() {
        guard.markProcessed(token(JTI))

        assertThatThrownBy { guard.markProcessed(token(JTI)) }
            .isInstanceOf(GlobalBadRequestException::class.java)
            .hasMessage(INVALID_LOGOUT_TOKEN_MESSAGE)
    }

    @Test
    fun `a jti whose entry has already expired is swept instead of counted as replayed`() {
        guard.markProcessed(token(JTI, expiryEpochSecond = Instant.now().epochSecond - EXPIRED_BY_SECONDS))

        assertThatCode { guard.markProcessed(token(JTI)) }.doesNotThrowAnyException()
    }

    @Test
    fun `the guard keeps admitting fresh tokens and rejecting recent ones past the cache limit`() {
        val jtis = (1..FILLED_CACHE_JTIS).map { "cache-jti-$it" }
        jtis.forEach { guard.markProcessed(token(it)) }

        assertThatCode { guard.markProcessed(token(jtis.first())) }.doesNotThrowAnyException()
        assertThatCode { guard.markProcessed(token(FRESH_JTI)) }.doesNotThrowAnyException()
        assertThatThrownBy { guard.markProcessed(token(jtis.last())) }
            .isInstanceOf(GlobalBadRequestException::class.java)
            .hasMessage(INVALID_LOGOUT_TOKEN_MESSAGE)
    }

    @Test
    fun `concurrent submissions of the same jti admit exactly one of them`() {
        val bothReady = CountDownLatch(CONCURRENT_SUBMITTERS)
        val admitted = AtomicInteger()
        val rejected = AtomicInteger()
        val attempts = List(CONCURRENT_SUBMITTERS) {
            Thread {
                bothReady.countDown()
                bothReady.await()
                try {
                    guard.markProcessed(token(JTI))
                    admitted.incrementAndGet()
                } catch (_: GlobalBadRequestException) {
                    rejected.incrementAndGet()
                }
            }.also { it.start() }
        }
        attempts.forEach { it.join(TERMINATION_TIMEOUT_MILLIS) }

        assertThat(attempts).noneMatch(Thread::isAlive)
        assertThat(admitted.get()).isEqualTo(1)
        assertThat(rejected.get()).isEqualTo(CONCURRENT_SUBMITTERS - 1)
    }

    private fun token(
        jti: String,
        expiryEpochSecond: Long = Instant.now().epochSecond + TOKEN_LIFETIME_SECONDS,
    ) = VerifiedLogoutToken(
        subject = SUBJECT,
        jti = jti,
        expiryEpochSecond = expiryEpochSecond,
    )

    private companion object {
        const val JTI = "logout-token-jti"
        const val FRESH_JTI = "brand-new-logout-token-jti"
        const val SUBJECT = "3f9e1c2a-7b64-4d0f-9a51-6e2c8b4d7f10"
        const val INVALID_LOGOUT_TOKEN_MESSAGE = "Invalid logout token"
        const val TOKEN_LIFETIME_SECONDS = 300L
        const val EXPIRED_BY_SECONDS = 300L
        const val MAX_CACHED_JTIS = 10_000
        const val FILLED_CACHE_JTIS = MAX_CACHED_JTIS + 50
        const val CONCURRENT_SUBMITTERS = 2
        const val TERMINATION_TIMEOUT_MILLIS = 30_000L
    }
}
