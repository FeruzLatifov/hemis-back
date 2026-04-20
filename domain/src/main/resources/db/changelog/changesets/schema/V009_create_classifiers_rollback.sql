-- Drop FKs that point at the new soato before dropping it
ALTER TABLE university_legal DROP CONSTRAINT IF EXISTS fk_ulegal_billing_soato;
ALTER TABLE employee DROP CONSTRAINT IF EXISTS employee_soato_code_fkey;

-- Restore employee FK to legacy table
ALTER TABLE employee ADD CONSTRAINT employee_soato_code_fkey
    FOREIGN KEY (soato_code) REFERENCES hemishe_h_soato(code);

DROP TABLE IF EXISTS terrain CASCADE;
DROP TABLE IF EXISTS soato CASCADE;
DROP TABLE IF EXISTS employee_rate CASCADE;
DROP TABLE IF EXISTS employment_form CASCADE;
DROP TABLE IF EXISTS hemis_version CASCADE;
DROP TABLE IF EXISTS contract_category CASCADE;
DROP TABLE IF EXISTS university_belongs_to CASCADE;
DROP TABLE IF EXISTS university_type CASCADE;
DROP TABLE IF EXISTS ownership CASCADE;
DROP TABLE IF EXISTS academic_rank CASCADE;
DROP TABLE IF EXISTS academic_degree CASCADE;
DROP TABLE IF EXISTS nationality CASCADE;
DROP TABLE IF EXISTS citizenship CASCADE;
DROP TABLE IF EXISTS gender CASCADE;
