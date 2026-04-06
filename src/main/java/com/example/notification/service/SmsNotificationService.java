package com.example.notification.service;

import com.example.notification.model.NotificationEvent;
import com.example.notification.model.NotificationType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SmsNotificationService implements NotificationService {

    @Override
    public void send(NotificationEvent event) throws Exception {
        try {
            log.info("Sending SMS to: {}", event.getRecipient());
            
            // Simulate SMS sending - In production, integrate with Twilio, AWS SNS, etc.
            log.info("SMS Content: {}", event.getMessage());
            
            // TODO: Integrate with actual SMS provider
            // Example: twilioClient.messages.create(...)
            
            // Simulate processing time
            Thread.sleep(100);
            
            log.info("Successfully sent SMS notification [ID: {}] to: {}", 
                event.getId(), event.getRecipient());
        } catch (Exception e) {
            log.error("Failed to send SMS notification [ID: {}]: {}", 
                event.getId(), e.getMessage());
            throw e;
        }
    }

    @Override
    public boolean supports(NotificationEvent event) {
        return event.getType() == NotificationType.SMS;
    }
}
