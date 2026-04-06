package com.example.notification.dto;

import com.example.notification.model.NotificationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
    
    private String id;
    private NotificationStatus status;
    private String message;
    private LocalDateTime timestamp;
}
