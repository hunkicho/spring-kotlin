package com.example.spring.config

import com.example.spring.config.dto.ErrorCode
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.validation.BindingResult
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException


@RestControllerAdvice
class ControllerAdvice {
    protected val log: Logger = LoggerFactory.getLogger(this::class.simpleName)

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun httpMessageNotReadableException(ex: HttpMessageNotReadableException): ResponseEntity<Any> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(
                BaseResponseException(
                    ErrorCode.INVALID_PARAMETER,
                    "파라미터를 확인해주세요."
                )
            )
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun methodArgumentTypeMismatchException(ex: MethodArgumentTypeMismatchException): ResponseEntity<Any> {
        val builder = StringBuilder()
        builder.append("[")
        builder.append(ex.errorCode)
        builder.append(": ")
        builder.append(ex.value)
        builder.append("] - ")
        builder.append(ex.name)
        builder.append(" required ")
        builder.append(ex.requiredType)

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(
                BaseResponseException(
                    ErrorCode.INVALID_PARAMETER,
                    builder.toString()
                )
            )
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun methodArgumentNotValidException(ex: MethodArgumentNotValidException): ResponseEntity<Any> {
        val bindingResult: BindingResult = ex.bindingResult
        val builder = StringBuilder()
        for (fieldError in bindingResult.fieldErrors) {
            builder.append("[")
            builder.append(fieldError.field)
            builder.append("](은)는 ")
            builder.append(fieldError.defaultMessage)
            builder.append(" 입력된 값: [")
            builder.append(fieldError.rejectedValue)
            builder.append("]")
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(
                BaseResponseException(
                    ErrorCode.INVALID_PARAMETER,
                    builder.toString()
                )
            )
    }

    @ExceptionHandler(NoDataException::class)
    fun noDataException(ex: NoDataException): ResponseEntity<Any> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(
                BaseResponseException(
                    ex.code,
                    ex.message
                )
            )
    }

    @ExceptionHandler(Exception::class)
    fun exception(ex: Exception): ResponseEntity<Any> {
        log.warn("INTERNAL_SERVER_ERROR")
        log.warn(ex.stackTraceToString())
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(
                BaseResponseException(
                    ErrorCode.INTERNAL_SERVER_ERROR,
                    "INTERNAL_SERVER_ERROR"
                )
            )
    }
}

data class NoDataException(
    var code: ErrorCode,
    override var message: String = "데이터가 존재하지 않습니다"
) : RuntimeException(message, null)