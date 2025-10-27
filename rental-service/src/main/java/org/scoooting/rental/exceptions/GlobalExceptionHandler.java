package org.scoooting.rental.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.scoooting.rental.dto.common.ErrorResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(TransportServiceException.class)
    public Mono<ResponseEntity<ErrorResponseDTO>> handleTransportServiceException(TransportServiceException ex) {
        log.error("Transport service error: {}", ex.getMessage());
        ErrorResponseDTO error = new ErrorResponseDTO(
                ex.getMessage(),
                "TRANSPORT_SERVICE_ERROR",
                LocalDateTime.now(),
                "/api/rentals",
                null
        );
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error));
    }

    @ExceptionHandler(UserServiceException.class)
    public Mono<ResponseEntity<ErrorResponseDTO>> handleUserServiceException(UserServiceException ex) {
        log.error("User service error: {}", ex.getMessage());
        ErrorResponseDTO error = new ErrorResponseDTO(
                ex.getMessage(),
                "USER_SERVICE_ERROR",
                LocalDateTime.now(),
                "/api/rentals",
                null
        );
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error));
    }

    @ExceptionHandler(IllegalStateException.class)
    public Mono<ResponseEntity<ErrorResponseDTO>> handleIllegalState(IllegalStateException ex) {
        log.error("Illegal state: {}", ex.getMessage());
        ErrorResponseDTO error = new ErrorResponseDTO(
                ex.getMessage(),
                "ILLEGAL_STATE",
                LocalDateTime.now(),
                "/api/rentals",
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
                "/api/rentals",
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
                "/api/rentals",
                fieldErrors
        );
        return Mono.just(ResponseEntity.badRequest().body(error));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ErrorResponseDTO>> handleGenericException(Exception ex) {
        log.error("Unexpected error", ex);
        ErrorResponseDTO error = new ErrorResponseDTO(
                "Internal server error occurred",
                "INTERNAL_SERVER_ERROR",
                LocalDateTime.now(),
                "/api/rentals",
                Map.of("details", ex.getMessage())
        );
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error));
    }
}