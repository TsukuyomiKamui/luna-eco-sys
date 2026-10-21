package com.kamui.luna.core.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.dao.DataIntegrityViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    @ExceptionHandler(NotFoundException.class)
    ResponseEntity<ApiError> notFound(NotFoundException ex, HttpServletRequest request) {
        return response(HttpStatus.NOT_FOUND, ex.getMessage(), request, Map.of());
    }

    @ExceptionHandler({BusinessException.class, IllegalArgumentException.class})
    ResponseEntity<ApiError> business(RuntimeException ex, HttpServletRequest request) {
        return response(HttpStatus.CONFLICT, ex.getMessage(), request, Map.of());
    }

    @ExceptionHandler(BadCredentialsException.class)
    ResponseEntity<ApiError> badCredentials(BadCredentialsException ex, HttpServletRequest request) {
        return response(HttpStatus.UNAUTHORIZED, "Неверный логин или пароль", request, Map.of());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ApiError> conflict(DataIntegrityViolationException ex, HttpServletRequest request) {
        return response(HttpStatus.CONFLICT, "Операция нарушает ограничение целостности данных", request, Map.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> validation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> fields = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> fields.put(error.getField(), error.getDefaultMessage()));
        return response(HttpStatus.BAD_REQUEST, "Ошибка валидации", request, fields);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiError> unexpected(Exception ex, HttpServletRequest request) {
        log.error("Unhandled API error", ex);
        return response(HttpStatus.INTERNAL_SERVER_ERROR, "Внутренняя ошибка сервера", request, Map.of());
    }

    private ResponseEntity<ApiError> response(HttpStatus status, String message,
                                               HttpServletRequest request, Map<String, String> fields) {
        return ResponseEntity.status(status).body(new ApiError(Instant.now(), status.value(),
                status.getReasonPhrase(), message, request.getRequestURI(), fields));
    }
}
