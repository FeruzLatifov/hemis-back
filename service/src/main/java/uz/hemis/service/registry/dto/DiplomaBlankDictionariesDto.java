package uz.hemis.service.registry.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Diploma Blank Registry Dictionaries DTO — reference data for the filters.
 *
 * <p>Cached ("diplomaBlanksDictionaries").</p>
 */
@Schema(name = "DiplomaBlankDictionaries", description = "Reference data for diploma-blank filter dropdowns (cached)")
public record DiplomaBlankDictionariesDto(

    @Schema(description = "University options {code,name}")
    List<DictionaryItem> universities,

    @Schema(description = "Status options {code,name}")
    List<DictionaryItem> statuses
) {

    @Schema(name = "DiplomaBlankDictionaryItem", description = "Generic dictionary item {code,name}")
    public record DictionaryItem(

        @Schema(description = "Code/value", example = "AVAILABLE", requiredMode = Schema.RequiredMode.REQUIRED)
        String code,

        @Schema(description = "Display label", example = "AVAILABLE", requiredMode = Schema.RequiredMode.REQUIRED)
        String name
    ) {}
}
