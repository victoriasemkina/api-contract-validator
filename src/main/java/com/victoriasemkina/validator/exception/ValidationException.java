package com.victoriasemkina.validator.exception;

/**
 * Exception thrown when validation fails due to invalid schema or response
 */
public class ValidationException extends RuntimeException {

    private final String fieldPath;
    private final String endpointPath;

    public ValidationException(String message, String fieldPath, String endpointPath) {
        super(message);
        this.fieldPath = fieldPath;
        this.endpointPath = endpointPath;
    }

    public ValidationException(String message, String fieldPath, String endpointPath, Throwable cause) {
        super(message, cause);
        this.fieldPath = fieldPath;
        this.endpointPath = endpointPath;
    }

    public String getFieldPath() {
        return fieldPath;
    }

    public String getEndpointPath() {
        return endpointPath;
    }
}