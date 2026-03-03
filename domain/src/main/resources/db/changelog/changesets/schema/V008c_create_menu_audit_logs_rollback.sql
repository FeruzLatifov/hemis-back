-- =====================================================
-- Rollback V008c: DROP MENU_AUDIT_LOGS TABLE
-- =====================================================

DO $$
DECLARE
    row_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO row_count FROM menu_audit_logs;

    IF row_count > 10000 THEN
        RAISE EXCEPTION 'ROLLBACK BLOCKED: menu_audit_logs table has % rows. '
            'Production database detected. Manual intervention required.', row_count;
    END IF;

    RAISE NOTICE 'V008c Rollback: Dropping menu_audit_logs table (% rows)', row_count;
END $$;

DROP TABLE IF EXISTS menu_audit_logs;
