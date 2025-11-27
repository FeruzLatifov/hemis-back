-- =====================================================
-- Rollback V001: DROP USERS TABLE
-- =====================================================
-- WARNING: This is a DESTRUCTIVE operation!
-- CASCADE will also drop dependent objects (user_roles, etc.)
-- =====================================================
-- Threshold: 1000 rows (production typically has 10,000+)
-- =====================================================

DO $$
DECLARE
    row_count INTEGER;
    dependent_count INTEGER := 0;
    user_roles_exists BOOLEAN;
BEGIN
    SELECT COUNT(*) INTO row_count FROM users;

    SELECT EXISTS (
        SELECT 1 FROM information_schema.tables
        WHERE table_name = 'user_roles'
    ) INTO user_roles_exists;

    IF user_roles_exists THEN
        SELECT COUNT(*) INTO dependent_count FROM user_roles;
    END IF;

    -- GUARD: Block if more than 1000 users (likely production)
    IF row_count > 1000 THEN
        RAISE EXCEPTION 'ROLLBACK BLOCKED: users table has % rows. '
            'Production database detected. Manual intervention required.', row_count;
    END IF;

    IF dependent_count > 0 THEN
        RAISE WARNING 'CASCADE will delete % user_roles entries', dependent_count;
    END IF;

    RAISE NOTICE 'V001 Rollback: Dropping users table (% rows)', row_count;
END $$;

DROP TABLE IF EXISTS users CASCADE;
