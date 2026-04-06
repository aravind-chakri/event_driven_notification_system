package com.example.notification.service;

import com.example.notification.model.NotificationEvent;
import com.example.notification.model.NotificationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.HashMap;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailNotificationServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailNotificationService emailNotificationService;

    private NotificationEvent emailEvent;

    @BeforeEach
    void setUp() {
        emailEvent = NotificationEvent.builder()
                .id(UUID.randomUUID().toString())
                .type(NotificationType.EMAIL)
                .recipient("test@example.com")
                .subject("Test Subject")
                .message("Test message content")
                .metadata(new HashMap<>())
                .retryCount(0)
                .build();
    }

    @Test
    void send_WithValidEmailEvent_ShouldSendEmail() {
        // Given
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // When
        emailNotificationService.send(emailEvent);

        // Then
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void supports_WithEmailType_ShouldReturnTrue() {
        // When
        boolean result = emailNotificationService.supports(emailEvent);

        // Then
        assert result;
    }

    @Test
    void supports_WithSmsType_ShouldReturnFalse() {
        // Given
        emailEvent.setType(NotificationType.SMS);

        // When
        boolean result = emailNotificationService.supports(emailEvent);

        // Then
        assert !result;
    }
}
