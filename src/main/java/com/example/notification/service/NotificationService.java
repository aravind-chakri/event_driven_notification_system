package com.example.notification.service;

import com.example.notification.model.NotificationEvent;

/**
 * Interface for notification delivery services
 */
public interface NotificationService {
    
    /**
     * Send notification to recipient
     * @param event The notification event to send
     * @throws Exception if sending fails
     */
    void send(NotificationEvent event) throws Exception;
    
    /**
     * Check if this service supports the given notification type
     * @param event The notification event
     * @return true if supported, false otherwise
     */
    boolean supports(NotificationEvent event);
}
