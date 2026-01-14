package org.scoooting.files.adapters.infrastructure.exceptions;

public class MinioException extends RuntimeException {
    public MinioException(String message) {
        super(message);
    }
}
