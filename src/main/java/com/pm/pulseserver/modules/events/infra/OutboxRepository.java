package com.pm.pulseserver.modules.events.infra;

import com.pm.pulseserver.modules.events.domain.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface OutboxRepository extends JpaRepository<OutboxEvent, UUID> {

    @Query(value = """
        SELECT *
        FROM outbox_events
        WHERE status = :status
          AND available_at <= :now
        ORDER BY created_at ASC
        LIMIT :limit
        """, nativeQuery = true)
    List<OutboxEvent> findReady(
            @Param("status") String status,
            @Param("now") Instant now,
            @Param("limit") int limit
    );
}
