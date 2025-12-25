package org.scoooting.user.exceptions;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.scoooting.user.dto.common.ErrorResponseDTO;
import org.scoooting.user.exceptions.common.DataNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Handle Spring Security Access Denied (403)
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDTO> handleAccessDenied(
            AccessDeniedException ex,
            WebRequest request
    ) {
        log.error("Access denied: {}", ex.getMessage());
        ErrorResponseDTO error = new ErrorResponseDTO(
                "Access denied. You don't have permission to access this resource.",
                "FORBIDDEN",
                LocalDateTime.now(),
                request.getDescription(false).replace("uri=", ""),
                Map.of("details", "Insufficient permissions")
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    /**
     * Handle Authentication errors (401)
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponseDTO> handleAuthenticationException(
            AuthenticationException ex,
            WebRequest request
    ) {
        log.error("Authentication failed: {}", ex.getMessage());
        ErrorResponseDTO error = new ErrorResponseDTO(
                "Authentication required",
                "UNAUTHORIZED",
                LocalDateTime.now(),
                request.getDescription(false).replace("uri=", ""),
                null
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    /**
     * Validation errors for @RequestParam, @PathVariable
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponseDTO> handleConstraintViolation(ConstraintViolationException ex) {

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
                errors.toString(),
                null
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ResponseEntity<ErrorResponseDTO> handleInvalidRefreshToken(
            InvalidRefreshTokenException ex,
            WebRequest request
    ) {
        ErrorResponseDTO error = new ErrorResponseDTO(
                ex.getMessage(),
                "INVALID_REFRESH_TOKEN",
                LocalDateTime.now(),
                request.getDescription(false).replace("uri=", ""),
                null
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleUserNotFound(
            UserNotFoundException ex,
            WebRequest request
    ) {
        ErrorResponseDTO error = new ErrorResponseDTO(
                ex.getMessage(),
                "USER_NOT_FOUND",
                LocalDateTime.now(),
                request.getDescription(false).replace("uri=", ""),
                null
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponseDTO> handleUserAlreadyExists(
            UserAlreadyExistsException ex,
            WebRequest request
    ) {
        ErrorResponseDTO error = new ErrorResponseDTO(
                ex.getMessage(),
                "USER_ALREADY_EXISTS",
                LocalDateTime.now(),
                request.getDescription(false).replace("uri=", ""),
                null
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(DataNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleDataNotFound(
            DataNotFoundException ex,
            WebRequest request
    ) {
        ErrorResponseDTO error = new ErrorResponseDTO(
                ex.getMessage(),
                "DATA_NOT_FOUND",
                LocalDateTime.now(),
                request.getDescription(false).replace("uri=", ""),
                null
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidationErrors(
            MethodArgumentNotValidException ex,
            WebRequest request
    ) {
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
                request.getDescription(false).replace("uri=", ""),
                fieldErrors
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGenericException(
            Exception ex,
            WebRequest request
    ) {
        ErrorResponseDTO error = new ErrorResponseDTO(
                "Internal server error occurred",
                "INTERNAL_SERVER_ERROR",
                LocalDateTime.now(),
                request.getDescription(false).replace("uri=", ""),
                Map.of("details", ex.getMessage())
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
