package com.example.notification.exception;

/**
 * Exception thrown when Kafka publish operation fails
 */
public class KafkaPublishException extends NotificationException {
    
    public KafkaPublishException(String message) {
        super(message);
    }
    
    public KafkaPublishException(String message, Throwable cause) {
        super(message, cause);
    }
}
