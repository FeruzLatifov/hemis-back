-- =====================================================
-- M001: MIGRATE OLD HEMIS USERS
-- =====================================================
-- Author: hemis-team
-- Date: 2025-01-23
-- Purpose: One-time migration of users from old HEMIS (sec_user)
-- Strategy: IDEMPOTENT - safe to re-run
-- =====================================================

-- Migrate users from sec_user if table exists
DO $$
DECLARE
    migrated_count INTEGER := 0;
    old_table_exists BOOLEAN;
BEGIN
    -- Check if old table exists (sec_user - underscore, not dollar sign)
    SELECT EXISTS (
        SELECT 1 FROM information_schema.tables
        WHERE table_schema = 'public' AND table_name = 'sec_user'
    ) INTO old_table_exists;

    IF old_table_exists THEN
        -- Migrate users that don't already exist
        INSERT INTO users (
            id,
            username,
            username_lowercase,
            password,
            password_encryption,
            email,
            name,
            first_name,
            last_name,
            middle_name,
            full_name,
            position,
            language,
            time_zone,
            time_zone_auto,
            user_type,
            group_id,
            group_names,
            enabled,
            active,
            ip_mask,
            change_password_at_logon,
            sys_tenant_id,
            dtype,
            version,
            created_at,
            created_by
        )
        SELECT
            COALESCE(id, gen_random_uuid()),
            login_lc,
            login_lc,
            COALESCE(password, '$2a$10$DISABLED_ACCOUNT_NO_PASSWORD'),  -- Handle NULL passwords
            password_encryption,
            email,
            name,
            first_name,
            last_name,
            middle_name,
            COALESCE(first_name || ' ' || last_name, name),
            position_,
            language_,
            time_zone,
            time_zone_auto,
            'SYSTEM',
            group_id,
            group_names,
            COALESCE(active, TRUE),
            COALESCE(active, TRUE),
            ip_mask,
            change_password_at_logon,
            sys_tenant_id,
            dtype,
            COALESCE(version, 1),
            COALESCE(create_ts, CURRENT_TIMESTAMP),
            'migration'
        FROM sec_user old
        WHERE old.delete_ts IS NULL  -- Only active users (not soft-deleted)
          AND NOT EXISTS (
            SELECT 1 FROM users u
            WHERE u.username = old.login_lc
               OR u.id = old.id
        )
        ON CONFLICT (username) DO NOTHING;

        GET DIAGNOSTICS migrated_count = ROW_COUNT;
        RAISE NOTICE 'M001: Migrated % users from sec_user', migrated_count;
    ELSE
        RAISE NOTICE 'M001: sec_user table not found - skipping migration';
    END IF;
END $$;

-- Assign default role (VIEWER) to migrated users without roles
INSERT INTO user_roles (user_id, role_id, assigned_by)
SELECT u.id, r.id, 'migration'
FROM users u
CROSS JOIN roles r
WHERE r.code = 'VIEWER'
  AND u.created_by = 'migration'
  AND NOT EXISTS (
      SELECT 1 FROM user_roles ur WHERE ur.user_id = u.id
  )
ON CONFLICT DO NOTHING;

-- Verification
DO $$
DECLARE
    total_users INTEGER;
    migrated_users INTEGER;
BEGIN
    SELECT COUNT(*) INTO total_users FROM users;
    SELECT COUNT(*) INTO migrated_users FROM users WHERE created_by = 'migration';
    RAISE NOTICE 'M001 Complete: % total users (% migrated from old HEMIS)', total_users, migrated_users;
END $$;
