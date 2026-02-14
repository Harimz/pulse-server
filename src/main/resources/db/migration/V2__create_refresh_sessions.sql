CREATE TABLE refresh_sessions
(
    id                 UUID         NOT NULL,
    created_at         TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at         TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    user_id            UUID         NOT NULL,
    refresh_token_hash VARCHAR(255) NOT NULL,
    expires_at         TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    revoked_at         TIMESTAMP WITHOUT TIME ZONE,
    rotated_from_id    UUID,
    CONSTRAINT pk_refresh_sessions PRIMARY KEY (id)
);

ALTER TABLE refresh_sessions
    ADD CONSTRAINT FK_REFRESH_SESSIONS_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);