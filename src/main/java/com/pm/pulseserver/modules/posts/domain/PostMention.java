package com.pm.pulseserver.modules.posts.domain;

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
@Table(
        name = "post_mentions",
        uniqueConstraints = @UniqueConstraint(name = "uq_post_mentions_post_user", columnNames = {"post_id", "mentioned_user_id"})
)
public class PostMention extends BaseEntity {

    @Id
    private UUID id;

    @Column(name = "post_id", nullable = false)
    private UUID postId;

    @Column(name = "mentioned_user_id", nullable = false)
    private UUID mentionedUserId;
}
