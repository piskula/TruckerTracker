package com.momosi.trucktrack.user.internal

import com.momosi.trucktrack.user.model.StoredTokens
import com.momosi.trucktrack.user.model.User
import com.momosi.trucktrack.user.model.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.json.Json
import platform.Foundation.NSUserDefaults

private const val TOKENS = "tokens"
private const val SERVER_PUBLIC_KEY = "server_public_key"
private const val USER_ID = "user_id"
private const val USER_NAME = "user_name"
private const val USER_EMAIL = "user_email"
private const val USER_USERNAME = "user_username"
private const val USER_ROLES = "user_roles"

class UserStorageImpl(private val defaults: NSUserDefaults) : UserStorage {

    override var tokens: StoredTokens?
        set(value) {
            if (value != null) defaults.setObject(Json.encodeToString(value), TOKENS) else defaults.removeObjectForKey(TOKENS)
        }
        get() = (defaults.stringForKey(TOKENS))?.let { runCatching { Json.decodeFromString<StoredTokens>(it) }.getOrNull() }

    override var serverPublicKey: String?
        set(value) {
            if (value != null) defaults.setObject(value, SERVER_PUBLIC_KEY) else defaults.removeObjectForKey(SERVER_PUBLIC_KEY)
        }
        get() = defaults.stringForKey(SERVER_PUBLIC_KEY)

    private val _userFlow = MutableStateFlow(loadUser())
    override val userFlow: StateFlow<User?> = _userFlow

    override var user: User?
        set(value) {
            if (value != null) {
                defaults.setObject(value.id, USER_ID)
                defaults.setObject(value.name, USER_NAME)
                defaults.setObject(value.email, USER_EMAIL)
                defaults.setObject(value.username, USER_USERNAME)
                defaults.setObject(value.roles.joinToString(",") { it.name }, USER_ROLES)
            } else {
                defaults.removeObjectForKey(USER_ID)
                defaults.removeObjectForKey(USER_NAME)
                defaults.removeObjectForKey(USER_EMAIL)
                defaults.removeObjectForKey(USER_USERNAME)
                defaults.removeObjectForKey(USER_ROLES)
            }
            _userFlow.value = value
        }
        get() = loadUser()

    private fun loadUser(): User? {
        val id = defaults.stringForKey(USER_ID) ?: return null
        val name = defaults.stringForKey(USER_NAME) ?: return null
        val email = defaults.stringForKey(USER_EMAIL) ?: return null
        val username = defaults.stringForKey(USER_USERNAME).orEmpty()
        val roles = defaults.stringForKey(USER_ROLES)
            ?.split(",")
            ?.mapNotNullTo(mutableSetOf()) { runCatching { UserRole.valueOf(it.trim()) }.getOrNull() }
            ?.takeIf { it.isNotEmpty() } ?: return null
        return User(id = id, name = name, email = email, username = username, roles = roles)
    }
}
