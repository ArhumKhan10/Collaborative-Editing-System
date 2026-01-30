package com.collab.common.exception;

/**
 * Unauthorized Exception
 * 
 * Custom exception for authentication and authorization failures.
 * Thrown when a user attempts to access a resource without proper credentials or permissions.
 * 
 * @author Arhum Khan
 * @version 1.0.0
 */
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }

    public UnauthorizedException() {
        super("Unauthorized access");
    }
}
