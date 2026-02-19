-- Rollback M004: Revert MV to M003 version (without transfer detection)
DROP MATERIALIZED VIEW IF EXISTS mv_student_duplicates;

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

CREATE UNIQUE INDEX idx_mv_dup_pinfl ON mv_student_duplicates (pinfl);
CREATE INDEX idx_mv_dup_reason_cnt ON mv_student_duplicates (reason, cnt DESC, pinfl);
CREATE INDEX idx_mv_dup_cnt ON mv_student_duplicates (cnt DESC, pinfl);
