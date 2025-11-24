package org.scoooting.rental.exceptions;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.scoooting.rental.dto.common.ErrorResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(AuthorizationDeniedException.class)
    public Mono<ResponseEntity<ErrorResponseDTO>> handleAuthorizationDenied(
            AuthorizationDeniedException ex,
            ServerWebExchange exchange
    ) {
        log.error("Authorization denied: {}", ex.getMessage());
        ErrorResponseDTO error = new ErrorResponseDTO(
                "Access denied. You don't have permission to access this resource.",
                "FORBIDDEN",
                LocalDateTime.now(),
                Map.of(
                        "path", exchange.getRequest().getPath().value(),
                        "details", "Insufficient permissions"
                )
        );
        return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).body(error));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public Mono<ResponseEntity<ErrorResponseDTO>> handleAccessDenied(
            AccessDeniedException ex,
            ServerWebExchange exchange
    ) {
        log.error("Access denied: {}", ex.getMessage());
        ErrorResponseDTO error = new ErrorResponseDTO(
                "Access denied. You don't have permission to access this resource.",
                "ACCESS_DENIED",
                LocalDateTime.now(),
                Map.of("path", exchange.getRequest().getPath().value())
        );
        return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).body(error));
    }

    @ExceptionHandler(AuthenticationException.class)
    public Mono<ResponseEntity<ErrorResponseDTO>> handleAuthentication(
            AuthenticationException ex,
            ServerWebExchange exchange
    ) {
        log.error("Authentication failed: {}", ex.getMessage());
        ErrorResponseDTO error = new ErrorResponseDTO(
                "Authentication failed. Please log in.",
                "AUTHENTICATION_FAILED",
                LocalDateTime.now(),
                Map.of("path", exchange.getRequest().getPath().value())
        );
        return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public Mono<ResponseEntity<ErrorResponseDTO>> handleConstraintViolation(
            ConstraintViolationException ex,
            ServerWebExchange exchange
    ) {
        Map<String, String> errors = new HashMap<>();
        ex.getConstraintViolations().forEach(violation -> {
            String field = violation.getPropertyPath().toString();
            String message = violation.getMessage();
            errors.put(field, message);
        });

        ErrorResponseDTO error = new ErrorResponseDTO(
                "Validation failed",
                "VALIDATION_ERROR",
                LocalDateTime.now(),
                errors
        );

        return Mono.just(ResponseEntity.badRequest().body(error));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public Mono<ResponseEntity<ErrorResponseDTO>> handleUserNotFound(
            UserNotFoundException ex,
            ServerWebExchange exchange
    ) {
        log.error("User not found: {}", ex.getMessage());
        ErrorResponseDTO error = new ErrorResponseDTO(
                ex.getMessage(),
                "USER_NOT_FOUND",
                LocalDateTime.now(),
                null
        );
        return Mono.just(ResponseEntity.badRequest().body(error));  // 400!
    }

    @ExceptionHandler(TransportServiceException.class)
    public Mono<ResponseEntity<ErrorResponseDTO>> handleTransportServiceException(TransportServiceException ex) {
        log.error("Transport service error: {}", ex.getMessage());
        ErrorResponseDTO error = new ErrorResponseDTO(
                ex.getMessage(),
                "TRANSPORT_SERVICE_ERROR",
                LocalDateTime.now(),
                null
        );
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error));
    }

    @ExceptionHandler(UserServiceException.class)
    public Mono<ResponseEntity<ErrorResponseDTO>> handleUserServiceException(
            UserServiceException ex,
            ServerWebExchange exchange
    ) {
        log.error("User service error: {}", ex.getMessage());
        ErrorResponseDTO error = new ErrorResponseDTO(
                ex.getMessage(),
                "USER_SERVICE_ERROR",
                LocalDateTime.now(),
                null
        );
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error));  // 503!
    }

    @ExceptionHandler(IllegalStateException.class)
    public Mono<ResponseEntity<ErrorResponseDTO>> handleIllegalState(IllegalStateException ex) {
        log.error("Illegal state: {}", ex.getMessage());
        ErrorResponseDTO error = new ErrorResponseDTO(
                ex.getMessage(),
                "ILLEGAL_STATE",
                LocalDateTime.now(),
                null
        );
        return Mono.just(ResponseEntity.badRequest().body(error));
    }

    @ExceptionHandler(DataNotFoundException.class)
    public Mono<ResponseEntity<ErrorResponseDTO>> handleDataNotFound(DataNotFoundException ex) {
        log.error("Data not found: {}", ex.getMessage());
        ErrorResponseDTO error = new ErrorResponseDTO(
                ex.getMessage(),
                "DATA_NOT_FOUND",
                LocalDateTime.now(),
                null
        );
        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(error));
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<ErrorResponseDTO>> handleValidationErrors(WebExchangeBindException ex) {
        log.error("Validation error: {}", ex.getMessage());
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
        });

        ErrorResponseDTO error = new ErrorResponseDTO(
                "Validation failed",
                "VALIDATION_ERROR",
                LocalDateTime.now(),
                fieldErrors
        );
        return Mono.just(ResponseEntity.badRequest().body(error));
    }

    @ExceptionHandler(TransportNotFoundException.class)
    public Mono<ResponseEntity<ErrorResponseDTO>> handleTransportNotFound(
            TransportNotFoundException ex,
            ServerWebExchange exchange
    ) {
        log.error("Transport not found: {}", ex.getMessage());
        ErrorResponseDTO error = new ErrorResponseDTO(
                ex.getMessage(),
                "TRANSPORT_NOT_FOUND",
                LocalDateTime.now(),
                null
        );
        return Mono.just(ResponseEntity.badRequest().body(error));  // 400!
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ErrorResponseDTO>> handleGenericException(Exception ex) {
        log.error("Unexpected error", ex);
        ErrorResponseDTO error = new ErrorResponseDTO(
                "Internal server error occurred",
                "INTERNAL_SERVER_ERROR",
                LocalDateTime.now(),
                Map.of("details", ex.getMessage())
        );
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error));
    }
}