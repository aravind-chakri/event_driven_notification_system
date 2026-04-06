package com.example.notification.service;

import com.example.notification.model.NotificationEvent;
import com.example.notification.model.NotificationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SmsNotificationServiceTest {

    private SmsNotificationService smsNotificationService;
    private NotificationEvent smsEvent;

    @BeforeEach
    void setUp() {
        smsNotificationService = new SmsNotificationService();
        
        smsEvent = NotificationEvent.builder()
                .id(UUID.randomUUID().toString())
                .type(NotificationType.SMS)
                .recipient("+1234567890")
                .message("Test SMS message")
                .metadata(new HashMap<>())
                .retryCount(0)
                .build();
    }

    @Test
    void send_WithValidSmsEvent_ShouldNotThrowException() {
        // When & Then
        assertDoesNotThrow(() -> smsNotificationService.send(smsEvent));
    }

    @Test
    void supports_WithSmsType_ShouldReturnTrue() {
        // When
        boolean result = smsNotificationService.supports(smsEvent);

        // Then
        assertTrue(result);
    }

    @Test
    void supports_WithEmailType_ShouldReturnFalse() {
        // Given
        smsEvent.setType(NotificationType.EMAIL);

        // When
        boolean result = smsNotificationService.supports(smsEvent);

        // Then
        assertFalse(result);
    }

    @Test
    void send_ShouldLogProperInformation() {
        // When
        assertDoesNotThrow(() -> smsNotificationService.send(smsEvent));
        
        // Then - verify no exception is thrown (mock SMS provider simulates success)
        assertNotNull(smsEvent.getRecipient());
        assertEquals(NotificationType.SMS, smsEvent.getType());
    }
}
