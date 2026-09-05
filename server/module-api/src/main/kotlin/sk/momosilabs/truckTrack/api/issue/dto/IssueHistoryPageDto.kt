package sk.momosilabs.truckTrack.api.issue.dto

// Not PageDto<IssueHistoryDto>: Jackson resolves generic type parameters from the erased class
// signature of PageDto<T> when Spring hands it a raw value, so IssueHistoryDto's @JsonTypeInfo
// discriminator silently disappears from the wire. A concrete, non-generic content type keeps it.
data class IssueHistoryPageDto(
    val totalElements: Long,
    val totalPages: Int,
    val number: Int,
    val size: Int,
    val numberOfElements: Int,
    val content: List<IssueHistoryDto>,
)
