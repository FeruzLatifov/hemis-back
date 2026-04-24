-- =====================================================
-- Rollback V006: DROP AUTH MODULE (users + password_* + oauth_client)
-- =====================================================
-- Self-contained: drops own tables only. FK constraints
-- (users.employee_id → employee, oauth_client.organization_id → organization)
-- are removed automatically with DROP TABLE.
--
-- Production safeguard: aborts when users table holds > 1000 rows.
-- =====================================================

DO $$
DECLARE
    row_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO row_count FROM users;
    IF row_count > 1000 THEN
        RAISE EXCEPTION 'ROLLBACK BLOCKED: users table has % rows. Production detected.', row_count;
    END IF;
    RAISE NOTICE 'V006 Rollback: Dropping auth tables (% users)', row_count;
END $$;

-- Drop oauth_client tables (junction first — oauth_client_role depends on oauth_client + role)
DROP TABLE IF EXISTS oauth_client_role  CASCADE;
DROP TABLE IF EXISTS oauth_client       CASCADE;

-- Drop users tables (CASCADE drops password_history, password_reset_token child FKs)
DROP TABLE IF EXISTS password_reset_token;
DROP TABLE IF EXISTS password_history;
DROP TABLE IF EXISTS users CASCADE;
