package com.pm.pulseserver.modules.notifications.infra;

import com.pm.pulseserver.modules.notifications.domain.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    @Query(value = """
        SELECT *
        FROM notifications n
        WHERE n.to_user_id = :toUserId
        ORDER BY n.created_at DESC, n.id DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<Notification> findFirstPage(@Param("toUserId") UUID toUserId, @Param("limit") int limit);

    @Query(value = """
        SELECT *
        FROM notifications n
        WHERE n.to_user_id = :toUserId
          AND (n.created_at, n.id) < (:cursorCreatedAt, :cursorId)
        ORDER BY n.created_at DESC, n.id DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<Notification> findAfterCursor(
            @Param("toUserId") UUID toUserId,
            @Param("cursorCreatedAt") Instant cursorCreatedAt,
            @Param("cursorId") UUID cursorId,
            @Param("limit") int limit
    );

    @Query("select count(n) from Notification n where n.toUserId = :toUserId and n.readAt is null")
    long countUnread(@Param("toUserId") UUID toUserId);
}
