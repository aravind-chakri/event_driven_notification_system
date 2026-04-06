package com.example.notification.service;

import com.example.notification.exception.NotificationDeliveryException;
import com.example.notification.model.NotificationEvent;
import com.example.notification.model.NotificationStatus;
import com.example.notification.model.NotificationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationConsumerTest {

    @Mock
    private EmailNotificationService emailNotificationService;

    @Mock
    private SmsNotificationService smsNotificationService;

    @Mock
    private DeadLetterQueueProducer dlqProducer;

    @Mock
    private Acknowledgment acknowledgment;

    @InjectMocks
    private NotificationConsumer notificationConsumer;

    private NotificationEvent emailEvent;
    private NotificationEvent smsEvent;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(notificationConsumer, "notificationServices", 
            Arrays.asList(emailNotificationService, smsNotificationService));
        ReflectionTestUtils.setField(notificationConsumer, "maxRetryAttempts", 3);

        emailEvent = NotificationEvent.builder()
                .id(UUID.randomUUID().toString())
                .type(NotificationType.EMAIL)
                .recipient("test@example.com")
                .subject("Test Subject")
                .message("Test message")
                .metadata(new HashMap<>())
                .retryCount(0)
                .build();

        smsEvent = NotificationEvent.builder()
                .id(UUID.randomUUID().toString())
                .type(NotificationType.SMS)
                .recipient("+1234567890")
                .message("Test SMS")
                .metadata(new HashMap<>())
                .retryCount(0)
                .build();
    }

    @Test
    void consumeNotification_WithValidEmailEvent_ShouldProcessSuccessfully() {
        // Given
        when(emailNotificationService.supports(emailEvent)).thenReturn(true);
        doNothing().when(emailNotificationService).send(emailEvent);

        // When
        notificationConsumer.consumeNotification(emailEvent, 0, 0L, acknowledgment);

        // Then
        verify(emailNotificationService, times(1)).send(emailEvent);
        verify(acknowledgment, times(1)).acknowledge();
        assertEquals(NotificationStatus.SENT, emailEvent.getStatus());
    }

    @Test
    void consumeNotification_WithValidSmsEvent_ShouldProcessSuccessfully() {
        // Given
        when(smsNotificationService.supports(smsEvent)).thenReturn(true);
        doNothing().when(smsNotificationService).send(smsEvent);

        // When
        notificationConsumer.consumeNotification(smsEvent, 0, 0L, acknowledgment);

        // Then
        verify(smsNotificationService, times(1)).send(smsEvent);
        verify(acknowledgment, times(1)).acknowledge();
        assertEquals(NotificationStatus.SENT, smsEvent.getStatus());
    }

    @Test
    void consumeNotification_WithUnsupportedType_ShouldThrowException() {
        // Given
        when(emailNotificationService.supports(emailEvent)).thenReturn(false);
        when(smsNotificationService.supports(emailEvent)).thenReturn(false);

        // When & Then
        assertThrows(NotificationDeliveryException.class, () -> 
            notificationConsumer.consumeNotification(emailEvent, 0, 0L, acknowledgment));
        
        assertEquals(NotificationStatus.FAILED, emailEvent.getStatus());
    }

    @Test
    void consumeNotification_WithServiceFailureAndRetryAvailable_ShouldRetry() {
        // Given
        emailEvent.setRetryCount(1);
        when(emailNotificationService.supports(emailEvent)).thenReturn(true);
        doThrow(new RuntimeException("Service failure")).when(emailNotificationService).send(emailEvent);

        // When & Then
        assertThrows(NotificationDeliveryException.class, () ->
            notificationConsumer.consumeNotification(emailEvent, 0, 0L, acknowledgment));

        verify(emailNotificationService, times(1)).send(emailEvent);
        verify(acknowledgment, never()).acknowledge();
        assertEquals(2, emailEvent.getRetryCount());
    }

    @Test
    void consumeNotification_WithMaxRetriesExceeded_ShouldSendToDLQ() {
        // Given
        emailEvent.setRetryCount(2);
        when(emailNotificationService.supports(emailEvent)).thenReturn(true);
        doThrow(new RuntimeException("Service failure")).when(emailNotificationService).send(emailEvent);

        // When
        notificationConsumer.consumeNotification(emailEvent, 0, 0L, acknowledgment);

        // Then
        verify(dlqProducer, times(1)).sendToDeadLetterQueue(eq(emailEvent), any(Exception.class));
        verify(acknowledgment, times(1)).acknowledge();
        assertEquals(3, emailEvent.getRetryCount());
    }
}
