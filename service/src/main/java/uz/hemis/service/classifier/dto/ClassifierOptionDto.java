package uz.hemis.service.classifier.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * One classifier option (code + multilingual name) for a FE picker — sourced from a modern
 * {@code h_*} reference table ({@code h_education_form}, {@code h_education_type}, ...), never a
 * hard-coded list. Multilingual so the FE renders the label in the active locale.
 *
 * @since 2.1.0
 */
@Schema(description = "Klassifikator opsiyasi (kod + ko'p tilli nom)")
public record ClassifierOptionDto(
        @Schema(description = "Kod", example = "11") String code,
        @Schema(description = "Nomi (uz)", example = "Kunduzgi") String name,
        @Schema(description = "Nomi (ru)", example = "Дневная") String nameRu,
        @Schema(description = "Nomi (en)", example = "Full-time") String nameEn
) {
}
