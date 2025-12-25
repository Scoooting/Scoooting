package org.scoooting.files.exceptions;

public class FileTypeException extends RuntimeException {
    public FileTypeException(String expected, String actual) {
        super("Expected: " + expected + ", actual: " + actual);
    }
}
