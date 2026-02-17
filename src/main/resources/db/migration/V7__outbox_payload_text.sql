ALTER TABLE outbox_events
ALTER COLUMN payload_json TYPE TEXT
  USING payload_json::text;
