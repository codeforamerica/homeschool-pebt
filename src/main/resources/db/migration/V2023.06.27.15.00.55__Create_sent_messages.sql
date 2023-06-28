CREATE TABLE sent_messages (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  submission_id UUID NOT NULL,
  message_name VARCHAR NOT NULL,
  sent_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  sent_status VARCHAR,
  provider VARCHAR,
  provider_message_id VARCHAR
)
