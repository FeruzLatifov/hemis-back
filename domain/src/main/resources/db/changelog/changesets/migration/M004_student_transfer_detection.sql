-- ═══════════════════════════════════════════════════════════════════
-- M004: Transfer detection — upgrade MV with speciality analysis
--
-- Adds 2 new duplicate categories for students with ≤1 active enrollment:
--   INTERNAL_TRANSFER — same university, different speciality
--   EXTERNAL_TRANSFER — different universities (any speciality)
--
-- These were previously hidden inside NORMAL (346K → ~35K actual NORMAL).
-- No base table changes — only MV is recreated.
-- ═══════════════════════════════════════════════════════════════════

-- ── Drop old MV and recreate with transfer detection ────────────

DROP MATERIALIZED VIEW IF EXISTS mv_student_duplicates;

CREATE MATERIALIZED VIEW mv_student_duplicates AS
SELECT
    pinfl,
    COUNT(*) AS cnt,
    COUNT(DISTINCT "_university") AS univ_count,
    COUNT(CASE WHEN "_student_status" = '11' THEN 1 END) AS active_count,
    COUNT(DISTINCT COALESCE("_speciality_bachelor"::text, "_speciality_master"::text, "_speciality_ordinatura"::text)) AS spec_count,
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
          AND COUNT(DISTINCT COALESCE("_speciality_bachelor"::text, "_speciality_master"::text, "_speciality_ordinatura"::text)) > 1
            THEN 'INTERNAL_TRANSFER'
        WHEN COUNT(DISTINCT "_university") > 1
            THEN 'EXTERNAL_TRANSFER'
        -- ODDIY: hammasi bir xil
        ELSE 'NORMAL'
    END AS reason
FROM hemishe_e_student
WHERE delete_ts IS NULL AND pinfl IS NOT NULL AND pinfl != ''
GROUP BY pinfl
HAVING COUNT(*) > 1;

CREATE UNIQUE INDEX idx_mv_dup_pinfl ON mv_student_duplicates (pinfl);
CREATE INDEX idx_mv_dup_reason_cnt ON mv_student_duplicates (reason, cnt DESC, pinfl);
CREATE INDEX idx_mv_dup_cnt ON mv_student_duplicates (cnt DESC, pinfl);
