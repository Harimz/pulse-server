package com.pm.pulseserver.modules.notifications.app;

import com.pm.pulseserver.common.cache.CacheService;
import com.pm.pulseserver.common.exception.NotFoundException;
import com.pm.pulseserver.common.pagination.CursorCodec;
import com.pm.pulseserver.common.pagination.CursorPageResponse;
import com.pm.pulseserver.modules.notifications.api.dto.NotificationResponse;
import com.pm.pulseserver.modules.notifications.domain.Notification;
import com.pm.pulseserver.modules.notifications.infra.NotificationRepository;
import com.pm.pulseserver.modules.users.domain.User;
import com.pm.pulseserver.modules.users.infra.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final CacheService cacheService;

    @Transactional(readOnly = true)
    public CursorPageResponse<NotificationResponse> list(UUID me, String cursor, int limit) {
        limit = Math.min(Math.max(limit, 1), 50);

        var decoded = (cursor == null || cursor.isBlank()) ? null : CursorCodec.decode(cursor);

        var rows = (decoded == null)
                ? notificationRepository.findFirstPage(me, limit)
                : notificationRepository.findAfterCursor(me, decoded.createdAt(), decoded.id(), limit);

        var fromIds = rows.stream()
                .map(Notification::getFromUserId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        var users = fromIds.isEmpty()
                ? List.<User>of()
                : userRepository.findAllByIdIn(fromIds);

        var byId = users.stream()
                .collect(Collectors.toMap(
                        com.pm.pulseserver.modules.users.domain.User::getId,
                        u -> u
                ));

        var items = rows.stream()
                .map(n -> toResponse(n, byId.get(n.getFromUserId())))
                .toList();

        String nextCursor = null;
        if (rows.size() == limit) {
            Notification last = rows.get(rows.size() - 1);
            nextCursor = CursorCodec.encode(last.getCreatedAt(), last.getId());
        }

        return new CursorPageResponse<>(items, nextCursor);
    }


    @Transactional
    public void markRead(UUID me, UUID notificationId) {
        Notification notification = notificationRepository.findById(notificationId).orElseThrow(() -> new NotFoundException("Notification not found"));

        if (!notification.getToUserId().equals(me)) {
            throw new NotFoundException("Notification not found");
        }

        if (notification.getReadAt() == null) {
            notification.setReadAt(Instant.now());
            notificationRepository.save(notification);

            long unread = notificationRepository.countUnread(me);
            cacheService.setLong("notif:unread:" + me, unread);
        }
    }

    @Transactional
    public void readAll(UUID me) {
        var first = notificationRepository.findFirstPage(me, 500);
        for (var n : first) {
            if (n.getReadAt() == null) n.setReadAt(Instant.now());
        }

        notificationRepository.saveAll(first);

        cacheService.setLong("notif:unread:" + me, 0);
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(UUID me) {
        String key = "notif:unread:" + me;

        Long cached = cacheService.getLong(key);
        if (cached != null && cached > 0) {
            return cached;
        }

        long dbCount = notificationRepository.countUnread(me);
        cacheService.setLong(key, dbCount);
        return dbCount;
    }

    private NotificationResponse toResponse(Notification n, com.pm.pulseserver.modules.users.domain.User fromUser) {
        NotificationResponse.FromUser from = null;

        if (fromUser != null) {
            String avatarUrl = (fromUser.getProfile() == null) ? null : fromUser.getProfile().getAvatarUrl();
            from = new NotificationResponse.FromUser(fromUser.getId(), fromUser.getUsername(), avatarUrl);
        }

        return new NotificationResponse(
                n.getId(),
                n.getToUserId(),
                n.getType(),
                n.getPayloadJson(),
                n.getCreatedAt(),
                n.getReadAt(),
                from
        );
    }
}
