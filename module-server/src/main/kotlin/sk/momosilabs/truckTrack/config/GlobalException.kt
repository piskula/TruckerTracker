package sk.momosilabs.truckTrack.config

import org.springframework.http.HttpStatus

open class GlobalException(
    open val userMessage: String,
    open val detailMessage: String = "",
    val httpStatus: HttpStatus = HttpStatus.INTERNAL_SERVER_ERROR,
    override val cause: Throwable? = null,
) : RuntimeException(userMessage, cause)

open class GlobalUnprocessableException(
    override val userMessage: String,
    override val detailMessage: String = "",
    override val cause: Throwable? = null,
) : GlobalException(userMessage, detailMessage, HttpStatus.UNPROCESSABLE_ENTITY, cause)

open class GlobalNotFoundException(
    override val userMessage: String,
    override val detailMessage: String = "",
    override val cause: Throwable? = null,
) : GlobalException(userMessage, detailMessage, HttpStatus.NOT_FOUND, cause)
