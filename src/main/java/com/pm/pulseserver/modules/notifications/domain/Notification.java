package com.pm.pulseserver.modules.notifications.domain;

import com.pm.pulseserver.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "notifications")
public class Notification extends BaseEntity {

    @Id
    private UUID id;

    @Column(name = "to_user_id", nullable = false)
    private UUID toUserId;

    @Column(name = "from_user_id")
    private UUID fromUserId;

    @Column(nullable = false, length = 64)
    private String type;

    @Column(name = "payload_json", nullable = false, columnDefinition = "text")
    private String payloadJson;

    @Column(name = "read_at")
    private Instant readAt;
}
