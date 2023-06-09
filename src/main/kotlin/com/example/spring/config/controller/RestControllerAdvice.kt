package com.example.spring.config.controller

import com.example.spring.application.service.board.exception.BoardDataNotFoundException
import com.example.spring.application.service.board.exception.CommentDataNotFoundException
import com.example.spring.application.service.board.exception.PostDataNotFoundException
import com.example.spring.application.service.board.exception.PostLikeException
import com.example.spring.application.service.member.exception.MemberAccessorNotMatchException
import com.example.spring.application.service.member.exception.MemberAlreadyExistException
import com.example.spring.application.service.member.exception.MemberDataNotFoundException
import com.example.spring.config.dto.BaseResponseException
import com.example.spring.config.code.ErrorCode
import com.example.spring.config.exception.WriterNotMatchException
import com.fasterxml.jackson.databind.exc.InvalidFormatException
import com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException
import jakarta.validation.ConstraintViolationException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.validation.BindingResult
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException


@RestControllerAdvice
class ControllerAdvice {
    protected val log: Logger = LoggerFactory.getLogger(this::class.simpleName)

    @ExceptionHandler(PostLikeException::class)
    fun postLikeException(ex: PostLikeException): ResponseEntity<BaseResponseException> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(
                BaseResponseException(
                    ex.code,
                    ex.message
                )
            )
    }

    @ExceptionHandler(MemberAccessorNotMatchException::class)
    fun memberAccessorNotMatchException(ex: MemberAccessorNotMatchException): ResponseEntity<BaseResponseException> {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(
                BaseResponseException(
                    ErrorCode.INVALID_USER,
                    ex.message
                )
            )
    }

    @ExceptionHandler(WriterNotMatchException::class)
    fun writerNotMatchException(ex: WriterNotMatchException): ResponseEntity<BaseResponseException> {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(
                BaseResponseException(
                    ErrorCode.INVALID_USER,
                    ex.message
                )
            )
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    fun httpRequestMethodNotSupportedException(ex: HttpRequestMethodNotSupportedException): ResponseEntity<BaseResponseException> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(
                BaseResponseException(
                    ErrorCode.NOT_SUPPORT_HTTP_METHOD,
                    ex.message.toString()
                )
            )
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun constraintViolationException(ex: ConstraintViolationException): ResponseEntity<BaseResponseException> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(
                BaseResponseException(
                    ErrorCode.INVALID_PARAMETER,
                    ex.message.toString()
                )
            )
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun httpMessageNotReadableException(ex: HttpMessageNotReadableException): ResponseEntity<BaseResponseException> {
        var msg: String = when (val causeException = ex.cause) {
            is InvalidFormatException -> "입력 받은 [${causeException.value}] 를 변환중 에러가 발생했습니다."
            is MissingKotlinParameterException -> "Parameter is missing: [${causeException.parameter.name}]"
            else -> "파라미터를 확인해주세요"
        }
        if (ex.cause is InvalidFormatException) {
            msg += " 오직 ["
            (ex.cause as InvalidFormatException).targetType.fields.map {
                msg += it.name + ", "
            }
            msg = msg.trim()
            msg = msg.substring(0, msg.length - 1)
            msg += "] 만 가능합니다"
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(
                BaseResponseException(
                    ErrorCode.INVALID_PARAMETER,
                    msg
                )
            )
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun methodArgumentTypeMismatchException(ex: MethodArgumentTypeMismatchException): ResponseEntity<BaseResponseException> {
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
    fun methodArgumentNotValidException(ex: MethodArgumentNotValidException): ResponseEntity<BaseResponseException> {
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

    @ExceptionHandler(CommentDataNotFoundException::class)
    fun commentDataNotFoundException(ex: CommentDataNotFoundException): ResponseEntity<BaseResponseException> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(
                BaseResponseException(
                    ex.code,
                    ex.message
                )
            )
    }

    @ExceptionHandler(PostDataNotFoundException::class)
    fun postDataNotFoundException(ex: PostDataNotFoundException): ResponseEntity<BaseResponseException> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(
                BaseResponseException(
                    ex.code,
                    ex.message
                )
            )
    }

    @ExceptionHandler(BoardDataNotFoundException::class)
    fun boardDataNotFoundException(ex: BoardDataNotFoundException): ResponseEntity<BaseResponseException> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(
                BaseResponseException(
                    ex.code,
                    ex.message
                )
            )
    }

    @ExceptionHandler(MemberDataNotFoundException::class)
    fun memberDataNotFoundException(ex: MemberDataNotFoundException): ResponseEntity<BaseResponseException> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(
                BaseResponseException(
                    ex.code,
                    ex.message
                )
            )
    }

    @ExceptionHandler(MemberAlreadyExistException::class)
    fun memberAlreadyExistException(ex: MemberAlreadyExistException): ResponseEntity<BaseResponseException> {
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
