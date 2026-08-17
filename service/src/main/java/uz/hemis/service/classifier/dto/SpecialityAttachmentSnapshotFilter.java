package uz.hemis.service.classifier.dto;

/**
 * Optional OTM-facing filters for the speciality-attachment snapshot pull.
 *
 * <p>Every field is nullable / blank-tolerant: a {@code null} (or blank) filter means "no
 * constraint on this column". These filters only <strong>narrow</strong> the caller's OWN
 * attachment set — the tenant ({@code universityCode}) is always the signed JWT claim, never a
 * filter, so a parameter can never widen scope to another OTM (IDOR-safe by construction).</p>
 *
 * <p><strong>Status is NOT a filter.</strong> The OTM pull is always ACTIVE-only (enforced in
 * {@code SpecialityAttachmentService#getSnapshot}); a SUSPENDED / REVOKED attachment means the OTM
 * is no longer permitted to run that speciality, so it is never distributed. The OTM therefore
 * cannot ask for non-active rows.</p>
 *
 * @since 2.1.0
 */
public record SpecialityAttachmentSnapshotFilter(
        Integer eduYear,
        String educationType,
        String educationForm,
        String specialityCode
) {

    /** An all-null filter — no column narrowing (the snapshot is still ACTIVE-only). */
    public static SpecialityAttachmentSnapshotFilter none() {
        return new SpecialityAttachmentSnapshotFilter(null, null, null, null);
    }

    /** {@code true} if at least one constraint is supplied (for logging / short-circuit). */
    public boolean isEmpty() {
        return eduYear == null
                && !isSet(educationType)
                && !isSet(educationForm)
                && !isSet(specialityCode);
    }

    /** Does this snapshot row satisfy every supplied (non-blank) constraint? */
    public boolean matches(SpecialityAttachmentSnapshotDto row) {
        if (eduYear != null && !eduYear.equals(row.eduYear())) {
            return false;
        }
        if (isSet(educationType) && !educationType.trim().equalsIgnoreCase(safe(row.educationType()))) {
            return false;
        }
        if (isSet(educationForm) && !educationForm.trim().equalsIgnoreCase(safe(row.educationForm()))) {
            return false;
        }
        return !isSet(specialityCode) || specialityCode.trim().equalsIgnoreCase(safe(row.specialityCode()));
    }

    private static boolean isSet(String s) {
        return s != null && !s.isBlank();
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }
}
