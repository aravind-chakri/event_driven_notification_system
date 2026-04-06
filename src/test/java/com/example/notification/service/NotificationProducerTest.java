package com.example.notification.service;

import com.example.notification.model.NotificationEvent;
import com.example.notification.model.NotificationType;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationProducerTest {

    @Mock
    private KafkaTemplate<String, NotificationEvent> kafkaTemplate;

    @InjectMocks
    private NotificationProducer notificationProducer;

    private NotificationEvent testEvent;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(notificationProducer, "topicName", "notification-events");
        
        testEvent = NotificationEvent.builder()
                .id(UUID.randomUUID().toString())
                .type(NotificationType.EMAIL)
                .recipient("test@example.com")
                .subject("Test Subject")
                .message("Test message")
                .metadata(new HashMap<>())
                .retryCount(0)
                .build();
    }

    @Test
    void sendNotification_WithValidEvent_ShouldPublishToKafka() {
        // Given
        ProducerRecord<String, NotificationEvent> producerRecord = 
            new ProducerRecord<>("notification-events", testEvent.getId(), testEvent);
        RecordMetadata recordMetadata = new RecordMetadata(
            new TopicPartition("notification-events", 0), 0, 0, 0, 0, 0);
        SendResult<String, NotificationEvent> sendResult = 
            new SendResult<>(producerRecord, recordMetadata);
        
        CompletableFuture<SendResult<String, NotificationEvent>> future = 
            CompletableFuture.completedFuture(sendResult);
        
        when(kafkaTemplate.send(anyString(), eq(testEvent.getId()), eq(testEvent)))
            .thenReturn(future);

        // When
        notificationProducer.sendNotification(testEvent);

        // Then
        verify(kafkaTemplate, times(1))
            .send(eq("notification-events"), eq(testEvent.getId()), eq(testEvent));
    }

    @Test
    void sendNotification_WithKafkaFailure_ShouldHandleException() {
        // Given
        CompletableFuture<SendResult<String, NotificationEvent>> future = 
            CompletableFuture.failedFuture(new RuntimeException("Kafka error"));
        
        when(kafkaTemplate.send(anyString(), eq(testEvent.getId()), eq(testEvent)))
            .thenReturn(future);

        // When
        notificationProducer.sendNotification(testEvent);

        // Then - should not throw exception, error is logged
        verify(kafkaTemplate, times(1))
            .send(eq("notification-events"), eq(testEvent.getId()), eq(testEvent));
    }
}
