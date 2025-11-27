-- =====================================================
-- M004: SYNC UNIVERSITY_ID FROM SEC_USER
-- =====================================================
-- Author: hemis-team
-- Date: 2025-11-27
-- Purpose: Sync university_id that was missing in M001 migration
-- =====================================================

DO $$
DECLARE
    updated_count INTEGER := 0;
    sec_user_exists BOOLEAN;
BEGIN
    SELECT EXISTS (
        SELECT 1 FROM information_schema.tables
        WHERE table_schema = 'public' AND table_name = 'sec_user'
    ) INTO sec_user_exists;

    IF NOT sec_user_exists THEN
        RAISE NOTICE 'M004: sec_user table not found - skipping';
        RETURN;
    END IF;

    -- Update university_id from sec_user._university
    UPDATE users u
    SET university_id = su._university,
        updated_at = CURRENT_TIMESTAMP,
        updated_by = 'migration-m004'
    FROM sec_user su
    WHERE u.username = su.login_lc
      AND u.university_id IS NULL
      AND su._university IS NOT NULL
      AND su.delete_ts IS NULL;

    GET DIAGNOSTICS updated_count = ROW_COUNT;
    RAISE NOTICE 'M004: Updated % users with university_id', updated_count;
END $$;

-- Verification
DO $$
DECLARE
    total_with_university INTEGER;
    total_users INTEGER;
BEGIN
    SELECT COUNT(*) INTO total_users FROM users;
    SELECT COUNT(*) INTO total_with_university FROM users WHERE university_id IS NOT NULL;
    RAISE NOTICE 'M004 Complete: %/% users have university_id', total_with_university, total_users;
END $$;
