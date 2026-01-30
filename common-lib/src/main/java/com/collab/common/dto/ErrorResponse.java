package com.collab.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Error Response DTO
 * 
 * Standardized error response for all API exceptions and failures.
 * Provides detailed error information including message, error code, validation details, and request path.
 * Used by global exception handlers across all microservices.
 * 
 * @author Arhum Khan
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    private boolean success;
    private String message;
    private String error;
    private List<String> details;
    private LocalDateTime timestamp;
    private String path;

    public ErrorResponse(String message, String error, String path) {
        this.success = false;
        this.message = message;
        this.error = error;
        this.path = path;
        this.timestamp = LocalDateTime.now();
    }

    public ErrorResponse(String message, String error, List<String> details, String path) {
        this.success = false;
        this.message = message;
        this.error = error;
        this.details = details;
        this.path = path;
        this.timestamp = LocalDateTime.now();
    }
}
