package org.scoooting.transport.exceptions;

import jakarta.validation.ConstraintViolationException;
import org.scoooting.transport.dto.common.ErrorResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Transport not found (404)
     */
    @ExceptionHandler(TransportNotFoundException.class)
    public Mono<ResponseEntity<ErrorResponseDTO>> handleTransportNotFound(
            TransportNotFoundException ex,
            ServerWebExchange exchange
    ) {
        ErrorResponseDTO error = new ErrorResponseDTO(
                ex.getMessage(),
                "TRANSPORT_NOT_FOUND",
                LocalDateTime.now(),
                Map.of("path", exchange.getRequest().getPath().value())
        );

        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(error));
    }

    /**
     * Data not found (404)
     */
    @ExceptionHandler(DataNotFoundException.class)
    public Mono<ResponseEntity<ErrorResponseDTO>> handleDataNotFound(
            DataNotFoundException ex,
            ServerWebExchange exchange
    ) {
        ErrorResponseDTO error = new ErrorResponseDTO(
                ex.getMessage(),
                "DATA_NOT_FOUND",
                LocalDateTime.now(),
                Map.of("path", exchange.getRequest().getPath().value())
        );

        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(error));
    }

    /**
     * Validation errors for @PathVariable, @RequestParam (400)
     */
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

    /**
     * Validation errors for @RequestBody (400)
     */
    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<ErrorResponseDTO>> handleWebExchangeBindException(
            WebExchangeBindException ex,
            ServerWebExchange exchange
    ) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );

        ErrorResponseDTO error = new ErrorResponseDTO(
                "Validation failed",
                "VALIDATION_ERROR",
                LocalDateTime.now(),
                errors
        );

        return Mono.just(ResponseEntity.badRequest().body(error));
    }

    /**
     * Illegal arguments (400)
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public Mono<ResponseEntity<ErrorResponseDTO>> handleIllegalArgument(
            IllegalArgumentException ex,
            ServerWebExchange exchange
    ) {
        ErrorResponseDTO error = new ErrorResponseDTO(
                ex.getMessage(),
                "INVALID_ARGUMENT",
                LocalDateTime.now(),
                Map.of("path", exchange.getRequest().getPath().value())
        );

        return Mono.just(ResponseEntity.badRequest().body(error));
    }

    /**
     * Catch-all for unexpected errors (500)
     */
    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ErrorResponseDTO>> handleGlobalException(
            Exception ex,
            ServerWebExchange exchange
    ) {
        ErrorResponseDTO error = new ErrorResponseDTO(
                "Internal server error occurred",
                "INTERNAL_SERVER_ERROR",
                LocalDateTime.now(),
                Map.of(
                        "path", exchange.getRequest().getPath().value(),
                        "error", ex.getClass().getSimpleName()
                )
        );

        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error));
    }
}
