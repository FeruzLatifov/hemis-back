-- =====================================================
-- Rollback V008b: DROP USER_FAVORITES TABLE
-- =====================================================

DO $$
DECLARE
    row_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO row_count FROM user_favorites;

    IF row_count > 5000 THEN
        RAISE EXCEPTION 'ROLLBACK BLOCKED: user_favorites table has % rows. '
            'Production database detected. Manual intervention required.', row_count;
    END IF;

    RAISE NOTICE 'V008b Rollback: Dropping user_favorites table (% rows)', row_count;
END $$;

DROP TABLE IF EXISTS user_favorites;
