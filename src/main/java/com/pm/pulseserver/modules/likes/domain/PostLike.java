package com.pm.pulseserver.modules.likes.domain;

import com.pm.pulseserver.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "post_likes",
        uniqueConstraints = @UniqueConstraint(name = "uq_post_likes_post_user", columnNames = {"post_id","user_id"}))
public class PostLike extends BaseEntity {

    @Id
    private UUID id;

    @Column(name = "post_id", nullable = false)
    private UUID postId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;
}
