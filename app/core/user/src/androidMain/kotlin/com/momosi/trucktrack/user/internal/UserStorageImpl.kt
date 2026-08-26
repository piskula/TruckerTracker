package com.momosi.trucktrack.user.internal

import android.content.SharedPreferences
import androidx.core.content.edit
import com.momosi.trucktrack.user.model.StoredTokens
import com.momosi.trucktrack.user.model.User
import com.momosi.trucktrack.user.model.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.json.Json

private const val TOKENS = "tokens"
private const val SERVER_PUBLIC_KEY = "server_public_key"
private const val USER_ID = "user_id"
private const val USER_NAME = "user_name"
private const val USER_EMAIL = "user_email"
private const val USER_USERNAME = "user_username"
private const val USER_ROLES = "user_roles"

class UserStorageImpl(private val authStorage: SharedPreferences) : UserStorage {

    override var tokens: StoredTokens?
        set(value) = authStorage.edit {
            if (value != null) putString(TOKENS, Json.encodeToString(value)) else remove(TOKENS)
        }
        get() = authStorage.getString(TOKENS, null)?.let { runCatching { Json.decodeFromString<StoredTokens>(it) }.getOrNull() }

    override var serverPublicKey: String?
        set(value) = authStorage.edit { putString(SERVER_PUBLIC_KEY, value) }
        get() = authStorage.getString(SERVER_PUBLIC_KEY, null)

    private val _userFlow = MutableStateFlow(loadUser())
    override val userFlow: StateFlow<User?> = _userFlow

    override var user: User?
        set(value) {
            authStorage.edit {
                if (value != null) {
                    putString(USER_ID, value.id)
                    putString(USER_NAME, value.name)
                    putString(USER_EMAIL, value.email)
                    putString(USER_USERNAME, value.username)
                    putString(USER_ROLES, value.roles.joinToString(",") { it.name })
                } else {
                    remove(USER_ID)
                    remove(USER_NAME)
                    remove(USER_EMAIL)
                    remove(USER_USERNAME)
                    remove(USER_ROLES)
                }
            }
            _userFlow.value = value
        }
        get() = loadUser()

    private fun loadUser(): User? {
        val id = authStorage.getString(USER_ID, null) ?: return null
        val name = authStorage.getString(USER_NAME, null) ?: return null
        val email = authStorage.getString(USER_EMAIL, null) ?: return null
        val username = authStorage.getString(USER_USERNAME, null).orEmpty()
        val roles = authStorage.getString(USER_ROLES, null)
            ?.split(",")
            ?.mapNotNullTo(mutableSetOf()) { runCatching { UserRole.valueOf(it.trim()) }.getOrNull() }
            ?.takeIf { it.isNotEmpty() } ?: return null
        return User(id = id, name = name, email = email, username = username, roles = roles)
    }
}
