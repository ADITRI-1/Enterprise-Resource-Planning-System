-- Login lockout and forced password change.
ALTER TABLE auth_db.user_auth
    ADD COLUMN must_change_password BOOLEAN     NOT NULL DEFAULT FALSE,
    ADD COLUMN failed_attempts      INTEGER     NOT NULL DEFAULT 0 CHECK (failed_attempts >= 0),
    ADD COLUMN locked_until         TIMESTAMPTZ NULL;
