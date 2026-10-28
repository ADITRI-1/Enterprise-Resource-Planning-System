package com.iiitd.erp.core.service;

import com.iiitd.erp.Domain.NotificationView;
import org.springframework.data.domain.Page;

import java.util.UUID;

/** In-app notifications shown under the bell icon. */
public interface NotificationService {

    String REGISTRATION = "REGISTRATION";
    String GRADES = "GRADES";
    String MAINTENANCE = "MAINTENANCE";

    void notify(UUID userId, String type, String title, String message, String link);

    /** Sends the same notification to every account. */
    void broadcast(String type, String title, String message, String link);

    Page<NotificationView> list(UUID userId, boolean unreadOnly, int page, int size);

    long unreadCount(UUID userId);

    void markRead(UUID userId, UUID notificationId);

    int markAllRead(UUID userId);
}
