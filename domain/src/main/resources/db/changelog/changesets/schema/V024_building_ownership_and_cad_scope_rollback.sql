-- Rollback V024: ownership_code FK/index/column + h_building_ownership; restore global cad_number unique.
ALTER TABLE university_building DROP CONSTRAINT IF EXISTS fk_ub_ownership;
DROP INDEX IF EXISTS idx_ub_ownership;
ALTER TABLE university_building DROP COLUMN IF EXISTS ownership_code;

-- cad_number uniqueness'ni GLOBAL holatiga qaytarish (V010 asl).
DROP INDEX IF EXISTS uq_ub_univ_cad;
CREATE UNIQUE INDEX IF NOT EXISTS uq_ub_cad_number
    ON university_building (cad_number)
    WHERE cad_number IS NOT NULL AND deleted_at IS NULL;

DROP TABLE IF EXISTS h_building_ownership;
