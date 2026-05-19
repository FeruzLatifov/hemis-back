-- =====================================================
-- M007 rollback — recreate employee_sync_log
-- =====================================================
-- V015 originaldagi DDL bilan bir xil. Faqat tabula rasa rollback —
-- ma'lumotlar yo'qoladi (production'da yo'q edi).
-- =====================================================

CREATE TABLE IF NOT EXISTS employee_sync_log (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    employee_id     UUID,
    employee_job_id UUID,
    university_code VARCHAR(255) NOT NULL,
    source_uid      VARCHAR(100),
    pinfl           VARCHAR(14),
    event_type      VARCHAR(30) NOT NULL,
    error_message   TEXT,
    duration_ms     INTEGER,
    request_payload JSONB,
    synced_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    synced_by       VARCHAR(50),

    CONSTRAINT chk_esl_event_type CHECK (
        event_type IN ('INSERT', 'UPDATE', 'SKIP_UNCHANGED', 'CONFLICT_OVERWRITE',
                       'SOFT_DELETE', 'ERROR_VALIDATION', 'ERROR_TENANT', 'ERROR_DB')
    ),
    CONSTRAINT chk_esl_duration CHECK (duration_ms IS NULL OR duration_ms >= 0),
    CONSTRAINT chk_esl_error_consistency CHECK (
        (event_type LIKE 'ERROR%' AND error_message IS NOT NULL)
        OR (event_type NOT LIKE 'ERROR%' AND error_message IS NULL)
    ),

    CONSTRAINT employee_sync_log_employee_id_fkey
        FOREIGN KEY (employee_id) REFERENCES employee(id) ON DELETE SET NULL,
    CONSTRAINT employee_sync_log_employee_job_id_fkey
        FOREIGN KEY (employee_job_id) REFERENCES employee_job(id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_esl_university_synced
    ON employee_sync_log (university_code, synced_at DESC);
CREATE INDEX IF NOT EXISTS idx_esl_employee_synced
    ON employee_sync_log (employee_id, synced_at DESC)
    WHERE employee_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_esl_errors
    ON employee_sync_log (synced_at DESC)
    WHERE event_type LIKE 'ERROR%';
CREATE INDEX IF NOT EXISTS idx_esl_pinfl
    ON employee_sync_log (pinfl, synced_at DESC)
    WHERE pinfl IS NOT NULL;
