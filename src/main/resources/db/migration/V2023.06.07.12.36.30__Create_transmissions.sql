CREATE TABLE transmissions (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  application_number VARCHAR NOT NULL,
  submission_id UUID NOT NULL,
  submitted_to_state_at TIMESTAMP WITHOUT TIME ZONE,
  submitted_to_state_filename VARCHAR,
  created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL
);

CREATE UNIQUE INDEX idx_application_number ON transmissions (application_number);
