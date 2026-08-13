package uz.hemis.service.classifier.dto;

import java.util.List;

/**
 * Filter-dropdown option sources for the speciality-attachments page.
 *
 * <p>Each list contains ONLY the values that actually occur in the (scope-filtered) attachment
 * data — never the full classifier — so a UI dropdown never offers a choice that would return
 * zero rows (e.g. only the ~98 OTMs that have attachments, not all 230 universities).</p>
 *
 * @since 2.1.0
 */
public record SpecialityAttachmentFilterOptionsDto(
        List<Option> universities,
        List<Option> educationTypes,
        List<Option> educationForms,
        List<Option> years
) {
    public record Option(String code, String name) {
    }
}
