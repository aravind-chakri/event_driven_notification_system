package com.example.notification.service;

import com.example.notification.model.NotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationProducer {

    private final KafkaTemplate<String, NotificationEvent> kafkaTemplate;
    private final NotificationPersistenceService persistenceService;

    @Value("${kafka.topic.notification}")
    private String notificationTopic;

    /**
     * Publishes notification event to Kafka topic and saves to database
     * @param event The notification event to publish
     */
    public void sendNotification(NotificationEvent event) {
        log.info("Publishing notification event: {} to topic: {}", event.getId(), notificationTopic);
        
        // Save to database first
        persistenceService.saveNotification(event);
        
        CompletableFuture<SendResult<String, NotificationEvent>> future = 
            kafkaTemplate.send(notificationTopic, event.getId(), event);
        
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Successfully published notification [ID: {}] with offset: {}", 
                    event.getId(), 
                    result.getRecordMetadata().offset());
            } else {
                log.error("Failed to publish notification [ID: {}]: {}", 
                    event.getId(), 
                    ex.getMessage());
                // Update status to failed in database
                persistenceService.updateNotificationStatusWithError(
                    event.getId(), 
                    event.getStatus(), 
                    ex.getMessage()
                );
            }
        });
    }
}
