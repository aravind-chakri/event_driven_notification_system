package com.example.notification.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEvent {
    
    private String id;
    private NotificationType type;
    private String recipient;
    private String subject;
    private String message;
    private Map<String, String> metadata;
    private NotificationStatus status;
    private LocalDateTime createdAt;
    private int retryCount;
}
