package com.collab.common.exception;

/**
 * Resource Not Found Exception
 * 
 * Custom exception for resource lookup failures.
 * Thrown when a requested resource (user, document, version) cannot be found in the database.
 * 
 * @author Arhum Khan
 * @version 1.0.0
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resource, String field, Object value) {
        super(String.format("%s not found with %s: '%s'", resource, field, value));
    }
}
