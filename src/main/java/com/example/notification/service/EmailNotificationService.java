package com.example.notification.service;

import com.example.notification.model.NotificationEvent;
import com.example.notification.model.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailNotificationService implements NotificationService {

    private final JavaMailSender mailSender;

    @Override
    public void send(NotificationEvent event) throws Exception {
        try {
            log.info("Sending email to: {}", event.getRecipient());
            
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(event.getRecipient());
            message.setSubject(event.getSubject());
            message.setText(event.getMessage());
            message.setFrom("noreply@notification-system.com");
            
            mailSender.send(message);
            
            log.info("Successfully sent email notification [ID: {}] to: {}", 
                event.getId(), event.getRecipient());
        } catch (Exception e) {
            log.error("Failed to send email notification [ID: {}]: {}", 
                event.getId(), e.getMessage());
            throw e;
        }
    }

    @Override
    public boolean supports(NotificationEvent event) {
        return event.getType() == NotificationType.EMAIL;
    }
}
