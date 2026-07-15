ALTER TABLE registration_requests
    ADD COLUMN IF NOT EXISTS email_status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    ADD COLUMN IF NOT EXISTS email_attempts INT NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS last_email_error VARCHAR(500);

CREATE INDEX IF NOT EXISTS idx_registration_requests_email_status
    ON registration_requests (email_status);
