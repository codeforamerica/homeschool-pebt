ALTER TABLE transmissions
  ADD COLUMN updated_at
  TIMESTAMP WITHOUT TIME ZONE;
UPDATE transmissions SET updated_at = NOW();
ALTER TABLE transmissions ALTER COLUMN updated_at SET NOT NULL;
