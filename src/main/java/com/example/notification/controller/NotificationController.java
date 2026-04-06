package com.example.notification.controller;

import com.example.notification.dto.NotificationRequest;
import com.example.notification.dto.NotificationResponse;
import com.example.notification.service.NotificationOrchestrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationOrchestrationService orchestrationService;

    /**
     * Send notification endpoint
     * Accepts notification request and queues it for async processing via Kafka
     * 
     * @param request The notification request containing type, recipient, and message
     * @return NotificationResponse with event ID and status
     */
    @PostMapping
    public ResponseEntity<NotificationResponse> sendNotification(
            @Valid @RequestBody NotificationRequest request) {
        
        log.info("POST /api/v1/notifications - Type: {}, Recipient: {}", 
            request.getType(), request.getRecipient());
        
        NotificationResponse response = orchestrationService.sendNotification(request);
        
        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(response);
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Notification Service is running");
    }
}
