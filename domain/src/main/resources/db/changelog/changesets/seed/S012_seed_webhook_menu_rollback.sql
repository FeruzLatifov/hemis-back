-- =====================================================
-- S012 rollback: remove sys-webhooks menu entry
-- =====================================================
DELETE FROM menu WHERE code = 'sys-webhooks';
