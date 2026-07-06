DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'chk_teachers_email_format'
    ) THEN
        ALTER TABLE teachers
            ADD CONSTRAINT chk_teachers_email_format
                CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$');
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'chk_teachers_phone_format'
    ) THEN
        ALTER TABLE teachers
            ADD CONSTRAINT chk_teachers_phone_format
                CHECK (phone IS NULL OR phone ~ '^\+?[1-9]\d{1,14}$');
    END IF;
END $$;
