-- ═══════════════════════════════════════════════════════════════════
-- M003: Student duplicates feature + transfer detection (consolidated)
--
-- 1. Update students parent menu URL (sidebar navigation)
-- 2. Add "Duplicates" submenu item
-- 3. Covering index for duplicate PINFL analysis CTE
-- 4. Materialized view with pre-computed duplicate categorization
--    INCLUDING speciality-based transfer detection (ex-M004).
--
-- Reason categories (priority order):
--   Problematic (2+ active enrollments):
--     MULTI_LEVEL       — different education types active (may be legitimate)
--     CROSS_UNIVERSITY  — same type at multiple universities (serious)
--     SAME_UNIVERSITY   — multiple records at same university
--   Transfer (≤1 active enrollment):
--     INTERNAL_TRANSFER — same university, different speciality
--     EXTERNAL_TRANSFER — different universities (any speciality)
--   Normal:
--     NORMAL            — everything consistent
-- ═══════════════════════════════════════════════════════════════════

-- ── Menu ─────────────────────────────────────────────────────────

UPDATE menu
SET url = '/students', updated_at = CURRENT_TIMESTAMP
WHERE code = 'students' AND (url IS NULL OR url = '');

INSERT INTO menu (id, code, i18n_key, url, icon, permission, order_number, is_active, parent_id, created_at, updated_at)
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
    i18n_key     = EXCLUDED.i18n_key,
    url          = EXCLUDED.url,
    icon         = EXCLUDED.icon,
    permission   = EXCLUDED.permission,
    order_number = EXCLUDED.order_number,
    parent_id    = EXCLUDED.parent_id,
    updated_at   = CURRENT_TIMESTAMP;

-- ── Covering index — M002b'ga ko'chirildi (CONCURRENTLY, bloklovchi lock'siz) ────
-- idx_student_dup_analysis endi M002b_student_indexes changeset'ida (runInTransaction:false).
-- Sabab: 1.15M-row hemishe_e_student'ga oddiy CREATE INDEX SHARE lock olib butun jadval
-- yozuvini bloklaydi (BACK-DB-01). Bu changeset endi faqat menu DML + mv_student_duplicates
-- (MATERIALIZED VIEW CREATE bazani ACCESS SHARE bilan skanerlaydi — yozuvni bloklamaydi).

-- ── Materialized view (consolidated M003+M004) ───────────────────

DROP MATERIALIZED VIEW IF EXISTS mv_student_duplicates;

CREATE MATERIALIZED VIEW mv_student_duplicates AS
SELECT
    pinfl,
    COUNT(*) AS cnt,
    COUNT(DISTINCT "_university") AS univ_count,
    COUNT(CASE WHEN "_student_status" = '11' THEN 1 END) AS active_count,
    COUNT(DISTINCT COALESCE("_speciality_bachelor"::text,
                            "_speciality_master"::text,
                            "_speciality_ordinatura"::text)) AS spec_count,
    CASE
        -- MUAMMOLI: 2+ faol yozuv
        WHEN COUNT(CASE WHEN "_student_status" = '11' THEN 1 END) > 1
          AND COUNT(DISTINCT CASE WHEN "_student_status" = '11' THEN "_education_type" END) > 1
            THEN 'MULTI_LEVEL'
        WHEN COUNT(CASE WHEN "_student_status" = '11' THEN 1 END) > 1
          AND COUNT(DISTINCT CASE WHEN "_student_status" = '11' THEN "_university" END) > 1
            THEN 'CROSS_UNIVERSITY'
        WHEN COUNT(CASE WHEN "_student_status" = '11' THEN 1 END) > 1
            THEN 'SAME_UNIVERSITY'
        -- KO'CHIRISH: 0-1 faol, mutaxasislik/universitet o'zgargan
        WHEN COUNT(DISTINCT "_university") = 1
          AND COUNT(DISTINCT COALESCE("_speciality_bachelor"::text,
                                      "_speciality_master"::text,
                                      "_speciality_ordinatura"::text)) > 1
            THEN 'INTERNAL_TRANSFER'
        WHEN COUNT(DISTINCT "_university") > 1
            THEN 'EXTERNAL_TRANSFER'
        ELSE 'NORMAL'
    END AS reason
FROM hemishe_e_student
WHERE delete_ts IS NULL AND pinfl IS NOT NULL AND pinfl <> ''
GROUP BY pinfl
HAVING COUNT(*) > 1;

CREATE UNIQUE INDEX idx_mv_dup_pinfl ON mv_student_duplicates (pinfl);
CREATE INDEX        idx_mv_dup_reason_cnt ON mv_student_duplicates (reason, cnt DESC, pinfl);
CREATE INDEX        idx_mv_dup_cnt ON mv_student_duplicates (cnt DESC, pinfl);
