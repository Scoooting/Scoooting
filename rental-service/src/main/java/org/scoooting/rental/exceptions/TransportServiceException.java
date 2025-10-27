package org.scoooting.rental.exceptions;

public class TransportServiceException extends RuntimeException {
    public TransportServiceException(String message) {
        super(message);
    }

    public TransportServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}