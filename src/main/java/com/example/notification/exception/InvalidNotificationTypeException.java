package com.example.notification.exception;

/**
 * Exception thrown when notification type is invalid or not supported
 */
public class InvalidNotificationTypeException extends NotificationException {
    
    public InvalidNotificationTypeException(String message) {
        super(message);
    }
}
