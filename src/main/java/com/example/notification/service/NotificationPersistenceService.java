package com.example.notification.service;

import com.example.notification.entity.NotificationEntity;
import com.example.notification.model.NotificationEvent;
import com.example.notification.model.NotificationStatus;
import com.example.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Service for managing notification persistence
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationPersistenceService {

    private final NotificationRepository notificationRepository;

    /**
     * Save or update notification
     */
    @Transactional
    public NotificationEntity saveNotification(NotificationEvent event) {
        NotificationEntity entity = NotificationEntity.builder()
                .id(event.getId())
                .type(event.getType())
                .recipient(event.getRecipient())
                .subject(event.getSubject())
                .message(event.getMessage())
                .status(event.getStatus())
                .retryCount(event.getRetryCount())
                .build();

        return notificationRepository.save(entity);
    }

    /**
     * Update notification status
     */
    @Transactional
    public void updateNotificationStatus(String notificationId, NotificationStatus status) {
        Optional<NotificationEntity> entityOpt = notificationRepository.findById(notificationId);
        if (entityOpt.isPresent()) {
            NotificationEntity entity = entityOpt.get();
            entity.setStatus(status);
            notificationRepository.save(entity);
            log.debug("Updated notification [ID: {}] status to {}", notificationId, status);
        }
    }

    /**
     * Update notification status with error message
     */
    @Transactional
    public void updateNotificationStatusWithError(String notificationId, 
                                                   NotificationStatus status, 
                                                   String errorMessage) {
        Optional<NotificationEntity> entityOpt = notificationRepository.findById(notificationId);
        if (entityOpt.isPresent()) {
            NotificationEntity entity = entityOpt.get();
            entity.setStatus(status);
            entity.setErrorMessage(errorMessage);
            notificationRepository.save(entity);
            log.debug("Updated notification [ID: {}] status to {} with error: {}", 
                notificationId, status, errorMessage);
        }
    }

    /**
     * Increment retry count
     */
    @Transactional
    public void incrementRetryCount(String notificationId) {
        Optional<NotificationEntity> entityOpt = notificationRepository.findById(notificationId);
        if (entityOpt.isPresent()) {
            NotificationEntity entity = entityOpt.get();
            entity.setRetryCount(entity.getRetryCount() + 1);
            notificationRepository.save(entity);
            log.debug("Incremented retry count for notification [ID: {}] to {}", 
                notificationId, entity.getRetryCount());
        }
    }

    /**
     * Find notification by ID
     */
    public Optional<NotificationEntity> findById(String notificationId) {
        return notificationRepository.findById(notificationId);
    }

    /**
     * Get notification statistics
     */
    public NotificationStats getStatistics() {
        return NotificationStats.builder()
                .totalNotifications(notificationRepository.count())
                .pendingCount(notificationRepository.countByStatus(NotificationStatus.PENDING))
                .processingCount(notificationRepository.countByStatus(NotificationStatus.PROCESSING))
                .sentCount(notificationRepository.countByStatus(NotificationStatus.SENT))
                .failedCount(notificationRepository.countByStatus(NotificationStatus.FAILED))
                .build();
    }

    /**
     * Inner class for notification statistics
     */
    @lombok.Data
    @lombok.Builder
    public static class NotificationStats {
        private long totalNotifications;
        private long pendingCount;
        private long processingCount;
        private long sentCount;
        private long failedCount;
    }
}
