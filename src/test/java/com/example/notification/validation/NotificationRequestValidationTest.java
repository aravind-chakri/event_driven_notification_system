package com.example.notification.validation;

import com.example.notification.dto.NotificationRequest;
import com.example.notification.model.NotificationType;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class NotificationRequestValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void validate_WithValidEmailRecipient_ShouldPass() {
        // Given
        NotificationRequest request = NotificationRequest.builder()
                .type(NotificationType.EMAIL)
                .recipient("test@example.com")
                .message("Test message")
                .build();

        // When
        Set<ConstraintViolation<NotificationRequest>> violations = validator.validate(request);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    void validate_WithValidPhoneRecipient_ShouldPass() {
        // Given
        NotificationRequest request = NotificationRequest.builder()
                .type(NotificationType.SMS)
                .recipient("+1234567890")
                .message("Test message")
                .build();

        // When
        Set<ConstraintViolation<NotificationRequest>> violations = validator.validate(request);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    void validate_WithInvalidEmailFormat_ShouldFail() {
        // Given
        NotificationRequest request = NotificationRequest.builder()
                .type(NotificationType.EMAIL)
                .recipient("invalid-email")
                .message("Test message")
                .build();

        // When
        Set<ConstraintViolation<NotificationRequest>> violations = validator.validate(request);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("recipient")));
    }

    @Test
    void validate_WithInvalidPhoneFormat_ShouldFail() {
        // Given
        NotificationRequest request = NotificationRequest.builder()
                .type(NotificationType.SMS)
                .recipient("123") // Too short
                .message("Test message")
                .build();

        // When
        Set<ConstraintViolation<NotificationRequest>> violations = validator.validate(request);

        // Then
        assertFalse(violations.isEmpty());
    }

    @Test
    void validate_WithMissingType_ShouldFail() {
        // Given
        NotificationRequest request = NotificationRequest.builder()
                .recipient("test@example.com")
                .message("Test message")
                .build();

        // When
        Set<ConstraintViolation<NotificationRequest>> violations = validator.validate(request);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("type")));
    }

    @Test
    void validate_WithEmptyMessage_ShouldFail() {
        // Given
        NotificationRequest request = NotificationRequest.builder()
                .type(NotificationType.EMAIL)
                .recipient("test@example.com")
                .message("")
                .build();

        // When
        Set<ConstraintViolation<NotificationRequest>> violations = validator.validate(request);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("message")));
    }

    @Test
    void validate_WithMessageTooLong_ShouldFail() {
        // Given
        String longMessage = "a".repeat(1001); // Over 1000 characters
        NotificationRequest request = NotificationRequest.builder()
                .type(NotificationType.EMAIL)
                .recipient("test@example.com")
                .message(longMessage)
                .build();

        // When
        Set<ConstraintViolation<NotificationRequest>> violations = validator.validate(request);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("message")));
    }

    @Test
    void validate_WithSubjectTooLong_ShouldFail() {
        // Given
        String longSubject = "a".repeat(201); // Over 200 characters
        NotificationRequest request = NotificationRequest.builder()
                .type(NotificationType.EMAIL)
                .recipient("test@example.com")
                .subject(longSubject)
                .message("Test message")
                .build();

        // When
        Set<ConstraintViolation<NotificationRequest>> violations = validator.validate(request);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("subject")));
    }
}
