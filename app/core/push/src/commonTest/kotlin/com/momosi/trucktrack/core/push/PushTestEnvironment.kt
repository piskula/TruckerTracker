package com.momosi.trucktrack.core.push

import com.momosi.trucktrack.core.common.error.ErrorReporter
import com.momosi.trucktrack.core.common.network.installApiExceptionMapping
import com.momosi.trucktrack.core.network.installOptInRequestRetry
import com.momosi.trucktrack.core.push.firebase.PushMessagingClient
import com.momosi.trucktrack.core.push.message.PushMessageDispatcher
import com.momosi.trucktrack.core.push.message.PushMessageDispatcherImpl
import com.momosi.trucktrack.core.push.message.PushMessageHandler
import com.momosi.trucktrack.core.push.registration.PushDeviceInfo
import com.momosi.trucktrack.core.push.registration.PushDeviceRegistrarImpl
import com.momosi.trucktrack.core.push.registration.PushRegistrationManager
import com.momosi.trucktrack.core.push.registration.PushRegistrationManagerImpl
import com.momosi.trucktrack.core.push.registration.PushRegistrationStorage
import com.momosi.trucktrack.core.push.registration.api.PushDeviceApi
import com.momosi.trucktrack.core.push.registration.api.createPushDeviceApi
import com.momosi.trucktrack.shared.push.PushDeviceDto
import com.momosi.trucktrack.shared.push.PushDeviceRegistrationDto
import com.momosi.trucktrack.shared.push.PushDeviceUpdateDto
import com.momosi.trucktrack.shared.push.PushPlatformDto
import com.momosi.trucktrack.user.AuthManager
import com.momosi.trucktrack.user.model.AuthActionResult
import com.momosi.trucktrack.user.model.AuthenticationState
import com.momosi.trucktrack.user.model.TokenResponse
import de.jensklingenberg.ktorfit.Ktorfit
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockEngineConfig
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.MockRequestHandler
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.toByteArray
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.serialization.json.Json
import kotlin.time.Clock
import kotlin.time.Instant

private const val TEST_BASE_URL = "https://api.example.test/"
private const val PUSH_DEVICE_PATH = "/api/v1/push-device"

internal const val THIS_DEVICE_FIREBASE_INSTALLATION_ID = "firebase-installation-this-phone"
internal const val OTHER_DEVICE_FIREBASE_INSTALLATION_ID = "firebase-installation-other-phone"
internal const val OTHER_DEVICE_NAME = "iPhone 15"

internal class PushTestEnvironment(testScope: TestScope) {
    private val scope = testScope.backgroundScope
    val callLog = mutableListOf<String>()
    val clock = MutableClock(Instant.parse("2026-11-23T08:00:00Z"))
    val server = FakePushDeviceServer(clock, callLog, StandardTestDispatcher(testScope.testScheduler))
    val storage = InMemoryPushRegistrationStorage()
    val messagingClient = FakePushMessagingClient()
    val authManager = FakeAuthManager(callLog)
    val errorReporter = RecordingErrorReporter()

    val manager: PushRegistrationManager by lazy {
        PushRegistrationManagerImpl(
            registrar = PushDeviceRegistrarImpl(
                api = server.api,
                deviceInfo = FakePushDeviceInfo,
                storage = storage,
                firebaseInstallationIdProvider = messagingClient,
            ),
            authManager = authManager,
            firebaseInstallationIdProvider = messagingClient,
            errorReporter = errorReporter,
            scope = scope,
        )
    }

    fun startManager(): PushRegistrationManager = manager

    fun startMessageDispatcher(handlers: List<PushMessageHandler>): PushMessageDispatcher = PushMessageDispatcherImpl(messagingClient, handlers, scope)

    fun thisDeviceRegisteredAt(registeredAt: Instant) = PushDeviceDto(
        firebaseInstallationId = THIS_DEVICE_FIREBASE_INSTALLATION_ID,
        deviceName = FakePushDeviceInfo.DEVICE_NAME,
        platform = PushPlatformDto.ANDROID,
        registeredAt = registeredAt,
    )

    fun otherDeviceRegisteredAt(registeredAt: Instant) = PushDeviceDto(
        firebaseInstallationId = OTHER_DEVICE_FIREBASE_INSTALLATION_ID,
        deviceName = OTHER_DEVICE_NAME,
        platform = PushPlatformDto.IOS,
        registeredAt = registeredAt,
    )
}

internal class MutableClock(var now: Instant) : Clock {
    override fun now(): Instant = now
}

internal class FakePushDeviceServer(private val clock: Clock, private val callLog: MutableList<String>, dispatcher: CoroutineDispatcher) {
    val requests = mutableListOf<HttpMethod>()
    val registrations = mutableListOf<PushDeviceRegistrationDto>()
    val updates = mutableListOf<Pair<String, PushDeviceUpdateDto>>()
    val unregistrations = mutableListOf<String>()
    var unavailableResponses = 0
    var rejectsUpdates = false
    var registeredDevice: PushDeviceDto? = null

