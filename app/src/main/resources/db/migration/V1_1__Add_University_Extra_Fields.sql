-- Migration: Add Extra Fields to University Table
-- Version: 1.1
-- Author: System
-- Date: 2025-01-11
-- Description: Adds 5 new fields to hemishe_e_university table (SAFE - No data loss)

-- CRITICAL: This migration is SAFE
--   ✅ Only ADDS new columns
--   ✅ Does NOT delete or rename existing columns
--   ✅ Does NOT modify existing data
--   ✅ All new columns are nullable (no breaking changes)

-- Add version type column
ALTER TABLE hemishe_e_university 
ADD COLUMN IF NOT EXISTS _version_type VARCHAR(32);

COMMENT ON COLUMN hemishe_e_university._version_type IS 'Version type code (classifier reference)';

-- Add terrain (mahalla) column
ALTER TABLE hemishe_e_university 
ADD COLUMN IF NOT EXISTS _terrain VARCHAR(32);

COMMENT ON COLUMN hemishe_e_university._terrain IS 'Terrain/mahalla code (classifier reference)';

-- Add mail address column
ALTER TABLE hemishe_e_university 
ADD COLUMN IF NOT EXISTS mail_address TEXT;

COMMENT ON COLUMN hemishe_e_university.mail_address IS 'Postal mailing address';

-- Add bank information column
ALTER TABLE hemishe_e_university 
ADD COLUMN IF NOT EXISTS bank_info TEXT;

COMMENT ON COLUMN hemishe_e_university.bank_info IS 'Bank account and details information';

-- Add accreditation information column
ALTER TABLE hemishe_e_university 
ADD COLUMN IF NOT EXISTS accreditation_info TEXT;

COMMENT ON COLUMN hemishe_e_university.accreditation_info IS 'Accreditation details and certificates';

-- Create indexes for performance (optional)
CREATE INDEX IF NOT EXISTS idx_university_version_type 
ON hemishe_e_university(_version_type);

CREATE INDEX IF NOT EXISTS idx_university_terrain 
ON hemishe_e_university(_terrain);

-- Migration completed successfully
-- No data was lost or modified
-- All existing functionality preserved

