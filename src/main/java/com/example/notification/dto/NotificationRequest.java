package com.example.notification.dto;

import com.example.notification.model.NotificationType;
import com.example.notification.validation.ValidNotificationRecipient;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {
    
    @NotNull(message = "Notification type is required")
    private NotificationType type;
    
    @NotBlank(message = "Recipient is required")
    @ValidNotificationRecipient
    private String recipient;
    
    @Size(max = 200, message = "Subject must not exceed 200 characters")
    private String subject;
    
    @NotBlank(message = "Message is required")
    @Size(min = 1, max = 1000, message = "Message must be between 1 and 1000 characters")
    private String message;
    
    private Map<String, String> metadata;
}
