CREATE TABLE IF NOT EXISTS registration_requests (
    id          BIGSERIAL PRIMARY KEY,
    fio         VARCHAR(255) NOT NULL,
    email       VARCHAR(255) NOT NULL,
    group_name  VARCHAR(255) NOT NULL,
    role        VARCHAR(50)  NOT NULL,
    token       VARCHAR(255) NOT NULL UNIQUE,
    expires_at  TIMESTAMP    NOT NULL,
    status      VARCHAR(50)  NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_registration_requests_email ON registration_requests (email);
CREATE INDEX IF NOT EXISTS idx_registration_requests_status ON registration_requests (status);
CREATE INDEX IF NOT EXISTS idx_registration_requests_expires_at ON registration_requests (expires_at);

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'chk_registration_email_format'
    ) THEN
        ALTER TABLE registration_requests
            ADD CONSTRAINT chk_registration_email_format
                CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$');
    END IF;
END $$;
