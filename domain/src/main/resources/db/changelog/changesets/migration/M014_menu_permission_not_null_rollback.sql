-- =====================================================
-- M014 ROLLBACK: menu.permission accepts NULL again
-- =====================================================
ALTER TABLE menu ALTER COLUMN permission DROP NOT NULL;
