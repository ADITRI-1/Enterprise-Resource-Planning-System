package com.iiitd.erp.persistent.persistence.repository;

import com.iiitd.erp.persistent.entity.erp_db.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    Page<Notification> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    Page<Notification> findByUserIdAndReadAtIsNullOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    long countByUserIdAndReadAtIsNull(UUID userId);

    Optional<Notification> findByNotificationIdAndUserId(UUID notificationId, UUID userId);

    @Modifying
    @Query("update Notification n set n.readAt = :now where n.userId = :userId and n.readAt is null")
    int markAllRead(@Param("userId") UUID userId, @Param("now") OffsetDateTime now);

    /** One row per account, for announcements that go to everyone. */
    @Modifying
    @Query(value = """
            insert into erp_db.notifications (notification_id, user_id, type, title, message, link, created_at)
            select gen_random_uuid(), u.user_id, :type, :title, :message, :link, now() from auth_db.user_auth u
            """, nativeQuery = true)
    int broadcast(@Param("type") String type, @Param("title") String title,
                  @Param("message") String message, @Param("link") String link);
}
