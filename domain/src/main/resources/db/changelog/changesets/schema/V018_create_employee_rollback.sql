-- Remove deferred FKs first (columns remain in their tables)
ALTER TABLE university_founder DROP CONSTRAINT IF EXISTS fk_uf_employee;
ALTER TABLE university_legal DROP CONSTRAINT IF EXISTS fk_ul_accountant;
ALTER TABLE university_legal DROP CONSTRAINT IF EXISTS fk_ul_director;
ALTER TABLE users DROP CONSTRAINT IF EXISTS fk_users_employee;
DROP TABLE IF EXISTS employee_job CASCADE;
DROP TABLE IF EXISTS employee CASCADE;
DROP TABLE IF EXISTS position CASCADE;
DROP TABLE IF EXISTS position_type CASCADE;
