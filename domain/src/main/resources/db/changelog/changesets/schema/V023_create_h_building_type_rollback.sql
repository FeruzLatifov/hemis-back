-- Rollback V023: drop building_type_code FK/index/column + h_building_type table.
-- NOTE: category_code NOT NULL TIKLANMAYDI — rollback vaqtiga qatorlar null category bilan
--       bo'lishi mumkin, NOT NULL qaytarish fail bo'lardi. Nullable qoldirish xavfsiz.
DROP TABLE IF EXISTS university_building_type;

ALTER TABLE university_building DROP CONSTRAINT IF EXISTS fk_ub_building_type;
DROP INDEX IF EXISTS idx_ub_building_type;
ALTER TABLE university_building DROP COLUMN IF EXISTS building_type_code;

DROP TABLE IF EXISTS h_building_type;
