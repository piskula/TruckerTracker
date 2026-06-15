package sk.momosilabs.truckTrack.config

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authorization.AuthorizationDeniedException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import org.springframework.web.util.WebUtils
import sk.momosilabs.truckTrack.api.common.ErrorDTO
import java.util.UUID

@ControllerAdvice
class GlobalExceptionHandler : ResponseEntityExceptionHandler() {

    companion object {
        private val LOG = KotlinLogging.logger {}
        private val internalServerErrorDto = ErrorDTO(
            userMessage = "Internal Server Error",
            errorIdentifier = UUID.randomUUID(),
        )
    }

    @ExceptionHandler(RuntimeException::class)
    fun handleApplicationException(exception: RuntimeException, request: WebRequest): ResponseEntity<ErrorDTO> {
        var httpStatus = if (exception is GlobalException) getHttpStatus(exception) else HttpStatus.INTERNAL_SERVER_ERROR
        if (exception is AuthorizationDeniedException) {
            httpStatus = HttpStatus.FORBIDDEN
        }
        if (httpStatus == HttpStatus.INTERNAL_SERVER_ERROR) {
            request.setAttribute(WebUtils.ERROR_EXCEPTION_ATTRIBUTE, exception, WebRequest.SCOPE_REQUEST)
        }

        val errorDto = if (exception is GlobalException) getErrorDto(exception) else internalServerErrorDto
        LOG.error(exception) { "Error = $errorDto" }

        return ResponseEntity(errorDto, HttpHeaders(), httpStatus)
    }

    private fun getHttpStatus(exception: GlobalException): HttpStatus {
        val cause = exception.cause
        if (cause is GlobalException)
            return cause.httpStatus
        else
            return exception.httpStatus
    }

    private fun getErrorDto(exception: GlobalException): ErrorDTO {
        val cause = exception.cause
        if (cause is GlobalException)
            return ErrorDTO(cause.userMessage, UUID.randomUUID())
        else
            return ErrorDTO(exception.userMessage, UUID.randomUUID())
    }

}
