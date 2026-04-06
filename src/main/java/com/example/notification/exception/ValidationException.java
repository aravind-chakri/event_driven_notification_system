package com.example.notification.exception;

/**
 * Exception thrown when validation fails for notification requests
 */
public class ValidationException extends NotificationException {
    
    public ValidationException(String message) {
        super(message);
    }
    
    public ValidationException(String field, String reason) {
        super(String.format("Validation failed for field '%s': %s", field, reason));
    }
}
