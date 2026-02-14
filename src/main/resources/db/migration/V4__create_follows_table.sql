CREATE TABLE follows
(
    id           UUID NOT NULL,
    follower_id  UUID NOT NULL,
    following_id UUID NOT NULL,
    CONSTRAINT pk_follows PRIMARY KEY (id)
);