-- =====================================================
-- V020 ROLLBACK: users shaxs ustunlarini olib tashlash
-- =====================================================

DROP INDEX IF EXISTS idx_users_passport;

ALTER TABLE users
    DROP COLUMN IF EXISTS birth_date,
    DROP COLUMN IF EXISTS birth_place,
    DROP COLUMN IF EXISTS passport,
    DROP COLUMN IF EXISTS passport_give_place,
    DROP COLUMN IF EXISTS passport_issued_date,
    DROP COLUMN IF EXISTS passport_expiry_date,
    DROP COLUMN IF EXISTS gender,
    DROP COLUMN IF EXISTS nationality,
    DROP COLUMN IF EXISTS address,
    DROP COLUMN IF EXISTS photo;
