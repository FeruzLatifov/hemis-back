-- =====================================================
-- M001: MIGRATE OLD HEMIS USERS
-- =====================================================
-- Author: hemis-team
-- Date: 2025-01-23
-- Updated: 2025-11-27 - Added all missing fields from sec_user
-- Purpose: One-time migration of users from old HEMIS (sec_user)
-- Strategy: IDEMPOTENT - safe to re-run
-- =====================================================

-- Migrate users from sec_user if table exists
DO $$
DECLARE
    migrated_count INTEGER := 0;
    old_table_exists BOOLEAN;
BEGIN
    -- Check if old table exists
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
            university_id,
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
            created_by,
            updated_at,
            updated_by
        )
        SELECT
            COALESCE(old.id, gen_random_uuid()),
            old.login_lc,
            old.login_lc,
            COALESCE(old.password, '$2a$10$DISABLED_ACCOUNT_NO_PASSWORD'),
            old.password_encryption,
            old.email,
            old.name,
            old.first_name,
            old.last_name,
            old.middle_name,
            COALESCE(old.first_name || ' ' || old.last_name, old.name),
            old.position_,
            old.language_,
            old.time_zone,
            old.time_zone_auto,
            'SYSTEM',
            old._university,
            old.group_id,
            old.group_names,
            COALESCE(old.active, TRUE),
            COALESCE(old.active, TRUE),
            old.ip_mask,
            old.change_password_at_logon,
            old.sys_tenant_id,
            old.dtype,
            COALESCE(old.version, 1),
            COALESCE(old.create_ts, CURRENT_TIMESTAMP),
            'migration',
            old.update_ts,
            old.updated_by
        FROM sec_user old
        WHERE old.delete_ts IS NULL
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

-- Assign SUPER_ADMIN role to 'admin' user (system administrator)
INSERT INTO user_roles (user_id, role_id, assigned_by)
SELECT u.id, r.id, 'migration'
FROM users u
CROSS JOIN roles r
WHERE u.username = 'admin'
  AND r.code = 'SUPER_ADMIN'
  AND NOT EXISTS (
      SELECT 1 FROM user_roles ur
      WHERE ur.user_id = u.id AND ur.role_id = r.id
  )
ON CONFLICT DO NOTHING;

-- Verification
DO $$
DECLARE
    total_users INTEGER;
    migrated_users INTEGER;
    with_university INTEGER;
BEGIN
    SELECT COUNT(*) INTO total_users FROM users;
    SELECT COUNT(*) INTO migrated_users FROM users WHERE created_by = 'migration';
    SELECT COUNT(*) INTO with_university FROM users WHERE university_id IS NOT NULL;
    RAISE NOTICE 'M001 Complete: % total users (% migrated, % with university)',
        total_users, migrated_users, with_university;
END $$;
