package com.momosi.trucktrack.user.model

data class User(val id: String, val name: String, val email: String, val roles: Set<UserRole>) {
    val isDriver: Boolean get() = UserRole.Driver in roles
    val isMechanic: Boolean get() = UserRole.Mechanic in roles
}
