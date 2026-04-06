package com.example.notification.service;

import com.example.notification.dto.NotificationRequest;
import com.example.notification.dto.NotificationResponse;
import com.example.notification.model.NotificationEvent;
import com.example.notification.model.NotificationStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationOrchestrationService {

    private final NotificationProducer notificationProducer;

    /**
     * Orchestrates the notification sending process
     * Creates event and publishes to Kafka for async processing
     */
    public NotificationResponse sendNotification(NotificationRequest request) {
        log.info("Received notification request for recipient: {}", request.getRecipient());
        
        // Create notification event
        NotificationEvent event = NotificationEvent.builder()
                .id(UUID.randomUUID().toString())
                .type(request.getType())
                .recipient(request.getRecipient())
                .subject(request.getSubject())
                .message(request.getMessage())
                .metadata(request.getMetadata())
                .status(NotificationStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .retryCount(0)
                .build();

        // Publish to Kafka for async processing
        notificationProducer.sendNotification(event);

        // Return immediate response (async processing)
        return NotificationResponse.builder()
                .id(event.getId())
                .status(NotificationStatus.PENDING)
                .message("Notification queued for processing")
                .timestamp(LocalDateTime.now())
                .build();
    }
}
