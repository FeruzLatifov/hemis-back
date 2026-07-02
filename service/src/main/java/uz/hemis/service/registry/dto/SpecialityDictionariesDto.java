package uz.hemis.service.registry.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * University Speciality Registry Dictionaries DTO - reference data for filters (cached).
 */
@Schema(name = "UniversitySpecialityDictionaries", description = "Reference data for university-speciality filter dropdowns (cached)")
public record SpecialityDictionariesDto(
    List<DictionaryItem> universities,
    List<DictionaryItem> educationTypes
) {

    @Schema(name = "UniversitySpecialityDictionaryItem", description = "Generic dictionary item {code,name}")
    public record DictionaryItem(String code, String name) {}
}
