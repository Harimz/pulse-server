CREATE TABLE user_profiles
(
    user_id      UUID        NOT NULL,
    display_name VARCHAR(80) NOT NULL,
    bio          VARCHAR(280),
    avatar_url   VARCHAR(255),
    CONSTRAINT pk_user_profiles PRIMARY KEY (user_id)
);

CREATE TABLE users
(
    id            UUID         NOT NULL,
    created_at    TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at    TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    username      VARCHAR(32)  NOT NULL,
    email         VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    CONSTRAINT pk_users PRIMARY KEY (id)
);

ALTER TABLE users
    ADD CONSTRAINT uc_users_email UNIQUE (email);

ALTER TABLE users
    ADD CONSTRAINT uc_users_username UNIQUE (username);

ALTER TABLE user_profiles
    ADD CONSTRAINT FK_USER_PROFILES_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);