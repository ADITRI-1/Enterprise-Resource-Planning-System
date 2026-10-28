package com.iiitd.erp.io.controller;

import com.iiitd.erp.Domain.NotificationView;
import com.iiitd.erp.core.service.NotificationService;
import com.iiitd.erp.io.controller.support.CurrentUser;
import com.iiitd.erp.io.dto.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/** The logged-in user's own notifications (any role). */
@RestController
@RequestMapping("/api/notifications")
@Tag(name = "Notifications")
public class NotificationController {

    private final NotificationService notifications;
    private final CurrentUser currentUser;

    public NotificationController(NotificationService notifications, CurrentUser currentUser) {
        this.notifications = notifications;
        this.currentUser = currentUser;
    }

    @GetMapping
    @Operation(summary = "My notifications, newest first")
    public PageResponse<NotificationView> list(@RequestParam(defaultValue = "false") boolean unreadOnly,
                                               @RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "20") int size) {
        Page<NotificationView> result = notifications.list(userId(), unreadOnly, page, size);
        return new PageResponse<>(result.getContent(), result.getNumber(), result.getSize(),
                result.getTotalElements(), result.getTotalPages());
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Number for the bell badge")
    public Map<String, Long> unreadCount() {
        return Map.of("count", notifications.unreadCount(userId()));
    }

    @PostMapping("/{id}/read")
    public Map<String, Boolean> markRead(@PathVariable UUID id) {
        notifications.markRead(userId(), id);
        return Map.of("read", true);
    }

    @PostMapping("/read-all")
    public Map<String, Integer> markAllRead() {
        return Map.of("updated", notifications.markAllRead(userId()));
    }

    private UUID userId() {
        return currentUser.requireAccount().getUserId();
    }
}
