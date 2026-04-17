-- Remove deferred FK from users first
ALTER TABLE users DROP CONSTRAINT IF EXISTS fk_users_employee;
DROP TABLE IF EXISTS employee_job CASCADE;
DROP TABLE IF EXISTS employee CASCADE;
DROP TABLE IF EXISTS position CASCADE;
DROP TABLE IF EXISTS position_type CASCADE;
