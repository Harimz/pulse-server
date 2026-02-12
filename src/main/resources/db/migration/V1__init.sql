CREATE TABLE IF NOT EXISTS flyway_sanity_check (
    id SERIAL PRIMARY KEY,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

INSERT INTO flyway_sanity_check DEFAULT VALUES;