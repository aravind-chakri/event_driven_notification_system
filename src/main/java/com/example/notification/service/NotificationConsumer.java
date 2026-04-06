package com.example.notification.service;

import com.example.notification.exception.NotificationDeliveryException;
import com.example.notification.model.NotificationEvent;
import com.example.notification.model.NotificationStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumer {

    private final List<NotificationService> notificationServices;
    private final DeadLetterQueueProducer dlqProducer;
    private final NotificationPersistenceService persistenceService;

    @Value("${kafka.consumer.retry.max-attempts:3}")
    private int maxRetryAttempts;

    /**
     * Consumes notification events from Kafka and processes them.
     * Uses manual acknowledgment and retry mechanism with exponential backoff.
     * Failed notifications after max retries are sent to Dead Letter Queue.
     */
    @KafkaListener(
        topics = "${kafka.topic.notification}",
        groupId = "${kafka.consumer.group-id}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeNotification(
            @Payload NotificationEvent event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {
        
        log.info("Consumed notification [ID: {}] from partition: {} with offset: {} (Retry: {})", 
            event.getId(), partition, offset, event.getRetryCount());
        
        try {
            // Update status to processing
            event.setStatus(NotificationStatus.PROCESSING);
            persistenceService.updateNotificationStatus(event.getId(), NotificationStatus.PROCESSING);
            
            // Find appropriate notification service
            NotificationService service = notificationServices.stream()
                .filter(s -> s.supports(event))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                    "No notification service found for type: " + event.getType()));
            
            // Send notification
            service.send(event);
            
            // Update status to sent
            event.setStatus(NotificationStatus.SENT);
            persistenceService.updateNotificationStatus(event.getId(), NotificationStatus.SENT);
            log.info("Successfully processed notification [ID: {}]", event.getId());
            
            // Acknowledge successful processing
            if (acknowledgment != null) {
                acknowledgment.acknowledge();
            }
            
        } catch (Exception e) {
            log.error("Failed to process notification [ID: {}] (Retry: {}): {}", 
                event.getId(), event.getRetryCount(), e.getMessage(), e);
            
            event.setStatus(NotificationStatus.FAILED);
            
            // Handle failed notification with retry or DLQ
            handleFailedNotification(event, e, acknowledgment);
        }
    }

    /**
     * Handles failed notifications with retry mechanism and DLQ.
     * Retry logic is handled by Kafka's error handler (configured with exponential backoff).
     * After max retries, the notification is sent to DLQ.
     */
    private void handleFailedNotification(NotificationEvent event, Exception e, Acknowledgment acknowledgment) {
        // Increment retry count
        event.setRetryCount(event.getRetryCount() + 1);
        persistenceService.incrementRetryCount(event.getId());
        
        if (event.getRetryCount() >= maxRetryAttempts) {
            log.error("Max retries ({}) exceeded for notification [ID: {}]. Moving to DLQ", 
                maxRetryAttempts, event.getId());
            
            // Update database with error
            persistenceService.updateNotificationStatusWithError(
                event.getId(), 
                NotificationStatus.FAILED, 
                e.getMessage()
            );
            
            // Send to Dead Letter Queue for manual intervention
            dlqProducer.sendToDeadLetterQueue(event, e);
            
            // Acknowledge to prevent reprocessing
            if (acknowledgment != null) {
                acknowledgment.acknowledge();
            }
        } else {
            log.warn("Notification [ID: {}] will be retried. Attempt {}/{}", 
                event.getId(), event.getRetryCount(), maxRetryAttempts);
            
            // Update database with retry status
            persistenceService.updateNotificationStatusWithError(
                event.getId(), 
                NotificationStatus.FAILED, 
                "Retry " + event.getRetryCount() + ": " + e.getMessage()
            );
            
            // Don't acknowledge - let Kafka retry with exponential backoff
            // The retry is handled by DefaultErrorHandler in KafkaConsumerConfig
            throw new NotificationDeliveryException(event.getId(), e.getMessage());
        }
    }
}
