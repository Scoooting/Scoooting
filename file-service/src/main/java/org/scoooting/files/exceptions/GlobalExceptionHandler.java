package org.scoooting.files.exceptions;

import io.minio.errors.ErrorResponseException;
import lombok.extern.slf4j.Slf4j;
import org.scoooting.files.dto.common.ErrorResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;
import java.time.LocalDateTime;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MinioException.class)
    public ResponseEntity<ErrorResponseDTO> minioException(
            MinioException ex
    ) {
        log.error(ex.getMessage());
        ErrorResponseDTO error = new ErrorResponseDTO(
                ex.getMessage(),
                LocalDateTime.now(),
                null
        );
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
    }

    @ExceptionHandler(FileTypeException.class)
    public ResponseEntity<ErrorResponseDTO> fileTypeException(
            FileTypeException ex
    ) {
        log.error(ex.getMessage());
        ErrorResponseDTO error = new ErrorResponseDTO(
                "Wrong file type. " + ex.getMessage(),
                LocalDateTime.now(),
                null
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(ErrorResponseException.class)
    public ResponseEntity<ErrorResponseDTO> errorResponseException(
            ErrorResponseException ex
    ) {
        log.error(ex.getMessage());
        ErrorResponseDTO error = new ErrorResponseDTO(
                ex.getMessage(),
                LocalDateTime.now(),
                null
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
}
