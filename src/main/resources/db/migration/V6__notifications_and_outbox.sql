CREATE TABLE notifications (
    id UUID PRIMARY KEY,
    to_user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    from_user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    type VARCHAR(64) NOT NULL,
    payload_json JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    read_at TIMESTAMPTZ NULL
);

CREATE INDEX idx_notifications_to_created_id
    ON notifications(to_user_id, created_at DESC, id DESC);

CREATE INDEX idx_notifications_to_readat
    ON notifications(to_user_id, read_at);

CREATE UNIQUE INDEX IF NOT EXISTS uq_notif_follow_unique
    ON notifications(to_user_id, from_user_id, type)
    WHERE type = 'USER_FOLLOWED';


CREATE TABLE outbox_events (
    id UUID PRIMARY KEY,
    event_type VARCHAR(64) NOT NULL,
    aggregate_type VARCHAR(64) NOT NULL,
    aggregate_id UUID NOT NULL,

    payload_json JSONB NOT NULL,

    status VARCHAR(32) NOT NULL,
    available_at TIMESTAMPTZ NOT NULL,
    attempts INT NOT NULL DEFAULT 0,
    last_error TEXT NULL,

    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_outbox_status_available
    ON outbox_events(status, available_at);

CREATE INDEX idx_outbox_created
    ON outbox_events(created_at);