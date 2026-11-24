package com.momosi.trucktrack.core.applinks

import com.momosi.trucktrack.core.common.logger.Logger
import com.momosi.trucktrack.user.AuthManager
import com.momosi.trucktrack.user.model.AuthenticationState
import io.ktor.http.URLParserException
import io.ktor.http.URLProtocol
import io.ktor.http.Url
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.receiveAsFlow

internal class AppLinksManagerImpl(private val authManager: AuthManager) : AppLinksManager {
    private val pending = Channel<AppLinkAction>(Channel.CONFLATED)
    override val actions: Flow<AppLinkAction> = pending.receiveAsFlow().filter { isAuthorized() }

    override fun openUrl(url: String): Boolean {
        if (!isAuthorized()) return false
        val action = parse(url)
        if (action == null) {
            Logger.w("AppLinks", "Ignoring unsupported app link")
            return false
        }
        return pending.trySend(action).onFailure {
            Logger.w("AppLinks", "Unable to deliver app link")
        }.isSuccess
    }

    private fun parse(url: String): AppLinkAction? {
        if (url.any { it <= ' ' || it == '\\' }) return null
        val parsed = try {
            Url(url)
        } catch (_: URLParserException) {
            return null
        }
        if (parsed.protocol != URLProtocol.HTTPS || parsed.port != URLProtocol.HTTPS.defaultPort) {
            return null
        }
        if (parsed.user != null || parsed.password != null) return null
        val path = parsed.encodedPath.removePrefix("/").removeSuffix("/").split('/')
        return when (parsed.host.lowercase()) {
            "transroute.org" -> when (path.firstOrNull()) {
                "checklist" -> AppLinkAction.Checklist.takeIf { path.size == 1 }

                "issue" -> {
                    if (path.size != 2) return null
                    val issueId = path[1].takeIf { id -> id.all { it in '0'..'9' } }
                        ?.toLongOrNull()
                        ?.takeIf { it > 0 }
                    issueId?.let(AppLinkAction::Issue)
                }

                else -> null
            }

            else -> null
        }
    }

    private fun isAuthorized(): Boolean {
        if (authManager.authenticationState.value != AuthenticationState.Authorized) {
            Logger.i("AppLinks", "Ignoring app link while signed out")
            return false
        }
        return true
    }
}
