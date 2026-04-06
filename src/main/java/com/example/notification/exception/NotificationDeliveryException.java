package com.example.notification.exception;

/**
 * Exception thrown when notification delivery fails
 */
public class NotificationDeliveryException extends NotificationException {
    
    public NotificationDeliveryException(String message) {
        super(message);
    }
    
    public NotificationDeliveryException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public NotificationDeliveryException(String notificationId, String reason) {
        super(String.format("Failed to deliver notification [ID: %s]. Reason: %s", 
            notificationId, reason));
    }
}
