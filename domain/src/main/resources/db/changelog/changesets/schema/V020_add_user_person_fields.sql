-- =====================================================
-- V020: users — shaxs (person) ma'lumoti ustunlari
-- =====================================================
-- Author: hemis-team
-- Purpose: "Shaxs" turidagi foydalanuvchi yaratishda GUVD/api_mspd passport-data
--          (172.18.9.171 gateway, ApiMspdTokenService) dan autofill bo'lgan
--          shaxsga doir ma'lumotlarni saqlash. PINFL + F.I.Sh. allaqachon V006'da bor.
--          Har bir tashqi-API maydoni o'z ustuniga saqlanadi.
--
-- Depends on: V006 users.
-- ALTER-only (V006 allaqachon deploy qilingan — o'sha faylni edit qilmaymiz).
-- Idempotent: ADD COLUMN IF NOT EXISTS.
-- =====================================================

ALTER TABLE users
    ADD COLUMN IF NOT EXISTS birth_date            DATE,
    ADD COLUMN IF NOT EXISTS birth_place           VARCHAR(255),
    ADD COLUMN IF NOT EXISTS passport              VARCHAR(16),
    ADD COLUMN IF NOT EXISTS passport_give_place   VARCHAR(255),
    ADD COLUMN IF NOT EXISTS passport_issued_date  DATE,
    ADD COLUMN IF NOT EXISTS passport_expiry_date  DATE,
    ADD COLUMN IF NOT EXISTS gender                VARCHAR(10),
    ADD COLUMN IF NOT EXISTS nationality           VARCHAR(64),
    ADD COLUMN IF NOT EXISTS address               VARCHAR(512),
    ADD COLUMN IF NOT EXISTS photo                 TEXT;

COMMENT ON COLUMN users.birth_date           IS 'Tug''ilgan sana (GUVD passport-data: birth_date).';
COMMENT ON COLUMN users.birth_place          IS 'Tug''ilgan joy (GUVD: birth_place).';
COMMENT ON COLUMN users.passport             IS 'Pasport seriya+raqam, masalan AB1234567 (GUVD: document). PII.';
COMMENT ON COLUMN users.passport_give_place  IS 'Pasport berilgan joy (GUVD: doc_give_place).';
COMMENT ON COLUMN users.passport_issued_date IS 'Pasport berilgan sana (GUVD: issued_date).';
COMMENT ON COLUMN users.passport_expiry_date IS 'Pasport amal qilish muddati (GUVD: expiry_date).';
COMMENT ON COLUMN users.gender               IS 'Jinsi (GUVD: sex).';
COMMENT ON COLUMN users.nationality          IS 'Millati (GUVD: nationality).';
COMMENT ON COLUMN users.address              IS 'Ro''yxatdan o''tgan manzil (GUVD person-address: region+district+address).';
COMMENT ON COLUMN users.photo                IS 'Shaxs fotosurati base64 (GUVD: photo). Og''ir; nullable.';

-- Passport lookup uchun partial index (unique EMAS — pasport qayta beriladi/almashadi;
-- yagonalik faqat PINFL orqali — uq_users_pinfl).
CREATE INDEX IF NOT EXISTS idx_users_passport ON users (passport)
    WHERE passport IS NOT NULL AND deleted_at IS NULL;
