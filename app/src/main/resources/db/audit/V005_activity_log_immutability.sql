-- =====================================================
-- V005: an audit row is a fact — block UPDATE and DELETE at the table
-- =====================================================
-- The V001 header claims immutability via `REVOKE UPDATE, DELETE ... FROM PUBLIC`, but the schema is
-- created BY the application account, which therefore owns the tables: an owner's rights do not come
-- from PUBLIC, so the REVOKE never applied to the one account that connects. A trigger does apply to
-- the owner.
--
-- This is defence against accident and against a compromised application session — not against a
-- superuser or the owner dropping the trigger. The remaining hardening is an OPERATOR step and is
-- deliberately not attempted from the app:
--     CREATE ROLE hemis_audit_ddl LOGIN PASSWORD '...';          -- owns the tables, runs migrations
--     CREATE ROLE hemis_audit_app LOGIN PASSWORD '...';          -- the app connects as this
--     GRANT INSERT ON activity_log, error_log, login_log TO hemis_audit_app;
--     GRANT SELECT ON activity_log, error_log, login_log TO hemis_audit_replica;
-- With that split the app cannot UPDATE or DELETE even by dropping a trigger it does not own.
--
-- Retention (see the runbook): deletes are performed by the operator/retention job connecting as the
-- owner, which sets `audit.purge = on` for its session so the trigger steps aside.
-- =====================================================

CREATE OR REPLACE FUNCTION audit_rows_are_immutable() RETURNS trigger AS $$
BEGIN
    IF current_setting('audit.purge', true) = 'on' THEN
        RETURN COALESCE(NEW, OLD);   -- retention job, running deliberately
    END IF;
    RAISE EXCEPTION 'audit rows are immutable (table %, operation %)', TG_TABLE_NAME, TG_OP
        USING HINT = 'set audit.purge = on in the retention job session to delete by policy';
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_activity_log_immutable ON activity_log;
CREATE TRIGGER trg_activity_log_immutable
    BEFORE UPDATE OR DELETE ON activity_log
    FOR EACH ROW EXECUTE FUNCTION audit_rows_are_immutable();

DROP TRIGGER IF EXISTS trg_login_log_immutable ON login_log;
CREATE TRIGGER trg_login_log_immutable
    BEFORE UPDATE OR DELETE ON login_log
    FOR EACH ROW EXECUTE FUNCTION audit_rows_are_immutable();

DROP TRIGGER IF EXISTS trg_error_log_immutable ON error_log;
CREATE TRIGGER trg_error_log_immutable
    BEFORE UPDATE OR DELETE ON error_log
    FOR EACH ROW EXECUTE FUNCTION audit_rows_are_immutable();
