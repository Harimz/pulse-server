ALTER TABLE notifications
ALTER COLUMN payload_json TYPE TEXT
  USING payload_json::text;