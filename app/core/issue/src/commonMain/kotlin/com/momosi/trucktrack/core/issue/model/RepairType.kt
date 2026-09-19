package com.momosi.trucktrack.core.issue.model

enum class RepairType {
    Damage,
    Fault,
    Installation,
    ;

    companion object {
        fun fromApiValue(value: String): RepairType = when (value) {
            "DAMAGE" -> Damage
            "FAULT" -> Fault
            "INSTALLATION" -> Installation
            else -> Damage
        }
    }

    fun toApiValue(): String = when (this) {
        Damage -> "DAMAGE"
        Fault -> "FAULT"
        Installation -> "INSTALLATION"
    }
}
