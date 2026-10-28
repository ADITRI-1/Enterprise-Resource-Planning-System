package com.iiitd.erp.Domain;

import java.time.OffsetDateTime;
import java.util.UUID;

public record NotificationView(UUID notificationId,
                               String type,
                               String title,
                               String message,
                               String link,
                               OffsetDateTime createdAt,
                               boolean read) {}
