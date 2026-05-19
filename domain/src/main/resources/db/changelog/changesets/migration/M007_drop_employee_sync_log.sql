-- =====================================================
-- M007: Drop employee_sync_log table
-- =====================================================
-- ADR-0010 audit (2026-05-19) — employee_sync_log 80% duplikat:
--   - INSERT/UPDATE/DELETE event_type   → activity_log.action covers
--   - error_message + stack             → error_log + Sentry covers
--   - request_payload                   → activity_log.new_value (JSONB) covers
--   - synced_by                         → activity_log.user_id/username covers
--   - duration_ms                       → Micrometer + Sentry traces covers
--
-- Faqat 2 ta unique field qolardi (source_uid, employee_job_id) — lekin
-- bular allaqachon employee_job.source_uid column'da saqlanadi (V015 da
-- qo'shilgan). SKIP_UNCHANGED/CONFLICT_OVERWRITE enum'lari content_hash
-- skip semantikasi orqali (DB write yo'q = skipped) ifodalanadi.
--
-- ADR-0003 buzilishi: bu jadval markaziy `hemis` DB ichida edi, audit esa
-- alohida `hemis_audit` DB'da bo'lishi shart (backup ajratish, hot OLTP
-- shishirmaslik). EmployeeSyncProcessor'ga @Audited annotation qo'shilib,
-- activity_log orqali avtomatik audit yoziladi.
-- =====================================================

DROP INDEX IF EXISTS idx_esl_university_synced;
DROP INDEX IF EXISTS idx_esl_employee_synced;
DROP INDEX IF EXISTS idx_esl_errors;
DROP INDEX IF EXISTS idx_esl_pinfl;
DROP TABLE IF EXISTS employee_sync_log;
