-- Remove deferred FK from users first
ALTER TABLE users DROP CONSTRAINT IF EXISTS fk_users_employee;
DROP TABLE IF EXISTS employee_jobs;
DROP TABLE IF EXISTS employee;
DROP TABLE IF EXISTS positions;
DROP TABLE IF EXISTS position_types;
