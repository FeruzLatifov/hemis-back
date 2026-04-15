-- =====================================================
-- Rollback V001: DROP AUTH MODULE TABLES
-- =====================================================
-- Drops: password_reset_token, password_history, users (CASCADE)
-- =====================================================

DO $$
DECLARE
    row_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO row_count FROM users;
    IF row_count > 1000 THEN
        RAISE EXCEPTION 'ROLLBACK BLOCKED: users table has % rows. Production detected.', row_count;
    END IF;
    RAISE NOTICE 'V001 Rollback: Dropping auth tables (% users)', row_count;
END $$;

DROP TABLE IF EXISTS password_reset_token;
DROP TABLE IF EXISTS password_history;
DROP TABLE IF EXISTS users CASCADE;
