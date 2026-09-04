-- =====================================================
-- M017: admit Ordinatura ('13') into the h_speciality education-type CHECK
-- =====================================================
-- V018 created the classifier with chk_h_speciality_edu_type CHECK (education_type IN ('11','12'))
-- because it shipped as a two-tab bachelor+master screen. V022 already anticipated this exact step
-- in its own header ("kelajakda Ordinatura/Doktorantura mutaxassisliklari qo'shilsa: CHECK relax +
-- ma'lumot yuklash + FE tab") and seeded all five education types into h_education_type, so '13'
-- (Ordinatura) is a live FK target today — only the CHECK stands between it and the classifier.
--
-- This widens the CHECK to ('11','12','13') and nothing else. '14'/'15' (Doktorantura PhD/DSc) stay
-- out: no doctoral speciality data has been handed over, and a CHECK that admits values nobody can
-- supply is not a guard, it is decoration. Widen it again when that data arrives.
--
-- MUST be ordered immediately BEFORE S042, which inserts the 69 ordinatura rows — with the old CHECK
-- in force every one of those INSERTs fails 23514.
--
-- Safety: DROP + ADD of a CHECK on a ~5.6k-row table. The ADD re-validates every existing row, which
-- is a full scan of a small table and takes milliseconds; every existing row is '11' or '12' and
-- therefore still satisfies the wider predicate, so it cannot fail on data. What it DOES need is an
-- ACCESS EXCLUSIVE lock, so lock_timeout is set: on a busy database this fails fast and is retried
-- rather than queueing behind a long read and blocking every writer behind it in turn.
--
-- Idempotent: the constraint is dropped IF EXISTS and re-added under the same name, so a re-run
-- converges on the same definition.
-- =====================================================

SET LOCAL lock_timeout = '3s';

ALTER TABLE h_speciality DROP CONSTRAINT IF EXISTS chk_h_speciality_edu_type;

ALTER TABLE h_speciality
    ADD CONSTRAINT chk_h_speciality_edu_type
    CHECK (education_type IN ('11', '12', '13'));

COMMENT ON CONSTRAINT chk_h_speciality_edu_type ON h_speciality IS
    'Education types this classifier admits: 11=Bakalavr, 12=Magistr, 13=Ordinatura. Mirrors HSpecialityService.ALLOWED_EDUCATION_TYPES — the two must be widened together, or a row the DB accepts is rejected by the API (and vice versa).';
