package org.scoooting.files.domain.exceptions;

public class FileTypeException extends RuntimeException {
    public FileTypeException(String expected, String actual) {
        super("Expected: " + expected + ", actual: " + actual);
    }
}
