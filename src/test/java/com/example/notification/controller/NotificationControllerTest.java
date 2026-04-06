package com.example.notification.controller;

import com.example.notification.dto.NotificationRequest;
import com.example.notification.model.NotificationType;
import com.example.notification.service.NotificationProducer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificationController.class)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private NotificationProducer notificationProducer;

    @Test
    void sendNotification_WithValidEmailRequest_ShouldReturnAccepted() throws Exception {
        // Given
        NotificationRequest request = NotificationRequest.builder()
                .type(NotificationType.EMAIL)
                .recipient("test@example.com")
                .subject("Test Subject")
                .message("Test message")
                .metadata(new HashMap<>())
                .build();

        doNothing().when(notificationProducer).sendNotification(any());

        // When & Then
        mockMvc.perform(post("/api/v1/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.message").value("Notification queued for processing"));
    }

    @Test
    void sendNotification_WithValidSmsRequest_ShouldReturnAccepted() throws Exception {
        // Given
        NotificationRequest request = NotificationRequest.builder()
                .type(NotificationType.SMS)
                .recipient("+1234567890")
                .message("Test SMS message")
                .build();

        doNothing().when(notificationProducer).sendNotification(any());

        // When & Then
        mockMvc.perform(post("/api/v1/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void sendNotification_WithInvalidEmail_ShouldReturnBadRequest() throws Exception {
        // Given
        NotificationRequest request = NotificationRequest.builder()
                .type(NotificationType.EMAIL)
                .recipient("invalid-email")
                .message("Test message")
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Error"));
    }

    @Test
    void sendNotification_WithMissingRecipient_ShouldReturnBadRequest() throws Exception {
        // Given
        NotificationRequest request = NotificationRequest.builder()
                .type(NotificationType.EMAIL)
                .message("Test message")
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void sendNotification_WithMissingType_ShouldReturnBadRequest() throws Exception {
        // Given
        NotificationRequest request = NotificationRequest.builder()
                .recipient("test@example.com")
                .message("Test message")
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void sendNotification_WithEmptyMessage_ShouldReturnBadRequest() throws Exception {
        // Given
        NotificationRequest request = NotificationRequest.builder()
                .type(NotificationType.EMAIL)
                .recipient("test@example.com")
                .message("")
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void healthCheck_ShouldReturnOk() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/notifications/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("Notification Service is running"));
    }
}
