package com.example.notification.service;

import com.example.notification.model.NotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * Service responsible for sending failed notifications to Dead Letter Queue
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DeadLetterQueueProducer {

    private final KafkaTemplate<String, NotificationEvent> kafkaTemplate;

    @Value("${kafka.topic.dlq}")
    private String dlqTopic;

    /**
     * Send failed notification to DLQ
     * 
     * @param event The notification event that failed
     * @param failureReason The reason for failure
     */
    public void sendToDeadLetterQueue(NotificationEvent event, String failureReason) {
        try {
            log.warn("Sending notification [ID: {}] to DLQ. Reason: {}", event.getId(), failureReason);
            
            // Add failure metadata to the event
            if (event.getMetadata() != null) {
                event.getMetadata().put("dlq_reason", failureReason);
                event.getMetadata().put("dlq_timestamp", String.valueOf(System.currentTimeMillis()));
            }
            
            CompletableFuture<SendResult<String, NotificationEvent>> future = 
                kafkaTemplate.send(dlqTopic, event.getId(), event);
            
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Successfully sent notification [ID: {}] to DLQ topic: {} at offset: {}",
                        event.getId(),
                        result.getRecordMetadata().topic(),
                        result.getRecordMetadata().offset());
                } else {
                    log.error("Failed to send notification [ID: {}] to DLQ: {}", 
                        event.getId(), ex.getMessage(), ex);
                }
            });
            
        } catch (Exception e) {
            log.error("Critical error: Failed to send notification [ID: {}] to DLQ: {}", 
                event.getId(), e.getMessage(), e);
        }
    }

    /**
     * Send failed notification with exception details
     */
    public void sendToDeadLetterQueue(NotificationEvent event, Exception exception) {
        String failureReason = exception.getClass().getSimpleName() + ": " + exception.getMessage();
        sendToDeadLetterQueue(event, failureReason);
    }
}
