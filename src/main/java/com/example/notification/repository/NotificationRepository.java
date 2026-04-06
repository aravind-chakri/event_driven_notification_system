package com.example.notification.repository;

import com.example.notification.entity.NotificationEntity;
import com.example.notification.model.NotificationStatus;
import com.example.notification.model.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for notification persistence operations
 */
@Repository
public interface NotificationRepository extends JpaRepository<NotificationEntity, String> {

    /**
     * Find notifications by recipient
     */
    List<NotificationEntity> findByRecipient(String recipient);

    /**
     * Find notifications by status
     */
    List<NotificationEntity> findByStatus(NotificationStatus status);

    /**
     * Find notifications by type
     */
    List<NotificationEntity> findByType(NotificationType type);

    /**
     * Find notifications by recipient and status
     */
    List<NotificationEntity> findByRecipientAndStatus(String recipient, NotificationStatus status);

    /**
     * Find notifications created within a date range
     */
    @Query("SELECT n FROM NotificationEntity n WHERE n.createdAt BETWEEN :startDate AND :endDate")
    List<NotificationEntity> findByCreatedAtBetween(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * Find failed notifications that need retry
     */
    @Query("SELECT n FROM NotificationEntity n WHERE n.status = 'FAILED' AND n.retryCount < :maxRetries")
    List<NotificationEntity> findFailedNotificationsForRetry(@Param("maxRetries") int maxRetries);

    /**
     * Count notifications by status
     */
    long countByStatus(NotificationStatus status);

    /**
     * Count notifications by type and status
     */
    long countByTypeAndStatus(NotificationType type, NotificationStatus status);

    /**
     * Find recent notifications with pagination
     */
    Page<NotificationEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
