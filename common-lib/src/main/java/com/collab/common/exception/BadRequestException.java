package com.collab.common.exception;

/**
 * Bad Request Exception
 * 
 * Custom exception for invalid client requests.
 * Thrown when request data fails validation or contains malformed information.
 * 
 * @author Arhum Khan
 * @version 1.0.0
 */
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
