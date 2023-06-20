ALTER TABLE transmissions RENAME application_number TO confirmation_number;

ALTER INDEX idx_application_number RENAME TO idx_confirmation_number;

ALTER TABLE transmissions ADD COLUMN flow VARCHAR;
UPDATE transmissions SET flow = 'pebt';
ALTER TABLE transmissions ALTER COLUMN flow SET NOT NULL;
