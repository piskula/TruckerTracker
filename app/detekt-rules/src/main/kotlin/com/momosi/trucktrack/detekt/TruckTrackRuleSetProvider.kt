package com.momosi.trucktrack.detekt

import dev.detekt.api.RuleSet
import dev.detekt.api.RuleSetId
import dev.detekt.api.RuleSetProvider

class TruckTrackRuleSetProvider : RuleSetProvider {
    override val ruleSetId = RuleSetId("TruckTrack")

    override fun instance() = RuleSet(
        ruleSetId,
        listOf(::UseTruckTrackIllustrations),
    )
}
