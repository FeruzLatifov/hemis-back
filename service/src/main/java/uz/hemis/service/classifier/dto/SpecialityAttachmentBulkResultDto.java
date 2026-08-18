package uz.hemis.service.classifier.dto;

import java.util.List;

/**
 * Result of a bulk speciality→OTM attach ({@link SpecialityAttachmentBulkCreateDto}).
 *
 * <p>{@code created} holds the rows actually inserted (one per new education form, with resolved
 * names); {@code skipped} holds the forms that were already attached for the same speciality + year
 * (a duplicate is skipped, not an error) — so the UI can report "N attached, M already attached".</p>
 *
 * @since 2.2.0
 */
public record SpecialityAttachmentBulkResultDto(
        List<SpecialityAttachmentRowDto> created,
        List<SkippedForm> skipped
) {
    /** A form that was NOT created because the (OTM, speciality, form, year) row already exists. */
    public record SkippedForm(String educationForm, String educationFormName) {
    }
}
