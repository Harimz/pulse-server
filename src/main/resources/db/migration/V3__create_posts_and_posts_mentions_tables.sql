CREATE TABLE post_mentions
(
    id                UUID NOT NULL,
    created_at        TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at        TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    post_id           UUID NOT NULL,
    mentioned_user_id UUID NOT NULL,
    CONSTRAINT pk_post_mentions PRIMARY KEY (id)
);

CREATE TABLE posts
(
    id         UUID NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    author_id  UUID NOT NULL,
    body       TEXT NOT NULL,
    CONSTRAINT pk_posts PRIMARY KEY (id)
);