package uz.hemis.domain.entity.classifier;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.AttributeConverter;

/**
 * Review workflow status for a speciality classifier row ({@code h_speciality}).
 *
 * <ul>
 *   <li>{@link #APPROVED} — curated data from the xlsx source (5367 rows). Re-seeded on redeploy.</li>
 *   <li>{@link #NEEDS_REVIEW} — 53 live-DB-new rows with no year (and 15 with no code);
 *       edited/curated in the frontend. Protected from the S014 re-seed by an
 *       {@code ON CONFLICT ... WHERE review_status='APPROVED'} guard.</li>
 * </ul>
 *
 * <p>Wire format (JSON and DB): UPPERCASE, matching the V018
 * {@code chk_h_speciality_review} CHECK constraint.</p>
 */
public enum ReviewStatus {
    APPROVED("APPROVED"),
    NEEDS_REVIEW("NEEDS_REVIEW");

    private final String value;

    ReviewStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static ReviewStatus fromValue(String value) {
        for (ReviewStatus status : values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown review status: " + value);
    }

    /** JPA AttributeConverter: enum ↔ UPPERCASE DB value. */
    @jakarta.persistence.Converter(autoApply = false)
    public static class Converter implements AttributeConverter<ReviewStatus, String> {

        @Override
        public String convertToDatabaseColumn(ReviewStatus attribute) {
            // MUST return null for a null attribute — otherwise a null query parameter (e.g.
            // findAllFiltered's :reviewStatus = "all statuses") binds as 'APPROVED' and
            // COALESCE(:reviewStatus, s.reviewStatus) collapses to review_status='APPROVED',
            // silently hiding every NEEDS_REVIEW row from the list/tree. Persist never passes null
            // (the entity field defaults to APPROVED), so null here only ever means "no filter".
            return attribute != null ? attribute.getValue() : null;
        }

        @Override
        public ReviewStatus convertToEntityAttribute(String dbData) {
            return dbData != null ? fromValue(dbData) : null;
        }
    }
}
