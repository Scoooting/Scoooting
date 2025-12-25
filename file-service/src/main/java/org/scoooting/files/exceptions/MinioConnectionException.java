package org.scoooting.files.exceptions;

public class MinioConnectionException extends RuntimeException {
    public MinioConnectionException(String message) {
        super(message);
    }
}
