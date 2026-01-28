-- =====================================================
-- HEMIS Password Hash Fix Script
-- =====================================================
-- Purpose: Fix incorrect BCrypt password hashes in sec_user table
-- Problem: Some passwords have invalid BCrypt format (61 chars instead of 60)
-- Solution: Re-hash all passwords that don't match BCrypt format
-- =====================================================

-- Step 1: Check current password formats
SELECT 
    'Total users' as metric,
    COUNT(*) as count
FROM sec_user 
WHERE active = true AND delete_ts IS NULL

UNION ALL

SELECT 
    'BCrypt format (60 chars)' as metric,
    COUNT(*) as count
FROM sec_user 
WHERE active = true 
  AND delete_ts IS NULL
  AND password LIKE '$2%'
  AND LENGTH(password) = 60

UNION ALL

SELECT 
    'Invalid format' as metric,
    COUNT(*) as count
FROM sec_user 
WHERE active = true 
  AND delete_ts IS NULL
  AND (LENGTH(password) != 60 OR password NOT LIKE '$2%')

UNION ALL

SELECT 
    'CUBA format (hash:salt:iteration)' as metric,
    COUNT(*) as count
FROM sec_user 
WHERE active = true 
  AND delete_ts IS NULL
  AND password LIKE '%:%:%';

-- Step 2: List users with invalid password formats
SELECT 
    login,
    LENGTH(password) as pwd_length,
    LEFT(password, 30) as pwd_sample,
    password_encryption,
    active
FROM sec_user
WHERE active = true 
  AND delete_ts IS NULL
  AND LENGTH(password) != 60
ORDER BY login
LIMIT 20;

-- =====================================================
-- MANUAL FIX REQUIRED
-- =====================================================
-- To fix passwords, run this for each user:
-- 
-- UPDATE sec_user 
-- SET password = '[NEW_BCRYPT_HASH]',
--     password_encryption = 'bcrypt',
--     version = version + 1
-- WHERE login = '[USERNAME]';
--
-- Generate BCrypt hash using:
-- 1. Online: https://bcrypt-generator.com/
-- 2. Command: htpasswd -bnBC 10 "" [password] | tr -d ':\n' | sed 's/$2y/$2a/'
-- 3. Kotlin/Java: BCryptPasswordEncoder().encode("[password]")
-- =====================================================
