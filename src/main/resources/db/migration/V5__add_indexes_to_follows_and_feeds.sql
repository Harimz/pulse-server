CREATE UNIQUE INDEX uq_follows_follower_following
    ON follows(follower_id, following_id);

CREATE INDEX idx_follows_follower ON follows(follower_id);
CREATE INDEX idx_follows_following ON follows(following_id);

CREATE INDEX IF NOT EXISTS idx_posts_created_id_desc
    ON posts(created_at DESC, id DESC);

CREATE INDEX IF NOT EXISTS idx_follows_follower_following
    ON follows(follower_id, following_id);

CREATE INDEX IF NOT EXISTS idx_follows_following
    ON follows(following_id);