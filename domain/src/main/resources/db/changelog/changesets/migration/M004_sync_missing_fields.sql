-- =====================================================
-- M004: SYNC MISSING FIELDS FROM SEC_USER
-- =====================================================
-- Author: hemis-team
-- Date: 2025-11-27
-- Purpose: Sync fields that were missed in M001 migration:
--   - _university -> university_id
--   - update_ts -> updated_at
--   - updated_by -> updated_by
--   - delete_ts -> deleted_at (for soft-deleted users)
--   - deleted_by -> deleted_by
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

    -- 1. Sync university_id
    UPDATE users u
    SET university_id = su._university
    FROM sec_user su
    WHERE u.username = su.login_lc
      AND u.university_id IS NULL
      AND su._university IS NOT NULL;

    GET DIAGNOSTICS updated_count = ROW_COUNT;
    RAISE NOTICE 'M004: Synced % university_id values', updated_count;

    -- 2. Sync updated_at and updated_by
    UPDATE users u
    SET
        updated_at = su.update_ts,
        updated_by = COALESCE(su.updated_by, 'migration')
    FROM sec_user su
    WHERE u.username = su.login_lc
      AND u.updated_at IS NULL
      AND su.update_ts IS NOT NULL;

    GET DIAGNOSTICS updated_count = ROW_COUNT;
    RAISE NOTICE 'M004: Synced % updated_at/updated_by values', updated_count;

    -- 3. Sync soft-deleted users (delete_ts, deleted_by)
    -- First, check if there are soft-deleted users in sec_user that exist in users
    UPDATE users u
    SET
        deleted_at = su.delete_ts,
        deleted_by = COALESCE(su.deleted_by, 'migration')
    FROM sec_user su
    WHERE u.username = su.login_lc
      AND u.deleted_at IS NULL
      AND su.delete_ts IS NOT NULL;

    GET DIAGNOSTICS updated_count = ROW_COUNT;
    RAISE NOTICE 'M004: Synced % deleted_at/deleted_by values', updated_count;

END $$;

-- Verification
DO $$
DECLARE
    total_users INTEGER;
    with_university INTEGER;
    with_updated_at INTEGER;
    with_deleted_at INTEGER;
BEGIN
    SELECT COUNT(*) INTO total_users FROM users;
    SELECT COUNT(*) INTO with_university FROM users WHERE university_id IS NOT NULL;
    SELECT COUNT(*) INTO with_updated_at FROM users WHERE updated_at IS NOT NULL;
    SELECT COUNT(*) INTO with_deleted_at FROM users WHERE deleted_at IS NOT NULL;

    RAISE NOTICE 'M004 Complete:';
    RAISE NOTICE '  Total users: %', total_users;
    RAISE NOTICE '  With university_id: %', with_university;
    RAISE NOTICE '  With updated_at: %', with_updated_at;
    RAISE NOTICE '  Soft-deleted: %', with_deleted_at;
END $$;
