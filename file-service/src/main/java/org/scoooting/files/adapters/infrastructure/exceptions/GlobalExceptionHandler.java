package org.scoooting.files.adapters.infrastructure.exceptions;

import io.minio.errors.ErrorResponseException;
import lombok.extern.slf4j.Slf4j;
import org.scoooting.files.adapters.infrastructure.exceptions.dto.ErrorResponseDTO;
import org.scoooting.files.domain.exceptions.FileNotFoundException;
import org.scoooting.files.domain.exceptions.FileTypeException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(FileNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> minioException(
            FileNotFoundException ex
    ) {
        log.error(ex.getMessage());
        ErrorResponseDTO error = new ErrorResponseDTO(
                ex.getMessage(),
                LocalDateTime.now(),
                null
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
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
}
