package sk.momosilabs.truckTrack.issueManagement.entity

enum class IssueStatus {
    OPEN, IN_PROGRESS, DONE, CANCELED;

    fun isClosed(): Boolean = this == DONE || this == CANCELED
}
