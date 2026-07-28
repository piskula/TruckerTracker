package com.momosi.trucktrack.feature.issues.impl.list

import androidx.compose.runtime.Immutable
import com.momosi.trucktrack.user.model.UserRole
import kotlinx.collections.immutable.ImmutableList

@Immutable
data class IssuesUserInfo(val name: String, val roles: ImmutableList<UserRole>)

@Immutable
data class IssuesState(val userInfo: IssuesUserInfo? = null, val selectedFilter: IssueFilter = IssueFilter.MyIssues)
