package org.scoooting.user.exceptions;

import org.scoooting.user.dto.common.ErrorResponseDTO;
import org.scoooting.user.exceptions.common.DataNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

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

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex
    ) {
        Map<String, Object> error = new HashMap<>();
        error.put("message", "Invalid parameter type");
        error.put("code", "BAD_REQUEST");
        error.put("timestamp", LocalDateTime.now());
        error.put("path", ex.getName());
        error.put("details", String.format(
                "Parameter '%s' must be a valid %s. Provided value: '%s'",
                ex.getName(),
                ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "number",
                ex.getValue()
        ));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
}
