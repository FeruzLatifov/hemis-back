package uz.hemis.service.classifier.dto;

/**
 * Outcome of a manual "sync to legacy" run — the comparison result the classifier page shows
 * after the {@code Sinxronlash} button is pressed.
 *
 * <p>The new unified {@code h_speciality} classifier is projected onto the frozen legacy
 * {@code hemishe_h_speciality_bachelor} / {@code hemishe_h_speciality_master} tables so the
 * old-hemis + Univer student-save path (which still resolves a speciality against those legacy
 * tables) can find a newly-curated speciality. Only the distributable set is projected
 * (APPROVED + active + code-bearing — the same predicate the OTM distribution uses); each missing
 * speciality becomes one legacy row keyed by its own UUID ({@code h_speciality.id = legacy.id}), so
 * a re-run inserts nothing already present and never touches an existing legacy row (idempotent).</p>
 *
 * @param bachelorInserted   new rows written to {@code hemishe_h_speciality_bachelor} (11=Bakalavr)
 * @param masterInserted     new rows written to {@code hemishe_h_speciality_master} (12=Magistr)
 * @param alreadyExisted     approved specialities already present in the legacy tables (matched by UUID)
 * @param skippedNoCode      approved active specialities with no code (cannot project — legacy {@code CODE} is NOT NULL)
 * @param skippedNotApproved active code-bearing specialities not yet APPROVED (promote them first, then re-sync)
 */
public record LegacySpecialitySyncResult(
        int bachelorInserted,
        int masterInserted,
        int alreadyExisted,
        int skippedNoCode,
        int skippedNotApproved
) {
    /** Total new legacy rows written across both tables. */
    public int totalInserted() {
        return bachelorInserted + masterInserted;
    }
}
