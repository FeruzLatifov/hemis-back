package uz.hemis.service.registry.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Research Activity Registry Dictionaries DTO - reference data for filters (cached).
 */
@Schema(name = "ResearchActivityDictionaries", description = "Reference data for research-activity filter dropdowns (cached)")
public record ResearchActivityDictionariesDto(
    List<DictionaryItem> universities,
    List<DictionaryItem> scholarDatabases
) {

    @Schema(name = "ResearchActivityDictionaryItem", description = "Generic dictionary item {code,name}")
    public record DictionaryItem(String code, String name) {}
}
