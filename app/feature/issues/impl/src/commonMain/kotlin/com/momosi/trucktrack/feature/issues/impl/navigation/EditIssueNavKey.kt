package com.momosi.trucktrack.feature.issues.impl.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data class EditIssueNavKey(val issueId: Long) : NavKey
