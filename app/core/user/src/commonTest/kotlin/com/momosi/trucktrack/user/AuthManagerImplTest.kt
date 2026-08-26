package com.momosi.trucktrack.user

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AuthManagerImplTest {

    @Test
    fun `claimsToUser reads username from preferred_username claim`() {
        val user = claimsToUser(
            sub = "user-1",
            claims = mapOf(
                "name" to "Mattia Binotto",
                "email" to "mattia@example.com",
                "preferred_username" to "mbinotto",
                "realm_access" to mapOf("roles" to listOf("ROLE_MECHANIC")),
            ),
        )

        assertEquals("mbinotto", user.username)
        assertEquals("Mattia Binotto", user.name)
        assertEquals("mattia@example.com", user.email)
        assertEquals("user-1", user.id)
        assertTrue(user.isMechanic)
    }

    @Test
    fun `claimsToUser defaults username to empty string when claim missing`() {
        val user = claimsToUser(
            sub = "user-2",
            claims = mapOf(
                "name" to "Michael Schumacher",
                "email" to "michael@example.com",
            ),
        )

        assertEquals("", user.username)
    }
}
