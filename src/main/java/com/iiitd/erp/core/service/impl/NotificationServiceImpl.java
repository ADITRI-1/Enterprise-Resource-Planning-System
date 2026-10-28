package com.iiitd.erp.core.service.impl;

import com.iiitd.erp.Domain.NotificationView;
import com.iiitd.erp.core.service.NotificationService;
import com.iiitd.erp.exception.ErpException;
import com.iiitd.erp.exception.ErrorCode;
import com.iiitd.erp.persistent.entity.erp_db.Notification;
import com.iiitd.erp.persistent.persistence.repository.NotificationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notifications;

    public NotificationServiceImpl(NotificationRepository notifications) {
        this.notifications = notifications;
    }

    @Override
    public void notify(UUID userId, String type, String title, String message, String link) {
        notifications.save(Notification.builder()
                .notificationId(UUID.randomUUID())
                .userId(userId)
                .type(type)
                .title(title)
                .message(message)
                .link(link)
                .createdAt(OffsetDateTime.now())
                .build());
    }

    @Override
    public void broadcast(String type, String title, String message, String link) {
        notifications.broadcast(type, title, message, link);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationView> list(UUID userId, boolean unreadOnly, int page, int size) {
        PageRequest request = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100));
        Page<Notification> rows = unreadOnly
                ? notifications.findByUserIdAndReadAtIsNullOrderByCreatedAtDesc(userId, request)
                : notifications.findByUserIdOrderByCreatedAtDesc(userId, request);
        return rows.map(n -> new NotificationView(n.getNotificationId(), n.getType(), n.getTitle(), n.getMessage(),
                n.getLink(), n.getCreatedAt(), n.getReadAt() != null));
    }

    @Override
    @Transactional(readOnly = true)
    public long unreadCount(UUID userId) {
        return notifications.countByUserIdAndReadAtIsNull(userId);
    }

    @Override
    public void markRead(UUID userId, UUID notificationId) {
        Notification n = notifications.findByNotificationIdAndUserId(notificationId, userId)
                .orElseThrow(() -> ErpException.of(ErrorCode.NOTIFICATION_NOT_FOUND, Map.of()));
        if (n.getReadAt() == null) {
            n.setReadAt(OffsetDateTime.now());
        }
    }

    @Override
    public int markAllRead(UUID userId) {
        return notifications.markAllRead(userId, OffsetDateTime.now());
    }
}
