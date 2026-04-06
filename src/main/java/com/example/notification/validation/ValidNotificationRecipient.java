package com.example.notification.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Custom validation annotation for notification recipients
 * Validates email format or phone number format based on context
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NotificationRecipientValidator.class)
@Documented
public @interface ValidNotificationRecipient {
    
    String message() default "Invalid recipient format";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
}
