package com.library.interfaces.advice;

import com.library.domain.shared.Result;
import com.library.domain.shared.exception.BusinessException;
import com.library.domain.shared.exception.ResultCode;
import com.library.infrastructure.alert.AlertSender;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private final AlertSender alertSender;

    public GlobalExceptionHandler(AlertSender alertSender) {
        this.alertSender = alertSender;
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<?>> handleBusinessException(BusinessException ex) {
        log.warn("Business exception: {}", ex.getMessage());
        HttpStatus status = switch (ex.resultCode()) {
            case AUTH_FORBIDDEN -> HttpStatus.FORBIDDEN;
            case AUTH_TOKEN_MISSING, AUTH_ACCESS_TOKEN_EXPIRED, AUTH_REFRESH_TOKEN_INVALID, AUTH_ACCOUNT_LOCKED -> HttpStatus.UNAUTHORIZED;
            default -> HttpStatus.OK;
        };
        return ResponseEntity.status(status)
                .body(Result.fail(ex.resultCode(), ex.getMessage(), getTraceId()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> handleValidationException(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return Result.fail(ResultCode.PARAM_INVALID, msg, getTraceId());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public Result<?> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        log.warn("Data integrity violation: {}", ex.getMessage());
        return Result.fail(ResultCode.DATA_INTEGRITY, ResultCode.DATA_INTEGRITY.defaultMessage(), getTraceId());
    }

    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<?> handleAuthenticationException(AuthenticationException ex, HttpServletRequest request) {
        String jwtError = (String) request.getAttribute("jwtError");
        String msg = jwtError != null ? jwtError : ex.getMessage();
        return Result.fail(ResultCode.AUTH_TOKEN_MISSING, msg, getTraceId());
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<?> handleAccessDeniedException(AccessDeniedException ex) {
        return Result.fail(ResultCode.AUTH_FORBIDDEN, ex.getMessage(), getTraceId());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<?> handleException(Exception ex) {
        log.error("Unhandled exception", ex);
        alertSender.sendError("Unhandled Exception", ex.getMessage(), ex);
        return Result.fail(ResultCode.SYSTEM_ERROR, ResultCode.SYSTEM_ERROR.defaultMessage(), getTraceId());
    }

    private String getTraceId() {
        return MDC.get("traceId");
    }
}
