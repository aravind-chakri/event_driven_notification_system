package com.example.notification.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Standardized error response DTO for consistent API error handling
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    
    /**
     * HTTP status code
     */
    private int status;
    
    /**
     * Error type/category
     */
    private String error;
    
    /**
     * Human-readable error message
     */
    private String message;
    
    /**
     * Detailed error information (for debugging)
     */
    private String details;
    
    /**
     * API path where error occurred
     */
    private String path;
    
    /**
     * Timestamp of the error
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    
    /**
     * List of validation errors (if applicable)
     */
    private List<ValidationError> validationErrors;
    
    /**
     * Nested class for validation error details
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValidationError {
        private String field;
        private String message;
        private Object rejectedValue;
    }
}
