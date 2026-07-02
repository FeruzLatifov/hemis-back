package uz.hemis.service.registry.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Scientific Project Registry Dictionaries DTO - reference data for filters (cached).
 */
@Schema(name = "ProjectDictionaries", description = "Reference data for scientific project filter dropdowns (cached)")
public record ProjectDictionariesDto(
    List<DictionaryItem> universities,
    List<DictionaryItem> projectTypes
) {

    @Schema(name = "ProjectDictionaryItem", description = "Generic dictionary item {code,name}")
    public record DictionaryItem(String code, String name) {}
}
