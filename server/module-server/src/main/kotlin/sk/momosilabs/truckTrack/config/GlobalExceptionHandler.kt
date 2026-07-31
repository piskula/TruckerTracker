package sk.momosilabs.truckTrack.config

import sk.momosilabs.truckTrack.api.common.ErrorDto
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
import java.util.UUID

@ControllerAdvice
class GlobalExceptionHandler : ResponseEntityExceptionHandler() {

    companion object {
        private val LOG = KotlinLogging.logger {}
        private const val INTERNAL_SERVER_ERROR_MESSAGE = "Internal Server Error"
    }

    @ExceptionHandler(RuntimeException::class)
    fun handleApplicationException(exception: RuntimeException, request: WebRequest): ResponseEntity<ErrorDto> {
        var httpStatus = if (exception is GlobalException) getHttpStatus(exception) else HttpStatus.INTERNAL_SERVER_ERROR
        if (exception is AuthorizationDeniedException) {
            httpStatus = HttpStatus.FORBIDDEN
        }
        if (httpStatus == HttpStatus.INTERNAL_SERVER_ERROR) {
            request.setAttribute(WebUtils.ERROR_EXCEPTION_ATTRIBUTE, exception, WebRequest.SCOPE_REQUEST)
        }

        val errorDto = if (exception is GlobalException) getErrorDto(exception) else ErrorDto(INTERNAL_SERVER_ERROR_MESSAGE, UUID.randomUUID())
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

    private fun getErrorDto(exception: GlobalException): ErrorDto {
        val cause = exception.cause
        if (cause is GlobalException)
            return ErrorDto(cause.userMessage, UUID.randomUUID())
        else
            return ErrorDto(exception.userMessage, UUID.randomUUID())
    }

}
