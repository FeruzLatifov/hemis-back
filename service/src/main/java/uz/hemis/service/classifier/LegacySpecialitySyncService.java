package uz.hemis.service.classifier;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.service.classifier.dto.LegacySpecialitySyncResult;

/**
 * Projects the new unified {@code h_speciality} classifier onto the frozen legacy speciality
 * tables ({@code hemishe_h_speciality_bachelor} / {@code hemishe_h_speciality_master}).
 *
 * <p><strong>Why this exists.</strong> {@code h_speciality} was built as an additive table
 * ({@link HSpecialityService}); it deliberately does not touch the legacy bachelor/master tables.
 * But student-save — in the frozen old-hemis (CUBA) app and in Univer — still resolves a student's
 * speciality against those legacy tables ({@code hemishe_e_student._speciality_bachelor} → a real
 * FK to {@code hemishe_h_speciality_bachelor(id)}). So a speciality curated only into
 * {@code h_speciality} is invisible to every student-save path and the student cannot be saved.</p>
 *
 * <p>This service is the <em>compatibility projection</em>: it copies the missing specialities down
 * into the legacy tables so the existing (unchanged) student-save path finds them. It is invoked on
 * demand from the classifier page's {@code Sinxronlash} button — a one-time backfill of everything
 * curated so far, and a safety-net re-run whenever new rows are added.</p>
 *
 * <ul>
 *   <li><strong>Match by UUID.</strong> {@code h_speciality.id} IS the legacy row id (the classifier
 *       was imported keyed by the legacy/xlsx UUID — ~95% of rows already share an id with the legacy
 *       table). So the projection compares on {@code id} and inserts each missing speciality with the
 *       SAME {@code id}, keeping the identity 1:1 across both tables. Existing legacy rows are never
 *       touched — never updated, never deleted (legacy-only rows the new table lacks are left alone).</li>
 *   <li><strong>No year expansion.</strong> The legacy tables have no edition-year dimension (their
 *       {@code _year} is largely empty); year-versioning is new. So a speciality becomes exactly ONE
 *       legacy row (by its UUID), not one per year, and {@code _year} is left null.</li>
 *   <li><strong>Scope.</strong> Only APPROVED + active + code-bearing rows are projected (the same
 *       predicate the OTM distribution uses); a NEEDS_REVIEW or code-less row never leaks into the
 *       student-save picker. Promote a row to APPROVED first, then re-sync.</li>
 *   <li><strong>Tree preserved safely.</strong> {@code _parent} is set to the speciality's parent id
 *       only when that parent already exists in the legacy table (a null-safe correlated subquery),
 *       so the self-referencing {@code fk_..._on__parent} is never violated; the rare orphan lands as
 *       a root.</li>
 *   <li><strong>Native SQL.</strong> The legacy tables are CUBA {@code StandardEntity} tables (not
 *       hemis-back JPA entities we write); a set-based {@code INSERT … SELECT} fills the required
 *       system columns ({@code ID}, {@code VERSION}) directly and stays a single statement per table.</li>
 * </ul>
 *
 * @since 2.1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LegacySpecialitySyncService {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Set-based projection into one legacy table. {@code %1$s} is the target table (formatted from a
     * fixed constant — never user input). Copies each APPROVED + active + code-bearing speciality that
     * is missing from the legacy table (matched by {@code id}), preserving the UUID. Required CUBA
     * system columns {@code ID}/{@code VERSION}/{@code CREATE_TS} are filled inline; {@code ACTIVE}/
     * {@code IS_CHECKED} are set true (an approved ministry row is a checked/active picker option).
     * {@code _parent} is carried over only when the parent already exists in the legacy table (FK-safe);
     * {@code _year}/{@code _education_form} are left null (legacy has no edition-year dimension).
     */
    private static final String INSERT_TEMPLATE = """
            INSERT INTO %1$s (id, version, create_ts, created_by, code, name, name_ru, name_en, _parent, active, is_checked)
            SELECT s.id, 1, now(), 'system',
                   s.code, s.name_uz, s.name_ru, s.name_en,
                   (SELECT p.id FROM %1$s p WHERE p.id = s.parent_id),
                   true, true
            FROM h_speciality s
            WHERE s.education_type = :eduType
              AND s.deleted_at IS NULL
              AND s.review_status = 'APPROVED'
              AND s.active = true
              AND s.code IS NOT NULL
              AND NOT EXISTS (SELECT 1 FROM %1$s b WHERE b.id = s.id)
            """;

    /** Distributable specialities per education type — the denominator for "already existed". */
    private static final String SCANNED_SQL = """
            SELECT count(*) FROM h_speciality s
            WHERE s.education_type IN ('11', '12')
              AND s.deleted_at IS NULL
              AND s.review_status = 'APPROVED'
              AND s.active = true
              AND s.code IS NOT NULL
            """;

    /** Approved + active specialities that cannot be projected because they have no code. */
    private static final String SKIPPED_NO_CODE_SQL = """
            SELECT count(*) FROM h_speciality s
            WHERE s.education_type IN ('11', '12')
              AND s.deleted_at IS NULL
              AND s.review_status = 'APPROVED'
              AND s.active = true
              AND s.code IS NULL
            """;

    /** Active, code-bearing specialities not yet APPROVED (need promotion before they sync). */
    private static final String SKIPPED_NOT_APPROVED_SQL = """
            SELECT count(*) FROM h_speciality s
            WHERE s.education_type IN ('11', '12')
              AND s.deleted_at IS NULL
              AND s.active = true
              AND s.code IS NOT NULL
              AND s.review_status <> 'APPROVED'
            """;

    /**
     * Copy every distributable speciality that is missing (by UUID) from the legacy bachelor/master
     * tables and report what happened. Idempotent — safe to press repeatedly.
     *
     * <p>Bachelor and master only, deliberately. Ordinatura ('13', added by M017/S042) is not a gap:
     * its 69 leaves were imported FROM {@code hemishe_h_speciality_ordinatura} under the legacy
     * UUIDs, so that table is already the mirror this method would try to build — there is nothing
     * to copy back, and every counter below correctly reports zero for it.
     *
     * <p>If ordinatura specialities ever start being CREATED here rather than imported, this method
     * needs a third target table AND a parent translation: S042 gives the '13' rows their own
     * cloned {@code 910000} category (so the classifier tree is not flat), while the legacy table's
     * {@code _parent} has an FK onto {@code hemishe_h_speciality_bachelor(id)} and only accepts
     * {@code 960be177-4e20-4a3c-b381-a1d816370e3f}. Copying the cloned id across would fail 23503.</p>
     */
    @Transactional
    public LegacySpecialitySyncResult syncToLegacy() {
        int bachelorInserted = insertMissing("11", "hemishe_h_speciality_bachelor");
        int masterInserted = insertMissing("12", "hemishe_h_speciality_master");

        long scanned = count(SCANNED_SQL);
        int inserted = bachelorInserted + masterInserted;
        int alreadyExisted = (int) Math.max(0, scanned - inserted);
        int skippedNoCode = (int) count(SKIPPED_NO_CODE_SQL);
        int skippedNotApproved = (int) count(SKIPPED_NOT_APPROVED_SQL);

        log.info("Legacy speciality sync: bachelor +{}, master +{}, alreadyExisted={}, skippedNoCode={}, skippedNotApproved={}",
                bachelorInserted, masterInserted, alreadyExisted, skippedNoCode, skippedNotApproved);
        return new LegacySpecialitySyncResult(
                bachelorInserted, masterInserted, alreadyExisted, skippedNoCode, skippedNotApproved);
    }

    /** Run the projection INSERT for one education type into its legacy table; returns rows inserted. */
    private int insertMissing(String eduType, String legacyTable) {
        return entityManager.createNativeQuery(INSERT_TEMPLATE.formatted(legacyTable))
                .setParameter("eduType", eduType)
                .executeUpdate();
    }

    /** Single-row {@code count(*)} native query → long. */
    private long count(String sql) {
        return ((Number) entityManager.createNativeQuery(sql).getSingleResult()).longValue();
    }
}
