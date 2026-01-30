package com.collab.common.exception;

/**
 * Duplicate Resource Exception
 * 
 * Custom exception for unique constraint violations.
 * Thrown when attempting to create a resource that already exists (e.g., duplicate email, username).
 * 
 * @author Arhum Khan
 * @version 1.0.0
 */
public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) {
        super(message);
    }

    public DuplicateResourceException(String resource, String field, Object value) {
        super(String.format("%s already exists with %s: '%s'", resource, field, value));
    }
}