    val api: PushDeviceApi = pushDeviceApiRespondingWith(dispatcher) { request -> respondTo(request) }

    private suspend fun MockRequestHandleScope.respondTo(request: HttpRequestData): HttpResponseData {
        requests += request.method
        if (unavailableResponses > 0) {
            unavailableResponses--
            return respond("", HttpStatusCode.ServiceUnavailable)
        }
        val firebaseInstallationIdInPath = request.url.encodedPath.substringAfter("$PUSH_DEVICE_PATH/", missingDelimiterValue = "")
        return when (request.method) {
            HttpMethod.Put -> respondJson(register(request.decodedBody()))
            HttpMethod.Patch -> update(firebaseInstallationIdInPath, request.decodedBody())
            HttpMethod.Get -> registeredDevice?.let { respondJson(it) } ?: respond("", HttpStatusCode.NoContent)
            HttpMethod.Delete -> unregister(firebaseInstallationIdInPath)
            else -> error("Unexpected ${request.method.value} ${request.url}")
        }
    }

    private fun register(registration: PushDeviceRegistrationDto): PushDeviceDto {
        registrations += registration
        return PushDeviceDto(
            firebaseInstallationId = registration.firebaseInstallationId,
            deviceName = registration.deviceName,
            platform = registration.platform,
            registeredAt = clock.now(),
        ).also { registeredDevice = it }
    }

    private fun MockRequestHandleScope.update(lastSentFirebaseInstallationId: String, update: PushDeviceUpdateDto): HttpResponseData {
        updates += lastSentFirebaseInstallationId to update
        return respond("", if (rejectsUpdates) HttpStatusCode.Conflict else HttpStatusCode.OK)
    }

    private fun MockRequestHandleScope.unregister(firebaseInstallationId: String): HttpResponseData {
        callLog += "unregister"
        unregistrations += firebaseInstallationId
        if (registeredDevice?.firebaseInstallationId == firebaseInstallationId) registeredDevice = null
        return respond("", HttpStatusCode.NoContent)
    }

    private inline fun <reified T> MockRequestHandleScope.respondJson(body: T): HttpResponseData = respond(
        content = Json.encodeToString(body),
        status = HttpStatusCode.OK,
        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
    )

    private suspend inline fun <reified T> HttpRequestData.decodedBody(): T = Json.decodeFromString(body.toByteArray().decodeToString())
}

internal fun pushDeviceApiRespondingWith(dispatcher: CoroutineDispatcher? = null, handler: MockRequestHandler): PushDeviceApi = Ktorfit.Builder()
    .baseUrl(TEST_BASE_URL)
    .httpClient(
        HttpClient(
            MockEngine(
                MockEngineConfig().apply {
                    this.dispatcher = dispatcher
                    addHandler(handler)
                },
            ),
        ) {
            expectSuccess = true
            defaultRequest {
                url(TEST_BASE_URL)
                contentType(ContentType.Application.Json)
            }
            install(ContentNegotiation) { json() }
            installOptInRequestRetry()
            installApiExceptionMapping()
        },
    )
    .build()
    .createPushDeviceApi()

internal class InMemoryPushRegistrationStorage : PushRegistrationStorage {
    override var lastSentFirebaseInstallationId: String? = null
}

internal class FakePushMessagingClient : PushMessagingClient {
    var firebaseInstallationId: String? = THIS_DEVICE_FIREBASE_INSTALLATION_ID
    override val firebaseInstallationIdChanges = MutableSharedFlow<String>()
    override val messages = MutableSharedFlow<Map<String, String>>()

    override suspend fun currentFirebaseInstallationId(): String? = firebaseInstallationId
}

internal object FakePushDeviceInfo : PushDeviceInfo {
    const val DEVICE_NAME = "Samsung Galaxy S8+"

    override val platform: PushPlatformDto = PushPlatformDto.ANDROID

    override suspend fun deviceName(): String = DEVICE_NAME
}

internal class RecordingErrorReporter : ErrorReporter {
    val reported = mutableListOf<Throwable>()
    override val errors: Flow<Throwable> = MutableSharedFlow()

    override fun report(error: Throwable) {
        reported += error
    }
}

internal class FakeAuthManager(private val callLog: MutableList<String>) : AuthManager {
    override val authenticationState = MutableStateFlow(AuthenticationState.Authorized)
    override val authenticationInProgress = MutableStateFlow(false)
    private val completedLogins = Channel<Unit>(Channel.UNLIMITED)
    override val loginCompletions: Flow<Unit> = completedLogins.receiveAsFlow()

    fun completeLogin() {
        authenticationState.value = AuthenticationState.Authorized
        completedLogins.trySend(Unit)
    }

    override suspend fun signIn(): AuthActionResult = error("Unexpected sign-in")

    override suspend fun signOut(): AuthActionResult {
        callLog += "signOut"
        authenticationState.value = AuthenticationState.Guest
        return AuthActionResult.Success(AuthenticationState.Guest)
    }

    override suspend fun token(): TokenResponse = TokenResponse.Token("access-token")

    override suspend fun invalidateSession(rejectedAccessToken: String) = Unit
}
