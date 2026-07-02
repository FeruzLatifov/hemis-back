package uz.hemis.service.registry.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Researcher Registry Dictionaries DTO - reference data for filters (cached).
 */
@Schema(name = "DoctoralStudentDictionaries", description = "Reference data for researcher filter dropdowns (cached)")
public record DoctoralStudentDictionariesDto(
    List<DictionaryItem> universities,
    List<DictionaryItem> scienceBranches,
    List<DictionaryItem> doctoralStudentTypes,
    List<DictionaryItem> statuses
) {

    @Schema(name = "DoctoralStudentDictionaryItem", description = "Generic dictionary item {code,name}")
    public record DictionaryItem(String code, String name) {}
}
