package com.pm.pulseserver.modules.notifications.api;

import com.pm.pulseserver.common.pagination.CursorPageResponse;
import com.pm.pulseserver.modules.auth.infra.JwtAuthenticationFilter;
import com.pm.pulseserver.modules.notifications.api.dto.NotificationResponse;
import com.pm.pulseserver.modules.notifications.app.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public CursorPageResponse<NotificationResponse> list(
            @AuthenticationPrincipal JwtAuthenticationFilter.AuthPrincipal principal,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int limit
    ) {
        UUID me = UUID.fromString(principal.userId());
        return notificationService.list(me, cursor, limit);
    }

    @PostMapping("/{id}/read")
    public void markRead(
            @AuthenticationPrincipal JwtAuthenticationFilter.AuthPrincipal principal,
            @PathVariable UUID id
    ) {
        UUID me = UUID.fromString(principal.userId());
        notificationService.markRead(me, id);
    }


    @PostMapping("/read-all")
    public void readAll(@AuthenticationPrincipal JwtAuthenticationFilter.AuthPrincipal principal) {
        UUID me = UUID.fromString(principal.userId());
        notificationService.readAll(me);
    }

    @GetMapping("/unread-count")
    public long unreadCount(
            @AuthenticationPrincipal JwtAuthenticationFilter.AuthPrincipal principal
    ) {
        UUID me = UUID.fromString(principal.userId());
        return notificationService.getUnreadCount(me);
    }
}
