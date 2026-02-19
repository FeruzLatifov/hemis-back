-- =====================================================
-- S010_seed_classifier_menus_rollback.sql
-- =====================================================

DELETE FROM menus WHERE code IN ('cls-financial', 'cls-diploma', 'cls-speciality');
