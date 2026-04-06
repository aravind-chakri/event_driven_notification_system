package com.example.notification.validation;

import com.example.notification.dto.NotificationRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

/**
 * Validator for notification recipients
 * Validates email addresses and phone numbers
 */
public class NotificationRecipientValidator implements ConstraintValidator<ValidNotificationRecipient, String> {

    // Email regex pattern (RFC 5322 simplified)
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );

    // Phone number pattern (international format with + and digits, 10-15 digits)
    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "^\\+?[1-9]\\d{9,14}$"
    );

    @Override
    public void initialize(ValidNotificationRecipient constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(String recipient, ConstraintValidatorContext context) {
        if (recipient == null || recipient.trim().isEmpty()) {
            return false;
        }

        // Check if it's a valid email or phone number
        boolean isValidEmail = EMAIL_PATTERN.matcher(recipient).matches();
        boolean isValidPhone = PHONE_PATTERN.matcher(recipient).matches();

        if (!isValidEmail && !isValidPhone) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                "Recipient must be a valid email address or phone number (format: +1234567890)"
            ).addConstraintViolation();
            return false;
        }

        return true;
    }
}
