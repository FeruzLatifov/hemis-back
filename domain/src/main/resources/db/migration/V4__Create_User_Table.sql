-- =====================================================
-- HEMIS Backend - OAuth2 User Table Creation
-- =====================================================
-- Version: V4
-- Purpose: Create user table for OAuth2 authentication
--
-- Background:
-- OLD-HEMIS uses CUBA Platform's built-in User entity.
-- NEW-HEMIS needs custom User table for Spring Security OAuth2.
-- This table stores university user credentials for API access.
--
-- MASTER PROMPT Compliance:
-- ✅ NO-RENAME: No existing tables modified
-- ✅ NO-DELETE: No data deleted
-- ✅ NO-BREAKING-CHANGES: New table, no API changes
-- ✅ REPLICATION-SAFE: Simple CREATE TABLE operation
-- =====================================================

-- =====================================================
-- Step 1: Create User Table
-- =====================================================

CREATE TABLE IF NOT EXISTS hemishe_user (
    -- Primary Key (UUID)
    id UUID NOT NULL PRIMARY KEY,

    -- Authentication
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,  -- BCrypt hashed

    -- Authorization
    roles VARCHAR(500),  -- Comma-separated: ROLE_ADMIN,ROLE_UNIVERSITY_ADMIN,ROLE_USER
    enabled BOOLEAN NOT NULL DEFAULT TRUE,

    -- University Reference (nullable for system admins)
    _university VARCHAR(255),

    -- Personal Information (Optional)
    full_name VARCHAR(255),
    email VARCHAR(255),
    phone VARCHAR(50),

    -- Account Lockout (Security)
    account_non_locked BOOLEAN DEFAULT TRUE,
    failed_attempts INTEGER DEFAULT 0,

    -- Audit Fields (from BaseEntity)
    create_ts TIMESTAMP,
    created_by VARCHAR(255),
    update_ts TIMESTAMP,
    updated_by VARCHAR(255),
    delete_ts TIMESTAMP,
    deleted_by VARCHAR(255),
    version INTEGER DEFAULT 1
);

-- =====================================================
-- Step 2: Create Indexes
-- =====================================================

-- Username index (for login queries)
CREATE INDEX IF NOT EXISTS idx_user_username
ON hemishe_user (username)
WHERE delete_ts IS NULL;

-- Active users index
CREATE INDEX IF NOT EXISTS idx_user_active
ON hemishe_user (enabled, account_non_locked)
WHERE delete_ts IS NULL;

-- University index (for university-specific queries)
CREATE INDEX IF NOT EXISTS idx_user_university
ON hemishe_user (_university)
WHERE delete_ts IS NULL AND _university IS NOT NULL;

-- =====================================================
-- Step 3: Insert Default Admin User
-- =====================================================
-- Username: admin
-- Password: admin (BCrypt hash: $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy)
-- Note: Change this password in production!
-- =====================================================

INSERT INTO hemishe_user (
    id,
    username,
    password,
    roles,
    enabled,
    _university,
    full_name,
    account_non_locked,
    failed_attempts,
    create_ts,
    version
) VALUES (
    gen_random_uuid(),
    'admin',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',  -- password: admin
    'ROLE_ADMIN',
    TRUE,
    NULL,  -- System admin (no university restriction)
    'System Administrator',
    TRUE,
    0,
    NOW(),
    1
) ON CONFLICT (username) DO NOTHING;

-- =====================================================
-- Step 4: Comments
-- =====================================================

COMMENT ON TABLE hemishe_user IS
'OAuth2 user authentication and authorization table. Maps to OLD-HEMIS CUBA User and UniversityUser tables.';

COMMENT ON COLUMN hemishe_user.username IS
'Unique login username. Used for OAuth2 password grant authentication.';

COMMENT ON COLUMN hemishe_user.password IS
'BCrypt hashed password. NEVER store plain text! Hash format: $2a$10$...';

COMMENT ON COLUMN hemishe_user.roles IS
'Comma-separated roles: ROLE_ADMIN, ROLE_UNIVERSITY_ADMIN, ROLE_USER. Spring Security requires ROLE_ prefix.';

COMMENT ON COLUMN hemishe_user.enabled IS
'Account enabled flag. Disabled users cannot login even with correct password.';

COMMENT ON COLUMN hemishe_user._university IS
'University code (nullable for system admins). Maps to UniversityUser.university in OLD-HEMIS.';

COMMENT ON COLUMN hemishe_user.account_non_locked IS
'Account lock flag. Set to false after N failed login attempts to prevent brute force attacks.';

COMMENT ON COLUMN hemishe_user.failed_attempts IS
'Failed login attempt counter. Reset to 0 on successful login. Lock account after 5 attempts.';

-- =====================================================
-- Step 5: Validation Query
-- =====================================================

DO $$
DECLARE
    user_count INTEGER;
BEGIN
    -- Verify admin user created
    SELECT COUNT(*) INTO user_count
    FROM hemishe_user
    WHERE username = 'admin' AND enabled = true;

    IF user_count = 0 THEN
        RAISE WARNING 'Admin user not created!';
    ELSE
        RAISE NOTICE 'Admin user created successfully.';
    END IF;
END $$;

-- =====================================================
-- Migration Complete
-- =====================================================
-- User table created successfully
-- Default admin user inserted (username: admin, password: admin)
-- Indexes created for performance
-- =====================================================

-- =====================================================
-- IMPORTANT NOTES FOR PRODUCTION
-- =====================================================
-- 1. CHANGE DEFAULT ADMIN PASSWORD!
--    Run this after deployment:
--    UPDATE hemishe_user SET password = '<new_bcrypt_hash>' WHERE username = 'admin';
--
-- 2. Create university admin users:
--    INSERT INTO hemishe_user (id, username, password, roles, _university, ...)
--    VALUES (gen_random_uuid(), 'tatu_admin', '<bcrypt_hash>', 'ROLE_UNIVERSITY_ADMIN', 'TATU', ...);
--
-- 3. Enable account lockout in production:
--    Configure maximum failed attempts (5-10)
--    Configure lockout duration (30 minutes)
--
-- 4. Consider adding:
--    - last_login_ts TIMESTAMP (track last successful login)
--    - password_changed_ts TIMESTAMP (enforce password expiration)
--    - two_factor_enabled BOOLEAN (optional 2FA support)
-- =====================================================
