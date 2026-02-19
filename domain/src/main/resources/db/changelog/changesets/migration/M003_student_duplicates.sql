-- ═══════════════════════════════════════════════════════════════════
-- M003: Student duplicates feature (menu + analysis MV)
--
-- 1. Update students parent menu URL (sidebar navigation)
-- 2. Add "Duplicates" submenu item
-- 3. Covering index for duplicate PINFL analysis CTE
-- 4. Materialized view with pre-computed duplicate categorization
--
-- Reason categories (priority order):
--   NORMAL           — 0 or 1 active enrollment
--   MULTI_LEVEL      — different education types active (may be legitimate)
--   CROSS_UNIVERSITY — same type at multiple universities (serious)
--   SAME_UNIVERSITY  — multiple records at same university
-- ═══════════════════════════════════════════════════════════════════

-- ── Menu ─────────────────────────────────────────────────────────

UPDATE menus
SET url = '/students', updated_at = CURRENT_TIMESTAMP
WHERE code = 'students' AND (url IS NULL OR url = '');

INSERT INTO menus (id, code, i18n_key, url, icon, permission, order_number, is_active, parent_id, created_at, updated_at)
VALUES (
    '20000003-0000-0000-0000-000000000007',
    'student-duplicates',
    'Duplicates',
    '/students/duplicates',
    'copy',
    'students.view',
    7,
    true,
    '10000000-0000-0000-0000-000000000003',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (code) DO UPDATE SET
    i18n_key = EXCLUDED.i18n_key,
    url = EXCLUDED.url,
    icon = EXCLUDED.icon,
    permission = EXCLUDED.permission,
    order_number = EXCLUDED.order_number,
    parent_id = EXCLUDED.parent_id,
    updated_at = CURRENT_TIMESTAMP;

-- ── Covering index for duplicate analysis CTE ────────────────────

CREATE INDEX IF NOT EXISTS idx_student_dup_analysis
    ON hemishe_e_student (pinfl, "_student_status", "_university", "_education_type")
    WHERE delete_ts IS NULL AND pinfl IS NOT NULL AND pinfl != '';

-- ── Materialized view ────────────────────────────────────────────

CREATE MATERIALIZED VIEW mv_student_duplicates AS
SELECT
    pinfl,
    COUNT(*) AS cnt,
    COUNT(DISTINCT "_university") AS univ_count,
    COUNT(CASE WHEN "_student_status" = '11' THEN 1 END) AS active_count,
    CASE
        WHEN COUNT(CASE WHEN "_student_status" = '11' THEN 1 END) <= 1
            THEN 'NORMAL'
        WHEN COUNT(DISTINCT CASE WHEN "_student_status" = '11' THEN "_education_type" END) > 1
            THEN 'MULTI_LEVEL'
        WHEN COUNT(DISTINCT CASE WHEN "_student_status" = '11' THEN "_university" END) > 1
            THEN 'CROSS_UNIVERSITY'
        ELSE 'SAME_UNIVERSITY'
    END AS reason
FROM hemishe_e_student
WHERE delete_ts IS NULL
    AND pinfl IS NOT NULL
    AND pinfl != ''
GROUP BY pinfl
HAVING COUNT(*) > 1;

CREATE UNIQUE INDEX idx_mv_dup_pinfl      ON mv_student_duplicates (pinfl);
CREATE INDEX idx_mv_dup_reason_cnt        ON mv_student_duplicates (reason, cnt DESC, pinfl);
CREATE INDEX idx_mv_dup_cnt               ON mv_student_duplicates (cnt DESC, pinfl);
