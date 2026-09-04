package uz.hemis.service.audit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The field diff decides what {@code changed_fields} claims, and a false positive there is worse
 * than none: it tells an auditor a field was touched when nobody touched it.
 *
 * <p>This is not hypothetical — a speciality RESTORE was recorded as "changed: hierarchyLevel"
 * because the before-image carried the level as {@code "4"} and the response DTO as {@code 4}. The
 * two snapshots come from different sources (JPA entity vs response record), so representation
 * differences are normal and must not read as edits.</p>
 */
@DisplayName("AuditAspect.differs() — value comparison, not type comparison")
class AuditFieldDiffTest {

    @Test
    @DisplayName("same number in different representations is NOT a change")
    void numericRepresentationsAgree() {
        assertThat(AuditAspect.differs("4", 4)).isFalse();
        assertThat(AuditAspect.differs(4, 4L)).isFalse();
        assertThat(AuditAspect.differs(4, 4.0)).isFalse();
        assertThat(AuditAspect.differs("2026", 2026)).isFalse();
    }

    @Test
    @DisplayName("a real value change is still a change")
    void realChangesSurvive() {
        assertThat(AuditAspect.differs(4, 3)).isTrue();
        assertThat(AuditAspect.differs("NEEDS_REVIEW", "APPROVED")).isTrue();
        assertThat(AuditAspect.differs(null, "APPROVED")).isTrue();
        assertThat(AuditAspect.differs("APPROVED", null)).isTrue();
    }

    @Test
    @DisplayName("two strings compare as TEXT — a leading-zero identifier fix is a real change")
    void identifiersAreNotParsedAsNumbers() {
        // Speciality codes, OTM codes and PINFLs are identifiers, not quantities. Comparing them
        // numerically would report "05310100 -> 5310100" as unchanged and lose the correction.
        assertThat(AuditAspect.differs("05310100", "5310100")).isTrue();
        assertThat(AuditAspect.differs("0301", "301")).isTrue();
        // The mismatch the leniency was written for still holds: one side genuinely a Number.
        assertThat(AuditAspect.differs("4", 4)).isFalse();
    }

    @Test
    @DisplayName("a non-numeric string is compared as text, not silently parsed")
    void textStaysText() {
        assertThat(AuditAspect.differs("shifrsiz", "shifrsiz")).isFalse();
        assertThat(AuditAspect.differs("shifrsiz", "60110100")).isTrue();
    }

    @Test
    @DisplayName("collections keep strict equality — order and shape carry meaning")
    void collectionsAreNotFlattenedToText() {
        assertThat(AuditAspect.differs(List.of(2025, 2026), List.of(2025, 2026))).isFalse();
        assertThat(AuditAspect.differs(List.of(2025), List.of(2025, 2026))).isTrue();
        assertThat(AuditAspect.differs(Map.of("a", 1), List.of(1))).isTrue();
    }
}
